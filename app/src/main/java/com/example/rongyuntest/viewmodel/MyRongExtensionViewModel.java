package com.example.rongyuntest.viewmodel;

import android.app.Application;
import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;




import android.app.Application;
import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView.BufferType;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import io.rong.common.RLog;
import io.rong.imkit.IMCenter;
import io.rong.imkit.R.string;
import io.rong.imkit.conversation.extension.InputMode;
import io.rong.imkit.conversation.extension.RongExtensionCacheHelper;
import io.rong.imkit.conversation.extension.RongExtensionManager;
import io.rong.imkit.conversation.extension.component.emoticon.AndroidEmoji;
import io.rong.imkit.feature.destruct.DestructManager;
import io.rong.imkit.feature.mention.IExtensionEventWatcher;
import io.rong.imkit.feature.mention.RongMentionManager;
import io.rong.imkit.picture.tools.ToastUtils;
import io.rong.imlib.IRongCallback;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.ConversationIdentifier;
import io.rong.imlib.model.Message;
import io.rong.message.TextMessage;
import java.util.Iterator;
import java.util.Objects;

public class MyRongExtensionViewModel extends AndroidViewModel {
    
    private final String TAG = this.getClass().getSimpleName();
    private MutableLiveData<Boolean> mExtensionBoardState = new MutableLiveData();
    private MutableLiveData<InputMode> mInputModeLiveData = new MutableLiveData();
    private MutableLiveData<Boolean> mAttachedInfoState = new MutableLiveData();
    private ConversationIdentifier mConversationIdentifier;
    private EditText mEditText;
    private boolean isSoftInputShow;
    private static final int MAX_MESSAGE_LENGTH_TO_SEND = 5500;
    private TextWatcher mTextWatcher = new TextWatcher() {
        private int start;
        private int count;
        private boolean isProcess;

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!this.isProcess) {
                this.start = start;
                this.count = count;
                int cursor;
                int offset;
                if (count == 0) {
                    cursor = start + before;
                    offset = -before;
                } else {
                    cursor = start;
                    offset = count;
                }

                RongMentionManager.getInstance().onTextChanged(MyRongExtensionViewModel.this.getApplication().getApplicationContext(), MyRongExtensionViewModel.this.mConversationIdentifier.getType(), MyRongExtensionViewModel.this.mConversationIdentifier.getTargetId(), cursor, offset, s.toString(), MyRongExtensionViewModel.this.mEditText);
                Iterator var7 = RongExtensionManager.getInstance().getExtensionEventWatcher().iterator();

                while(var7.hasNext()) {
                    IExtensionEventWatcher watcher = (IExtensionEventWatcher)var7.next();
                    watcher.onTextChanged(MyRongExtensionViewModel.this.getApplication().getApplicationContext(), MyRongExtensionViewModel.this.mConversationIdentifier.getType(), MyRongExtensionViewModel.this.mConversationIdentifier.getTargetId(), cursor, offset, s.toString());
                }

                if (MyRongExtensionViewModel.this.mInputModeLiveData.getValue() != InputMode.EmoticonMode && MyRongExtensionViewModel.this.mInputModeLiveData.getValue() != InputMode.RecognizeMode) {
                    MyRongExtensionViewModel.this.mInputModeLiveData.postValue(InputMode.TextInput);
                    if (MyRongExtensionViewModel.this.mEditText.getText() != null && MyRongExtensionViewModel.this.mEditText.getText().length() > 0) {
                        MyRongExtensionViewModel.this.mEditText.postDelayed(new Runnable() {
                            public void run() {
                                MyRongExtensionViewModel.this.setSoftInputKeyBoard(true);
                            }
                        }, 100L);
                    }
                }

            }
        }

        public void afterTextChanged(Editable s) {
            if (!this.isProcess) {
                int selectionStart = MyRongExtensionViewModel.this.mEditText.getSelectionStart();
                if (AndroidEmoji.isEmoji(s.subSequence(this.start, this.start + this.count).toString())) {
                    this.isProcess = true;
                    String resultStr = AndroidEmoji.replaceEmojiWithText(s.toString());
                    MyRongExtensionViewModel.this.mEditText.setText(AndroidEmoji.ensure(resultStr), TextView.BufferType.SPANNABLE);
                    MyRongExtensionViewModel.this.mEditText.setSelection(Math.min(MyRongExtensionViewModel.this.mEditText.getText().length(), Math.max(0, selectionStart)));
                    this.isProcess = false;
                }

            }
        }
    };

    public MyRongExtensionViewModel(@NonNull Application application) {
        super(application);
    }

    public void setAttachedConversation(ConversationIdentifier conversationIdentifier, EditText editText) {
        this.mConversationIdentifier = conversationIdentifier;
        this.mEditText = editText;
        this.mEditText.addTextChangedListener(this.mTextWatcher);
        if (this.mConversationIdentifier.getType().equals(Conversation.ConversationType.GROUP) || this.mConversationIdentifier.getType().equals(Conversation.ConversationType.ULTRA_GROUP)) {
            RongMentionManager.getInstance().createInstance(this.mConversationIdentifier.getType(), this.mConversationIdentifier.getTargetId(), this.mEditText);
        }

    }

    public void onSendClick() {
        if (!TextUtils.isEmpty(this.mEditText.getText()) && !TextUtils.isEmpty(this.mEditText.getText().toString().trim())) {
            String text = this.mEditText.getText().toString();
            if (text.length() > 5500) {
                ToastUtils.s(this.getApplication().getApplicationContext(), this.getApplication().getString(string.rc_message_too_long));
                RLog.d(this.TAG, "The text you entered is too long to send.");
            } else {
                this.mEditText.setText("");
                TextMessage textMessage = TextMessage.obtain(text);
                if (DestructManager.isActive()) {
                    int length = text.length();
                    long time;
                    if (length <= 20) {
                        time = 10L;
                    } else {
                        time = Math.round((double)(length - 20) * 0.5D + 10.0D);
                    }

                    textMessage.setDestruct(true);
                    textMessage.setDestructTime(time);
                }

                Message message = Message.obtain(this.mConversationIdentifier, textMessage);
                RongMentionManager.getInstance().onSendToggleClick(message, this.mEditText);
                if (RongExtensionManager.getInstance().getExtensionEventWatcher().size() > 0) {
                    Iterator var7 = RongExtensionManager.getInstance().getExtensionEventWatcher().iterator();

                    while(var7.hasNext()) {
                        IExtensionEventWatcher watcher = (IExtensionEventWatcher)var7.next();
                        watcher.onSendToggleClick(message);
                    }
                }

                IMCenter.getInstance().sendMessage(message, DestructManager.isActive() ? this.getApplication().getResources().getString(string.rc_conversation_summary_content_burn) : null, (String)null, (IRongCallback.ISendMessageCallback)null);
            }
        } else {
            RLog.d(this.TAG, "can't send empty content.");
            this.mEditText.setText("");
        }
    }

    public boolean isSoftInputShow() {
        return this.isSoftInputShow;
    }

    public void exitMoreInputMode(Context context) {
        if (context != null) {
            if (RongExtensionCacheHelper.isVoiceInputMode(context, this.mConversationIdentifier.getType(), this.mConversationIdentifier.getTargetId())) {
                this.mInputModeLiveData.postValue(InputMode.VoiceInput);
            } else {
                this.collapseExtensionBoard();
            }

        }
    }

    public void collapseExtensionBoard() {
        if (this.mExtensionBoardState.getValue() != null && ((Boolean)this.mExtensionBoardState.getValue()).equals(false)) {
            RLog.d(this.TAG, "already collapsed, return directly.");
        } else {
            RLog.d(this.TAG, "collapseExtensionBoard");
            this.setSoftInputKeyBoard(false);
            this.mExtensionBoardState.postValue(false);
            if (!DestructManager.isActive()) {
                this.mInputModeLiveData.postValue(InputMode.NormalMode);
            }

        }
    }

    public void setSoftInputKeyBoard(boolean isShow) {
        this.forceSetSoftInputKeyBoard(isShow);
    }

    public void setSoftInputKeyBoard(boolean isShow, boolean clearFocus) {
        this.forceSetSoftInputKeyBoard(isShow, clearFocus);
    }

    public void forceSetSoftInputKeyBoard(boolean isShow) {
        this.forceSetSoftInputKeyBoard(isShow, true);
    }

    public void forceSetSoftInputKeyBoard(boolean isShow, boolean clearFocus) {
        if (this.mEditText != null) {
            InputMethodManager imm = (InputMethodManager)this.getApplication().getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                if (isShow) {
                    this.mEditText.requestFocus();
                    imm.showSoftInput(this.mEditText, 0);
                } else {
                    imm.hideSoftInputFromWindow(this.mEditText.getWindowToken(), 0);
                    if (clearFocus) {
                        this.mEditText.clearFocus();
                    }
                }

                this.isSoftInputShow = isShow;
            }

            if (isShow && this.mExtensionBoardState.getValue() != null && ((Boolean)this.mExtensionBoardState.getValue()).equals(false)) {
                this.mExtensionBoardState.setValue(true);
            }

        }
    }

    public EditText getEditTextWidget() {
        return this.mEditText;
    }

    public void setEditTextWidget(EditText editText) {
        if (!Objects.equals(this.mEditText, editText)) {
            this.mEditText = editText;
            this.mEditText.addTextChangedListener(this.mTextWatcher);
        }

    }

    public MutableLiveData<Boolean> getAttachedInfoState() {
        return this.mAttachedInfoState;
    }

    public MutableLiveData<Boolean> getExtensionBoardState() {
        return this.mExtensionBoardState;
    }

    public MutableLiveData<InputMode> getInputModeLiveData() {
        return this.mInputModeLiveData;
    }
}
