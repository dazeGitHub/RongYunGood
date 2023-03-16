package com.example.rongyuntest.activity

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
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

    var myRongYunUserId = "1"
    var myRongYunUserName = "userName1"
    val mMyRongYunToken = "R88Mcx4IW5/gHTFGZkr8jiA5N7pQvzOH@9lg3.cn.rongnav.com;9lg3.cn.rongcfg.com"
    var myRongYunAvatarUrl = "https://img1.baidu.com/it/u=4289695845,2474608469&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500"

    var targetRongYunUserId = "72C0247BEB675B7A2327C253315A33E8"
    var targetRongYunUserName = "张三1";
    var targetRongYunAvatarUrl = "https://img1.baidu.com/it/u=1403245892,3051757811&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500";

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        refreshShowContent()

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
        findViewById<View>(R.id.btn_refresh_show).setOnClickListener{
            refreshShowContent();
        }
        findViewById<View>(R.id.btn_sync_myrongyun_userinfo).setOnClickListener{
            syncMyRongYunUserInfo()
        }
        findViewById<View>(R.id.btn_sync_target_rongyun_userinfo).setOnClickListener{
            syncTargetRongYunUserInfo()
        }
        findViewById<View>(R.id.btn_logout).setOnClickListener{
            logout()
        }
    }

    fun rongYunLogin(){
        //调用 Server 接口, Server 通过 userId 获取 Token (也就是注册融云用户)
        RongIM.connect(mMyRongYunToken, object : RongIMClient.ConnectCallback() {
            override fun onSuccess(userId: String) {
                Log.e("TAG", "userId = $userId")
                Toast.makeText(this@MainActivity, "RongIM 登录成功, rongyun userId = $userId", Toast.LENGTH_SHORT).show()
            }

            override fun onError(errorCode: RongIMClient.ConnectionErrorCode) {
                Toast.makeText(this@MainActivity, "RongIM 登录失败, errorCode = " + errorCode.value + " value = " + errorCode.name, Toast.LENGTH_SHORT).show()
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
        RouteUtils.routeToConversationActivity(this, Conversation.ConversationType.PRIVATE, targetRongYunUserId.toString(), false)
    }

    fun syncMyRongYunUserInfo(){
        val myUserInfo = UserInfo(myRongYunUserId, myRongYunUserName, Uri.parse(myRongYunAvatarUrl))
        RongUserInfoManager.getInstance().refreshUserInfoCache(myUserInfo)
        Toast.makeText(this@MainActivity, "同步自己的融云用户信息 成功", Toast.LENGTH_SHORT).show()
    }

    fun syncTargetRongYunUserInfo(){
        val targetUserInfo = UserInfo(targetRongYunUserId, targetRongYunUserName, Uri.parse(targetRongYunAvatarUrl))
        RongUserInfoManager.getInstance().refreshUserInfoCache(targetUserInfo)
        Toast.makeText(this@MainActivity, "同步对方的融云用户信息 成功", Toast.LENGTH_SHORT).show()
    }

    fun setRongYunUserInfoCallback(){
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

    fun getRongYunUserInfo(rongYunUserId : String): UserInfo?{
        val userInfo : UserInfo?  = RongUserInfoManager.getInstance().getUserInfo(rongYunUserId)
        Log.d("TAG", "getRongYunUserInfo() userInfo = $userInfo")
        return userInfo;
    }

    fun refreshShowContent(){
        val myUserInfo = getRongYunUserInfo(myRongYunUserId)
        findViewById<TextView>(R.id.tv_content).text =
            "当前登录的 融云用户Id : " + (myUserInfo?.userId) + "\n当前登录的 融云用户名 : " + (myUserInfo?.name) +
            "\n当前登录的头像链接 : " + (myUserInfo?.portraitUri) +
            "\n对方的 融云用户Id : " + targetRongYunUserId + "\n对方的 融云用户名 : " + targetRongYunUserName +
            "\n对方的 头像链接 : " + targetRongYunAvatarUrl
    }

    fun logout(){
        RongIM.getInstance().logout()
    }
}