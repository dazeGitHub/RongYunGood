//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.example.rongyuntest.fragment;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.example.rongyuntest.R;
import com.example.rongyuntest.widgets.MyInputPanel;
import com.example.rongyuntest.widgets.MyRongExtension;

import io.rong.imkit.conversation.ConversationFragment;
import io.rong.imkit.model.UiMessage;
import io.rong.imkit.widget.adapter.IViewProviderListener;
import io.rong.imkit.widget.refresh.listener.OnLoadMoreListener;
import io.rong.imkit.widget.refresh.listener.OnRefreshListener;

public class MyConversationFragment extends ConversationFragment implements OnRefreshListener, OnClickListener, OnLoadMoreListener, IViewProviderListener<UiMessage> {
    private final String TAG = ConversationFragment.class.getSimpleName();
    private int mRecyLastPosition = 0;
    private boolean mIsJustKeyBoardChanged = false;
//    private KeyBoardUtils keyBoardUtils = null;

//    Observer<List<UiMessage>> mListObserver = new Observer<List<UiMessage>>() {
//        public void onChanged(List<UiMessage> uiMessages) {
//            MyConversationFragment.this.refreshList(uiMessages);
//            mRecyLastPosition = MyConversationFragment.this.mAdapter.getItemCount() - 1;
//            if(mRecyLastPosition != 0){
//                mRecyclerView.scrollToPosition(mRecyLastPosition);
//            }
//        }
//    };
//    private OnScrollListener mScrollListener = new OnScrollListener() {
//        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
//            super.onScrolled(recyclerView, dx, dy);
//            if(!mIsJustKeyBoardChanged){
//                //当软键盘抬起后, 手动滑动 RecyclerView 时, 会触发 mScrollListener, 但是 dy != 0
//                //当软键盘抬起时, 也会触发 mScrollListener, 但是 dy == 0;  所以需要屏蔽掉
//                if(dy != 0){
//                    mRecyLastPosition = mLinearLayoutManager.findLastVisibleItemPosition();
//                    MyConversationFragment.this.mMessageViewModel.onScrolled(recyclerView, dx, dy);
//                }
//            }else{
//                mIsJustKeyBoardChanged = false;
//            }
//        }
//    };


    @Override
    protected Integer getLayoutId() {
        return R.layout.my_rc_conversation_fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initKeyBoards();
    }

    private void initKeyBoards(){

//        if(this.getActivity() != null){
//            keyBoardUtils = new KeyBoardUtils(this.getActivity());
//            keyBoardUtils.addSoftKeyboardChangedListener(new KeyBoardUtils.OnSoftKeyboardStateChangedListener(){
//                @Override
//                public void OnSoftKeyboardStateChanged(boolean isKeyBoardShow, int keyboardHeight) {
//                    ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) mBottomPlaceHolderView.getLayoutParams();
//                    mIsJustKeyBoardChanged = true;
//                    if(isKeyBoardShow){
//                        mBottomPlaceHolderView.setVisibility(View.VISIBLE);
//                        if(mRecyLastPosition != 0){
//                            mRecyclerView.scrollToPosition(mRecyLastPosition);
//                        }
//                    }else{
//                        mBottomPlaceHolderView.setVisibility(View.INVISIBLE);
//                        if(mRecyLastPosition != 0){
//                            mRecyclerView.scrollToPosition(mRecyLastPosition);
//                        }
//                    }
//                }
//            });
//        }
    }

    public void setSendStatus(boolean status) {
        ((MyInputPanel)((MyRongExtension)getRongExtension()).getInputPanel()).setSendStatus(status);
    }
}
