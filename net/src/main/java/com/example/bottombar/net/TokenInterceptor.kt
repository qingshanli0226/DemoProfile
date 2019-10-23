package com.example.bottombar.net

import android.accounts.AccountManager
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class TokenInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()

        var newRequest:Request
        if (TokenCache.token != null) {
            newRequest = request.newBuilder().addHeader("token", TokenCache.token).build()
            return chain.proceed(newRequest)//已经登录了，生成新的request，使用新的request进行网络调用
        } else {
            return chain.proceed(request) //还没有登录，没必要添加token
        }
    }

}