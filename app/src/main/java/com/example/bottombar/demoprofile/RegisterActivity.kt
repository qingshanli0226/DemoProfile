package com.example.bottombar.demoprofile

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.example.bottombar.net.NetBean
import com.example.bottombar.net.RetrofitCreator
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_register)

        register.setOnClickListener{
            register()
        }
    }

    private fun register() {
        RetrofitCreator.getApiService().register(name.text.toString(), password.text.toString())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<NetBean<String>> {
                override fun onNext(t: NetBean<String>) {
                    Toast.makeText(this@RegisterActivity, "注册成功${t.result}", Toast.LENGTH_SHORT).show()
                    AccountManager.getInstance().notifyRegisterSuccess()//通知注册成功
                    var intent = Intent()//跳转
                    intent.setClass(this@RegisterActivity, LoginActivity::class.java)
                    startActivity(intent)
                    finish()//注册成功后，关闭注册界面
                }

                override fun onComplete() {

                }

                override fun onError(e: Throwable) {
                    Log.d("LQS", "" + e.printStackTrace())
                    Toast.makeText(this@RegisterActivity, "注册失败${e.printStackTrace()}", Toast.LENGTH_SHORT).show()
                }

                override fun onSubscribe(d: Disposable) {

                }
            } )
    }



}