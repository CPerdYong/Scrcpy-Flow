package com.scrcpystudio.connect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Called by Scrcpy Flow right after installing the app:
 *   adb shell am broadcast -a com.scrcpystudio.connect.SETUP -n com.scrcpystudio.connect/.SetupReceiver --ez keep_on true
 * Protected by android.permission.DUMP, so other apps cannot change these settings.
 */
public class SetupReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        if (intent.getBooleanExtra("keep_on", false) && AdbWifi.hasPermission(context)) {
            AdbWifi.setKeepOn(context, true);
            AdbWifi.ensureOn(context);
            Bridge.log(context, "Set up by Scrcpy Flow. Keep it on: ON");
        }
        Bridge.notifyChange(context);
    }
}
