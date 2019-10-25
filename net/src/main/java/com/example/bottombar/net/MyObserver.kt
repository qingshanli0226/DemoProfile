package com.example.bottombar.net

import io.reactivex.Observer
import io.reactivex.disposables.Disposable

abstract class MyObserver<T> : Observer<T> {
    override fun onComplete() {
    }

    override fun onSubscribe(d: Disposable) {
    }

    abstract override fun onNext(t: T)

    override fun onError(e: Throwable) {
        //在此类中统一处理error信息
        ErrorUtil.handlerError(e)
    }

}