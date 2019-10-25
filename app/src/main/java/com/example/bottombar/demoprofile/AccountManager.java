package com.example.bottombar.demoprofile;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;
import com.example.bottombar.net.LoginBean;
import com.example.bottombar.net.RetrofitCreator;
import com.example.bottombar.net.TokenCache;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class AccountManager {
    private static AccountManager instance;
    private final String TOKEN = "token";
    private final String AVATAR = "avatar";
    private LoginBean loginBean;

    private String newAlbumPath;
    private String avatarDir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "com.example.bottombar.demoprofile"+ "/";


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
        return sp.getString(TOKEN, "").isEmpty() ? false:false;
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

        //检查当前头像是否是最新的
        if (checkIfAvatarNeedUpdate()) {
            updateAvatar();
        }
    }

    //通知UI头像已经更新
    private void notifyAvatarUpdate(String avatarPath) {
        synchronized (AccountManager.class) {
            for(IAccountStatusChangeListener listener:accountStatusChangeListeners) {
                listener.onAvatarUpdate(avatarPath);
            }
        }
        SharedPreferences sp = ProfileApplication.instance.getSharedPreferences(TOKEN, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        editor.putString(AVATAR, avatarPath).commit();
    }

    //更新头像
    private void updateAvatar() {

        Log.d("LQS", "updateAvatar");
        //第一步下载头像
        //先删除原来的
        File[] albumFiles = new File(avatarDir).listFiles();
        File albumFile = null;
        if (albumFiles.length > 0) {
            albumFile = albumFiles[0];
            if (albumFile.exists()) {
                albumFile.delete();
            }
        }

                //生成一个url地址
        String avatarServer = (String) loginBean.getAvatar();
        String downloadUrl = RetrofitCreator.Companion.getBaseUrl()+"atguigu/img/" + avatarServer.substring(1, avatarServer.length());
        Log.d("LQS", "downloadUrl: " + downloadUrl);
        RetrofitCreator.Companion.getApiService().downloadAvatar(downloadUrl)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        byte[] buffer = new byte[1024];
                        Log.d("LQS", "下载文件开始");
                        InputStream inputStream = responseBody.byteStream();
                        FileOutputStream fileOutputStream = null;
                        try {
                            fileOutputStream = new FileOutputStream(generateNewAlbumPath());
                            while (inputStream.read(buffer) != -1) {
                                Log.d("LQS", "下载文件....");
                                fileOutputStream.write(buffer);
                            }
                            fileOutputStream.flush();
                            notifyAvatarUpdate(newAlbumPath);//通知用户头像已经更新.*/

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            if (inputStream != null) {
                                try {
                                    inputStream.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (fileOutputStream != null) {
                                try {
                                    fileOutputStream.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("LQS", "onError");
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {

                    }
                });


        //第二步通知头像更新完成，刷新UI
    }

    public boolean checkIfAvatarNeedUpdate() {
        String avatarServerString = (String) loginBean.getAvatar();
        if (avatarServerString == null) {
            return false;
        }
        String[] str = avatarServerString.substring(1, avatarServerString.length()).split("/");
        String[] str1 = str[1].split(".");
        //Log.d("LQS str1[0]: ", str1[0]);
        long timeServer = Long.valueOf(str[1].substring(0,str[1].length()-4));
        Log.d("LQS timeserver: ", timeServer+"");

        String albumPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "com.example.bottombar.demoprofile";
        //如果本地没有文件，需要从服务端下载
        if (new File(albumPath).list().length == 0) {
            return true;
        }

        File albumFile = new File(albumPath).listFiles()[0];
        String[] localStr = albumFile.getAbsolutePath().split("/");
        long timeLocal = Long.valueOf(localStr[localStr.length - 1].split("_")[0]);
        if (timeServer > timeLocal) { //服务端的时间比本地时间新，需要下载头像更新
            return true;
        }

        return false;
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
        SharedPreferences sp = ProfileApplication.instance.getSharedPreferences(TOKEN, Context.MODE_PRIVATE);

        return sp.getString(AVATAR, "");
    }
    public String generateNewAlbumPath() {
        newAlbumPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "com.example.bottombar.demoprofile"+ "/" + System.currentTimeMillis() + "_1704_album.jpg";
        return newAlbumPath;
    }

    public String getNewAlbumPath() {
        return newAlbumPath;
    }

    public void setNewAlbumPath(String newAlbumPath) {
        this.newAlbumPath = newAlbumPath;
    }


    public interface IAccountStatusChangeListener{
        void onRegisterSuccess();//注册成功调用
        void onLoginSuccess(LoginBean bean);//登录成功调用
        void onAvatarUpdate(String avatarPath);//头像更新的回调
        void onLogout();//退出登录调用
    }
}
