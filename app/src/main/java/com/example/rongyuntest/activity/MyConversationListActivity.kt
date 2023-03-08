package com.example.rongyuntest.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.rongyuntest.R
import com.example.rongyuntest.fragment.MyConversationListFragment

class MyConversationListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_conversation_list)

        supportFragmentManager.beginTransaction().run {
            this.replace(R.id.container, MyConversationListFragment())
            this.commit()
        }
    }
}