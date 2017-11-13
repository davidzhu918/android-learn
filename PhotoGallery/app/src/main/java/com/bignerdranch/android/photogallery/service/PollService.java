package com.bignerdranch.android.photogallery.service;

import android.content.Context;

import java.util.concurrent.TimeUnit;

/**
 * Created by zixiangzhu on 11/5/17.
 */

public interface PollService {
    String TAG = "PollService";

    // Set interval to 1 minute.
    long POLL_INTERVAL_MS = TimeUnit.MINUTES.toMillis(1);

    String ACTION_SHOW_NOTIFICATION =
            "com.bignerdranch.android.photogallery.SHOW_NOTIFICATION";

    String PERM_PRIVATE =
            "com.bignerdranch.android.photogallery.PRIVATE";

    String REQUEST_CODE = "REQUEST_CODE";

    String NOTIFICATION = "NOTIFICATION";

    void setServiceJob(Context context, boolean isOn);
    boolean isServiceOn(Context context);
}
