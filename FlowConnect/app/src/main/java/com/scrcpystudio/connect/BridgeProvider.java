package com.scrcpystudio.connect;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

/**
 * Lets Scrcpy Flow talk to this app over adb, for example:
 *   adb shell content query  --uri content://com.scrcpystudio.connect.bridge/request
 *   adb shell content insert --uri content://com.scrcpystudio.connect.bridge/status --bind state:s:running
 * Protected by android.permission.DUMP in the manifest, so only adb (shell) and the
 * system can use it - other apps on the phone cannot.
 */
public class BridgeProvider extends ContentProvider {

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Context c = getContext();
        if (c == null) return null;
        String path = uri.getPath();
        if ("/request".equals(path)) {
            MatrixCursor cursor = new MatrixCursor(new String[]{"action", "mode", "id"});
            SharedPreferences p = Bridge.prefs(c);
            String action = p.getString("req_action", null);
            if (action != null) {
                cursor.addRow(new Object[]{action, p.getString("req_mode", Bridge.MODE_MIRROR), p.getLong("req_id", 0)});
                // Hand the request over once
                p.edit().remove("req_action").remove("req_mode").commit();
                Bridge.log(c, "Scrcpy Flow picked up the request");
            }
            return cursor;
        }
        if ("/status".equals(path)) {
            MatrixCursor cursor = new MatrixCursor(new String[]{"state", "mode", "detail", "app_version"});
            cursor.addRow(new Object[]{Bridge.state(c), Bridge.stateMode(c), Bridge.stateDetail(c), BuildInfo.VERSION});
            return cursor;
        }
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        applyStatus(uri, values);
        return uri;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return applyStatus(uri, values) ? 1 : 0;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Context c = getContext();
        if (c == null) return 0;
        if ("/request".equals(uri.getPath())) {
            Bridge.prefs(c).edit().remove("req_action").remove("req_mode").commit();
            return 1;
        }
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    private boolean applyStatus(Uri uri, ContentValues values) {
        Context c = getContext();
        if (c == null || values == null || !"/status".equals(uri.getPath())) return false;
        String state = values.getAsString("state");
        String mode = values.getAsString("mode");
        String detail = values.getAsString("detail");
        Bridge.setState(c, state, mode, detail);
        if ("running".equals(state)) {
            Bridge.log(c, "Mirroring on PC (" + Bridge.label(mode) + ")");
        } else if ("idle".equals(state)) {
            Bridge.log(c, "Mirroring stopped");
        } else if ("error".equals(state)) {
            Bridge.log(c, "PC could not start: " + (detail == null ? "" : detail));
        }
        return true;
    }
}
