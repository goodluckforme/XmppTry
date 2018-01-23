package com.huang.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.huang.testxmpp.R;


/**
 * 我
 * Created by Administrator on 2017/11/15.
 */

public class MeFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = null;
        try {
            layout = inflater.inflate(R.layout.fragment_session, container, false);
            init();// 初始化
        } catch (Exception e) {
            e.printStackTrace();
        }
        return layout;
    }

    private void init() {

    }
}
