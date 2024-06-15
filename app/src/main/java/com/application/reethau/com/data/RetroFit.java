package com.application.reethau.com.data;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetroFit {

    private static String baseurl = "https://kamon.id/pushnotif/store/";

    private static Retrofit getRetrofit() {
        return new Retrofit.Builder()
                .baseUrl(baseurl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static RestApi getInstanceRetrofit() {
        return getRetrofit().create(RestApi.class);
    }
}