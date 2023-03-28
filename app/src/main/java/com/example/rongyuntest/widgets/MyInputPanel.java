//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.example.rongyuntest.widgets;

import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;


import com.example.rongyuntest.R;

import io.rong.imkit.conversation.extension.component.inputpanel.InputPanel;
import io.rong.imkit.picture.tools.ToastUtils;
import io.rong.imlib.model.Conversation.ConversationType;

public class MyInputPanel extends InputPanel{
    private final String TAG = this.getClass().getSimpleName();
    private TextView mSendBtn;
    private boolean mCanSendStatus = true;

    public MyInputPanel(Fragment fragment, ViewGroup parent, InputStyle inputStyle, ConversationType type, String targetId) {
        super(fragment, parent, inputStyle, type, targetId);
    }

    @Override
    protected void onSendBtnClick() {
        if(mCanSendStatus){
            super.onSendBtnClick();
        }else{
//            ToastUtils.toast(mContext.getString(R.string.cannot_send));
            Toast.makeText(mContext, "不支持给平台发送消息", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onAddBtnClick() {
        if(mCanSendStatus){
            super.onAddBtnClick();
        }else{
            Toast.makeText(mContext, "不支持给平台发送消息", Toast.LENGTH_SHORT).show();
        }
    }

    public void setSendStatus(boolean status) {
        mCanSendStatus = status;
    }
}
