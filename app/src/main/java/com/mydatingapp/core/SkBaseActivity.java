package com.mydatingapp.core;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.TextView;

import com.mydatingapp.model.base.BaseRestCommand;
import com.mydatingapp.model.base.BaseServiceHelper;
import com.mydatingapp.model.base.classes.SkUser;
import com.mydatingapp.ui.auth.AuthActivity;
import com.mydatingapp.ui.auth.UrlActivity;
import com.mydatingapp.ui.fbconnect.FacebookLoginStepManager;
import com.mydatingapp.utils.SKDimensions;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by sardar on 4/30/14.
 */
public class SkBaseActivity extends Activity implements SkServiceCallbackListener {

    protected BaseServiceHelper baseHelper;
    protected boolean activityIsFinished;


    public SkBaseActivity() {

    }

    public SkApplication getApp() {
        return (SkApplication) getApplication();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        onCreate(savedInstanceState, true);
    }

    protected void onCreate(Bundle savedInstanceState, boolean getSiteData) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        baseHelper = BaseServiceHelper.getInstance(getApp());

        checkRedirects();

        if( getSiteData ){
            getSiteInfo();
        }

        int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
        TextView titleTextView = (TextView) findViewById(titleId);
        if( titleTextView != null ) {
            titleTextView.setPadding(SKDimensions.convertDpToPixel(5, this), 0, 0, 0);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getBaseHelper().addListener(this);
        getApp().setForegroundActivityClass(getClass());
    }

    @Override
    protected void onPause() {
        super.onPause();
        getBaseHelper().removeListener(this);
        getApp().setForegroundActivityClass(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getBaseHelper().removeListener(this);
    }

    public BaseServiceHelper getBaseHelper(){
        return baseHelper;
    }

    private int siteInfoRequestId;

    private void getSiteInfo(){
        siteInfoRequestId = getBaseHelper().runRestRequest(BaseRestCommand.ACTION_TYPE.SITE_INFO);
    }

    protected void processSiteInfoData(){

    }

    private void checkRedirects(){

        ArrayList<Class> list = new ArrayList<>();
        list.addAll(Arrays.asList(UrlActivity.class, AuthActivity.class, FacebookLoginStepManager.class));

        if( list.contains(getClass()) ){
            return;
        }

        // remove history and redirect to URL activity if URL is empty
        if( getApp().getSiteUrl() == null ){
            Intent in = new Intent(this, UrlActivity.class);
            in.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(in);
            activityIsFinished = true;
            finish();
        }

        else if( getApp().getAuthToken() == null ){
            Intent in = new Intent(this, AuthActivity.class);
            in.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(in);
            activityIsFinished = true;
            finish();
        }
    }

    @Override
    public void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle data) {
        String aaa = (String)data.get("data");
    }

    public SkUser getCurrentUser(){
        return null;
    }
}
