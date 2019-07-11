package com.example.ricardo.oqueapp.api;

import com.example.ricardo.oqueapp.model.NotificacaoDados;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface NotificacaoService {

    @Headers({
            "Authorization:key=AAAA-SH-0uE:APA91bHvoCOl8zxUo-jVf_NayiLQTjRNx4zedY_Xb7fHI7uWuHUcOzqCRxb9znzdAkim9JqB74V15d05fGhGM8P5Z7o4GBFxjz_1hGH86pPReuK_ktnB2MxMKbT5GFbvyOU7u4y2vRxp"
            ,"Content-Type:application/json"
    })
    @POST("send")
    Call<NotificacaoDados> salvarNotificacao(
            @Body NotificacaoDados notificacaoDados);
}
