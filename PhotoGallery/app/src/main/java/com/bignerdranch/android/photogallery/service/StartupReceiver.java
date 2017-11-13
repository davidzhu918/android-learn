package com.bignerdranch.android.photogallery.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.bignerdranch.android.photogallery.QueryPreferences;

/**
 * Created by zixiangzhu on 11/12/17.
 */

public class StartupReceiver extends BroadcastReceiver {
    private static final String TAG = "StartupReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Received broadcast intent: " + intent.getAction());
        boolean isOn = QueryPreferences.isAlarmOn(context);
        PollServiceFactory.getPollService().setServiceJob(context, isOn);
    }
}
