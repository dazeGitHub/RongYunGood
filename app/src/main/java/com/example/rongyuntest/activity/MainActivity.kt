package com.example.rongyuntest.activity

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.rongyuntest.R
import io.rong.imkit.RongIM
import io.rong.imkit.userinfo.RongUserInfoManager
import io.rong.imkit.utils.RouteUtils
import io.rong.imlib.RongIMClient
import io.rong.imlib.model.Conversation
import io.rong.imlib.model.UserInfo


class MainActivity : AppCompatActivity() {

    //userId 为 1 的 token
    val mToken = "prmq5fOQH91/GdcM+pAKF5Lc08mnNsXT@h767.cn.rongnav.com;h767.cn.rongcfg.com"
    var targetUserId = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.btn_login_rongyun).setOnClickListener {
            rongYunLogin()
        }
        findViewById<View>(R.id.btn_register_cust_page).setOnClickListener{
            registerCustActivity()
        }
        findViewById<View>(R.id.btn_jump_conversation_list_page).setOnClickListener{
            jumpConversationListActivity()
        }
        findViewById<View>(R.id.btn_jump_conversation_page).setOnClickListener{
            jumpConversationActivity()
        }
        findViewById<View>(R.id.btn_logout).setOnClickListener{
            logout()
        }
    }

    fun rongYunLogin(){
        //调用 Server 接口, Server 通过 userId 获取 Token (也就是注册融云用户)
        RongIM.connect(mToken, object : RongIMClient.ConnectCallback() {
            override fun onSuccess(userId: String) {
                Toast.makeText(this@MainActivity, "RongIM 登录成功", Toast.LENGTH_SHORT).show()
            }

            override fun onError(errorCode: RongIMClient.ConnectionErrorCode) {
                when (errorCode) {
                    RongIMClient.ConnectionErrorCode.RC_CONN_TOKEN_EXPIRE -> {
                        //从 APP 服务请求新 token，获取到新 token 后重新 connect()
                    }
                    RongIMClient.ConnectionErrorCode.RC_CONNECT_TIMEOUT -> {
                        //连接超时，弹出提示，可以引导用户等待网络正常的时候再次点击进行连接
                    }
                    else -> {
                        //其它业务错误码，请根据相应的错误码作出对应处理。
                    }
                }
            }
            override fun onDatabaseOpened(databaseOpenStatus: RongIMClient.DatabaseOpenStatus) {}
        })
    }


    fun registerCustActivity(){
        //注册自定义会话列表页
        RouteUtils.registerActivity(RouteUtils.RongActivityType.ConversationListActivity, MyConversationListActivity::class.java)

        //注册自定义会话页
        RouteUtils.registerActivity(RouteUtils.RongActivityType.ConversationActivity, MyConversationActivity::class.java)
    }

    //跳转到自定义会话列表页
    fun jumpConversationListActivity(){
        RouteUtils.routeToConversationListActivity(this@MainActivity, "")
    }

    //跳转到自定义会话页
    fun jumpConversationActivity(){
        RouteUtils.routeToConversationActivity(this, Conversation.ConversationType.PRIVATE, targetUserId.toString(), false)
    }



    fun refreshRongYunUserInfo(){
        // 允许 SDK 在本地持久化存储用户信息
        val isCacheUserInfo = true
        RongUserInfoManager.getInstance().setUserInfoProvider({ userId ->
            // 在需要展示用户信息时（例如会话列表页面、会话页面），IMKit 首先会根据用户 ID 逐个调用 getUserInfo。
            // 此处由 App 自行完成异步请求用户信息的逻辑。后续通过 refreshUserInfoCache 提供给 SDK。

            //同步返回 userInfo
            val userInfo : UserInfo = UserInfo(userId, "userName", Uri.parse("user 头像 url"))

            //异步返回 userInfo
            RongUserInfoManager.getInstance().refreshUserInfoCache(userInfo)

            userInfo
        }, isCacheUserInfo)
    }

    fun getRongYunUserInfo(){
        val userId : String  = "1"
        val userInfo : UserInfo  = RongUserInfoManager.getInstance().getUserInfo(userId)
        Log.d("TAG", "getRongYunUserInfo() userInfo = $userInfo")
    }

    fun logout(){
        RongIM.getInstance().logout()
    }
}