package com.example.operatest;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubInterstitial;
import com.mopub.mobileads.MoPubInterstitial.InterstitialAdListener;
import com.mopub.mobileads.MoPubView;
import com.squareup.picasso.Picasso;

public class MainActivity extends Activity implements InterstitialAdListener {

    private static final String LOG_TAG = MainActivity.class.getCanonicalName();
    
	private MoPubInterstitial mInterstitial;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_main);
        
        String url = "http://i.imgur.com/R8sDxfS.jpg";
        
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        Picasso.with(MainActivity.this)
        	.load(url)
//        	.resize(200, 200)
//        	.centerCrop()
        	.into(imageView);
        
        MoPubView adView = (MoPubView) findViewById(R.id.adview);
        adView.setAdUnitId(getString(R.string.banner_ad_unit));
        adView.loadAd();
        
        mInterstitial = new MoPubInterstitial(MainActivity.this, getString(R.string.interstitial_ad_unit));
    	mInterstitial.setInterstitialAdListener(this);
    	
        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showInterstitial();
			}
		});
    }

    private void showInterstitial() {
    	mInterstitial.load();
    }
    
    @Override
    protected void onDestroy() {
    	mInterstitial.destroy();
    	super.onDestroy();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

	@Override
	public void onInterstitialClicked(MoPubInterstitial arg0) {
		Log.d(LOG_TAG, "onInterstitialClicked()");
	}

	@Override
	public void onInterstitialDismissed(MoPubInterstitial arg0) {
		Log.d(LOG_TAG, "onInterstitialDismissed()");		
	}

	@Override
	public void onInterstitialFailed(MoPubInterstitial arg0, MoPubErrorCode arg1) {
		Log.d(LOG_TAG, "onInterstitialFailed()");		
	}

	@Override
	public void onInterstitialLoaded(MoPubInterstitial arg0) {
		Log.d(LOG_TAG, "onInterstitialLoaded()");	
		if (mInterstitial.isReady()) {
			mInterstitial.show();
		} else {
			Log.e(LOG_TAG, "!!! Interstitial not ready");
		}
	}

	@Override
	public void onInterstitialShown(MoPubInterstitial arg0) {
		Log.d(LOG_TAG, "onInterstitialShown()");		
	}

}
