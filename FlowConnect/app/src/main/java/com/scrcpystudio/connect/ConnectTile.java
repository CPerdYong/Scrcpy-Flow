package com.scrcpystudio.connect;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

/** Quick Settings tile: one tap to switch Wireless debugging on or off. */
public class ConnectTile extends TileService {
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void onStartListening() {
        update(null);
    }

    @Override
    public void onClick() {
        if (!AdbWifi.hasPermission(this)) {
            openApp();
            return;
        }
        boolean on = AdbWifi.isOn(this);
        if (!on && !AdbWifi.onWifi(this)) {
            showMessage("No Wi-Fi");
            return;
        }
        boolean wanted = !on;
        if (AdbWifi.set(this, wanted)) Bridge.log(this, "Tile: Wireless debugging " + (wanted ? "on" : "off"));
        update(wanted);
        handler.postDelayed(() -> update(null), 1200);
    }

    private void update(Boolean forced) {
        Tile t = getQsTile();
        if (t == null) return;
        boolean perm = AdbWifi.hasPermission(this);
        boolean on = forced != null ? forced : AdbWifi.isOn(this);
        t.setLabel(getString(R.string.tile_label));
        t.setState(perm && on ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        String sub;
        if (!perm) {
            sub = "Set up";
        } else if (on) {
            String ip = AdbWifi.wifiIp(this);
            sub = ip != null ? ip : "On";
        } else {
            sub = "Off";
        }
        t.setSubtitle(sub);
        t.updateTile();
    }

    private void showMessage(String text) {
        Tile t = getQsTile();
        if (t == null) return;
        t.setSubtitle(text);
        t.updateTile();
        handler.postDelayed(() -> update(null), 2000);
    }

    private void openApp() {
        Intent i = new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 34) {
            PendingIntent pi = PendingIntent.getActivity(this, 0, i,
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            startActivityAndCollapse(pi);
        } else {
            startActivityAndCollapse(i);
        }
    }
}
