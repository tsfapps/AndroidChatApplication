package com.videocall.tsfchat.data.repository.remote;

import com.google.gson.JsonObject;
import com.videocall.tsfchat.util.Configuration;
import com.qiscus.sdk.Qiscus;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import rx.Single;

/**
 * Created on : May 16, 2018
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public enum ContactRestApi {
    INSTANCE;

    private final Api api;

    ContactRestApi() {
        String baseUrl = Configuration.BASE_URL;

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(makeLoggingInterceptor(Qiscus.isEnableLog()))
                .build();

        api = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build()
                .create(Api.class);
    }

    private HttpLoggingInterceptor makeLoggingInterceptor(boolean isDebug) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(isDebug ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE);
        return logging;
    }

    public Single<JsonObject> getContacts() {
        return api.getContacts();
    }

    private interface Api {
        @GET("/api/contacts?show_all=true")
        Single<JsonObject> getContacts();
    }
}
