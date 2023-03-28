//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.example.rongyuntest.fragment;

import android.view.View;


import io.rong.common.RLog;
import io.rong.imkit.config.ConversationListBehaviorListener;
import io.rong.imkit.config.RongConfigCenter;
import io.rong.imkit.conversationlist.ConversationListFragment;
import io.rong.imkit.conversationlist.model.BaseUiConversation;
import io.rong.imkit.conversationlist.model.GatheredConversation;
import io.rong.imkit.utils.RouteUtils;
import io.rong.imkit.widget.adapter.ViewHolder;

public class MyConversationListFragment extends ConversationListFragment{
    protected final long NOTICE_SHOW_DELAY_MILLIS = 4000L;
    private final String TAG = MyConversationListFragment.class.getSimpleName();

    public void onItemClick(View view, ViewHolder holder, int position) {
        if (position < 0) {
            return;
        }
        BaseUiConversation baseUiConversation = mAdapter.getItem(position);
        ConversationListBehaviorListener listBehaviorListener = RongConfigCenter.conversationListConfig().getListener();
        if (listBehaviorListener != null && listBehaviorListener.onConversationClick(view.getContext(), view, baseUiConversation)) {
            RLog.d(TAG, "ConversationList item click event has been intercepted by App.");
            return;
        }
        if (baseUiConversation != null && baseUiConversation.mCore != null) {
            if (baseUiConversation instanceof GatheredConversation) {
                RouteUtils.routeToSubConversationListActivity(view.getContext(), ((GatheredConversation) baseUiConversation).mGatheredType, baseUiConversation.mCore.getConversationTitle());
            } else {
//                Constant.TEMP_RY_TARGET_ID = baseUiConversation.mCore.getTargetId();
//                Constant.TEMP_RY_TARGET_TITLE = baseUiConversation.mCore.getConversationTitle();
                RouteUtils.routeToConversationActivity(view.getContext(), baseUiConversation.mCore.getConversationType(), baseUiConversation.mCore.getTargetId());
            }
        } else {
            RLog.e(TAG, "invalid conversation.");
        }
    }
}
