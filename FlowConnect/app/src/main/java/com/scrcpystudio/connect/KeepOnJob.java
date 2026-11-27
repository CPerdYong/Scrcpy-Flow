package com.scrcpystudio.connect;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;

/**
 * Background check that switches Wireless debugging back on after a restart
 * or when the phone reconnects to Wi-Fi ("Keep it on" option).
 */
public class KeepOnJob extends JobService {
    private static final int PERIODIC_ID = 4101;
    private static final int ON_WIFI_ID = 4102;

    static void schedule(Context c) {
        JobScheduler js = c.getSystemService(JobScheduler.class);
        if (js == null) return;
        ComponentName cn = new ComponentName(c, KeepOnJob.class);
        // Runs as soon as Wi-Fi is available (for example right after a restart)
        js.schedule(new JobInfo.Builder(ON_WIFI_ID, cn)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                .build());
        // Then checks again every 15 minutes
        js.schedule(new JobInfo.Builder(PERIODIC_ID, cn)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(15 * 60 * 1000L)
                .setPersisted(true)
                .build());
    }

    static void cancel(Context c) {
        JobScheduler js = c.getSystemService(JobScheduler.class);
        if (js == null) return;
        js.cancel(PERIODIC_ID);
        js.cancel(ON_WIFI_ID);
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        AdbWifi.ensureOn(this);
        AdbWifi.refreshTile(this);
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
