package com.shark.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.shark.expandabletextview.ExpandableTextView;

public class MainActivity extends AppCompatActivity {
    private ExpandableTextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = (ExpandableTextView) findViewById(R.id.tv_expandable_text_view);
        init();
    }

    private void init() {
        mTextView.setExpandText("Read More");
        mTextView.setShrinkText("Shrink");
        mTextView.setTextColor(R.color.color1);
        mTextView.setExpandTextColor(R.color.color2);
        mTextView.setCurrState(ExpandableTextView.STATE_SHRINK);
        mTextView.setMaxLinesOnShrink(4);
        mTextView.setMarginLeft(16);//px
    }

    public void setContent(View view) {
        mTextView.setText("Look:\n" +
                "自定义View实现卷尺效果，博客实现原理分析+github开源 (None)\n" +
                "自定义View实现卷尺效果，博客实现原理分析+github开源 (None)\n" +
                "自定义View实现卷尺效果，博客实现原理分析+github开源 (None)\n" +
                "自定义View实现卷尺效果，博客实现原理分析+github开源 (None)\n" +
                "自定义View实现卷尺效果，博客实现原理分析+github开源 (None)");
    }
}
