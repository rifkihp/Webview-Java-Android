package com.application.reethau.com.data;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import com.application.reethau.com.ResponseRegistrasiRegId;

public interface RestApi {

    @FormUrlEncoded
    @POST("androidRegistrasiRegid.php")
    Call<ResponseRegistrasiRegId> postRegistrasiRegId(
            @Field("reg_id") String regId
    );

    @FormUrlEncoded
    @POST("androidUnregistrasiRegid.php")
    Call<ResponseRegistrasiRegId> postUnregistrasiRegId(
            @Field("reg_id") String regId
    );
}
