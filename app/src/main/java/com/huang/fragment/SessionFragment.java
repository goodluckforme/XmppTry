package com.huang.fragment;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.huang.testxmpp.R;
import com.huang.utils.CommonUtils;
import com.huang.utils.Watch.Watcher;
import com.huang.xmpp.Constant;
import com.huang.xmpp.XMChatMessageListener;
import com.huang.xmpp.XmppConnection;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.muc.MultiUserChat;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * 会话
 * Created by Administrator on 2017/11/15.
 */

public class SessionFragment extends Fragment implements Watcher {
    TextView show;
    ImageView show_iv;
    Button show_getfirend;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = null;
        try {
            layout = inflater.inflate(R.layout.fragment_session, container, false);
            show = (TextView) layout.findViewById(R.id.show);
            show_iv = (ImageView) layout.findViewById(R.id.show_iv);
            show_iv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Drawable much = XmppConnection.getInstance().getUserImage("much");
                    boolean b = (much != null);
                    if (b && !much.equals("")) {
                        show_iv.setImageDrawable(much);
                    } else {
                        Bitmap imageFromAssetsFile = CommonUtils.getImageFromAssetsFile(getActivity(), "123.jpg");
                        String path = CommonUtils.saveBitmapToSDCard(imageFromAssetsFile, Calendar.getInstance().getTimeInMillis() / 1000 + "");
                        show_iv.setImageBitmap(imageFromAssetsFile);
                        XmppConnection.getInstance().changeImage(new File(path));
                    }
                    Toast.makeText(getActivity(), "客户是否有头像 :" + b, Toast.LENGTH_SHORT).show();
                }
            });
            layout.findViewById(R.id.show_getfirend).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    initGroupChatManager();
                }
            });
            init();// 初始化
        } catch (Exception e) {
            e.printStackTrace();
        }
        return layout;
    }
    // 群聊的聊天室列表
    public static List<MultiUserChat> multiUserChatList = new ArrayList<>();

    // 群聊，加入聊天室，并且监听聊天室消息
    private void initGroupChatManager() {

        for (String hostedRoomStr : Arrays.asList(Constant.roomNameList)) {
            multiUserChatList.add(XmppConnection.getInstance().joinMultiUserChat(hostedRoomStr, hostedRoomStr));
        }
        for (MultiUserChat multiUserChat : multiUserChatList) {
            multiUserChat.addMessageListener(new XMChatMessageListener());
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        XMChatMessageListener.removeWatcher(this);// 删除XMPP消息观察者
    }

    private void init() {
        for (String hostedRoomStr : Arrays.asList(Constant.roomNameList)) {
            XmppConnection.getInstance().createRoom(hostedRoomStr, hostedRoomStr);
        }
        XMChatMessageListener.addWatcher(this);// 增加XMPP消息观察者
    }

    @Override
    public void update(final Message message) {// 通过会话DB，刷新会话列表
        Log.e("SessionFragmentMessage", message.toString());
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                show.setText("有新消息：" + message.getBody() + "\n来自于" + message.getFrom());
                Toast.makeText(getActivity(), message.getFrom() + " :" + message.getBody(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
