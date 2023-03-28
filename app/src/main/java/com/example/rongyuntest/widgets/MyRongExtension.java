//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.example.rongyuntest.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import androidx.fragment.app.Fragment;

import io.rong.imkit.conversation.extension.RongExtension;
import io.rong.imkit.conversation.extension.component.inputpanel.InputPanel;
import io.rong.imkit.conversation.extension.component.inputpanel.InputPanel.InputStyle;
import io.rong.imlib.model.Conversation.ConversationType;

public class MyRongExtension extends RongExtension {
    private String TAG = MyRongExtension.class.getSimpleName();

    public MyRongExtension(Context context) {
        super(context);
    }

    public MyRongExtension(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected InputPanel generateInputPanel(Fragment fragment, RelativeLayout inputPanelContainer, InputStyle inputStyle, ConversationType conversationType, String targetId) {
        return new MyInputPanel(fragment, inputPanelContainer, inputStyle, conversationType, targetId);
    }
}
