package com.example.ricardo.oqueapp.helper;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitConfig {

    public Retrofit getRetrofit(){
        return
        new Retrofit.Builder()
                .baseUrl("https://fcm.googleapis.com/fcm/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }


}
