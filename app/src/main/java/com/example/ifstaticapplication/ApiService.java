package com.example.ifstaticapplication;

import com.example.ifstaticapplication.model.ApiResponse;


import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("get_resturants")
    Call<ApiResponse> getRestaurants(@Body RequestBody body);
}

