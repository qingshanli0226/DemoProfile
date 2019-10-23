package com.example.bottombar.demoprofile

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.MediaController
import android.widget.Toast
import com.example.bottombar.net.LoginBean
import com.example.bottombar.net.NetBean
import com.example.bottombar.net.RetrofitCreator
import com.squareup.picasso.Picasso
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

import kotlinx.android.synthetic.main.fragment_profile.view.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class ProfileFragment : Fragment(), AccountManager.IAccountStatusChangeListener {

    val CAPTURE_REQUEST_CODE = 200
    val CROP_REQUEST_CODE = 300
    val BASER_URL:String = "http://169.254.230.253:8080/atguigu/img"

    //需要申请这两个权限
    private var permissions:Array<String> = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA)

    private lateinit var avatarImageView: ImageView

    //拍的照片存储路径
    private var photoPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "com.example.bottombar.demoprofile"+ "/1704.jpg"
    //存放裁减后的图片
    private var cropPhotoPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "com.example.bottombar.demoprofile"+ "/" + System.currentTimeMillis() + "_crop.jpg"
    private var testPhotoPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "image.jpg"


    override fun onRegisterSuccess() {

    }

    override fun onLoginSuccess(bean: LoginBean?) {
        if (bean!!.avatar != null) {//如果我S们没有上传图片，avatar值为null，直接返回
            Picasso.get().load(BASER_URL+ bean!!.avatar as String).into(avatarImageView)
            return
        } else {//否则上传一张图片
            sendAvatarToServer()
        }

    }

    override fun onLogout() {
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        var rootView = inflater.inflate(R.layout.fragment_profile, container, false)
        avatarImageView = rootView.avatar

        rootView.avatar.setOnClickListener{
            if (AccountManager.getInstance().isLogin) {
                //需要根据当前用户是否已经上传头像，如果之前已经上传则提示用户是否要更新头像,进行上传头像
                if (AccountManager.getInstance().isHasAvatar) {
                    Log.d("LQS","isHasAvatar")
                    //提示用户更新
                } else {
                    if (Build.VERSION.SDK_INT >= 23) {//只有系统版本大于23时才申请
                        //上传头像
                        var requestPermissons: ArrayList<String> = getRequestPermissions()
                        if (requestPermissons.isNotEmpty()) {
                            var arrayPerm: Array<String?> = arrayOfNulls<String>(requestPermissons.size)
                            var i:Int = 0
                            for(i in requestPermissons.indices) {
                                arrayPerm.set(i, requestPermissons.get(i))
                            }

                            //异步请求
                            requestPermissions(arrayPerm, 100) //第二参数是requestcode
                        } else {//之前已经授权了直接执行
                            Log.d("LQS","takePhoto")
                            takePhoto()
                        }
                    }
                }
            } else {
                //如果没有登录，点击默认头像，将会跳转到登录界面
                var intent = Intent()
                Log.d("LQS","LoginActivity")
                intent.setClass(activity, LoginActivity::class.java)
                activity!!.startActivity(intent)
            }
        }

        AccountManager.getInstance().addIAccountStatusChangeListener(this)

        return rootView
    }

    //申请权限的回调函数
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != 100) { //确保只处理我们自己申请的权限
            return
        }

        var flag : Boolean = true

        //判断申请权限是否成功
        for (grantItem in grantResults) {
            //判断申请的某一个权限是否赋值,如果不等于它，代表有个权限，用户并没有对这个权限授权
            if (grantItem != PackageManager.PERMISSION_GRANTED) {
                flag = false
            }
        }

        if (flag == false) {
            Toast.makeText(activity, "需要全部授权，否则，有些工作无法完成", Toast.LENGTH_SHORT).show()
        } else {
            //调用Camera拍照头像
            Log.d("LQS","全部授权，开始拍照")
            takePhoto()
        }
    }

    //拍照函数
    private fun takePhoto() {
        var photoFile = File(photoPath)
        if (!photoFile.parentFile.exists())  {//如果存放照片的路径不存在，创建该路径
            photoFile.parentFile.mkdir()
            Log.d("LQS", "takePhoto mkdir ${photoFile.parentFile.absolutePath}")
        }
        Log.d("LQS", "takePhoto...")
        //如果之前的照片存在则删掉
        if (photoFile.exists()) {
            photoFile.delete()
        }

        var photoUri:Uri
        var intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        //版本判断
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            photoUri = FileProvider.getUriForFile(activity!!, activity!!.packageName + ".fileprovider", photoFile)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)//添加读取uri的权限
        } else {
            photoUri = Uri.fromFile(photoFile)//当版本低于23时
        }

        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        startActivityForResult(intent, CAPTURE_REQUEST_CODE)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("LQS", "onActivityResult: " + requestCode)

        when(requestCode) {
            //拍照返回
            CAPTURE_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    var bitmap = BitmapFactory.decodeFile(photoPath)//照片返回，生成bitmap
                    Log.d("LQS", "尺寸：${bitmap.byteCount}")

                    //需要裁减的图片.
                    var inFile = File(photoPath)
                    //生成需要裁减图片的uri，最为输入
                    var inUri = FileProvider.getUriForFile(activity!!, activity!!.packageName + ".fileprovider", inFile)
                    var outUri = Uri.fromFile(File(cropPhotoPath))//输出uri

                    cropImage(activity!!, inUri, outUri, 300, 600)
                }
            }
            //裁减返回
            CROP_REQUEST_CODE-> {
                if (resultCode == Activity.RESULT_OK) {
                    var bitmap = BitmapFactory.decodeFile(cropPhotoPath)//照片返回，生成bitmap
                    Log.d("LQS", "crop 尺寸：${bitmap.byteCount}")
                    avatarImageView.setImageBitmap(bitmap)//设置bitmap

                    sendAvatarToServer()
                }
            }
        }

    }

    private fun sendAvatarToServer() {
        Log.d("LQS server path: ", "sendAvatarToServer")
        //创建一个请求body
        var uploadFile = File(testPhotoPath)
        var requestBody = RequestBody.create(MediaType.parse("image/*"), uploadFile)

        //创建Part参数
        var uploadPart = MultipartBody.Part.createFormData("file", uploadFile.name, requestBody)

        RetrofitCreator.getApiService().uploadAvatar(uploadPart)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object: Observer<NetBean<String>> {
                override fun onNext(t: NetBean<String>) {

                    Log.d("LQS server path: ", t.result)
                    Picasso.get().load(BASER_URL+t.result).into(avatarImageView)
                }

                override fun onComplete() {

                }

                override fun onError(e: Throwable) {
                    Log.d("LQS server path: ", e.printStackTrace().toString())
                }

                override fun onSubscribe(d: Disposable) {

                }
            })

    }

    private fun cropImage(context: Context, inUri:Uri, outUri:Uri, width:Int, height:Int) {
        var intent = Intent("com.android.camera.action.CROP")
        intent.setDataAndType(inUri, "image/*")//告诉是一张图片
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) //读写Uri的权限
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

        intent.putExtra("crop", true)
        intent.putExtra("aspectX", 1)//长和宽的比例为1 ： 1
        intent.putExtra("aspectY", 2)
        intent.putExtra("outputX", width)//设置裁减后图片的宽度和高度
        intent.putExtra("outputY", height)
        intent.putExtra("scale", true)//原图课缩放
        intent.putExtra("return-data", false)//是否返回数据
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outUri)//输出uri
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString())//输出文件的格式为jpg
        intent.putExtra("noFaceDetection", true)//无需做人脸识别

        startActivityForResult(intent, CROP_REQUEST_CODE)//调用系统应用做裁减

    }

    private fun getRequestPermissions():ArrayList<String> {
        var requestPermission:ArrayList<String> = ArrayList()
         for(perm in permissions) {
             //判断当前应用是否已经有该权限
             if (ContextCompat.checkSelfPermission(activity!!, perm) != PackageManager.PERMISSION_GRANTED) {
                  requestPermission.add(perm)//如果没有授权，则把它放到需要申请权限的列表中

             }
         }

        return  requestPermission
    }


}