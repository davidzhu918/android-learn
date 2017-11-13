package com.bignerdranch.android.photogallery.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.bignerdranch.android.photogallery.PhotoGalleryActivity;
import com.bignerdranch.android.photogallery.QueryPreferences;
import com.bignerdranch.android.photogallery.R;
import com.bignerdranch.android.photogallery.net.FlickrFetchr;
import com.bignerdranch.android.photogallery.net.Gallery.GalleryItem;

import java.util.List;

import static android.app.job.JobInfo.getMinFlexMillis;
import static android.app.job.JobInfo.getMinPeriodMillis;

/**
 * Created by zixiangzhu on 11/5/17.
 */
@TargetApi(21)
public class JobPollService extends JobService implements PollService {
    private final int JOB_ID = 1;
    private PollTask mCurrentTask;

    @Override
    public void setServiceJob(Context context, boolean isOn) {
        JobScheduler scheduler = (JobScheduler)
                context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (!isOn) {
            scheduler.cancel(JOB_ID);
            Log.d(TAG, "Job cancelled");
            return;
        }

        Log.d(TAG, "min interval: " + getMinPeriodMillis ());
        Log.d(TAG, "min flex interval: " + getMinFlexMillis ());
        JobInfo jobInfo = new JobInfo.Builder(
                JOB_ID, new ComponentName(context, JobPollService.class))
                .setPeriodic(POLL_INTERVAL_MS)
                .setPersisted(true)
                .build();
        scheduler.schedule(jobInfo);
        Log.d(TAG, "Job scheduled");

        QueryPreferences.setAlarmOn(context, isOn);
    }

    @Override
    public boolean isServiceOn(Context context) {
        boolean hasBeenScheduled = false;
        JobScheduler scheduler = (JobScheduler)
                context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        for (JobInfo jobInfo : scheduler.getAllPendingJobs()) {
            if (jobInfo.getId() == JOB_ID) {
                hasBeenScheduled = true;
            }
        }
        return hasBeenScheduled;
    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        mCurrentTask = new PollTask();
        mCurrentTask.execute(jobParameters);
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        if (mCurrentTask != null) {
            mCurrentTask.cancel(true);
        }
        return true;
    }

    private class PollTask extends AsyncTask<JobParameters, Void, Void> {
        @Override
        protected Void doInBackground(JobParameters... params) {
            JobParameters jobParameters = params[0];

            Context context = getApplicationContext();
            String query = QueryPreferences.getStoredQuery(context);
            String lastResultId = QueryPreferences.getLastResultId(context);
            List<GalleryItem> items;

            if (query == null) {
                items = new FlickrFetchr().fetchRecentPhotos(1);
            } else {
                items = new FlickrFetchr().searchPhotos(query, 1);
            }

            if (items.size() != 0) {
                String resultId = items.get(0).getId();
                if (resultId.equals(lastResultId)) {
                    Log.i(TAG, "Got an old result: " + resultId);
                } else {
                    Log.i(TAG, "Got a new result: " + resultId);

                    Resources resources = getResources();
                    Intent i = PhotoGalleryActivity.newIntent(context);
                    PendingIntent pi = PendingIntent.getActivity(context, 0, i, 0);

                    Notification notification = new NotificationCompat.Builder(context)
                            .setTicker(resources.getString(R.string.new_pictures_title))
                            .setSmallIcon(android.R.drawable.ic_menu_report_image)
                            .setContentTitle(resources.getString(R.string.new_pictures_title))
                            .setContentText(resources.getString(R.string.new_pictures_text))
                            .setContentIntent(pi)
                            .setAutoCancel(true)
                            .build();

                    NotificationManagerCompat notificationManagerCompat =
                            NotificationManagerCompat.from(context);
                    notificationManagerCompat.notify(0, notification);
                }

                QueryPreferences.setLastResultId(context, resultId);
            }

            jobFinished(jobParameters, false);
            return null;
        }
    }
}
