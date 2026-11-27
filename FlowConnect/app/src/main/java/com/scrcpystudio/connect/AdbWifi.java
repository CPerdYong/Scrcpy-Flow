package com.scrcpystudio.connect;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.provider.Settings;
import android.service.quicksettings.TileService;

import java.net.Inet4Address;

/** Small helper around the "Wireless debugging" system setting. */
final class AdbWifi {
    /** Settings.Global key behind Developer options > Wireless debugging (Android 11+). */
    static final String KEY = "adb_wifi_enabled";
    private static final String PREFS = "studio_connect";
    private static final String PREF_KEEP_ON = "keep_on";

    private AdbWifi() {
    }

    /** True once Scrcpy Flow has granted WRITE_SECURE_SETTINGS over adb. */
    static boolean hasPermission(Context c) {
        return c.checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS)
                == PackageManager.PERMISSION_GRANTED;
    }

    static boolean isOn(Context c) {
        return Settings.Global.getInt(c.getContentResolver(), KEY, 0) == 1;
    }

    /** Turns Wireless debugging on or off. Returns false if Android refused. */
    static boolean set(Context c, boolean on) {
        try {
            ContentResolver cr = c.getContentResolver();
            if (on) {
                // Wireless debugging only works while Developer options are enabled
                Settings.Global.putInt(cr, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 1);
            }
            boolean ok = Settings.Global.putInt(cr, KEY, on ? 1 : 0);
            refreshTile(c);
            return ok;
        } catch (SecurityException e) {
            return false;
        }
    }

    static boolean onWifi(Context c) {
        ConnectivityManager cm = c.getSystemService(ConnectivityManager.class);
        if (cm == null) return false;
        Network n = cm.getActiveNetwork();
        if (n == null) return false;
        NetworkCapabilities nc = cm.getNetworkCapabilities(n);
        return nc != null && nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
    }

    /** The phone's Wi-Fi IPv4 address, or null when not on Wi-Fi. */
    static String wifiIp(Context c) {
        ConnectivityManager cm = c.getSystemService(ConnectivityManager.class);
        if (cm == null) return null;
        Network n = cm.getActiveNetwork();
        if (n == null) return null;
        NetworkCapabilities nc = cm.getNetworkCapabilities(n);
        if (nc == null || !nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return null;
        LinkProperties lp = cm.getLinkProperties(n);
        if (lp == null) return null;
        for (LinkAddress la : lp.getLinkAddresses()) {
            if (la.getAddress() instanceof Inet4Address) {
                return la.getAddress().getHostAddress();
            }
        }
        return null;
    }

    static boolean keepOn(Context c) {
        return prefs(c).getBoolean(PREF_KEEP_ON, false);
    }

    static void setKeepOn(Context c, boolean value) {
        prefs(c).edit().putBoolean(PREF_KEEP_ON, value).apply();
        Bridge.log(c, "Keep it on automatically: " + (value ? "ON" : "OFF"));
        if (value) {
            KeepOnJob.schedule(c);
        } else {
            KeepOnJob.cancel(c);
        }
    }

    /** Turn Wireless debugging back on if the user asked us to keep it on. */
    static void ensureOn(Context c) {
        // Respect the user's choice when Developer mode was turned off on purpose
        if (keepOn(c) && hasPermission(c) && devOptionsOn(c) && onWifi(c) && !isOn(c)) {
            if (set(c, true)) Bridge.log(c, "Turned Wireless debugging back on");
        }
    }

    // ------------------------------------------------------------------ developer settings

    static int getInt(Context c, String key, int def) {
        return Settings.Global.getInt(c.getContentResolver(), key, def);
    }

    static boolean putInt(Context c, String key, int value) {
        try {
            return Settings.Global.putInt(c.getContentResolver(), key, value);
        } catch (SecurityException e) {
            return false;
        }
    }

    static float getFloat(Context c, String key, float def) {
        return Settings.Global.getFloat(c.getContentResolver(), key, def);
    }

    static boolean putFloat(Context c, String key, float value) {
        try {
            return Settings.Global.putFloat(c.getContentResolver(), key, value);
        } catch (SecurityException e) {
            return false;
        }
    }

    static boolean devOptionsOn(Context c) {
        return getInt(c, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) == 1;
    }

    static boolean usbDebuggingOn(Context c) {
        return getInt(c, Settings.Global.ADB_ENABLED, 0) == 1;
    }

    static boolean stayAwakeOn(Context c) {
        return getInt(c, Settings.Global.STAY_ON_WHILE_PLUGGED_IN, 0) != 0;
    }

    /** Animation speed used by Android (1 = normal, 0 = off). */
    static float animationScale(Context c) {
        return getFloat(c, Settings.Global.ANIMATOR_DURATION_SCALE, 1f);
    }

    static boolean setAnimationScale(Context c, float scale) {
        boolean ok = putFloat(c, Settings.Global.WINDOW_ANIMATION_SCALE, scale);
        ok &= putFloat(c, Settings.Global.TRANSITION_ANIMATION_SCALE, scale);
        ok &= putFloat(c, Settings.Global.ANIMATOR_DURATION_SCALE, scale);
        return ok;
    }

    static void refreshTile(Context c) {
        try {
            TileService.requestListeningState(c, new ComponentName(c, ConnectTile.class));
        } catch (RuntimeException ignored) {
            // The tile may not be added yet
        }
    }

    static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
