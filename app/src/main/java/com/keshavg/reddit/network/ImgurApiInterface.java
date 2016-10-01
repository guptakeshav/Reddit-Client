package com.keshavg.reddit.network;

import com.keshavg.reddit.models.UploadImageResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by keshavgupta on 9/27/16.
 */

public interface ImgurApiInterface {

    @FormUrlEncoded
    @POST("image")
    Call<UploadImageResponse> uploadImage(@Field("image") String image);
}
