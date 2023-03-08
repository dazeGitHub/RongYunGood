package com.example.rongyuntest.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.rongyuntest.R
import com.example.rongyuntest.fragment.MyConversationFragment

class MyConversationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_conversation)

        // 添加会话界面
//        supportFragmentManager.beginTransaction().run {
//            this.replace(R.id.container, MyConversationFragment())
//            this.commit()
//        }
    }
}