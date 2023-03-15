package com.example.rongyuntest

import android.app.Application
import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import io.rong.imkit.GlideKitImageEngine
import io.rong.imkit.RongIM
import io.rong.imkit.config.RongConfigCenter
import io.rong.imlib.model.Conversation


class MyApp : Application() {
    val mRongYunAppKey = "qd46yzrfqpr8f"

    override fun onCreate() {
        super.onCreate()
        RongIM.init(this, mRongYunAppKey, true)

        //伪代码, 从 sp 里读取用户是否已接受隐私协议
//        val isPrivacyAccepted: Boolean = getPrivacyStateFromSp()
//        //用户已接受隐私协议，进行初始化
//        if (isPrivacyAccepted) {
//            RongIM.init(this, mRongYunAppKey, true)
//        } else {
//            //用户未接受隐私协议，跳转到隐私授权页面。
//            goToPrivacyActivity()
//        }

        //设置会话页面的头像为圆形
        RongConfigCenter.featureConfig().kitImageEngine = object : GlideKitImageEngine() {

            //override fun loadConversationPortrait(context: Context, url: String, imageView: ImageView) {
            override fun loadConversationListPortrait(context: Context, url: String, imageView: ImageView, conversation: Conversation) {
                Glide.with(context).load(url)
                    .apply(RequestOptions.bitmapTransform(CircleCrop()))
                    .into(imageView)
            }
        }
    }
}