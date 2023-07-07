package com.example.testapi

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*


interface ApiInterface {

    @GET("data")
    fun getData(): Call<MyDataItem>

    @PUT("data")
    fun putData(@Body myDataItem: MyDataItem): Call<MyDataItem>



    @POST("updata")
    fun postData(@Body postData: MyDataItem): Call<MyDataItem>

    @Multipart
    @POST("update")
    fun uploadFile(
        @Part filePart: MultipartBody.Part
    ): Call<ResponseBody>
}