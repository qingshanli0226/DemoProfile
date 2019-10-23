package com.example.bottombar.net

import io.reactivex.Observable
import okhttp3.MultipartBody
import retrofit2.http.*

interface ApiService {
    @POST("register")
    @FormUrlEncoded
    fun register(@Field("name") name:String, @Field("password") password:String): Observable<NetBean<String>>


    //用表单形式传递参数
    //Post请求
    @POST("login")
    @FormUrlEncoded
    fun login(@Field("name") name:String, @Field("password") password: String):Observable<NetBean<LoginBean>>


    //上传文件, 返回上传后服务端的地址
    @Multipart
    @POST("upload")
    fun uploadAvatar(@Part file:MultipartBody.Part) : Observable<NetBean<String>>

}