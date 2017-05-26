/*
 * Copyright (C) 2016 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.instructure.canvasapi2;

import android.net.http.HttpResponseCache;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.GsonBuilder;
import com.instructure.canvasapi2.builders.RestParams;
import com.instructure.canvasapi2.models.CanvasContext;
import com.instructure.canvasapi2.utils.ApiPrefs;
import com.instructure.canvasapi2.utils.ContextKeeper;
import com.instructure.canvasapi2.utils.Logger;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * To use this adapter use RestBuilder
 */
public abstract class CanvasRestAdapter {

    private static final boolean DEBUG = true;
    private static final int TIMEOUT_IN_SECONDS = 60;
    private static final long CACHE_SIZE = 20 * 1024 * 1024;

    @Nullable private static StatusCallback mCallback;
    @Nullable private static File mHttpCacheDirectory;
    @NonNull private static Dispatcher mDispatcher = new Dispatcher();
    @Nullable private static Cache mCache;
    @Nullable private static OkHttpClient mOkHttpClient;
    @Nullable private static OkHttpClient mOkHttpClientNoRedirects;

    /**
     * Constructor for CanvasRestAdapter
     * @param statusCallback Only null when not making calls via callbacks. RestBuilder requires one, RXRestBuilder should not pass one
     */
    protected CanvasRestAdapter(@Nullable StatusCallback statusCallback) {
        mCallback = statusCallback;
    }

    public void setStatusCallback(@Nullable StatusCallback callback) {
        mCallback = callback;
    }

    @Nullable
    public StatusCallback getStatusCallback() {
        return mCallback;
    }

    @Nullable public static OkHttpClient getClient() {
        return mOkHttpClient;
    }

    public void deleteCache() {
        try {
            getOkHttpClient().cache().evictAll();
        } catch (IOException e) {
            Logger.e("Failed to delete cache " + e);
        }
    }

    public static File getCacheDirectory() {
        if(mHttpCacheDirectory == null) {
            mHttpCacheDirectory = new File(ContextKeeper.getAppContext().getCacheDir(), "canvasCache");
        }
        return mHttpCacheDirectory;
    }

    @NonNull
    public static OkHttpClient getOkHttpClient() {

        if(mCache == null) {
            mCache = new Cache(getCacheDirectory(), CACHE_SIZE);
        }

        try {
            if(HttpResponseCache.getInstalled() == null) {
                HttpResponseCache.install(getCacheDirectory(), CACHE_SIZE);
            }
        } catch (IOException e) {
            Logger.e("Failed to install the cache directory");
        }

        if(mOkHttpClient == null) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(DEBUG ? HttpLoggingInterceptor.Level.HEADERS : HttpLoggingInterceptor.Level.NONE);

            mOkHttpClient = new OkHttpClient.Builder()
                    .cache(mCache)
                    .addInterceptor(loggingInterceptor)
                    .addInterceptor(new RequestInterceptor(mCallback))
                    .addNetworkInterceptor(new ResponseInterceptor())
                    .readTimeout(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
                    .dispatcher(mDispatcher)
                    .build();
        }

        return mOkHttpClient;
    }

    @NonNull
    private OkHttpClient getOkHttpClientNoRedirects() {

        OkHttpClient client = getOkHttpClient();
        client = client.newBuilder().followRedirects(false).build();

        return client;
    }

    //region Adapter Builders

    public Retrofit buildAdapterNoRedirects(@NonNull RestParams params) {
        if(params.getDomain() == null || params.getDomain().length() == 0) {
            params = new RestParams.Builder(params).withDomain(ApiPrefs.getFullDomain()).build();
        }

        if(mCallback != null) {
            mCallback.onCallbackStarted();
        }

        //Can make this check as we KNOW that the setter doesn't allow empty strings.
        if (params.getDomain().equals("")) {
            Logger.d("The RestAdapter hasn't been set up yet. Call setupInstance(context,token,domain)");
            return new Retrofit.Builder().baseUrl("http://invalid.domain.com/").build();
        }

        String apiContext = "";
        if (params.getCanvasContext() != null) {
            if (params.getCanvasContext().getType() == CanvasContext.Type.COURSE) {
                apiContext = "courses/";
            } else if (params.getCanvasContext().getType() == CanvasContext.Type.GROUP) {
                apiContext = "groups/";
            } else if (params.getCanvasContext().getType() == CanvasContext.Type.SECTION) {
                apiContext = "sections/";
            } else {
                apiContext = "users/";
            }
        }


        //Adds current requested params to the config to be used with the OkHttpClient
        ApiPrefs.setRestParams(params);

        //Sets the auth token, user agent, and handles masquerading.
        return new Retrofit.Builder()
                .baseUrl(params.getDomain() + params.getAPIVersion() + apiContext)
                .addConverterFactory(GsonConverterFactory.create())
                .client(getOkHttpClientNoRedirects()).build();
    }

    public Retrofit buildAdapterSerializeNulls(@NonNull RestParams params) {
        if(params.getDomain() == null || params.getDomain().length() == 0) {
            params = new RestParams.Builder(params).withDomain(ApiPrefs.getFullDomain()).build();
        }

        if(mCallback != null) {
            mCallback.onCallbackStarted();
        }

        //Can make this check as we KNOW that the setter doesn't allow empty strings.
        if (params.getDomain().equals("")) {
            Logger.d("The RestAdapter hasn't been set up yet. Call setupInstance(context,token,domain)");
            return new Retrofit.Builder().baseUrl("http://invalid.domain.com/").build();
        }

        String apiContext = "";
        if (params.getCanvasContext() != null) {
            if (params.getCanvasContext().getType() == CanvasContext.Type.COURSE) {
                apiContext = "courses/";
            } else if (params.getCanvasContext().getType() == CanvasContext.Type.GROUP) {
                apiContext = "groups/";
            } else if (params.getCanvasContext().getType() == CanvasContext.Type.SECTION) {
                apiContext = "sections/";
            } else {
                apiContext = "users/";
            }
        }


        //Adds current requested params to the config to be used with the OkHttpClient
        ApiPrefs.setRestParams(params);

        //Sets the auth token, user agent, and handles masquerading.
        return new Retrofit.Builder()
                .baseUrl(params.getDomain() + params.getAPIVersion() + apiContext)
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().serializeNulls().create()))
                .client(getOkHttpClient()).build();
    }

    public Retrofit buildPingAdapter(@NonNull String url) {
        return new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .client(new OkHttpClient.Builder()
                        .readTimeout(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
                        .build())
                .build();
    }

    public Retrofit buildAdapter(@NonNull RestParams params) {

        if(params.getDomain() == null || params.getDomain().length() == 0) {
            params = new RestParams.Builder(params).withDomain(ApiPrefs.getFullDomain()).build();
        }

        return buildAdapterHelper(params);
    }

    private Retrofit buildAdapterHelper(@NonNull RestParams params) {

        if(mCallback != null) {
            mCallback.onCallbackStarted();
        }

        //Can make this check as we KNOW that the setter doesn't allow empty strings.
        if (params.getDomain().equals("")) {
            Logger.d("The RestAdapter hasn't been set up yet. Call setupInstance(context,token,domain)");
            return new Retrofit.Builder().baseUrl("http://invalid.domain.com/").build();
        }

        String apiContext = "";
        if (params.getCanvasContext() != null) {
            if (params.getCanvasContext().getType() == CanvasContext.Type.COURSE) {
                apiContext = "courses/";
            } else if (params.getCanvasContext().getType() == CanvasContext.Type.GROUP) {
                apiContext = "groups/";
            } else if (params.getCanvasContext().getType() == CanvasContext.Type.SECTION) {
                apiContext = "sections/";
            } else {
                apiContext = "users/";
            }
        }

        return finalBuildAdapter(params, apiContext).build();
    }

    /**
     * protected because of RX may want to override to provide a different converter factory
     * @param params RXParams
     * @param apiContext courses, groups, sections, users, or nothing
     * @return Retrofit.Builder
     */
    protected Retrofit.Builder finalBuildAdapter(@NonNull RestParams params, String apiContext) {
        //Adds current requested params to the config to be used with the OkHttpClient
        ApiPrefs.setRestParams(params);

        //Sets the auth token, user agent, and handles masquerading.
        return new Retrofit.Builder()
                .baseUrl(params.getDomain() + params.getAPIVersion() + apiContext)
                .addConverterFactory(GsonConverterFactory.create())
                .client(getOkHttpClient());
    }
    //endregion

    public static void cancelAllCalls() {
        mDispatcher.cancelAll();
    }

    /**
     * Saves the login information
     *
     * Short hand for setdomain, setToken, and setProtocol.
     *
     * Clears out any old data before setting the new data.
     *
     * @param token An OAuth2 Token
     * @param domain The domain for the signed in user.
     *
     * @return Whether or not the login info was saved. Only returns false if the data is empty or invalid.
     */
    public static boolean saveLoginInfo(String token, String domain){
        if (token == null || token.equals("") || domain == null) {
            return false;
        }

        String protocol = "https";
        if(domain.startsWith("http://")) {
            protocol = "http";
        }

        ApiPrefs.setDomain(domain);
        ApiPrefs.setToken(token);
        ApiPrefs.setProtocol(protocol);
        return true;
    }

}
