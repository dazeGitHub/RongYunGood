package com.example.rongyuntest.activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.rongyuntest.R
import com.example.rongyuntest.fragment.MyConversationFragment

class MyConversationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_conversation)


        val bundle = intent.extras
        Log.e("TAG", "bundle" + bundle);

        // 添加会话界面
        supportFragmentManager.beginTransaction().run {
            this.replace(R.id.container, MyConversationFragment())
            this.commit()
        }
    }
}