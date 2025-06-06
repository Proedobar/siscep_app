package com.proedobar.siscep.api;

import com.proedobar.siscep.models.LoginResponse;
import com.proedobar.siscep.models.VerificacionResponse;
import com.proedobar.siscep.models.FirmanteResponse;
import com.proedobar.siscep.models.NominasResponse;
import com.proedobar.siscep.models.DetallesNominaResponse;
import com.proedobar.siscep.models.RegisterResponse;
import com.proedobar.siscep.models.RegisterRequest;
import com.proedobar.siscep.models.VerifyResponse;
import com.proedobar.siscep.models.VerifyRequest;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Body;
import retrofit2.http.Query;
import retrofit2.http.Streaming;

public interface ApiService {
    @GET("api/login")
    Call<LoginResponse> login(
        @Query("email") String email,
        @Query("password") String password
    );

    @POST("api/register-user")
    Call<RegisterResponse> registerUser(
        @Body RegisterRequest request
    );

    @POST("api/verify-user")
    Call<VerifyResponse> verifyUser(
        @Body VerifyRequest request
    );

    @GET("api/verificar-constancia")
    Call<VerificacionResponse> verificarConstancia(
        @Query("id") int id
    );

    @GET("api/get-firmante")
    Call<FirmanteResponse> getFirmante(
        @Query("user_id") int userId
    );

    @Streaming
    @GET("api/get-constancia")
    Call<ResponseBody> getConstancia(
        @Query("user_id") int userId
    );

    @GET("api/get-user-nominas")
    Call<NominasResponse> getUserNominas(
        @Query("user_id") int userId
    );

    @GET("api/search-detalles")
    Call<DetallesNominaResponse> searchDetalles(
        @Query("user_id") int userId,
        @Query("mes") int mes,
        @Query("anio") int anio,
        @Query("nomina_id") String nominaId
    );

    @Streaming
    @GET("api/get-recibo")
    Call<ResponseBody> getRecibo(
        @Query("detail_id") String detailId,
        @Query("user_id") int userId
    );
} 
