package com.example.bottombar.net;

import android.util.Log;
import org.json.JSONException;

import java.net.SocketTimeoutException;

public class ErrorUtil {
    static void handlerError(Throwable throwable) {
        if (throwable instanceof RuntimeException) {
            Log.d("LQS ErrorUtil:", throwable.getMessage());
        } else if(throwable instanceof JSONException) {

        } else if (throwable instanceof SocketTimeoutException) {

        } else if (throwable instanceof TokenInvalidException) {

        }
    }
}
