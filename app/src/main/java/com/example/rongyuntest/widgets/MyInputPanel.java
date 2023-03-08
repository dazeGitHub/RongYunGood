package com.example.rongyuntest.widgets;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.rongyuntest.R;
import com.example.rongyuntest.viewmodel.MyRongExtensionViewModel;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import io.rong.imkit.IMCenter;
import io.rong.imkit.config.RongConfigCenter;
import io.rong.imkit.conversation.extension.InputMode;
import io.rong.imkit.conversation.extension.RongExtensionCacheHelper;
import io.rong.imkit.conversation.extension.RongExtensionViewModel;
import io.rong.imkit.conversation.extension.component.inputpanel.InputPanel;
import io.rong.imkit.feature.reference.ReferenceManager;
import io.rong.imkit.manager.AudioPlayManager;
import io.rong.imkit.manager.AudioRecordManager;
import io.rong.imkit.utils.PermissionCheckUtil;
import io.rong.imkit.utils.RongUtils;
import io.rong.imkit.widget.RongEditText;
import io.rong.imlib.ChannelClient;
import io.rong.imlib.IMLibExtensionModuleManager;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.IRongCoreEnum.CoreErrorCode;
import io.rong.imlib.RongIMClient.ResultCallback;
import io.rong.imlib.model.ConversationIdentifier;
import io.rong.imlib.model.Conversation.ConversationType;
import io.rong.imlib.model.HardwareResource.ResourceType;

public class MyInputPanel extends InputPanel {
    private final String TAG = this.getClass().getSimpleName();
    
    private Context mContext;
    private ConversationIdentifier mConversationIdentifier;
    private MyInputPanel.InputStyle mInputStyle;
    private Fragment mFragment;
    private View mInputPanel;
    private boolean mIsVoiceInputMode;
    private ImageView mVoiceToggleBtn;
    private EditText mEditText;
    private TextView mVoiceInputBtn;
    private ImageView mEmojiToggleBtn;
    private TextView mSendBtn;
    private ImageView mAddBtn;
    private ViewGroup mAddOrSendBtn;
    private MyRongExtensionViewModel mMyExtensionViewModel;
    private String mInitialDraft = "";
    private float mLastTouchY;
    private boolean mUpDirection;
    
    private OnTouchListener mOnVoiceBtnTouchListener = new OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            float mOffsetLimit = 70.0F * v.getContext().getResources().getDisplayMetrics().density;
            String[] permissions = new String[]{"androR.id.permission.RECORD_AUDIO"};
            if (!PermissionCheckUtil.checkPermissions(v.getContext(), permissions) && event.getAction() == 0) {
                PermissionCheckUtil.requestPermissions(MyInputPanel.this.mFragment, permissions, 100);
                return true;
            } else {
                if (event.getAction() == 0) {
                    if (AudioPlayManager.getInstance().isPlaying()) {
                        AudioPlayManager.getInstance().stopPlay();
                    }

                    if (RongUtils.phoneIsInUse(v.getContext()) || IMLibExtensionModuleManager.getInstance().onRequestHardwareResource(ResourceType.VIDEO) || IMLibExtensionModuleManager.getInstance().onRequestHardwareResource(ResourceType.AUDIO)) {
                        Toast.makeText(v.getContext(), v.getContext().getResources().getString(R.string.rc_voip_occupying), Toast.LENGTH_SHORT).show();
                        return true;
                    }

                    AudioRecordManager.getInstance().startRecord(v.getRootView(), MyInputPanel.this.mConversationIdentifier);
                    MyInputPanel.this.mLastTouchY = event.getY();
                    MyInputPanel.this.mUpDirection = false;
                    ((TextView)v).setText(R.string.rc_voice_release_to_send);
                    ((TextView)v).setBackground(v.getContext().getResources().getDrawable(R.drawable.rc_ext_voice_touched_button));
                } else if (event.getAction() == 2) {
                    if (MyInputPanel.this.mLastTouchY - event.getY() > mOffsetLimit && !MyInputPanel.this.mUpDirection) {
                        AudioRecordManager.getInstance().willCancelRecord();
                        MyInputPanel.this.mUpDirection = true;
                        ((TextView)v).setText(R.string.rc_voice_press_to_input);
                        ((TextView)v).setBackground(v.getContext().getResources().getDrawable(R.drawable.rc_ext_voice_idle_button));
                    } else if (event.getY() - MyInputPanel.this.mLastTouchY > -mOffsetLimit && MyInputPanel.this.mUpDirection) {
                        AudioRecordManager.getInstance().continueRecord();
                        MyInputPanel.this.mUpDirection = false;
                        ((TextView)v).setBackground(v.getContext().getResources().getDrawable(R.drawable.rc_ext_voice_touched_button));
                        ((TextView)v).setText(R.string.rc_voice_release_to_send);
                    }
                } else if (event.getAction() == 1 || event.getAction() == 3) {
                    AudioRecordManager.getInstance().stopRecord();
                    ((TextView)v).setText(R.string.rc_voice_press_to_input);
                    ((TextView)v).setBackground(v.getContext().getResources().getDrawable(R.drawable.rc_ext_voice_idle_button));
                }

                if (MyInputPanel.this.mConversationIdentifier.getType().equals(ConversationType.PRIVATE)) {
                    RongIMClient.getInstance().sendTypingStatus(MyInputPanel.this.mConversationIdentifier.getType(), MyInputPanel.this.mConversationIdentifier.getTargetId(), "RC:VcMsg");
                }

                return true;
            }
        }
    };
    private OnClickListener mOnSendBtnClick = new OnClickListener() {
        public void onClick(View v) {
            MyInputPanel.this.mMyExtensionViewModel.onSendClick();
        }
    };
    private OnFocusChangeListener mOnEditTextFocusChangeListener = new OnFocusChangeListener() {
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                if (MyInputPanel.this.mMyExtensionViewModel != null && MyInputPanel.this.mMyExtensionViewModel.getInputModeLiveData() != null) {
                    MyInputPanel.this.mMyExtensionViewModel.getInputModeLiveData().postValue(InputMode.TextInput);
                }

                if (!TextUtils.isEmpty(MyInputPanel.this.mEditText.getText())) {
                    MyInputPanel.this.mSendBtn.setVisibility(View.VISIBLE);
                    MyInputPanel.this.mAddBtn.setVisibility(View.GONE);
                }
            } else if (MyInputPanel.this.mMyExtensionViewModel != null) {
                EditText editText = MyInputPanel.this.mMyExtensionViewModel.getEditTextWidget();
                if (editText.getText() != null && editText.getText().length() == 0) {
                    MyInputPanel.this.mSendBtn.setVisibility(View.GONE);
                    MyInputPanel.this.mAddBtn.setVisibility(View.VISIBLE);
                }
            }

        }
    };
    private TextWatcher mEditTextWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s != null && s.length() != 0) {
                MyInputPanel.this.mAddOrSendBtn.setVisibility(View.VISIBLE);
                MyInputPanel.this.mSendBtn.setVisibility(View.VISIBLE);
                MyInputPanel.this.mAddBtn.setVisibility(View.GONE);
            } else {
                IMCenter.getInstance().saveTextMessageDraft(MyInputPanel.this.mConversationIdentifier, MyInputPanel.this.mEditText.getText().toString(), (ResultCallback)null);
                if (!MyInputPanel.this.mInputStyle.equals(MyInputPanel.InputStyle.STYLE_CONTAINER_EXTENSION) && !MyInputPanel.this.mInputStyle.equals(MyInputPanel.InputStyle.STYLE_SWITCH_CONTAINER_EXTENSION)) {
                    MyInputPanel.this.mAddOrSendBtn.setVisibility(View.GONE);
                } else {
                    MyInputPanel.this.mAddOrSendBtn.setVisibility(View.VISIBLE);
                    MyInputPanel.this.mAddBtn.setVisibility(View.VISIBLE);
                    MyInputPanel.this.mSendBtn.setVisibility(View.GONE);
                }
            }

            int offset;
            if (count == 0) {
                int var10000 = start + before;
                offset = -before;
            } else {
                offset = count;
            }

            if (ConversationType.PRIVATE.equals(MyInputPanel.this.mConversationIdentifier.getType()) && offset != 0) {
                RongIMClient.getInstance().sendTypingStatus(MyInputPanel.this.mConversationIdentifier.getType(), MyInputPanel.this.mConversationIdentifier.getTargetId(), "RC:TxtMsg");
            }

        }

        public void afterTextChanged(Editable s) {
        }
    };

    public MyInputPanel(Fragment fragment, ViewGroup parent, MyInputPanel.InputStyle inputStyle, ConversationIdentifier conversationIdentifier) {
        super(fragment, parent, inputStyle, conversationIdentifier);
        this.mFragment = fragment;
        this.mInputStyle = inputStyle;
        this.mConversationIdentifier = conversationIdentifier;
        parent.removeAllViews();
        this.initView(fragment.getContext(), parent);
        this.mMyExtensionViewModel = (MyRongExtensionViewModel)(new ViewModelProvider(fragment)).get(MyRongExtensionViewModel.class);
        this.mMyExtensionViewModel.getInputModeLiveData().observe(fragment.getViewLifecycleOwner(), new Observer<InputMode>() {
            public void onChanged(InputMode inputMode) {
                MyInputPanel.this.updateViewByInputMode(inputMode);
            }
        });
        if (fragment.getContext() != null) {
            this.mIsVoiceInputMode = RongExtensionCacheHelper.isVoiceInputMode(fragment.getContext(), conversationIdentifier.getType(), conversationIdentifier.getTargetId());
        }

        if (this.mIsVoiceInputMode) {
            this.mMyExtensionViewModel.getInputModeLiveData().setValue(InputMode.VoiceInput);
        } else {
            this.getDraft();
            this.mMyExtensionViewModel.getInputModeLiveData().setValue(InputMode.TextInput);
        }

    }

    @SuppressLint({"ClickableViewAccessibility"})
    private void initView(final Context context, ViewGroup parent) {
        this.mContext = context;
        this.mInputPanel = LayoutInflater.from(context).inflate(R.layout.my_rc_extension_input_panel, parent, false);
        this.mVoiceToggleBtn = (ImageView)this.mInputPanel.findViewById(R.id.input_panel_voice_toggle);
        this.mEditText = (EditText)this.mInputPanel.findViewById(R.id.edit_btn);
        this.mVoiceInputBtn = (TextView)this.mInputPanel.findViewById(R.id.press_to_speech_btn);
        this.mEmojiToggleBtn = (ImageView)this.mInputPanel.findViewById(R.id.input_panel_emoji_btn);
        this.mAddOrSendBtn = (ViewGroup)this.mInputPanel.findViewById(R.id.input_panel_add_or_send);
        this.mSendBtn = (TextView)this.mInputPanel.findViewById(R.id.input_panel_send_btn);
        this.mAddBtn = (ImageView)this.mInputPanel.findViewById(R.id.input_panel_add_btn);
        this.mSendBtn.setOnClickListener(this.mOnSendBtnClick);
        this.mEditText.setOnFocusChangeListener(this.mOnEditTextFocusChangeListener);
        this.mEditText.addTextChangedListener(this.mEditTextWatcher);
        this.mVoiceToggleBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (MyInputPanel.this.mIsVoiceInputMode) {
                    MyInputPanel.this.mIsVoiceInputMode = false;
                    MyInputPanel.this.mMyExtensionViewModel.getInputModeLiveData().setValue(InputMode.TextInput);
                    MyInputPanel.this.mEditText.requestFocus();
                    if (TextUtils.isEmpty(MyInputPanel.this.mInitialDraft)) {
                        MyInputPanel.this.getDraft();
                    }
                } else {
                    MyInputPanel.this.mMyExtensionViewModel.getInputModeLiveData().postValue(InputMode.VoiceInput);
                    MyInputPanel.this.mIsVoiceInputMode = true;
                }

                RongExtensionCacheHelper.saveVoiceInputMode(context, MyInputPanel.this.mConversationIdentifier.getType(), MyInputPanel.this.mConversationIdentifier.getTargetId(), MyInputPanel.this.mIsVoiceInputMode);
            }
        });
        this.mEmojiToggleBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (MyInputPanel.this.mMyExtensionViewModel != null) {
                    if (MyInputPanel.this.mMyExtensionViewModel.getInputModeLiveData().getValue() != null && ((InputMode) MyInputPanel.this.mMyExtensionViewModel.getInputModeLiveData().getValue()).equals(InputMode.EmoticonMode)) {
                        MyInputPanel.this.mEditText.requestFocus();
                        MyInputPanel.this.mMyExtensionViewModel.getInputModeLiveData().postValue(InputMode.TextInput);
                    } else {
                        MyInputPanel.this.mMyExtensionViewModel.getInputModeLiveData().postValue(InputMode.EmoticonMode);
                    }

                }
            }
        });
        this.mAddBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (MyInputPanel.this.mMyExtensionViewModel.getInputModeLiveData().getValue() != null && ((InputMode) MyInputPanel.this.mMyExtensionViewModel.getInputModeLiveData().getValue()).equals(InputMode.PluginMode)) {
                    MyInputPanel.this.mEditText.requestFocus();
                    MyInputPanel.this.mMyExtensionViewModel.getInputModeLiveData().setValue(InputMode.TextInput);
                } else {
                    MyInputPanel.this.mMyExtensionViewModel.getInputModeLiveData().setValue(InputMode.PluginMode);
                    ReferenceManager.getInstance().hideReferenceView();
                }

                if (TextUtils.isEmpty(MyInputPanel.this.mInitialDraft)) {
                    MyInputPanel.this.getDraft();
                }

            }
        });
        this.mVoiceInputBtn.setOnTouchListener(this.mOnVoiceBtnTouchListener);
        this.setInputPanelStyle(this.mInputStyle);
    }

    private void updateViewByInputMode(InputMode inputMode) {
        if (!inputMode.equals(InputMode.TextInput) && !inputMode.equals(InputMode.PluginMode)) {
            if (inputMode.equals(InputMode.VoiceInput)) {
                this.mVoiceToggleBtn.setImageDrawable(this.mContext.getResources().getDrawable(R.drawable.rc_ext_toggle_keyboard_btn));
                this.mVoiceInputBtn.setVisibility(View.VISIBLE);
                this.mEditText.setVisibility(View.GONE);
                this.mEmojiToggleBtn.setImageDrawable(this.mContext.getResources().getDrawable(R.drawable.rc_ext_input_panel_emoji));
            } else if (inputMode.equals(InputMode.EmoticonMode)) {
                this.mVoiceToggleBtn.setImageDrawable(this.mContext.getResources().getDrawable(R.drawable.rc_ext_toggle_voice_btn));
                this.mEmojiToggleBtn.setImageDrawable(this.mContext.getResources().getDrawable(R.drawable.rc_ext_toggle_keyboard_btn));
                this.mEditText.setVisibility(View.VISIBLE);
                this.mVoiceInputBtn.setVisibility(View.GONE);
            } else if (inputMode.equals(InputMode.QuickReplyMode)) {
                this.mIsVoiceInputMode = false;
                this.mVoiceToggleBtn.setImageDrawable(this.mContext.getResources().getDrawable(R.drawable.rc_ext_toggle_voice_btn));
                this.mEmojiToggleBtn.setImageDrawable(this.mContext.getResources().getDrawable(R.drawable.rc_ext_input_panel_emoji));
                this.mEditText.setVisibility(View.VISIBLE);
                this.mVoiceInputBtn.setVisibility(View.GONE);
                this.mEmojiToggleBtn.setImageDrawable(this.mContext.getResources().getDrawable(R.drawable.rc_ext_input_panel_emoji));
            } else if (inputMode.equals(InputMode.NormalMode)) {
                this.mIsVoiceInputMode = false;
                this.mVoiceToggleBtn.setImageDrawable(this.mContext.getResources().getDrawable(R.drawable.rc_ext_toggle_voice_btn));
                this.mEmojiToggleBtn.setImageDrawable(this.mContext.getResources().getDrawable(R.drawable.rc_ext_input_panel_emoji));
                this.mEditText.setVisibility(View.VISIBLE);
                this.mVoiceInputBtn.setVisibility(View.GONE);
                this.resetInputView();
            }
        } else {
            if (inputMode.equals(InputMode.TextInput)) {
                this.mIsVoiceInputMode = false;
            }

            this.mVoiceToggleBtn.setImageDrawable(this.mContext.getResources().getDrawable(R.drawable.rc_ext_toggle_voice_btn));
            this.mEmojiToggleBtn.setImageDrawable(this.mContext.getResources().getDrawable(R.drawable.rc_ext_input_panel_emoji));
            this.mEditText.setVisibility(View.VISIBLE);
            this.mVoiceInputBtn.setVisibility(View.GONE);
            this.resetInputView();
        }

    }

    private void resetInputView() {
        Editable text = this.mEditText.getText();
        if (text != null && text.length() != 0) {
            this.mAddOrSendBtn.setVisibility(View.VISIBLE);
            this.mSendBtn.setVisibility(View.VISIBLE);
            this.mAddBtn.setVisibility(View.GONE);
        } else if (!this.mInputStyle.equals(MyInputPanel.InputStyle.STYLE_CONTAINER_EXTENSION) && !this.mInputStyle.equals(MyInputPanel.InputStyle.STYLE_SWITCH_CONTAINER_EXTENSION)) {
            this.mAddOrSendBtn.setVisibility(View.GONE);
        } else {
            this.mAddOrSendBtn.setVisibility(View.VISIBLE);
            this.mAddBtn.setVisibility(View.VISIBLE);
            this.mSendBtn.setVisibility(View.GONE);
        }

    }

    public EditText getEditText() {
        return this.mEditText;
    }

    public View getRootView() {
        return this.mInputPanel;
    }

    public void setVisible(int viewId, boolean visible) {
        this.mInputPanel.findViewById(viewId).setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public void setInputPanelStyle(MyInputPanel.InputStyle style) {
        switch(style) {
            case STYLE_SWITCH_CONTAINER_EXTENSION:
                this.setSCE();
                break;
            case STYLE_CONTAINER:
                this.setC();
                break;
            case STYLE_CONTAINER_EXTENSION:
                this.setCE();
                break;
            case STYLE_SWITCH_CONTAINER:
                this.setSC();
                break;
            default:
                this.setSCE();
        }

        this.mInputStyle = style;
    }

    private void setSCE() {
        if (this.mInputPanel != null) {
            this.mVoiceToggleBtn.setVisibility(View.VISIBLE);
            this.mEmojiToggleBtn.setVisibility(this.shouldShowEmojiButton() ? View.VISIBLE : View.GONE);
            this.mAddBtn.setVisibility(View.VISIBLE);
        }

    }

    private void setC() {
        if (this.mInputPanel != null) {
            this.mVoiceToggleBtn.setVisibility(View.GONE);
            this.mAddOrSendBtn.setVisibility(View.GONE);
            this.mEmojiToggleBtn.setVisibility(View.GONE);
            this.mAddBtn.setVisibility(View.GONE);
            this.mSendBtn.setVisibility(View.GONE);
        }

    }

    private void setCE() {
        if (this.mInputPanel != null) {
            this.mVoiceToggleBtn.setVisibility(View.GONE);
            this.mAddOrSendBtn.setVisibility(View.VISIBLE);
            this.mEmojiToggleBtn.setVisibility(this.shouldShowEmojiButton() ? View.VISIBLE : View.GONE);
            this.mAddBtn.setVisibility(View.VISIBLE);
        }

    }

    private void setSC() {
        if (this.mInputPanel != null) {
            this.mVoiceToggleBtn.setVisibility(View.VISIBLE);
            this.mAddOrSendBtn.setVisibility(View.GONE);
            this.mAddBtn.setVisibility(View.GONE);
        }

    }

    private boolean shouldShowEmojiButton() {
        return !RongConfigCenter.featureConfig().isHideEmojiButton();
    }

    private void getDraft() {
        ChannelClient.getInstance().getTextMessageDraft(this.mConversationIdentifier.getType(), this.mConversationIdentifier.getTargetId(), this.mConversationIdentifier.getChannelId(), new io.rong.imlib.IRongCoreCallback.ResultCallback<String>() {
            public void onSuccess(final String s) {
                if (!TextUtils.isEmpty(s)) {
                    MyInputPanel.this.mEditText.postDelayed(new Runnable() {
                        public void run() {
                            MyInputPanel.this.mInitialDraft = s;
                            if (MyInputPanel.this.mEditText instanceof RongEditText) {
                                ((RongEditText) MyInputPanel.this.mEditText).setText(s, false);
                            } else {
                                MyInputPanel.this.mEditText.setText(s);
                            }

                            MyInputPanel.this.mEditText.setSelection(s.length());
                            MyInputPanel.this.mEditText.requestFocus();
                            MyInputPanel.this.resetInputView();
                        }
                    }, 50L);
                }

            }

            public void onError(CoreErrorCode errorCode) {
            }
        });
    }

    public void onDestroy() {
        this.mFragment = null;
        this.mMyExtensionViewModel = null;
        if (this.mEditText != null && this.mEditText.getText() != null && !this.mInitialDraft.equals(this.mEditText.getText().toString())) {
            IMCenter.getInstance().saveTextMessageDraft(this.mConversationIdentifier, this.mEditText.getText().toString(), (ResultCallback)null);
        }

    }

}

