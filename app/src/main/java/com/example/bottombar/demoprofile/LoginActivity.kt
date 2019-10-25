package com.example.bottombar.demoprofile

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.example.bottombar.net.*
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
            .map(NetFunction<NetBean<LoginBean>, LoginBean>())
            .subscribe(object :MyObserver<LoginBean>() {
                override fun onNext(t: LoginBean) {
                    Toast.makeText(this@LoginActivity, "${t.toString()}", Toast.LENGTH_SHORT).show()
                    AccountManager.getInstance().notifyLoginSuccess(t)//通知登录成功
                }
            })
    }



}