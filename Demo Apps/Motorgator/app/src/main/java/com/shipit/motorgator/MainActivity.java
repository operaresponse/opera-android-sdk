package com.shipit.motorgator;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;

import com.etsy.android.grid.StaggeredGridView;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubInterstitial;
import com.mopub.mobileads.MoPubView;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;


public class MainActivity extends Activity implements Callback<BingResultContainer>, Handler.Callback, SearchView.OnQueryTextListener, AdapterView.OnItemClickListener, MoPubInterstitial.InterstitialAdListener {

    private static final long TEN_MB = 50 * 1024 * 1024L;
    private static final String LOG_TAG = MainActivity.class.getCanonicalName();

    private static final int LOAD_IMAGES = 1;
    private static final int PAGE = 50;

    private ImageFeedAdapter _feedAdapter;
    private BingImageService _bingImageService;
    private Handler _handler = new Handler(this);

    private AtomicBoolean _loading = new AtomicBoolean(false);
    private int _top = 0;
    private String _searchTerms;
    private View _progressBar;

    private MoPubView _moPubView;
    private MenuItem _searchItem;
    private ArrayAdapter<String> _adUnitAdapter;
    private DrawerLayout _drawerLayout;
    private MoPubInterstitial _interstitial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            _searchTerms = savedInstanceState.getString("search_terms", getString(R.string.motorcycles));
        } else {
            _searchTerms = getString(R.string.motorcycles);
        }

        _progressBar = findViewById(R.id.pb);

        _feedAdapter = new ImageFeedAdapter(MainActivity.this);
        StaggeredGridView sgv = (StaggeredGridView) findViewById(R.id.sgv_feed);
        sgv.setAdapter(_feedAdapter);
        sgv.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // do nothing
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                boolean loadMore = !_loading.get() && (_feedAdapter.getCount() > 0) && (firstVisibleItem + visibleItemCount >= (totalItemCount - 2));

                if (loadMore) {
                    performSearch();
                }
            }
        });

        // setup drawer
        _drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ListView drawerList = (ListView) findViewById(R.id.left_drawer);

        String [] adUnits = getResources().getStringArray(R.array.ad_units);
        _adUnitAdapter = new ArrayAdapter<String>(MainActivity.this, R.layout.item_drawer, R.id.tv_drawer_item, adUnits);
        drawerList.setAdapter(_adUnitAdapter);
        drawerList.setOnItemClickListener(this);

        buildAndStartImageService();

        initMoPub();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("search_terms", _searchTerms);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState != null) {
            _searchTerms = savedInstanceState.getString("search_terms");
        }
    }

    @Override
    protected void onDestroy() {
        if (_moPubView != null) {
            _moPubView.destroy();
        }

        if (_interstitial != null) {
            _interstitial.destroy();
        }

        super.onDestroy();
    }

    private RestAdapter buildRestAdapter() {
        final OkHttpClient httpClient = new OkHttpClient();

        try {
            final File cacheDir = new File(getApplicationContext().getCacheDir().getAbsolutePath(), "HttpCache");
            final Cache cache = new Cache(cacheDir, TEN_MB);
            httpClient.setCache(cache);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Could not create http cache", e);
        }

        final RequestInterceptor interceptor = new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade request) {
                request.addHeader("Authorization", "Basic " + BingImageService.ACCOUNT_KEY_ENC);
            }
        };

        final RestAdapter restAdapter = new RestAdapter.Builder()
                .setClient(new OkClient(httpClient))
                .setEndpoint("https://api.datamarket.azure.com")
                .setRequestInterceptor(interceptor)
                .build();

        return restAdapter;
    }

    private void buildAndStartImageService() {
        RestAdapter restAdapter = buildRestAdapter();
        _bingImageService = restAdapter.create(BingImageService.class);
        _handler.sendEmptyMessage(LOAD_IMAGES);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        _searchItem = menu.findItem(R.id.menu_item_action_search);
        SearchView searchView = (SearchView) _searchItem.getActionView();
        searchView.setOnQueryTextListener(this);
        searchView.setQueryHint(getString(R.string.query_hint));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_item_action_search) {
            return true;
        } else if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void success(BingResultContainer bingResultContainer, Response response) {
        doneLoading();

        List<BingResultContainer.BingImage> images = bingResultContainer.images();
        if (images != null) {
            _feedAdapter.addAll(images);
            _feedAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void failure(RetrofitError error) {
        Log.e(LOG_TAG, "bing failure", error);
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (msg.what == LOAD_IMAGES) {
            performSearch();
            return true;
        }

        return false;
    }

    private void performSearch() {
        startLoading();
        _top += PAGE;
        _bingImageService.search("'" + _searchTerms + "'", _top, "json", MainActivity.this);
    }

    private void startLoading() {
        _loading.set(true);

        if (_searchItem != null) {
            _searchItem.setEnabled(false);
        }

        updateProgressVisibility();
    }

    private void doneLoading() {
        _loading.set(false);

        if (_searchItem != null) {
            _searchItem.setEnabled(true);
        }

        updateProgressVisibility();
    }

    private void updateProgressVisibility() {
        int visibility = _loading.get() ? View.VISIBLE : View.GONE;
        _progressBar.setVisibility(visibility);
    }

    private void initMoPub() {
        _moPubView = (MoPubView) findViewById(R.id.adview);
        _moPubView.setAdUnitId(getString(R.string.mopub_320x50_ad_unit_id));
        _moPubView.loadAd();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        _searchItem.collapseActionView();
        _feedAdapter.clear();
        _top = 0;
        _searchTerms = query;
        performSearch();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        _drawerLayout.closeDrawers();

        String ad = _adUnitAdapter.getItem(position);
        Log.d(LOG_TAG, "ad = " + ad);

        final int width;
        final int height;
        final String adUnitId;

        if ("320x50".equals(ad)) {
            width = 320;
            height = 50;
            adUnitId = getString(R.string.mopub_320x50_ad_unit_id);
        } else if ("Interstitial".equals(ad)) {
            width = -1;//320;
            height = -1; //480;
            adUnitId = getString(R.string.mopub_320x480_ad_unit_id);
//            adUnitId = getString(R.string.mopub_custom_320x480_ad_unit_id);

            if (_interstitial != null) {
                _interstitial.destroy();
            }

            _interstitial = new MoPubInterstitial(MainActivity.this, adUnitId);
            _interstitial.setInterstitialAdListener(this);
            _interstitial.load();
        } else if ("480x320".equals(ad)) {
            width = 480;
            height = 320;
            adUnitId = getString(R.string.mopub_480x320_ad_unit_id);
        } else {
            width = -1;
            height = -1;
            adUnitId = null;
        }

        if (!isFinishing() && width > 0 && height > 0) {
            int dpWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, width, getResources().getDisplayMetrics());
            int dpHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, height, getResources().getDisplayMetrics());

            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) _moPubView.getLayoutParams();
            params.width = dpWidth;
            params.height = dpHeight;

            _moPubView.setLayoutParams(params);
            _moPubView.setAdUnitId(adUnitId);
            _moPubView.loadAd();
        }
    }

    @Override
    public void onInterstitialLoaded(MoPubInterstitial interstitial) {
        Log.d(LOG_TAG, "onInterstitialLoaded()");

        if (interstitial.isReady()) {
            interstitial.show();
        }
    }

    @Override
    public void onInterstitialFailed(MoPubInterstitial interstitial, MoPubErrorCode errorCode) {
        Log.d(LOG_TAG, "onInterstitialFailed(), errorCode = " + errorCode);

    }

    @Override
    public void onInterstitialShown(MoPubInterstitial interstitial) {
        Log.d(LOG_TAG, "onInterstitialShown()");
    }

    @Override
    public void onInterstitialClicked(MoPubInterstitial interstitial) {
        Log.d(LOG_TAG, "onInterstitialClicked()");
    }

    @Override
    public void onInterstitialDismissed(MoPubInterstitial interstitial) {
        Log.d(LOG_TAG, "onInterstitialDismissed()");
    }
}
