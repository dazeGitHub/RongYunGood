//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.example.rongyuntest.adapter;

import android.view.ViewGroup;
import androidx.annotation.NonNull;
import io.rong.imkit.config.RongConfigCenter;
import io.rong.imkit.conversationlist.model.BaseUiConversation;
import io.rong.imkit.widget.adapter.BaseAdapter;
import io.rong.imkit.widget.adapter.ViewHolder;
import java.util.ArrayList;
import java.util.List;

public class MyConversationListAdapter extends BaseAdapter<BaseUiConversation> {

    public MyConversationListAdapter() {
        this.mProviderManager = RongConfigCenter.conversationListConfig().getProviderManager();
    }

    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return super.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
    }

    public void setDataCollection(List<BaseUiConversation> data) {
        if (data == null) {
            data = new ArrayList();
        }

        super.setDataCollection((List)data);
        this.notifyDataSetChanged();
    }
}
