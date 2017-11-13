package com.bignerdranch.android.photogallery.service;

import android.os.Build;

/**
 * Created by zixiangzhu on 11/5/17.
 */

public class PollServiceFactory {
    private static PollService pollService;

    private PollServiceFactory() {}

    public static PollService getPollService() {
        if (pollService != null) {
            return pollService;
        }

        pollService = Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP
                ? new JobPollService() : new DefaultPollService();
        return pollService;
    }


}
