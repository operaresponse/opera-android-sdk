package com.shipit.motorgator;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

import static android.util.Base64.NO_WRAP;
import static android.util.Base64.URL_SAFE;
import static android.util.Base64.encode;

/**
 * Created by sumeet on 9/17/14.
 */

public interface BingImageService {

    static final String ACCOUNT_KEY = "H9trTlmpHhJ3Rs/nmDIby3Dv6jiYcXHxR5Lj+K9cuhE";
    static final byte [] ACCOUNT_KEY_BYTES = encode((ACCOUNT_KEY + ":" + ACCOUNT_KEY).getBytes(), NO_WRAP|URL_SAFE);
    static final String ACCOUNT_KEY_ENC = new String(ACCOUNT_KEY_BYTES);

    @GET("/Data.ashx/Bing/Search/v1/Image")
    public void search(@Query("Query") String query, @Query("$top") int top, @Query("$format") String format, Callback<BingResultContainer> callback);
}
