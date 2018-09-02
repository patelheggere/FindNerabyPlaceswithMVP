package com.patelheggere.goldfarm.view.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.patelheggere.goldfarm.R;
import com.patelheggere.goldfarm.commons.BaseActivity;
import com.patelheggere.goldfarm.helper.AppUtils;

import static com.patelheggere.goldfarm.helper.AppUtils.isLocationEnabled;

public class SplashScreenActivity extends BaseActivity {

    private LinearLayout l1,l2;
    private Animation uptodown,downtoup, bounce;
    private TextView mTextViewTitle;
    private static boolean isLocation;

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
    }

    @Override
    protected void initListener() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(isLocationEnabled(context))
                {
                    startActivity(new Intent(context, SecondMainActivity.class));
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_in_right);
                    finish();
                }
                else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SplashScreenActivity.this);
                    builder.setMessage(getString(R.string.loc_enabled));
                    builder.setTitle("Alert");
                    builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });

                    builder.setPositiveButton(getString(R.string.loc_enabled), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            isLocation = true;
                            Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(myIntent);
                        }
                    });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }

            }
        }, AppUtils.Constants.TWO_SECOND);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isLocation) {
            recreate();
        }
    }
}
