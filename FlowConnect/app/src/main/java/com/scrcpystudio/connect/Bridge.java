package com.scrcpystudio.connect;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Shared state between this app and Scrcpy Flow on the PC.
 * The PC reads requests and writes status through {@link BridgeProvider} over adb.
 */
final class Bridge {
    static final String AUTHORITY = "com.scrcpystudio.connect.bridge";
    static final Uri STATUS_URI = Uri.parse("content://" + AUTHORITY + "/status");
    static final String MODE_MIRROR = "mirror";
    static final String MODE_VIRTUAL = "virtual";

    private static final String PREFS = "studio_bridge";
    private static final String LOG_PREFS = "studio_log";
    private static final int MAX_LOG_LINES = 30;

    private Bridge() {
    }

    static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    /** Leave a request for Scrcpy Flow ("start" or "stop"). */
    static void request(Context c, String action, String mode) {
        long now = System.currentTimeMillis();
        prefs(c).edit()
                .putString("req_action", action)
                .putString("req_mode", mode)
                .putLong("req_id", now)
                .putString("state", "stop".equals(action) ? "stopping" : "waiting")
                .putString("state_mode", mode)
                .putLong("state_time", now)
                .commit();
        log(c, ("stop".equals(action) ? "Asked PC to stop" : "Asked PC to start: " + label(mode)));
        notifyChange(c);
    }

    static void setState(Context c, String state, String mode, String detail) {
        prefs(c).edit()
                .putString("state", state == null ? "idle" : state)
                .putString("state_mode", mode == null ? "" : mode)
                .putString("state_detail", detail == null ? "" : detail)
                .putLong("state_time", System.currentTimeMillis())
                .commit();
        notifyChange(c);
    }

    static String state(Context c) {
        return prefs(c).getString("state", "idle");
    }

    static String stateMode(Context c) {
        return prefs(c).getString("state_mode", "");
    }

    static String stateDetail(Context c) {
        return prefs(c).getString("state_detail", "");
    }

    static long stateAgeMs(Context c) {
        return System.currentTimeMillis() - prefs(c).getLong("state_time", 0);
    }

    static String preferredMode(Context c) {
        return prefs(c).getString("pref_mode", MODE_MIRROR);
    }

    static void setPreferredMode(Context c, String mode) {
        prefs(c).edit().putString("pref_mode", mode).apply();
    }

    static String label(String mode) {
        return MODE_VIRTUAL.equals(mode) ? "second screen" : "phone screen";
    }

    static void notifyChange(Context c) {
        try {
            c.getContentResolver().notifyChange(STATUS_URI, null);
        } catch (RuntimeException ignored) {
        }
    }

    // ------------------------------------------------------------------ activity log

    static void log(Context c, String message) {
        SharedPreferences p = c.getSharedPreferences(LOG_PREFS, Context.MODE_PRIVATE);
        String time = new SimpleDateFormat("HH:mm", Locale.US).format(new Date());
        String all = time + "  " + message + "\n" + p.getString("log", "");
        String[] lines = all.split("\n");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.length && i < MAX_LOG_LINES; i++) {
            if (lines[i].trim().isEmpty()) continue;
            sb.append(lines[i]).append('\n');
        }
        p.edit().putString("log", sb.toString()).apply();
    }

    static String readLog(Context c) {
        String s = c.getSharedPreferences(LOG_PREFS, Context.MODE_PRIVATE).getString("log", "");
        return s.isEmpty() ? "Nothing yet." : s.trim();
    }

    static SharedPreferences logPrefs(Context c) {
        return c.getSharedPreferences(LOG_PREFS, Context.MODE_PRIVATE);
    }
}
