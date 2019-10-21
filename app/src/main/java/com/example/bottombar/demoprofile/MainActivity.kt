package com.example.bottombar.demoprofile

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.bottombar.net.LoginBean
import com.example.bottombar.net.NetBean
import com.example.bottombar.net.RetrofitCreator
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import java.util.function.Consumer

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnRegister.setOnClickListener{
            testRegister()
        }

        btnLogin.setOnClickListener{
            testLogin()
        }

    }

    private fun testLogin() {
        RetrofitCreator.getApiService().login("1704","123456")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<NetBean<LoginBean>> {
                override fun onComplete() {

                }

                override fun onNext(t: NetBean<LoginBean>) {
                    Toast.makeText(this@MainActivity, "登录成功${t.result}", Toast.LENGTH_SHORT).show()
                    Log.d("LQS", "${t.toString()}")
                }

                override fun onError(e: Throwable) {

                }

                override fun onSubscribe(d: Disposable) {

                }
            })
    }

    private fun testRegister() {
         RetrofitCreator.getApiService().register("1704", "123456")
             .subscribeOn(Schedulers.io())
             .observeOn(AndroidSchedulers.mainThread())
             .subscribe(object : Observer<NetBean<String>> {
                 override fun onNext(t: NetBean<String>) {
                     Toast.makeText(this@MainActivity, "注册成功${t.result}", Toast.LENGTH_SHORT).show()
                 }

                 override fun onComplete() {

                 }

                 override fun onError(e: Throwable) {
                     Log.d("LQS", "" + e.printStackTrace())
                     Toast.makeText(this@MainActivity, "注册失败${e.printStackTrace()}", Toast.LENGTH_SHORT).show()
                 }

                 override fun onSubscribe(d: Disposable) {

                 }
             } )
    }


}
