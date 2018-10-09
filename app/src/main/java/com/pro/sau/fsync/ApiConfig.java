package com.pro.sau.fsync;

import com.pro.sau.fsync.model.ImeiModel;
import com.pro.sau.fsync.model.upvidsModel;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

interface ApiConfig {
    @Multipart
    @POST("upvids")
    Call<upvidsModel> uploadVideo(@Part MultipartBody.Part video);

    @Multipart
    @POST("upimg")
    Call<upvidsModel> uploadImage(@Part MultipartBody.Part image);


    @GET("getimei")
    Call<ImeiModel> getIMEI();

}
