package com.lightel.di3000;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public class RestApiManager {

    private static RestApiManager mInstance = new RestApiManager();
    private APIService mAPIService;
    private final static String DM_URL = "https://dm.dev.myviewboard.cloud/";
    private final static String CGI_URL = "http://192.168.1.1/";

    private RestApiManager() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(CGI_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        mAPIService = retrofit.create(APIService.class);
    }

    public static RestApiManager getInstance() {
        return mInstance;
    }

//	public APIService getAPI() {
//		return mAPIService;
//	}

    void GET(Callback<JsonObject> callback) {
        Call<JsonObject> call = mAPIService.get();
        call.enqueue(callback);
    }

//    void PUT(Callback<JsonObject> callback, EntityInfo body) {
//        Call<JsonObject> call = mAPIService.put(body);
//        call.enqueue(callback);
//    }
//
//    void POST(Callback<JsonObject> callback, EnrollmentInfo body) {
//        Call<JsonObject> call = mAPIService.post(body);
//        call.enqueue(callback);
//    }

    public interface APIService {
        // url		https://dm.dev.myviewboard.cloud
        // GET		https://dm.dev.myviewboard.cloud/v1/code
        // PUT		https://dm.dev.myviewboard.cloud/v1/code
        // POST     https://dm.dev.myviewboard.cloud/v1/enroll

        @GET("do/getmodel")
        Call<JsonObject> get();
//
//        @PUT("v1/code")
//        Call<JsonObject> put(@Body EntityInfo entityInfo);
//
//        @POST("v1/enroll")
//        Call<JsonObject> post(@Body EnrollmentInfo enrollmentInfo);
    }
}