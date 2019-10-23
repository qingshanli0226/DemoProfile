package com.example.bottombar.demoprofile;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.bottombar.net.LoginBean;
import com.example.bottombar.net.TokenCache;

import java.util.ArrayList;
import java.util.List;

public class AccountManager {
    private static AccountManager instance;
    private final String TOKEN = "token";
    private LoginBean loginBean;

    //登录信息多个页面需要监听
    private List<IAccountStatusChangeListener> accountStatusChangeListeners = new ArrayList<>();

    private AccountManager() {

    }
    public static AccountManager getInstance(){
        if (instance == null) {
            instance = new AccountManager();
        }

        return instance;
    }

    public void addIAccountStatusChangeListener(IAccountStatusChangeListener listener) {
        synchronized (AccountManager.class) {//需要考虑使用同步，保证数据的多线程安全
            accountStatusChangeListeners.add(listener);
        }
    }

    public void removeIAccountStatusChangeListener(IAccountStatusChangeListener listener) {
        synchronized (AccountManager.class) {
            if (accountStatusChangeListeners.contains(listener)) {//判断是否存在，存在则删除
                accountStatusChangeListeners.remove(listener);
            }
        }
    }

    public String getToken(){
        if (isLogin()) {
            SharedPreferences sp = ProfileApplication.instance.getSharedPreferences(TOKEN, Context.MODE_PRIVATE);
            return sp.getString(TOKEN, "");
        }
        return null;
    }

    //存储token
    public void saveToken(String token) {
        SharedPreferences sp = ProfileApplication.instance.getSharedPreferences(TOKEN, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        editor.putString(TOKEN, token).commit();
    }

    //判断当前应用是否登录
    public boolean isLogin() {
        SharedPreferences sp = ProfileApplication.instance.getSharedPreferences(TOKEN, Context.MODE_PRIVATE);
        return sp.getString(TOKEN, "").isEmpty() ? false:true;
    }

    //通知注册成功
    public void notifyRegisterSuccess() {
        synchronized (AccountManager.class) {
            for(IAccountStatusChangeListener listener:accountStatusChangeListeners) {
                listener.onRegisterSuccess();
            }
        }
    }

    //通知登录成功,带参数，把用户信息传递过去
    public void notifyLoginSuccess(LoginBean bean) {
        loginBean = bean;//内存缓存一份
        saveToken(bean.getToken());//token存储到本地
        TokenCache.token = bean.getToken();//把token 设置到net 模块
        synchronized (AccountManager.class) {
            for(IAccountStatusChangeListener listener:accountStatusChangeListeners) {
                listener.onLoginSuccess(bean);
            }

        }
    }

    //通知退出登录
    public void notifyLogout() {
        synchronized (AccountManager.class) {
            for(IAccountStatusChangeListener listener:accountStatusChangeListeners) {
                listener.onLogout();
            }
            loginBean = null;//内存缓存一份
        }
    }

    //判断当前用户是否上传头像
    public boolean isHasAvatar() {
        if (loginBean == null) {
            return  false;
        }
        return loginBean.getAvatar() != null;
    }

    //获取头像地址
    public String getAvatar() {
        return (String) loginBean.getAvatar();
    }


    public interface IAccountStatusChangeListener{
        void onRegisterSuccess();//注册成功调用
        void onLoginSuccess(LoginBean bean);//登录成功调用
        void onLogout();//退出登录调用
    }
}
