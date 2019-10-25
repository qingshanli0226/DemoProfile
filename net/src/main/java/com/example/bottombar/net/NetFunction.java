package com.example.bottombar.net;

import io.reactivex.functions.Function;


//做类型转换,把 R 类型转换成 T 类型，并且检查错误
public class NetFunction<R extends NetBean<T>, T> implements Function<R, T> {
    @Override
    public T apply(R r) throws Exception {
        if (r.getCode().equals("200")) {//如果网络请求数据正确
            return r.getResult();//
        } else if (r.getCode().equals("1006")) {//token失效
             throw new TokenInvalidException("token 失效");
        } else {//其他类型错误
            throw new RuntimeException("业务类型错误：" + r.getCode() + r.getMessage());
        }
    }
}
