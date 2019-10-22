package com.example.bottombar.demoprofile

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter

class ProfileFragmentAdapter(manager:FragmentManager) : FragmentStatePagerAdapter(manager) {

    private var fragments:Array<Fragment> = arrayOf(HomeFragment(), ProfileFragment())
    private var titles:Array<String> = arrayOf("首页", "个人中心")

    override fun getItem(index: Int): Fragment {
        return fragments[index]

    }

    override fun getCount(): Int {
        return fragments.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return titles[position]
    }

}