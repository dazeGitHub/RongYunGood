//package com.example.rongyuntest.fragment
//
//
//import android.app.AlertDialog
//import android.content.Intent
//import android.os.Bundle
//import android.os.Parcelable
//import android.text.TextUtils
//import android.view.*
//import android.view.GestureDetector.SimpleOnGestureListener
//import android.widget.LinearLayout
//import android.widget.TextView
//import android.widget.Toast
//import androidx.annotation.LayoutRes
//import androidx.lifecycle.Observer
//import androidx.lifecycle.ViewModelProvider
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import androidx.recyclerview.widget.RecyclerView.ItemAnimator
//import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener
//import com.example.rongyuntest.R
//import io.rong.common.RLog
//import io.rong.imkit.IMCenter
//import io.rong.imkit.MessageItemLongClickAction
//import io.rong.imkit.MessageItemLongClickActionManager
//import io.rong.imkit.R.layout
//import io.rong.imkit.R.string
//import io.rong.imkit.config.RongConfigCenter
//import io.rong.imkit.conversation.ConversationFragment
//import io.rong.imkit.conversation.MessageListAdapter
//import io.rong.imkit.conversation.extension.InputMode
//import io.rong.imkit.conversation.extension.RongExtension
//import io.rong.imkit.conversation.extension.RongExtensionViewModel
//import io.rong.imkit.conversation.messgelist.processor.IConversationUIRenderer
//import io.rong.imkit.conversation.messgelist.status.MessageProcessor.GetMessageCallback
//import io.rong.imkit.conversation.messgelist.viewmodel.MessageViewModel
//import io.rong.imkit.event.Event
//import io.rong.imkit.event.uievent.*
//import io.rong.imkit.feature.location.LocationUiRender
//import io.rong.imkit.feature.reference.ReferenceManager
//import io.rong.imkit.manager.MessageProviderPermissionHandler
//import io.rong.imkit.manager.hqvoicemessage.HQVoiceMsgDownloadManager
//import io.rong.imkit.model.UiMessage
//import io.rong.imkit.picture.tools.ToastUtils
//import io.rong.imkit.utils.PermissionCheckUtil
//import io.rong.imkit.utils.RongViewUtils
//import io.rong.imkit.widget.FixedLinearLayoutManager
//import io.rong.imkit.widget.adapter.BaseAdapter
//import io.rong.imkit.widget.adapter.IViewProviderListener
//import io.rong.imkit.widget.adapter.ViewHolder
//import io.rong.imkit.widget.dialog.OptionsPopupDialog
//import io.rong.imkit.widget.refresh.SmartRefreshLayout
//import io.rong.imkit.widget.refresh.api.RefreshLayout
//import io.rong.imkit.widget.refresh.constant.RefreshState
//import io.rong.imkit.widget.refresh.listener.OnLoadMoreListener
//import io.rong.imkit.widget.refresh.listener.OnRefreshListener
//import io.rong.imkit.widget.refresh.wrapper.RongRefreshHeader
//import io.rong.imlib.model.Conversation
//import io.rong.imlib.model.ConversationIdentifier
//import io.rong.imlib.model.Message
//import java.text.MessageFormat
//import java.util.*
//
//
//class MyConversationFragment : ConversationFragment(), OnRefreshListener,
//    View.OnClickListener, OnLoadMoreListener, IViewProviderListener<UiMessage?> {
//
//    private val TAG = ConversationFragment::class.java.simpleName
//    protected var mRefreshLayout: SmartRefreshLayout? = null
//    protected var mList: RecyclerView? = null
//    protected var mLinearLayoutManager: RecyclerView.LayoutManager? = null
//    protected var mAdapter = onResolveAdapter()
//    protected var mMessageViewModel: MessageViewModel? = null
//    protected var mRongExtensionViewModel: RongExtensionViewModel? = null
//    //var rongExtension: RongExtension? = null
//    protected var mNewMessageNum: TextView? = null
//    protected var mUnreadHistoryMessageNum: TextView? = null
//    protected var mUnreadMentionMessageNum: TextView? = null
//    protected var activitySoftInputMode = 0
//    private var mNotificationContainer: LinearLayout? = null
//    protected var onScrollStopRefreshList = false
//    private var bindToConversation = false
//
//    var mListObserver = Observer<List<UiMessage>> { uiMessages -> refreshList(uiMessages) }
//
//    var mNewMessageUnreadObserver: Observer<Int> = Observer { count ->
//            if (RongConfigCenter.conversationConfig()
//                    .isShowNewMessageBar(mMessageViewModel?.curConversationType)
//            ) {
//                if (count != null && count > 0) {
//                    mNewMessageNum?.visibility = View.VISIBLE
//                    mNewMessageNum?.text = if (count > 99) "99+" else count.toString()
//                } else {
//                    mNewMessageNum?.visibility = View.INVISIBLE
//                }
//            }
//        }
//
//    var mHistoryMessageUnreadObserver: Observer<Int> = Observer { count ->
//            if (RongConfigCenter.conversationConfig()
//                    .isShowHistoryMessageBar(mMessageViewModel?.curConversationType)
//            ) {
//                if (count != null && count > 0) {
//                    mUnreadHistoryMessageNum?.visibility = View.VISIBLE
//                    mUnreadHistoryMessageNum?.text = MessageFormat.format(
//                        this@MyConversationFragment.getString(string.rc_unread_message),
//                        if (count > 99) "99+" else count
//                    )
//                } else {
//                    mUnreadHistoryMessageNum?.visibility = View.GONE
//                }
//            }
//        }
//
//    var mNewMentionMessageUnreadObserver: Observer<Int> = Observer { count ->
//            if (RongConfigCenter.conversationConfig()
//                    .isShowNewMentionMessageBar(mMessageViewModel?.curConversationType)
//            ) {
//                if (count != null && count > 0) {
//                    mUnreadMentionMessageNum?.visibility = View.VISIBLE
//                    mUnreadMentionMessageNum?.text =
//                        this@MyConversationFragment.getString(
//                            string.rc_mention_messages, arrayOf<Any>(
//                                "($count)"
//                            )
//                        )
//                } else {
//                    mUnreadMentionMessageNum?.visibility = View.GONE
//                }
//            }
//        }
//
//    var mPageObserver: Observer<PageEvent> =
//        Observer { event ->
//            val var2: Iterator<*> = RongConfigCenter.conversationConfig().viewProcessors.iterator()
//            while (var2.hasNext()) {
//                val processor = var2.next() as IConversationUIRenderer
//                if (processor.handlePageEvent(event)) {
//                    return@Observer
//                }
//            }
//            if (event is Event.RefreshEvent) {
//                if (event.state == RefreshState.RefreshFinish) {
//                    mRefreshLayout?.finishRefresh()
//                } else if (event.state == RefreshState.LoadFinish) {
//                    mRefreshLayout?.finishLoadMore()
//                }
//            } else if (event is ToastEvent) {
//                val msg = event.message
//                if (!TextUtils.isEmpty(msg)) {
//                    Toast.makeText(this@MyConversationFragment.context, msg, Toast.LENGTH_SHORT).show()
//                }
//            } else if (event is ScrollToEndEvent) {
//                mList?.scrollToPosition(mAdapter.itemCount - 1)
//            } else if (event is ScrollMentionEvent) {
//                mMessageViewModel?.onScrolled(
//                    mList,
//                    0,
//                    0,
//                    mAdapter.headersCount,
//                    mAdapter.footersCount
//                )
//            } else if (event is ScrollEvent) {
//                if (mList?.layoutManager is LinearLayoutManager) {
//                    (mList?.layoutManager as LinearLayoutManager?)?.scrollToPositionWithOffset(
//                        mAdapter.headersCount + event.position, 0
//                    )
//                }
//            } else if (event is SmoothScrollEvent) {
//                if (mList?.layoutManager is LinearLayoutManager) {
//                    (mList?.layoutManager as LinearLayoutManager?)?.scrollToPositionWithOffset(
//                        mAdapter.headersCount + event.position, 0
//                    )
//                }
//            } else if (event is ShowLongClickDialogEvent) {
//                val bean = event.bean
//                val messageItemLongClickActions = bean.messageItemLongClickActions
//                Collections.sort(messageItemLongClickActions, object : Comparator<MessageItemLongClickAction> {
//                        override fun compare(
//                            lhs: MessageItemLongClickAction,
//                            rhs: MessageItemLongClickAction
//                        ): Int {
//                            return rhs.priority - lhs.priority
//                        }
//                })
//                val titles: MutableList<String> = ArrayList<String>()
//                val var5: Iterator<*> = messageItemLongClickActions.iterator()
//                while (var5.hasNext()) {
//                    val action = var5.next() as MessageItemLongClickAction
//                    titles.add(action.getTitle(this@MyConversationFragment.context))
//                }
//                val dialog = OptionsPopupDialog.newInstance(
//                    this@MyConversationFragment.context,
//                    titles.toTypedArray()
//                ).setOptionsPopupDialogListener { which ->
//                    (messageItemLongClickActions[which] as MessageItemLongClickAction).listener.onMessageItemLongClick(
//                        this@MyConversationFragment.context,
//                        bean.uiMessage
//                    )
//                }
//                MessageItemLongClickActionManager.getInstance().longClickDialog = dialog
//                MessageItemLongClickActionManager.getInstance().longClickMessage =
//                    bean.uiMessage.message
//                dialog.setOnDismissListener {
//                    MessageItemLongClickActionManager.getInstance().longClickDialog =
//                        null as OptionsPopupDialog?
//                    MessageItemLongClickActionManager.getInstance().longClickMessage =
//                        null as Message?
//                }
//                dialog.show()
//            } else if (event is PageDestroyEvent) {
//                val fm = this@MyConversationFragment.childFragmentManager
//                if (fm.backStackEntryCount > 0) {
//                    fm.popBackStack()
//                } else if (this@MyConversationFragment.activity != null) {
//                    this@MyConversationFragment.activity?.finish()
//                }
//            } else if (event is ShowWarningDialogEvent) {
//                onWarningDialog(event.message)
//            } else if (event is ShowLoadMessageDialogEvent) {
//                showLoadMessageDialog(event.callback, event.list)
//            }
//        }
////    var notificationContainer: LinearLayout? = null
////        private set
//    private var onViewCreated = false
//    private var mDisableSystemEmoji = false
//    private var mBundle: Bundle? = null
//    private var conversationIdentifier: ConversationIdentifier? = null
//    private val mScrollListener: RecyclerView.OnScrollListener =
//        object : RecyclerView.OnScrollListener() {
//            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                super.onScrolled(recyclerView, dx, dy)
//                mMessageViewModel?.onScrolled(
//                    recyclerView,
//                    dx,
//                    dy,
//                    mAdapter.headersCount,
//                    mAdapter.footersCount
//                )
//            }
//
//            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//                if (newState == 0 && onScrollStopRefreshList) {
//                    onScrollStopRefreshList = false
//                    RLog.d(TAG, "onScrollStateChanged refresh List")
//                    refreshList(mMessageViewModel?.uiMessageLiveData?.value as List<UiMessage>)
//                }
//            }
//        }
//
//    override fun initConversation(
//        targetId: String?,
//        conversationType: Conversation.ConversationType?,
//        bundle: Bundle?
//    ) {
//        if (onViewCreated) {
//            bindConversation(
//                ConversationIdentifier.obtain(conversationType, targetId, ""),
//                false,
//                bundle
//            )
//        } else {
//            conversationIdentifier = ConversationIdentifier.obtain(conversationType, targetId, "")
//            mBundle = bundle
//        }
//    }
//
//    private fun bindConversation(
//        conversationIdentifier: ConversationIdentifier?,
//        disableSystemEmoji: Boolean,
//        bundle: Bundle?
//    ) {
//        if (conversationIdentifier?.type != null && !TextUtils.isEmpty(
//                conversationIdentifier.targetId
//            )
//        ) {
//            val var4: Iterator<*> = RongConfigCenter.conversationConfig().viewProcessors.iterator()
//            while (var4.hasNext()) {
//                val processor = var4.next() as IConversationUIRenderer
//                processor.init(
//                    this,
//                    rongExtension, conversationIdentifier.type, conversationIdentifier.targetId
//                )
//            }
//            rongExtension?.bindToConversation(this, conversationIdentifier, disableSystemEmoji)
//            mMessageViewModel?.bindConversation(conversationIdentifier, bundle)
//            subscribeUi()
//            bindToConversation = true
//        } else {
//            RLog.e(
//                TAG,
//                "Invalid intent data !!! Must put targetId and conversation type to intent."
//            )
//        }
//    }
//
//    private fun subscribeUi() {
//        mMessageViewModel?.pageEventLiveData?.observeForever(mPageObserver)
//        mMessageViewModel?.uiMessageLiveData?.observeForever(mListObserver)
//        mMessageViewModel?.newMessageUnreadLiveData?.observe(
//            this.viewLifecycleOwner,
//            mNewMessageUnreadObserver
//        )
//        mMessageViewModel?.historyMessageUnreadLiveData?.observe(
//            this.viewLifecycleOwner,
//            mHistoryMessageUnreadObserver
//        )
//        mMessageViewModel?.newMentionMessageUnreadLiveData?.observe(
//            this.viewLifecycleOwner,
//            mNewMentionMessageUnreadObserver
//        )
//        mRongExtensionViewModel?.extensionBoardState?.observe(
//            this.viewLifecycleOwner
//        ) { value ->
//            RLog.d(TAG, "scroll to the bottom")
//            mList?.postDelayed({
//                if (mRongExtensionViewModel?.inputModeLiveData?.value != InputMode.MoreInputMode && java.lang.Boolean.TRUE == value) {
//                    if (mMessageViewModel?.isNormalState == true) {
//                        mList?.scrollToPosition(mAdapter.itemCount - 1)
//                    } else if (!mMessageViewModel?.isHistoryState!!) {
//                        mMessageViewModel?.newMessageBarClick()
//                    }
//                }
//            }, 150L)
//        }
//    }
//
//    override fun onViewClick(clickType: Int, data: UiMessage?) {
//        if (!MessageProviderPermissionHandler.getInstance()
//                .handleMessageClickPermission(data, this)
//        ) {
//            mMessageViewModel?.onViewClick(clickType, data)
//        }
//    }
//
//    override fun onViewLongClick(clickType: Int, data: UiMessage?): Boolean {
//        return mMessageViewModel?.onViewLongClick(clickType, data)?:false
//    }
//
//    override fun hideNotificationView(notificationView: View?) {
//        if (notificationView != null) {
//            val view = notificationContainer?.findViewById<View>(notificationView.id)
//            if (view != null) {
//                notificationContainer?.removeView(view)
//                if (notificationContainer?.childCount == 0) {
//                    notificationContainer?.visibility = View.GONE
//                }
//            }
//        }
//    }
//
//    override fun showNotificationView(notificationView: View?) {
//        if (notificationView != null) {
//            notificationContainer?.removeAllViews()
//            RongViewUtils.addView(notificationContainer, notificationView)
//            notificationContainer?.visibility = View.VISIBLE
//        }
//    }
//
//    private fun refreshList(data: List<UiMessage>) {
//        if (!mList?.isComputingLayout!! && mList?.scrollState == 0) {
//            mAdapter.setDataCollection(data)
//        } else {
//            onScrollStopRefreshList = true
//        }
//    }
//
//    override fun onBackPressed(): Boolean {
//        var result = false
//        val var2: Iterator<*> = RongConfigCenter.conversationConfig().viewProcessors.iterator()
//        while (var2.hasNext()) {
//            val processor = var2.next() as IConversationUIRenderer
//            val temp = processor.onBackPressed()
//            if (temp) {
//                result = true
//            }
//        }
//        if (mMessageViewModel != null) {
//            val temp = mMessageViewModel?.onBackPressed()
//            if (temp == true) {
//                result = true
//            }
//        }
//        if (mRongExtensionViewModel != null) {
//            mRongExtensionViewModel?.exitMoreInputMode(this.context)
//            mRongExtensionViewModel?.collapseExtensionBoard()
//        }
//        return result
//    }
//
//    override fun onRefresh(refreshLayout: RefreshLayout) {
//        if (mMessageViewModel != null && bindToConversation) {
//            mMessageViewModel?.onRefresh()
//        }
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (resultCode == -1) {
//            ReferenceManager.getInstance().hideReferenceView()
//        }
//        if (requestCode == 104) {
//            if (mMessageViewModel != null) {
//                mMessageViewModel?.forwardMessage(data)
//            }
//        } else {
//            rongExtension?.onActivityPluginResult(requestCode, resultCode, data)
//        }
//    }
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<String>,
//        grantResults: IntArray
//    ) {
//        if (PermissionCheckUtil.checkPermissionResultIncompatible(permissions, grantResults)) {
//            if (this.context != null) {
//                ToastUtils.s(this.context, this.getString(string.rc_permission_request_failed))
//            }
//        } else if (requestCode == 1000) {
//            if (grantResults.size > 0 && grantResults[0] == 0) {
//                HQVoiceMsgDownloadManager.getInstance().resumeDownloadService()
//            } else {
//                PermissionCheckUtil.showRequestPermissionFailedAlter(
//                    this.context,
//                    permissions,
//                    grantResults
//                )
//            }
//        } else {
//            if (requestCode == 101) {
//                if (PermissionCheckUtil.checkPermissions(this.activity, permissions)) {
//                    var locationUiRender: LocationUiRender? = null
//                    val var5: Iterator<*> =
//                        RongConfigCenter.conversationConfig().viewProcessors.iterator()
//                    while (var5.hasNext()) {
//                        val processor = var5.next() as IConversationUIRenderer
//                        if (processor is LocationUiRender) {
//                            locationUiRender = processor
//                            break
//                        }
//                    }
//                    locationUiRender?.joinLocation()
//                } else if (this.activity != null) {
//                    PermissionCheckUtil.showRequestPermissionFailedAlter(
//                        this.activity,
//                        permissions,
//                        grantResults
//                    )
//                }
//            } else if (requestCode == 3000) {
//                MessageProviderPermissionHandler.getInstance()
//                    .onRequestPermissionsResult(this.activity, permissions, grantResults)
//            }
//            if (requestCode == 100 && grantResults.size > 0 && grantResults[0] != 0) {
//                PermissionCheckUtil.showRequestPermissionFailedAlter(
//                    this.context,
//                    permissions,
//                    grantResults
//                )
//            } else {
//                rongExtension?.onRequestPermissionResult(requestCode, permissions, grantResults)
//            }
//        }
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        val rootView = inflater.inflate(R.layout.my_rc_conversation_fragment, container, false)
//        mList = rootView.findViewById<View>(R.id.rc_message_list) as RecyclerView
//        mRongExtension = rootView.findViewById<View>(R.id.rc_extension) as RongExtension
//        mRefreshLayout = rootView.findViewById<View>(R.id.rc_refresh) as SmartRefreshLayout
//        mNewMessageNum = rootView.findViewById<View>(R.id.rc_new_message_number) as TextView
//        mUnreadHistoryMessageNum = rootView.findViewById<View>(R.id.rc_unread_message_count) as TextView
//        mUnreadMentionMessageNum = rootView.findViewById<View>(R.id.rc_mention_message_count) as TextView
//        mNotificationContainer = rootView.findViewById<View>(R.id.rc_notification_container) as LinearLayout
//        mNewMessageNum?.setOnClickListener(this)
//        mUnreadHistoryMessageNum?.setOnClickListener(this)
//        mUnreadMentionMessageNum?.setOnClickListener(this)
//        mLinearLayoutManager = createLayoutManager()
//        if (mList != null) {
//            mList?.layoutManager = mLinearLayoutManager
//        }
//        mRefreshLayout?.setOnTouchListener { v, event ->
//            closeExpand()
//            false
//        }
//        mAdapter.setItemClickListener(object : BaseAdapter.OnItemClickListener {
//            override fun onItemClick(view: View, holder: ViewHolder, position: Int) {
//                closeExpand()
//            }
//
//            override fun onItemLongClick(view: View, holder: ViewHolder, position: Int): Boolean {
//                return false
//            }
//        })
//        if (mList != null) {
//            mList?.adapter = mAdapter
//            mList?.addOnScrollListener(mScrollListener)
//            mList?.itemAnimator = null as ItemAnimator?
//            val gd = GestureDetector(this.context, object : SimpleOnGestureListener() {
//                override fun onScroll(
//                    e1: MotionEvent,
//                    e2: MotionEvent,
//                    distanceX: Float,
//                    distanceY: Float
//                ): Boolean {
//                    closeExpand()
//                    return super.onScroll(e1, e2, distanceX, distanceY)
//                }
//            })
//            mList?.addOnItemTouchListener(object : OnItemTouchListener {
//                override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
//                    return gd.onTouchEvent(e)
//                }
//
//                override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
//                override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
//            })
//        }
//        mRefreshLayout?.isNestedScrollingEnabled = false
//        mRefreshLayout?.setRefreshHeader(RongRefreshHeader(this.context))
//        mRefreshLayout?.setRefreshFooter(RongRefreshHeader(this.context))
//        mRefreshLayout?.setEnableRefresh(true)
//        mRefreshLayout?.setOnRefreshListener(this)
//        mRefreshLayout?.setOnLoadMoreListener(this)
//        return rootView
//    }
//
//    private fun createLayoutManager(): RecyclerView.LayoutManager {
//        val linearLayoutManager: LinearLayoutManager = FixedLinearLayoutManager(this.context)
//        linearLayoutManager.stackFromEnd = true
//        return linearLayoutManager
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        if (this.activity != null && this.activity?.intent != null) {
//            if (!IMCenter.getInstance().isInitialized) {
//                RLog.e(TAG, "Please init SDK first!")
//            } else {
//                super.onViewCreated(view, savedInstanceState)
//                initIntentExtra()
//                if (Conversation.ConversationType.SYSTEM == conversationIdentifier?.type) {
//                    rongExtension?.visibility = View.GONE
//                } else {
//                    rongExtension?.visibility = View.VISIBLE
//                }
//                mMessageViewModel = ViewModelProvider(this).get(
//                    MessageViewModel::class.java
//                )
//                mRongExtensionViewModel = ViewModelProvider(this).get(
//                    RongExtensionViewModel::class.java
//                )
//                bindConversation(conversationIdentifier, mDisableSystemEmoji, mBundle)
//                onViewCreated = true
//            }
//        } else {
//            RLog.e(
//                TAG,
//                "Must put targetId and conversation type to intent when start conversation."
//            )
//        }
//    }
//
//    private fun initIntentExtra() {
//        val intent = this.activity?.intent
//        if (intent?.hasExtra("ConversationIdentifier") == true) {
//            val identifier =
//                intent.getParcelableExtra<Parcelable>("ConversationIdentifier") as ConversationIdentifier?
//            if (identifier != null) {
//                conversationIdentifier = identifier
//            }
//        }
//        if (conversationIdentifier == null) {
//            val typeValue = intent?.getStringExtra("ConversationType")
//            val type = Conversation.ConversationType.valueOf(
//                typeValue?.uppercase(Locale.US) ?:""
//            )
//            val targetId = intent?.getStringExtra("targetId")
//            conversationIdentifier = ConversationIdentifier.obtain(type, targetId, "")
//        }
//        mDisableSystemEmoji = intent?.getBooleanExtra("disableSystemEmoji", false)?:false
//        if (mBundle == null) {
//            mBundle = intent?.extras
//        }
//    }
//
//    override fun onResume() {
//        super.onResume()
//        if (this.view != null) {
//            if (mMessageViewModel != null) {
//                mMessageViewModel?.onResume()
//            }
//            this.view?.setOnKeyListener { v, keyCode, event -> if (event.action == 1 && keyCode == 4) onBackPressed() else false }
//            rongExtension?.onResume()
//        }
//    }
//
//    override fun onPause() {
//        super.onPause()
//        if (mMessageViewModel != null) {
//            mMessageViewModel?.onPause()
//        }
//        if (rongExtension != null) {
//            rongExtension?.onPause()
//        }
//    }
//
//    override fun onStart() {
//        super.onStart()
//        val activity = this.activity
//        if (activity != null) {
//            activitySoftInputMode = activity.window.attributes.softInputMode
//            if (rongExtension != null && rongExtension?.useKeyboardHeightProvider() == true) {
//                resetSoftInputMode(48)
//            } else {
//                resetSoftInputMode(16)
//            }
//        }
//    }
//
//    override fun onStop() {
//        super.onStop()
//        if (mMessageViewModel != null) {
//            mMessageViewModel?.onStop()
//        }
//        resetSoftInputMode(activitySoftInputMode)
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        val var1: Iterator<*> = RongConfigCenter.conversationConfig().viewProcessors.iterator()
//        while (var1.hasNext()) {
//            val processor = var1.next() as IConversationUIRenderer
//            processor.onDestroy()
//        }
//        mList?.removeOnScrollListener(mScrollListener)
//        if (mMessageViewModel != null) {
//            mMessageViewModel?.pageEventLiveData?.removeObserver(mPageObserver)
//            mMessageViewModel?.uiMessageLiveData?.removeObserver(mListObserver)
//            mMessageViewModel?.newMentionMessageUnreadLiveData?.removeObserver(
//                mNewMentionMessageUnreadObserver
//            )
//            mMessageViewModel?.onDestroy()
//        }
//        if (rongExtension != null) {
//            rongExtension?.onDestroy()
//            mRongExtension = null
//        }
//        bindToConversation = false
//    }
//
//    private fun resetSoftInputMode(mode: Int) {
//        val activity = this.activity
//        activity?.window?.setSoftInputMode(mode)
//    }
//
//    override fun onClick(v: View) {
//        val id = v.id
//        if (id == R.id.rc_new_message_number) {
//            if (mMessageViewModel != null) {
//                mMessageViewModel?.newMessageBarClick()
//            }
//        } else if (id == R.id.rc_unread_message_count) {
//            if (mMessageViewModel != null) {
//                mMessageViewModel?.unreadBarClick()
//            }
//        } else if (id == R.id.rc_mention_message_count && mMessageViewModel != null) {
//            mMessageViewModel?.newMentionMessageBarClick()
//        }
//    }
//
//    override fun onLoadMore(refreshLayout: RefreshLayout) {
//        if (mMessageViewModel != null && bindToConversation) {
//            mMessageViewModel?.onLoadMore()
//        }
//    }
//
//    override fun onWarningDialog(msg: String?) {
//        val builder = AlertDialog.Builder(this.activity)
//        builder.setCancelable(false)
//        val alertDialog = builder.create()
//        alertDialog.show()
//        val window = alertDialog.window
//        if (window != null) {
//            window.setContentView(R.layout.my_rc_cs_alert_warning)
//            val tv = window.findViewById<View>(R.id.rc_cs_msg) as TextView
//            tv.text = msg
//            window.findViewById<View>(R.id.rc_btn_ok).setOnClickListener {
//                alertDialog.dismiss()
//                if (this@MyConversationFragment.isAdded) {
//                    val fm = this@MyConversationFragment.childFragmentManager
//                    if (fm.backStackEntryCount > 0) {
//                        fm.popBackStack()
//                    } else if (this@MyConversationFragment.activity != null) {
//                        this@MyConversationFragment.activity?.finish()
//                    }
//                }
//            }
//        }
//    }
//
//    private fun showLoadMessageDialog(callback: GetMessageCallback?, list: List<Message>) {
//        AlertDialog.Builder(this.activity, 5)
//            .setMessage(this.getString(string.rc_load_local_message)).setPositiveButton(
//                this.getString(string.rc_dialog_ok)
//            ) { dialog, which -> callback?.onSuccess(list, true) }
//            .setNegativeButton(
//                this.getString(string.rc_cancel)
//            ) { dialog, which -> callback?.onErrorAsk(list) }.show()
//    }
//
//    private fun closeExpand() {
//        if (mRongExtensionViewModel != null) {
//            mRongExtensionViewModel?.collapseExtensionBoard()
//        }
//    }
//
//    protected override fun onResolveAdapter(): MessageListAdapter {
//        return MessageListAdapter(this)
//    }
//
//    override fun addHeaderView(view: View?) {
//        mAdapter.addHeaderView(view)
//    }
//
//    override fun addFooterView(view: View?) {
//        mAdapter.addFootView(view)
//    }
//
//    override fun setEmptyView(view: View?) {
//        mAdapter.setEmptyView(view)
//    }
//
//    override fun setEmptyView(@LayoutRes emptyId: Int) {
//        mAdapter.setEmptyView(emptyId)
//    }
//
//    companion object {
//        const val REQUEST_CODE_FORWARD = 104
//        private const val REQUEST_MSG_DOWNLOAD_PERMISSION = 1000
//    }
//}
