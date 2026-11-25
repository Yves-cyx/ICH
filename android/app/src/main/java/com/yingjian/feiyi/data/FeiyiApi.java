package com.yingjian.feiyi.data;

import com.yingjian.feiyi.data.Models.*;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

interface FeiyiApi {
    @POST("api/auth/login") Call<Envelope<Token>> login(@Body Credentials input);
    @POST("api/auth/register") Call<Envelope<Token>> register(@Body Credentials input);
    @GET("api/heritage")
    Call<Envelope<List<Heritage>>> catalog(@Query("keyword") String keyword, @Query("category") String category,
                                         @Query("page") Integer page, @Query("size") Integer size);
    @GET("api/heritage/{id}") Call<Envelope<Heritage>> detail(@Path("id") long id);
    @GET("api/me") Call<Envelope<Profile>> profile(@Header("Authorization") String bearer);
    @PUT("api/me") Call<Envelope<Profile>> saveProfile(@Header("Authorization") String bearer, @Body Profile input);
    @GET("api/me/favorites")
    Call<Envelope<List<Heritage>>> favorites(@Header("Authorization") String bearer,
                                           @Query("page") Integer page, @Query("size") Integer size);
    @PUT("api/me/favorites/{heritageId}")
    Call<Envelope<Void>> addFavorite(@Header("Authorization") String bearer, @Path("heritageId") long heritageId);
    @DELETE("api/me/favorites/{heritageId}")
    Call<Envelope<Void>> removeFavorite(@Header("Authorization") String bearer, @Path("heritageId") long heritageId);
    @GET("api/works") Call<Envelope<List<Work>>> works(@Header("Authorization") String bearer);
    @POST("api/works") Call<Envelope<Work>> create(@Header("Authorization") String bearer, @Body Work input);
    @PUT("api/works/{id}") Call<Envelope<Work>> edit(@Header("Authorization") String bearer, @Path("id") long id, @Body Work input);
    @DELETE("api/works/{id}") Call<Envelope<Void>> delete(@Header("Authorization") String bearer, @Path("id") long id);
}
