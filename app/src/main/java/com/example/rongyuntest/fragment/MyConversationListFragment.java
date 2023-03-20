package com.example.rongyuntest.fragment;//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.OnScrollListener;

import com.example.rongyuntest.adapter.MyConversationListAdapter;

import io.rong.common.RLog;
import io.rong.imkit.IMCenter;
import io.rong.imkit.R.id;
import io.rong.imkit.R.layout;
import io.rong.imkit.R.string;
import io.rong.imkit.config.ConversationListBehaviorListener;
import io.rong.imkit.config.RongConfigCenter;
import io.rong.imkit.conversationlist.ConversationListAdapter;
import io.rong.imkit.conversationlist.ConversationListFragment;
import io.rong.imkit.conversationlist.model.BaseUiConversation;
import io.rong.imkit.conversationlist.model.GatheredConversation;
import io.rong.imkit.conversationlist.viewmodel.ConversationListViewModel;
import io.rong.imkit.event.Event.RefreshEvent;
import io.rong.imkit.model.NoticeContent;
import io.rong.imkit.utils.RouteUtils;
import io.rong.imkit.widget.FixedLinearLayoutManager;
import io.rong.imkit.widget.adapter.ViewHolder;
import io.rong.imkit.widget.adapter.BaseAdapter.OnItemClickListener;
import io.rong.imkit.widget.dialog.OptionsPopupDialog;
import io.rong.imkit.widget.dialog.OptionsPopupDialog.OnOptionsItemClickedListener;
import io.rong.imkit.widget.refresh.SmartRefreshLayout;
import io.rong.imkit.widget.refresh.api.RefreshLayout;
import io.rong.imkit.widget.refresh.constant.RefreshState;
import io.rong.imkit.widget.refresh.listener.OnLoadMoreListener;
import io.rong.imkit.widget.refresh.listener.OnRefreshListener;
import io.rong.imkit.widget.refresh.wrapper.RongRefreshHeader;
import io.rong.imlib.RongIMClient.ErrorCode;
import io.rong.imlib.RongIMClient.ResultCallback;
import java.util.ArrayList;
import java.util.List;

public class MyConversationListFragment extends Fragment implements OnItemClickListener {
    protected final long NOTICE_SHOW_DELAY_MILLIS = 4000L;
    private final String TAG = MyConversationListFragment.class.getSimpleName();
    protected MyConversationListAdapter mAdapter = this.onResolveAdapter();
    protected RecyclerView mList;
    protected View mNoticeContainerView;
    protected TextView mNoticeContentTv;
    protected ImageView mNoticeIconIv;
    protected ConversationListViewModel mConversationListViewModel;
    protected SmartRefreshLayout mRefreshLayout;
    protected Handler mHandler = new Handler(Looper.getMainLooper());
    protected int mNewState = 0;
    protected boolean delayRefresh = false;

    public MyConversationListFragment() {
    }

    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(layout.rc_conversationlist_fragment, (ViewGroup)null, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!IMCenter.getInstance().isInitialized()) {
            RLog.e(this.TAG, "Please init SDK first!");
        } else {
            this.mList = (RecyclerView)view.findViewById(id.rc_conversation_list);
            this.mRefreshLayout = (SmartRefreshLayout)view.findViewById(id.rc_refresh);
            this.mAdapter.setItemClickListener(this);
            LinearLayoutManager layoutManager = new FixedLinearLayoutManager(this.getActivity());
            this.mList.setLayoutManager(layoutManager);
            this.mList.setAdapter(this.mAdapter);
            this.mList.addOnScrollListener(new OnScrollListener() {
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    MyConversationListFragment.this.mNewState = newState;
                    if (MyConversationListFragment.this.mNewState == 0 && MyConversationListFragment.this.delayRefresh && MyConversationListFragment.this.mAdapter != null && MyConversationListFragment.this.mConversationListViewModel != null) {
                        MyConversationListFragment.this.delayRefresh = false;
                        MyConversationListFragment.this.mAdapter.setDataCollection((List)MyConversationListFragment.this.mConversationListViewModel.getConversationListLiveData().getValue());
                    }

                }
            });
            this.mNoticeContainerView = view.findViewById(id.rc_conversationlist_notice_container);
            this.mNoticeContentTv = (TextView)view.findViewById(id.rc_conversationlist_notice_tv);
            this.mNoticeIconIv = (ImageView)view.findViewById(id.rc_conversationlist_notice_icon_iv);
            this.initRefreshView();
            this.subscribeUi();
        }
    }

    public void onResume() {
        super.onResume();
        if (this.mConversationListViewModel != null) {
            this.mConversationListViewModel.clearAllNotification();
        }

    }

    protected void initRefreshView() {
        this.mRefreshLayout.setNestedScrollingEnabled(false);
        this.mRefreshLayout.setRefreshHeader(new RongRefreshHeader(this.getContext()));
        this.mRefreshLayout.setRefreshFooter(new RongRefreshHeader(this.getContext()));
        this.mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                MyConversationListFragment.this.onConversationListRefresh(refreshLayout);
            }
        });
        this.mRefreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                MyConversationListFragment.this.onConversationListLoadMore();
            }
        });
    }

    protected void subscribeUi() {
        this.mConversationListViewModel = (ConversationListViewModel)(new ViewModelProvider(this)).get(ConversationListViewModel.class);
        this.mConversationListViewModel.getConversationList(false, false, 0L);
        this.mConversationListViewModel.getConversationListLiveData().observe(this.getViewLifecycleOwner(), new Observer<List<BaseUiConversation>>() {
            public void onChanged(List<BaseUiConversation> uiConversations) {
                RLog.d(MyConversationListFragment.this.TAG, "conversation list onChanged.");
                if (MyConversationListFragment.this.mNewState == 0) {
                    MyConversationListFragment.this.mAdapter.setDataCollection(uiConversations);
                } else {
                    MyConversationListFragment.this.delayRefresh = true;
                }

            }
        });
        this.mConversationListViewModel.getNoticeContentLiveData().observe(this.getViewLifecycleOwner(), new Observer<NoticeContent>() {
            public void onChanged(NoticeContent noticeContent) {
                if (MyConversationListFragment.this.mNoticeContainerView.getVisibility() == View.GONE) {
                    MyConversationListFragment.this.mHandler.postDelayed(new Runnable() {
                        public void run() {
                            MyConversationListFragment.this.updateNoticeContent((NoticeContent)MyConversationListFragment.this.mConversationListViewModel.getNoticeContentLiveData().getValue());
                        }
                    }, 4000L);
                } else {
                    MyConversationListFragment.this.updateNoticeContent(noticeContent);
                }

            }
        });
        this.mConversationListViewModel.getRefreshEventLiveData().observe(this.getViewLifecycleOwner(), new Observer<RefreshEvent>() {
            public void onChanged(RefreshEvent refreshEvent) {
                if (refreshEvent.state.equals(RefreshState.LoadFinish)) {
                    MyConversationListFragment.this.mRefreshLayout.finishLoadMore();
                } else if (refreshEvent.state.equals(RefreshState.RefreshFinish)) {
                    MyConversationListFragment.this.mRefreshLayout.finishRefresh();
                }

            }
        });
    }

    protected void onConversationListRefresh(RefreshLayout refreshLayout) {
        if (this.mConversationListViewModel != null) {
            this.mConversationListViewModel.getConversationList(false, true, 0L);
        }

    }

    protected void onConversationListLoadMore() {
        if (this.mConversationListViewModel != null) {
            this.mConversationListViewModel.getConversationList(true, true, 0L);
        }

    }

    protected void updateNoticeContent(NoticeContent content) {
        if (content != null) {
            if (content.isShowNotice()) {
                this.mNoticeContainerView.setVisibility(View.VISIBLE);
                this.mNoticeContentTv.setText(content.getContent());
                if (content.getIconResId() != 0) {
                    this.mNoticeIconIv.setImageResource(content.getIconResId());
                }
            } else {
                this.mNoticeContainerView.setVisibility(View.GONE);
            }

        }
    }

    protected MyConversationListAdapter onResolveAdapter() {
        this.mAdapter = new MyConversationListAdapter();
        this.mAdapter.setEmptyView(layout.rc_conversationlist_empty_view);
        return this.mAdapter;
    }

    public void onItemClick(View view, ViewHolder holder, int position) {
        if (position >= 0) {
            BaseUiConversation baseUiConversation = (BaseUiConversation)this.mAdapter.getItem(position);
            ConversationListBehaviorListener listBehaviorListener = RongConfigCenter.conversationListConfig().getListener();
            if (listBehaviorListener != null && listBehaviorListener.onConversationClick(view.getContext(), view, baseUiConversation)) {
                RLog.d(this.TAG, "ConversationList item click event has been intercepted by App.");
            } else {
                if (baseUiConversation != null && baseUiConversation.mCore != null) {
                    if (baseUiConversation instanceof GatheredConversation) {
                        RouteUtils.routeToSubConversationListActivity(view.getContext(), ((GatheredConversation)baseUiConversation).mGatheredType, baseUiConversation.mCore.getConversationTitle());
                    } else {
                        RouteUtils.routeToConversationActivity(view.getContext(), baseUiConversation.getConversationIdentifier());
                    }
                } else {
                    RLog.e(this.TAG, "invalid conversation.");
                }

            }
        }
    }

    public boolean onItemLongClick(final View view, ViewHolder holder, int position) {
        if (position < 0) {
            return false;
        } else {
            final BaseUiConversation baseUiConversation = (BaseUiConversation)this.mAdapter.getItem(position);
            ConversationListBehaviorListener listBehaviorListener = RongConfigCenter.conversationListConfig().getListener();
            if (listBehaviorListener != null && listBehaviorListener.onConversationLongClick(view.getContext(), view, baseUiConversation)) {
                RLog.d(this.TAG, "ConversationList item click event has been intercepted by App.");
                return true;
            } else {
                final ArrayList<String> items = new ArrayList();
                final String removeItem = view.getContext().getResources().getString(string.rc_conversation_list_dialog_remove);
                final String setTopItem = view.getContext().getResources().getString(string.rc_conversation_list_dialog_set_top);
                final String cancelTopItem = view.getContext().getResources().getString(string.rc_conversation_list_dialog_cancel_top);
                if (!(baseUiConversation instanceof GatheredConversation)) {
                    if (baseUiConversation.mCore.isTop()) {
                        items.add(cancelTopItem);
                    } else {
                        items.add(setTopItem);
                    }
                }

                items.add(removeItem);
                int size = items.size();
                OptionsPopupDialog.newInstance(view.getContext(), (String[])items.toArray(new String[size])).setOptionsPopupDialogListener(new OnOptionsItemClickedListener() {
                    public void onOptionsItemClicked(final int which) {
                        if (!((String)items.get(which)).equals(setTopItem) && !((String)items.get(which)).equals(cancelTopItem)) {
                            if (((String)items.get(which)).equals(removeItem)) {
                                IMCenter.getInstance().removeConversation(baseUiConversation.mCore.getConversationType(), baseUiConversation.mCore.getTargetId(), (ResultCallback)null);
                            }
                        } else {
                            IMCenter.getInstance().setConversationToTop(baseUiConversation.getConversationIdentifier(), !baseUiConversation.mCore.isTop(), false, new ResultCallback<Boolean>() {
                                public void onSuccess(Boolean value) {
                                    Toast.makeText(view.getContext(), (CharSequence)items.get(which), Toast.LENGTH_SHORT).show();
                                }

                                public void onError(ErrorCode errorCode) {
                                }
                            });
                        }

                    }
                }).show();
                return true;
            }
        }
    }

    public void addHeaderView(View view) {
        this.mAdapter.addHeaderView(view);
    }

    public void addFooterView(View view) {
        this.mAdapter.addFootView(view);
    }

    public void setEmptyView(View view) {
        this.mAdapter.setEmptyView(view);
    }

    public void setEmptyView(@LayoutRes int emptyId) {
        this.mAdapter.setEmptyView(emptyId);
    }
}
