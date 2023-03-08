package com.example.rongyuntest.widgets;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.rongyuntest.viewmodel.MyRongExtensionViewModel;

import io.rong.common.RLog;
import io.rong.imkit.R.dimen;
import io.rong.imkit.R.id;
import io.rong.imkit.R.layout;
import io.rong.imkit.R.styleable;
import io.rong.imkit.conversation.extension.IExtensionModule;
import io.rong.imkit.conversation.extension.InputMode;
import io.rong.imkit.conversation.extension.RongExtension;
import io.rong.imkit.conversation.extension.RongExtensionCacheHelper;
import io.rong.imkit.conversation.extension.RongExtensionManager;
import io.rong.imkit.conversation.extension.component.emoticon.EmoticonBoard;
import io.rong.imkit.conversation.extension.component.moreaction.MoreInputPanel;
import io.rong.imkit.conversation.extension.component.plugin.IPluginModule;
import io.rong.imkit.conversation.extension.component.plugin.IPluginRequestPermissionResultCallback;
import io.rong.imkit.conversation.extension.component.plugin.PluginBoard;
import io.rong.imkit.conversation.messgelist.viewmodel.MessageViewModel;
import io.rong.imkit.event.uievent.InputBarEvent;
import io.rong.imkit.event.uievent.PageEvent;
import io.rong.imkit.feature.destruct.DestructManager;
import io.rong.imkit.feature.mention.IExtensionEventWatcher;
import io.rong.imkit.feature.mention.RongMentionManager;
import io.rong.imkit.utils.PermissionCheckUtil;
import io.rong.imkit.utils.RongUtils;
import io.rong.imkit.utils.RongViewUtils;
import io.rong.imkit.utils.keyboard.KeyboardHeightObserver;
import io.rong.imkit.utils.keyboard.KeyboardHeightProvider;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.ConversationIdentifier;
import io.rong.imlib.model.Conversation.ConversationType;
import java.util.Iterator;

public class MyRongExtension extends RongExtension {
    private String TAG = MyRongExtension.class.getSimpleName();
    
    private Fragment mFragment;
    private ConversationIdentifier mConversationIdentifier;
    private ViewGroup mRoot;
    private MyRongExtensionViewModel mExtensionViewModel;
    private MessageViewModel mMessageViewModel;
    private RelativeLayout mAttachedInfoContainer;
    private RelativeLayout mBoardContainer;
    private RelativeLayout mInputPanelContainer;
    private MyInputPanel mInputPanel;
    private EmoticonBoard mEmoticonBoard;
    private PluginBoard mPluginBoard;
    private MyInputPanel.InputStyle mInputStyle;
    private MoreInputPanel mMoreInputPanel;
    private InputMode mPreInputMode;
    private KeyboardHeightProvider keyboardHeightProvider = null;
    private boolean editTextIsFocused = false;
    private final KeyboardHeightObserver mKeyboardHeightObserver;

    public MyRongExtension(Context context) {
        super(context);
        this.mKeyboardHeightObserver = new MyRongExtension.NamelessClass_1();
        this.initView(context);
    }

    public MyRongExtension(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.mKeyboardHeightObserver = new NamelessClass_1();
        TypedArray a = context.obtainStyledAttributes(attrs, styleable.RongExtension);
        int attr = a.getInt(styleable.RongExtension_RCStyle, 291);
        a.recycle();
        this.mInputStyle = MyInputPanel.InputStyle.getStyle(attr);
        this.initView(context);
    }

    private void initView(Context context) {
        this.mRoot = (LinearLayout) LayoutInflater.from(context).inflate(layout.rc_extension_board, this, true);
        this.mAttachedInfoContainer = (RelativeLayout)this.mRoot.findViewById(id.rc_ext_attached_info_container);
        this.mInputPanelContainer = (RelativeLayout)this.mRoot.findViewById(id.rc_ext_input_container);
        this.mBoardContainer = (RelativeLayout)this.mRoot.findViewById(id.rc_ext_board_container);
    }

    class NamelessClass_1 implements KeyboardHeightObserver {
        NamelessClass_1() {
        }

        public void onKeyboardHeightChanged(int orientation, boolean isOpen, int keyboardHeight) {
            if (MyRongExtension.this.getActivityFromView() != null) {
                if (isOpen) {
                    int saveKeyBoardHeight = RongUtils.getSaveKeyBoardHeight(MyRongExtension.this.getContext(), orientation);
                    if (saveKeyBoardHeight != keyboardHeight) {
                        RongUtils.saveKeyboardHeight(MyRongExtension.this.getContext(), orientation, keyboardHeight);
                        MyRongExtension.this.updateBoardContainerHeight();
                    }

                    MyRongExtension.this.mBoardContainer.setVisibility(View.VISIBLE);
                    MyRongExtension.this.mExtensionViewModel.getExtensionBoardState().setValue(true);
                } else if (MyRongExtension.this.mExtensionViewModel != null) {
                    if (MyRongExtension.this.mExtensionViewModel.isSoftInputShow()) {
                        MyRongExtension.this.mExtensionViewModel.setSoftInputKeyBoard(false, false);
                    }

                    if (MyRongExtension.this.mPreInputMode != null && (MyRongExtension.this.mPreInputMode == InputMode.TextInput || MyRongExtension.this.mPreInputMode == InputMode.VoiceInput)) {
                        MyRongExtension.this.mBoardContainer.setVisibility(View.GONE);
                        MyRongExtension.this.mExtensionViewModel.getExtensionBoardState().setValue(false);
                    }
                }
            }

        }
    }
    
    public void bindToConversation(Fragment fragment, ConversationIdentifier conversationIdentifier, boolean disableSystemEmoji) {
        this.mFragment = fragment;
        this.mConversationIdentifier = conversationIdentifier;
        this.mExtensionViewModel = (MyRongExtensionViewModel)(new ViewModelProvider(this.mFragment)).get(MyRongExtensionViewModel.class);
        this.mExtensionViewModel.getAttachedInfoState().observe(this.mFragment, new Observer<Boolean>() {
            public void onChanged(Boolean isVisible) {
                MyRongExtension.this.mAttachedInfoContainer.setVisibility(isVisible ? View.VISIBLE : View.GONE);
            }
        });
        this.mExtensionViewModel.getExtensionBoardState().observe(this.mFragment, new Observer<Boolean>() {
            public void onChanged(Boolean value) {
                if (!value) {
                    MyRongExtension.this.mBoardContainer.setVisibility(View.GONE);
                }

            }
        });
        this.mMessageViewModel = (MessageViewModel)(new ViewModelProvider(this.mFragment)).get(MessageViewModel.class);
        this.mMessageViewModel.getPageEventLiveData().observe(this.mFragment, new Observer<PageEvent>() {
            public void onChanged(PageEvent pageEvent) {
                if (pageEvent instanceof InputBarEvent) {
                    if (((InputBarEvent)pageEvent).mType.equals(InputBarEvent.Type.ReEdit)) {
                        MyRongExtension.this.insertToEditText(((InputBarEvent)pageEvent).mExtra);
                    } else if (((InputBarEvent)pageEvent).mType.equals(InputBarEvent.Type.ShowMoreMenu)) {
                        MyRongExtension.this.mExtensionViewModel.getInputModeLiveData().postValue(InputMode.MoreInputMode);
                    } else if (((InputBarEvent)pageEvent).mType.equals(InputBarEvent.Type.HideMoreMenu)) {
                        if (DestructManager.isActive()) {
                            DestructManager.getInstance().activeDestructMode(MyRongExtension.this.getContext());
                            MyRongExtension.this.mAttachedInfoContainer.removeAllViews();
                            MyRongExtension.this.mAttachedInfoContainer.setVisibility(View.GONE);
                        } else {
                            MyRongExtension.this.resetToDefaultView(((InputBarEvent)pageEvent).mExtra);
                        }
                    } else if (((InputBarEvent)pageEvent).mType.equals(InputBarEvent.Type.ActiveMoreMenu) && MyRongExtension.this.mMoreInputPanel != null) {
                        MyRongExtension.this.mMoreInputPanel.refreshView(true);
                    } else if (((InputBarEvent)pageEvent).mType.equals(InputBarEvent.Type.InactiveMoreMenu) && MyRongExtension.this.mMoreInputPanel != null) {
                        MyRongExtension.this.mMoreInputPanel.refreshView(false);
                    }
                }

            }
        });
        this.mEmoticonBoard = new EmoticonBoard(fragment, this.mBoardContainer, this.mConversationIdentifier.getType(), this.mConversationIdentifier.getTargetId(), disableSystemEmoji);
        this.mPluginBoard = new PluginBoard(fragment, this.mBoardContainer, this.mConversationIdentifier.getType(), this.mConversationIdentifier.getTargetId());
        this.mInputPanel = new MyInputPanel(fragment, this.mInputPanelContainer, this.mInputStyle, this.mConversationIdentifier);
        if (this.mInputPanelContainer.getChildCount() <= 0) {
            RongViewUtils.addView(this.mInputPanelContainer, this.mInputPanel.getRootView());
        }

        this.mExtensionViewModel.setAttachedConversation(conversationIdentifier, this.mInputPanel.getEditText());
        this.mExtensionViewModel.getInputModeLiveData().observe(this.mFragment, new Observer<InputMode>() {
            public void onChanged(InputMode inputMode) {
                MyRongExtension.this.mPreInputMode = inputMode;
                MyRongExtension.this.updateInputMode(inputMode);
            }
        });
        Iterator var4 = RongExtensionManager.getInstance().getExtensionModules().iterator();

        while(var4.hasNext()) {
            IExtensionModule module = (IExtensionModule)var4.next();
            module.onAttachedToExtension(fragment, this);
        }

    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    public void onResume() {
        if (this.mExtensionViewModel != null) {
            if (this.useKeyboardHeightProvider()) {
                this.keyboardHeightProvider = new KeyboardHeightProvider(this.getActivityFromView());
                this.keyboardHeightProvider.setKeyboardHeightObserver(this.mKeyboardHeightObserver);
            }

            this.post(new Runnable() {
                public void run() {
                    KeyboardHeightProvider keyboardHeightProvider = MyRongExtension.this.keyboardHeightProvider;
                    if (keyboardHeightProvider != null) {
                        MyRongExtension.this.keyboardHeightProvider.start();
                    }

                }
            });
            final EditText editText = this.mExtensionViewModel.getEditTextWidget();
            if (editText != null) {
                if (this.editTextIsFocused) {
                    this.postDelayed(new Runnable() {
                        public void run() {
                            editText.setSelection(editText.getText().toString().length());
                            editText.requestFocus();
                            MyRongExtension.this.mExtensionViewModel.forceSetSoftInputKeyBoard(true);
                        }
                    }, 200L);
                }

                if (editText.getText().length() > 0 || editText.isFocused()) {
                    editText.setOnKeyListener(new OnKeyListener() {
                        public boolean onKey(View v, int keyCode, KeyEvent event) {
                            if (keyCode == 67 && event.getAction() == 0) {
                                int cursorPos = editText.getSelectionStart();
                                RongMentionManager.getInstance().onDeleteClick(MyRongExtension.this.mConversationIdentifier.getType(), MyRongExtension.this.mConversationIdentifier.getTargetId(), editText, cursorPos);
                            }

                            return false;
                        }
                    });
                }
            }

        }
    }

    public void onPause() {
        if (this.keyboardHeightProvider != null) {
            this.keyboardHeightProvider.stop();
            this.keyboardHeightProvider.setKeyboardHeightObserver((KeyboardHeightObserver)null);
            this.keyboardHeightProvider = null;
        }

        if (this.mExtensionViewModel != null) {
            if (this.mExtensionViewModel.getEditTextWidget() != null) {
                this.editTextIsFocused = this.mExtensionViewModel.getEditTextWidget().isFocused();
            }

            if (this.mPreInputMode != null && this.mPreInputMode == InputMode.TextInput && this.mBoardContainer != null) {
                this.mExtensionViewModel.collapseExtensionBoard();
            }
        }

    }

    public void setAttachedInfo(View view) {
        this.mAttachedInfoContainer.removeAllViews();
        if (view != null) {
            this.mAttachedInfoContainer.addView(view);
        }

        this.mAttachedInfoContainer.setVisibility(View.VISIBLE);
    }

    public RelativeLayout getContainer(MyRongExtension.ContainerType type) {
        if (type == null) {
            return null;
        } else if (type.equals(MyRongExtension.ContainerType.ATTACH)) {
            return this.mAttachedInfoContainer;
        } else {
            return type.equals(MyRongExtension.ContainerType.INPUT) ? this.mInputPanelContainer : this.mBoardContainer;
        }
    }

    public MyInputPanel getInputPanel() {
        return this.mInputPanel;
    }

    public PluginBoard getPluginBoard() {
        return this.mPluginBoard;
    }

    public EmoticonBoard getEmoticonBoard() {
        return this.mEmoticonBoard;
    }

    public void resetToDefaultView() {
        this.resetToDefaultView((String)null);
    }

    public void resetToDefaultView(String conversationType) {
        if (!TextUtils.equals(conversationType, Conversation.ConversationType.PUBLIC_SERVICE.getName()) && !TextUtils.equals(conversationType, Conversation.ConversationType.APP_PUBLIC_SERVICE.getName())) {
            this.mInputPanelContainer.removeAllViews();
            if (this.mInputPanel == null) {
                this.mInputPanel = new MyInputPanel(this.mFragment, this.mInputPanelContainer, this.mInputStyle, this.mConversationIdentifier);
            }

            this.mExtensionViewModel.setEditTextWidget(this.mInputPanel.getEditText());
            RongViewUtils.addView(this.mInputPanelContainer, this.mInputPanel.getRootView());
            if (this.mFragment.getContext() != null) {
                this.mAttachedInfoContainer.removeAllViews();
                this.mAttachedInfoContainer.setVisibility(View.GONE);
                this.updateInputMode(RongExtensionCacheHelper.isVoiceInputMode(this.mFragment.getContext(), this.mConversationIdentifier.getType(), this.mConversationIdentifier.getTargetId()) ? InputMode.VoiceInput : InputMode.TextInput);
            }

        } else {
            this.mInputPanelContainer.setVisibility(View.VISIBLE);
            Fragment fragment = this.mFragment;
            if (fragment != null && fragment.getContext() != null) {
                this.mAttachedInfoContainer.removeAllViews();
                this.mAttachedInfoContainer.setVisibility(View.GONE);
                this.mExtensionViewModel.getInputModeLiveData().postValue(InputMode.TextInput);
            }

        }
    }

    public void updateInputMode(InputMode inputMode) {
        if (inputMode != null) {
            RLog.d(this.TAG, "update to inputMode:" + inputMode);
            if (inputMode.equals(InputMode.TextInput)) {
                EditText editText = this.mExtensionViewModel.getEditTextWidget();
                if (editText == null || editText.getText() == null) {
                    return;
                }

                if (this.isEditTextSameProperty(editText)) {
                    return;
                }

                RLog.d(this.TAG, "update for TextInput mode");
                this.mInputPanelContainer.setVisibility(View.VISIBLE);
                this.updateBoardContainerHeight();
                this.mBoardContainer.removeAllViews();
                RongViewUtils.addView(this.mBoardContainer, this.mPluginBoard.getView());
                if (!this.useKeyboardHeightProvider()) {
                    this.mExtensionViewModel.getExtensionBoardState().setValue(false);
                } else {
                    this.mExtensionViewModel.getExtensionBoardState().setValue(true);
                }

                if (!editText.isFocused() && editText.getText().length() <= 0) {
                    this.mExtensionViewModel.setSoftInputKeyBoard(false);
                    this.mExtensionViewModel.getExtensionBoardState().setValue(false);
                } else {
                    this.postDelayed(new Runnable() {
                        public void run() {
                            if (MyRongExtension.this.mFragment != null && MyRongExtension.this.mFragment.getActivity() != null && !MyRongExtension.this.mFragment.getActivity().isFinishing()) {
                                MyRongExtension.this.mExtensionViewModel.setSoftInputKeyBoard(true);
                            }

                        }
                    }, 100L);
                }
            } else if (inputMode.equals(InputMode.VoiceInput)) {
                this.mInputPanelContainer.setVisibility(View.VISIBLE);
                this.mBoardContainer.setVisibility(View.GONE);
                this.mExtensionViewModel.forceSetSoftInputKeyBoard(false);
                this.mExtensionViewModel.getExtensionBoardState().setValue(false);
            } else if (inputMode.equals(InputMode.EmoticonMode)) {
                this.mExtensionViewModel.setSoftInputKeyBoard(false);
                this.postDelayed(new Runnable() {
                    public void run() {
                        MyRongExtension.this.updateBoardContainerHeight();
                        MyRongExtension.this.mBoardContainer.removeAllViews();
                        RongViewUtils.addView(MyRongExtension.this.mBoardContainer, MyRongExtension.this.mEmoticonBoard.getView());
                        MyRongExtension.this.mBoardContainer.setVisibility(View.VISIBLE);
                        MyRongExtension.this.mExtensionViewModel.getExtensionBoardState().setValue(true);
                    }
                }, 100L);
            } else if (inputMode.equals(InputMode.PluginMode)) {
                this.mExtensionViewModel.setSoftInputKeyBoard(false);
                this.postDelayed(new Runnable() {
                    public void run() {
                        MyRongExtension.this.updateBoardContainerHeight();
                        MyRongExtension.this.mBoardContainer.removeAllViews();
                        RongViewUtils.addView(MyRongExtension.this.mBoardContainer, MyRongExtension.this.mPluginBoard.getView());
                        MyRongExtension.this.mBoardContainer.setVisibility(View.VISIBLE);
                        MyRongExtension.this.mExtensionViewModel.forceSetSoftInputKeyBoard(false);
                        MyRongExtension.this.mExtensionViewModel.getExtensionBoardState().setValue(true);
                    }
                }, 100L);
            } else if (inputMode.equals(InputMode.MoreInputMode)) {
                this.mInputPanelContainer.setVisibility(View.GONE);
                this.mBoardContainer.setVisibility(View.GONE);
                if (this.mMoreInputPanel == null) {
                    this.mMoreInputPanel = new MoreInputPanel(this.mFragment, this.mAttachedInfoContainer);
                }

                this.mAttachedInfoContainer.removeAllViews();
                RongViewUtils.addView(this.mAttachedInfoContainer, this.mMoreInputPanel.getRootView());
                this.mAttachedInfoContainer.setVisibility(View.VISIBLE);
                this.mExtensionViewModel.setSoftInputKeyBoard(false);
                this.mExtensionViewModel.getExtensionBoardState().setValue(false);
            } else if (inputMode.equals(InputMode.QuickReplyMode)) {
                this.mInputPanelContainer.setVisibility(View.VISIBLE);
                this.mBoardContainer.setVisibility(View.VISIBLE);
                this.mExtensionViewModel.forceSetSoftInputKeyBoard(false);
                this.mExtensionViewModel.getExtensionBoardState().setValue(true);
            } else if (inputMode.equals(InputMode.NormalMode)) {
                this.mInputPanelContainer.setVisibility(View.VISIBLE);
                this.mBoardContainer.setVisibility(View.GONE);
                this.mExtensionViewModel.forceSetSoftInputKeyBoard(false);
                this.mExtensionViewModel.getExtensionBoardState().setValue(false);
            }

        }
    }

    private void updateBoardContainerHeight() {
        if (this.useKeyboardHeightProvider()) {
            int saveKeyboardHeight = RongUtils.getSaveKeyBoardHeight(this.getContext(), this.getContext().getResources().getConfiguration().orientation);
            LayoutParams layoutParams = (LayoutParams) this.mBoardContainer.getLayoutParams();
            if (saveKeyboardHeight <= 0 && layoutParams.height != this.getResources().getDimensionPixelSize(dimen.rc_extension_board_height)) {
                layoutParams.height = this.getResources().getDimensionPixelSize(dimen.rc_extension_board_height);
                this.mBoardContainer.setLayoutParams(layoutParams);
            } else if (layoutParams.height != saveKeyboardHeight) {
                layoutParams.height = saveKeyboardHeight;
                this.mBoardContainer.setLayoutParams(layoutParams);
            }

        }
    }

    public void collapseExtension() {
        RLog.d(this.TAG, "collapseExtension");
        this.mExtensionViewModel.collapseExtensionBoard();
    }

    public void addPluginPager(View v) {
        if (null != this.mPluginBoard) {
            this.mPluginBoard.addPager(v);
        }

    }

    public EditText getInputEditText() {
        return this.mInputPanel.getEditText();
    }

    public Conversation.ConversationType getConversationType() {
        return this.mConversationIdentifier.getType();
    }

    public String getTargetId() {
        return this.mConversationIdentifier.getTargetId();
    }

    public ConversationIdentifier getConversationIdentifier() {
        return this.mConversationIdentifier;
    }

    public void requestPermissionForPluginResult(String[] permissions, int requestCode, IPluginModule pluginModule) {
        if ((requestCode & -256) != 0) {
            throw new IllegalArgumentException("requestCode must less than 256");
        } else if (null != this.mPluginBoard) {
            int position = this.mPluginBoard.getPluginPosition(pluginModule);
            int req = (position + 1 << 8) + (requestCode & 255);
            PermissionCheckUtil.requestPermissions(this.mFragment, permissions, req);
        }
    }

    public boolean onRequestPermissionResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        int position = (requestCode >> 8) - 1;
        int reqCode = requestCode & 255;
        if (null == this.mPluginBoard) {
            return false;
        } else {
            IPluginModule pluginModule = this.mPluginBoard.getPluginModule(position);
            return pluginModule instanceof IPluginRequestPermissionResultCallback ? ((IPluginRequestPermissionResultCallback)pluginModule).onRequestPermissionResult(this.mFragment, this, reqCode, permissions, grantResults) : false;
        }
    }

    public void startActivityForPluginResult(Intent intent, int requestCode, IPluginModule pluginModule) {
        if ((requestCode & -256) != 0) {
            throw new IllegalArgumentException("requestCode must less than 256.");
        } else if (null != this.mPluginBoard) {
            int position = this.mPluginBoard.getPluginPosition(pluginModule);
            this.mFragment.startActivityForResult(intent, (position + 1 << 8) + (requestCode & 255));
        }
    }

    public void onActivityPluginResult(int requestCode, int resultCode, Intent data) {
        int position = (requestCode >> 8) - 1;
        int reqCode = requestCode & 255;
        if (null != this.mPluginBoard) {
            IPluginModule pluginModule = this.mPluginBoard.getPluginModule(position);
            if (pluginModule != null) {
                pluginModule.onActivityResult(reqCode, resultCode, data);
            }

        }
    }

    public void onDestroy() {
        if (this.mInputPanel != null) {
            this.mInputPanel.onDestroy();
            RongMentionManager.getInstance().destroyInstance(this.mConversationIdentifier.getType(), this.mConversationIdentifier.getTargetId(), this.getInputEditText());
        }

        Iterator var1 = RongExtensionManager.getInstance().getExtensionEventWatcher().iterator();

        while(var1.hasNext()) {
            IExtensionEventWatcher watcher = (IExtensionEventWatcher)var1.next();
            watcher.onDestroy(this.mConversationIdentifier.getType(), this.mConversationIdentifier.getTargetId());
        }

        var1 = RongExtensionManager.getInstance().getExtensionModules().iterator();

        while(var1.hasNext()) {
            IExtensionModule extensionModule = (IExtensionModule)var1.next();
            extensionModule.onDetachedFromExtension();
        }

    }

    private void insertToEditText(String content) {
        EditText editText = this.mExtensionViewModel.getEditTextWidget();
        int len = content.length();
        int cursorPos = editText.getSelectionStart();
        editText.getEditableText().insert(cursorPos, content);
        editText.setSelection(cursorPos + len);
    }

    private boolean isEditTextSameProperty(EditText editText) {
        if (this.mPreInputMode == null) {
            return false;
        } else {
            return this.mPreInputMode.equals(InputMode.TextInput) && (editText.isFocused() || editText.getText().length() > 0) && this.mExtensionViewModel.isSoftInputShow();
        }
    }

    private Activity getActivityFromView() {
        for(Context context = this.getContext(); context instanceof ContextWrapper; context = ((ContextWrapper)context).getBaseContext()) {
            if (context instanceof Activity) {
                return (Activity)context;
            }
        }

        return null;
    }

    public boolean useKeyboardHeightProvider() {
        if (Build.VERSION.SDK_INT < 24) {
            return false;
        } else {
            Activity activity = this.getActivityFromView();
            return activity != null && !activity.isInMultiWindowMode();
        }
    }

    public static enum ContainerType {
        ATTACH,
        INPUT,
        BOARD;

        private ContainerType() {
        }
    }
}
