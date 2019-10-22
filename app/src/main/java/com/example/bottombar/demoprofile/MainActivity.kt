package com.example.bottombar.demoprofile

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewpager.adapter = ProfileFragmentAdapter(supportFragmentManager)
        tabLayout.setupWithViewPager(viewpager)
    }

    //如果使用SingleTask，SingleTop， SingletInstance几种启动模式都需要重写onNewIntent
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
    }
}
