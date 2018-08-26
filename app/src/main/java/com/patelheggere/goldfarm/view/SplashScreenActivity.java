package com.patelheggere.goldfarm.view;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.patelheggere.goldfarm.R;
import com.patelheggere.goldfarm.commons.BaseActivity;

public class SplashScreenActivity extends BaseActivity {

    private LinearLayout l1,l2;
    private Animation uptodown,downtoup, bounce;
    private TextView mTextViewTitle;

    @Override
    protected int getContentView() {
        return R.layout.activity_splash_screen;
    }

    @Override
    protected void initView() {
        l1 = (LinearLayout) findViewById(R.id.l1);
        l2 = (LinearLayout) findViewById(R.id.l2);
        mTextViewTitle = findViewById(R.id.tv_title);

    }

    @Override
    protected void initData() {
        bounce = AnimationUtils.loadAnimation(context, R.anim.bounce);
        uptodown = AnimationUtils.loadAnimation(context,R.anim.topdown);
        downtoup = AnimationUtils.loadAnimation(context,R.anim.bottomtop);
        l1.setAnimation(uptodown);
        l2.setAnimation(downtoup);
        mTextViewTitle.startAnimation(bounce);
    }

    @Override
    protected void initListener() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(context, SecondMainActivity.class));
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_in_right);
                finish();
            }
        },2000);
    }
}
