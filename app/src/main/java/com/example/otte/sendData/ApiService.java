package com.example.otte.sendData;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiService {
    @POST("/api/token/")
    Call<ConnectionOTTE> getToken(@Body ConnectionOTTE token);

    @POST("/clothes-set-reviews/review_sensor/")
    Call<ConnectionOTTE> sendReviewData(@Header("Authorization") String authorization, @Body ConnectionOTTE reviewData);

};


