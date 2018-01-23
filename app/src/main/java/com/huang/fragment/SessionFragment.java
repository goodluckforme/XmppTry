package com.huang.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.huang.testxmpp.R;
import com.huang.utils.Watch.Watcher;
import com.huang.xmpp.XMChatMessageListener;

import org.jivesoftware.smack.packet.Message;

/**
 * 会话
 * Created by Administrator on 2017/11/15.
 */

public class SessionFragment extends Fragment implements Watcher {
    TextView show;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = null;
        try {
            layout = inflater.inflate(R.layout.fragment_session, container, false);
            show = (TextView) layout.findViewById(R.id.show);
            init();// 初始化
        } catch (Exception e) {
            e.printStackTrace();
        }
        return layout;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        XMChatMessageListener.removeWatcher(this);// 删除XMPP消息观察者
    }

    private void init() {
        XMChatMessageListener.addWatcher(this);// 增加XMPP消息观察者
    }

    @Override
    public void update(final Message message) {// 通过会话DB，刷新会话列表
        Log.e("SessionFragmentMessage", message.toString());
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                show.setText("有新消息："+message.getBody()+"\n来自于"+message.getFrom());
                Toast.makeText(getActivity(),message.getFrom()+" :"+message.getBody(),Toast.LENGTH_SHORT).show();
            }
        });
    }
}
