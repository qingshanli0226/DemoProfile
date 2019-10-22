package com.example.bottombar.demoprofile

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.example.bottombar.net.LoginBean
import com.example.bottombar.net.NetBean
import com.example.bottombar.net.RetrofitCreator
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_login.name
import kotlinx.android.synthetic.main.activity_login.password

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        login.setOnClickListener{
            login()
        }

        //如果之前没有注册，需先注册
        pleaseRegister.setOnClickListener{
            var intent = Intent()
            intent.setClass(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }


    private fun login() {
        RetrofitCreator.getApiService().login(name.text.toString(), password.text.toString())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<NetBean<LoginBean>> {
                override fun onComplete() {

                }

                override fun onNext(t: NetBean<LoginBean>) {
                    Toast.makeText(this@LoginActivity, "登录成功${t.result}", Toast.LENGTH_SHORT).show()
                    Log.d("LQS", "${t.toString()}")
                    AccountManager.getInstance().notifyLoginSuccess(t.result)//通知登录成功
                    //跳转到主页面
                    finish()//关闭该页面
                }

                override fun onError(e: Throwable) {

                }

                override fun onSubscribe(d: Disposable) {

                }
            })
    }



}