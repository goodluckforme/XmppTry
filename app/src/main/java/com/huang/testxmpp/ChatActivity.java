package com.huang.testxmpp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.huang.adapter.ChatRecyclerListAdapter;
import com.huang.bean.YtecConsultMsgBean;
import com.huang.utils.CommonUtils;
import com.huang.utils.Watch.Watcher;
import com.huang.utils.voice.MediaPlayManager;
import com.huang.utils.voice.VoiceRecorderButton;
import com.huang.xmpp.XMChatMessageListener;
import com.huang.xmpp.XmppConnection;

import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smackx.muc.MultiUserChat;

import java.util.ArrayList;

/**
 * 聊天类-->即时聊天
 *
 * @author huang
 */
public class ChatActivity extends BaseActivity implements OnClickListener, SwipeRefreshLayout.OnRefreshListener, Watcher {
    private Chat chat;// 单聊
    private MultiUserChat muc;// 群组

    private Button chatNewSend;// 发信息
    private EditText chatNewEditText;// 输入框
    private LinearLayout chatNewLinearLayout_other;// 显示功能布局

    private int PageNumber = 0;// 当前页数
    private SwipeRefreshLayout swipeView;// 下拉加载更多
    private android.support.v7.widget.RecyclerView msgListView;
    private ArrayList<YtecConsultMsgBean> list = new ArrayList<>();
    private ChatRecyclerListAdapter adapter;

    // 发送语音新增的内容
    private Button chatNewVoice, chatNewKeyboard, chatAddButton;// 语音按钮,键盘按钮,增加图片和配方的按钮
    private VoiceRecorderButton btn_recorder;// 长按录制语音按钮
    private String audio_seconds = "";// 录音时长
    private String audio_file_path = "";// 保存录音的路径

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_chat);
            init();// 初始化
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.chatNewVoice:// 语音按钮
                if (!CommonUtils.ExistSDCard()) {
                    CommonUtils.dealWithToast(this, "请插入SD卡", 0);
                } else if (CommonUtils.getSDFreeSize() < 2) {
                    CommonUtils.dealWithToast(this, "SD卡剩余空间不足2M，请先清理SD卡空间", 0);
                } else {
                    btn_recorder.setVisibility(View.VISIBLE);
                    chatNewEditText.setVisibility(View.GONE);
                    chatNewVoice.setVisibility(View.GONE);
                    chatNewKeyboard.setVisibility(View.VISIBLE);
                    CommonUtils.doSomethingForKeyboard(this, false, chatNewEditText);// 隐藏软键盘
                }
                break;
            case R.id.chatNewkeyboard:// 键盘按钮
                btn_recorder.setVisibility(View.GONE);
                chatNewEditText.setVisibility(View.VISIBLE);
                chatNewVoice.setVisibility(View.VISIBLE);
                chatNewKeyboard.setVisibility(View.GONE);
                CommonUtils.doSomethingForKeyboard(this, true, chatNewEditText);// 显示软键盘
                break;
            case R.id.chatAddButton:// 点击+号按钮,显示或者隐藏开方等布局
                if (chatNewLinearLayout_other.getVisibility() == View.GONE) {
                    chatNewLinearLayout_other.setVisibility(View.VISIBLE);
                } else {
                    chatNewLinearLayout_other.setVisibility(View.GONE);
                }
                CommonUtils.doSomethingForKeyboard(this, false, chatNewEditText);// 隐藏软键盘
                break;
            case R.id.chatNewPictureTextView:// 图片
                chatNewLinearLayout_other.setVisibility(View.GONE);

                if (!CommonUtils.ExistSDCard()) {
                    CommonUtils.dealWithToast(this, "请插入SD卡", 0);
                } else if (CommonUtils.getSDFreeSize() < 2) {
                    CommonUtils.dealWithToast(this, "SD卡剩余空间不足2M，请先清理SD卡空间", 0);
                } else {
                    // 从SD卡中读取图片,然后再发送
                }
                break;
            case R.id.chatNewArticleTextView:// 文章
                chatNewLinearLayout_other.setVisibility(View.GONE);
                break;
            case R.id.chatNewCollectTextView:// 收藏
                chatNewLinearLayout_other.setVisibility(View.GONE);
                break;
            case R.id.chatNewPrescriptionTextView:// 开方
                chatNewLinearLayout_other.setVisibility(View.GONE);
                break;
            case R.id.chatNewSend:
                String msg = chatNewEditText.getText().toString();

                if (!TextUtils.isEmpty(CommonUtils.replaceStr(msg))) {
                    int receiver_id = 0;// 0 is test

                    // 拼接JSON格式进行传输
                    YtecConsultMsgBean msgBean = new YtecConsultMsgBean(0, msg, 1, 1, "1", "", "",
                            receiver_id, 0, CommonUtils.getCurrentTime(), true, "");
                    String msgJson = (new Gson()).toJson(msgBean);
                    sendMsg(msgJson);// 发送信息
                }
                break;
            default:
                break;
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    /*
     * 初始化
     */
    @SuppressLint("InlinedApi")
    @SuppressWarnings("deprecation")
    private void init() {
        if (getIntent().getBooleanExtra("ChatType", false)) {
            muc = LoginActivity.multiUserChatList.get(getIntent().getIntExtra("MultiUserChatPosition", 0));
        } else {
            chat = XmppConnection.getInstance().getFriendChat(getIntent().getStringExtra("SingleUserChatJID"));
        }
        XMChatMessageListener.addWatcher(this);// 增加XMPP消息观察者

        swipeView = (SwipeRefreshLayout) findViewById(R.id.swipe);// 下拉刷新
        swipeView.setOnRefreshListener(this);
        swipeView.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                android.R.color.holo_orange_light, android.R.color.holo_red_light);

        btn_recorder = (VoiceRecorderButton) findViewById(R.id.btn_recorder);
        btn_recorder.setOnAudioRecorderListener(new MyAudioRecorderListener());

        chatNewVoice = (Button) findViewById(R.id.chatNewVoice);
        chatNewKeyboard = (Button) findViewById(R.id.chatNewkeyboard);
        chatAddButton = (Button) findViewById(R.id.chatAddButton);
        chatNewVoice.setOnClickListener(this);
        chatNewKeyboard.setOnClickListener(this);
        chatAddButton.setOnClickListener(this);
        chatNewSend = (Button) findViewById(R.id.chatNewSend);
        chatNewSend.setOnClickListener(this);
        chatNewLinearLayout_other = (LinearLayout) findViewById(R.id.chatLinearLayout_other);
        chatNewEditText = (EditText) findViewById(R.id.chatNewEditText);
        chatNewEditText.addTextChangedListener(new EditChangedListener());

        TextView chatNewPictureTextView = (TextView) findViewById(R.id.chatNewPictureTextView);
        chatNewPictureTextView.setOnClickListener(this);
        TextView chatNewArticleTextView = (TextView) findViewById(R.id.chatNewArticleTextView);
        chatNewArticleTextView.setOnClickListener(this);
        TextView chatNewCollectTextView = (TextView) findViewById(R.id.chatNewCollectTextView);
        chatNewCollectTextView.setOnClickListener(this);
        TextView chatNewPrescriptionTextView = (TextView) findViewById(R.id.chatNewPrescriptionTextView);
        chatNewPrescriptionTextView.setOnClickListener(this);

        msgListView = (android.support.v7.widget.RecyclerView) findViewById(R.id.chatNewListView);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        adapter = new ChatRecyclerListAdapter(this, 1, list);
        msgListView.setLayoutManager(mLayoutManager);
        msgListView.setAdapter(adapter);
        loadData(0);// 从本地读取旧的信息并加载数据源
    }

    /*
     * 从本地读取旧的信息并加载数据源
     */
    private void loadData(int PageNumber) {
    }

    /*
     * open fire发送消息
     */
    private void sendMsg(final String msg) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                XmppConnection.getInstance().sendMessage(chat, muc, msg);

                Message message = new Message();// 发送message
                message.what = 2;
                message.obj = msg;
                handler.sendMessage(message);
            }
        }).start();
    }

    @Override
    public void onPause() {
        super.onPause();
        MediaPlayManager.pause();
    }

    @Override
    public void onResume() {
        super.onResume();
        MediaPlayManager.resume();
    }

    @Override
    protected void onDestroy() {
        MediaPlayManager.release();
        XMChatMessageListener.removeWatcher(this);// 删除XMPP消息观察者
        super.onDestroy();
    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            // 从线程那里接收到从后台返回来的数据
            String queryStr = (String) msg.obj;
            Log.e("log", "ChatActivity.queryStr=" + queryStr);

            switch (msg.what) {
                case 1:// 收消息
                    refreshMessage(queryStr);// 刷新界面
                    break;
                case 2:// 发消息
//                    String time = CommonUtils.getTime();// 拼装获得的消息
//                    int read = 1;
//                    int latest = 0;
//                    String body = queryStr;
//                    String to = "1";
//                    String from = "JID";
//                    MsgBean mybean = new MsgBean(0, 1, from, to, body, time, read, latest);
//                    // 将消息存入数据库
//
//                    if (chat != null) {// 单聊
//                        refreshMessage(queryStr);// 刷新界面
//                    } else if (muc != null) {// 群聊
//                        // do not need
//                    }

                    chatNewEditText.setText("");// 输入框内容设置为空
                    chatAddButton.setVisibility(View.VISIBLE);// 显示“+”按钮
                    chatNewSend.setVisibility(View.GONE);// 隐藏发送按钮
                    break;
                case 5:// 下拉加载更多
                    PageNumber++;
                    loadData(PageNumber);// 从本地读取旧的信息并加载数据源
                    swipeView.setRefreshing(false);
                    break;
            }
        }
    };

    /*
     * 刷新界面
     */
    private void refreshMessage(String message) {
        try {
            YtecConsultMsgBean bean = (new Gson()).fromJson(message, YtecConsultMsgBean.class);

            list.add(bean);
            adapter.notifyDataSetChanged();
            msgListView.scrollToPosition(list.size() - 1);//刷新到底部
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(final org.jivesoftware.smack.packet.Message message) {
        // 是当前聊天对象或者聊天群才显示这条消息
        if (chat != null) {// 单聊
            if (message.getFrom() != null && !message.getFrom().toString().contains(chat.getXmppAddressOfChatPartner())) {
                return;
            }
        } else if (muc != null) {// 群聊
            if (!message.getFrom().toString().contains(muc.getRoom().toString())) {
                return;
            }
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                refreshMessage(message.getBody());
            }
        });
        Log.e("ChatActivityMessage", message.toString());
    }

    // 长按语音按钮的监听事件
    private class MyAudioRecorderListener implements VoiceRecorderButton.AudioRecorderListener {
        @Override
        public void onFinish(float seconds, String filePath) {
            audio_seconds = Math.round(seconds) + "";
            audio_file_path = filePath;
            // 上传语音文件,上传成功后,添加语音的网络地址到列表中去

        }
    }

    // 输入框文本改变监听
    private class EditChangedListener implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // 输入文本之前的状态
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // 输入文字中的状态，count是一次性输入字符数
        }

        @Override
        public void afterTextChanged(Editable s) {
            // 输入文字后的状态
            if (chatNewEditText.getText().toString().length() == 0) {
                chatAddButton.setVisibility(View.VISIBLE);// 显示“+”按钮
                chatNewSend.setVisibility(View.GONE);// 隐藏发送按钮
            } else {
                chatAddButton.setVisibility(View.GONE);// 隐藏“+”按钮
                chatNewSend.setVisibility(View.VISIBLE);// 显示发送按钮
            }
        }
    }

    @Override
    public void onRefresh() {
        // 下拉加载更多
        handler.sendEmptyMessageDelayed(5, 500);
    }
}
