package dev.medicare.models;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface GoogleService {

    @Headers("Content-Type: application/json")
    @POST("v1/images:annotate")
    Call<GoogleResponse> getResponse(@Query("key") String apiKey, @Body GoogleRequest body);
}
