package com.scrcpystudio.connect;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.StatusBarManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Flow Connect main screen:
 *  - big ON/OFF button for Wireless debugging
 *  - start mirroring on the PC (phone screen or second screen)
 *  - options, and a hidden Developer section (tap the version 7 times)
 */
public class MainActivity extends Activity {
    private static final int[] ON_COLORS = {UiKit.ACCENT, UiKit.PURPLE};
    private static final int[] OFF_COLORS = {0xFF2A3142, 0xFF232835};
    private static final float[] ANIM_SCALES = {0f, 0.5f, 1f, 2f};
    private static final String[] ANIM_LABELS = {"Off", "0.5x", "1x", "2x"};

    private final Handler handler = new Handler(Looper.getMainLooper());

    // Power button
    private FrameLayout powerButton;
    private GradientDrawable powerBg;
    private View ring1;
    private View ring2;
    private ObjectAnimator ringAnim1;
    private ObjectAnimator ringAnim2;
    private int[] powerColors = OFF_COLORS.clone();
    private ValueAnimator colorAnim;
    private TextView bigLabel;
    private TextView bigSub;
    private TextView status;
    private TextView detail;
    private String lastStatus = "";

    // Cards
    private LinearLayout setupCard;
    private LinearLayout mirrorCard;
    private LinearLayout optionsCard;
    private LinearLayout devCard;

    // Mirror card
    private FrameLayout segFrame;
    private View segHighlight;
    private TextView segMirror;
    private TextView segVirtual;
    private TextView modeDesc;
    private TextView startButton;
    private TextView mirrorStatus;
    private TextView mirrorBadge;
    private String mode = Bridge.MODE_MIRROR;
    private int dots;

    // Options
    private Switch keepSwitch;

    // Developer
    private TextView devMaster;
    private TextView devPill;
    private TextView devHint;
    private LinearLayout devBody;
    private Boolean lastDevOn;
    private Switch swUsb;
    private Switch swAwake;
    private final TextView[] chips = new TextView[4];
    private TextView devInfo;
    private TextView devLog;
    private TextView versionText;

    private boolean updating;

    private final ContentObserver observer = new ContentObserver(new Handler(Looper.getMainLooper())) {
        @Override
        public void onChange(boolean selfChange) {
            refresh();
        }
    };

    private final SharedPreferences.OnSharedPreferenceChangeListener prefsListener = (p, key) -> handler.post(this::refresh);

    private final Runnable dotsTicker = new Runnable() {
        @Override
        public void run() {
            dots = (dots + 1) % 4;
            refreshMirror(false);
            String st = Bridge.state(MainActivity.this);
            if ("waiting".equals(st) || "starting".equals(st) || "stopping".equals(st)) {
                handler.postDelayed(this, 450);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(UiKit.BG_TOP);
        getWindow().setNavigationBarColor(UiKit.BG);
        mode = Bridge.preferredMode(this);
        setContentView(buildUi());
        refresh();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getContentResolver().registerContentObserver(Settings.Global.getUriFor(AdbWifi.KEY), false, observer);
        getContentResolver().registerContentObserver(Settings.Global.getUriFor(Settings.Global.ADB_ENABLED), false, observer);
        getContentResolver().registerContentObserver(Settings.Global.getUriFor(Settings.Global.DEVELOPMENT_SETTINGS_ENABLED), false, observer);
        getContentResolver().registerContentObserver(Bridge.STATUS_URI, false, observer);
        AdbWifi.prefs(this).registerOnSharedPreferenceChangeListener(prefsListener);
        Bridge.logPrefs(this).registerOnSharedPreferenceChangeListener(prefsListener);
        refresh();
    }

    @Override
    protected void onPause() {
        super.onPause();
        getContentResolver().unregisterContentObserver(observer);
        AdbWifi.prefs(this).unregisterOnSharedPreferenceChangeListener(prefsListener);
        Bridge.logPrefs(this).unregisterOnSharedPreferenceChangeListener(prefsListener);
        handler.removeCallbacks(dotsTicker);
    }

    // ================================================================== UI

    private View buildUi() {
        FrameLayout rootFrame = new FrameLayout(this);
        rootFrame.setBackground(UiKit.background(this));

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setVerticalScrollBarEnabled(false);
        rootFrame.addView(scroll, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(26), dp(20), dp(30));
        scroll.addView(root);

        // Header
        TextView title = UiKit.text(this, "Flow Connect", 28, UiKit.TEXT, true);
        UiKit.gradientText(title, 0xFFFFFFFF, 0xFFB7A6FF);
        root.addView(title);
        TextView subtitle = UiKit.text(this, "Wireless link to Scrcpy Flow", 14, UiKit.SUB, false);
        subtitle.setPadding(0, dp(2), 0, dp(18));
        root.addView(subtitle);
        UiKit.fadeUp(title, 0);
        UiKit.fadeUp(subtitle, 60);

        // Power area
        FrameLayout powerArea = new FrameLayout(this);
        LinearLayout.LayoutParams pap = new LinearLayout.LayoutParams(dp(250), dp(250));
        pap.gravity = Gravity.CENTER_HORIZONTAL;
        powerArea.setLayoutParams(pap);
        powerArea.setClipChildren(false);
        ring1 = ring();
        ring2 = ring();
        powerArea.addView(ring1);
        powerArea.addView(ring2);

        powerBg = new GradientDrawable(GradientDrawable.Orientation.TL_BR, powerColors);
        powerBg.setShape(GradientDrawable.OVAL);
        powerButton = new FrameLayout(this);
        FrameLayout.LayoutParams pbp = new FrameLayout.LayoutParams(dp(190), dp(190));
        pbp.gravity = Gravity.CENTER;
        powerButton.setLayoutParams(pbp);
        powerButton.setBackground(UiKit.ripple(powerBg, dp(95)));
        powerButton.setElevation(dp(10));
        powerButton.setClickable(true);
        powerButton.setOnClickListener(v -> toggle());
        UiKit.pressEffect(powerButton, 0.93f);
        LinearLayout labels = new LinearLayout(this);
        labels.setOrientation(LinearLayout.VERTICAL);
        labels.setGravity(Gravity.CENTER);
        bigLabel = UiKit.text(this, "OFF", 42, Color.WHITE, true);
        bigLabel.setGravity(Gravity.CENTER);
        bigSub = UiKit.text(this, "Tap to turn on", 13, Color.WHITE, false);
        bigSub.setGravity(Gravity.CENTER);
        bigSub.setAlpha(0.85f);
        labels.addView(bigLabel);
        labels.addView(bigSub);
        powerButton.addView(labels, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        powerArea.addView(powerButton);
        root.addView(powerArea);
        powerButton.setScaleX(0.6f);
        powerButton.setScaleY(0.6f);
        powerButton.setAlpha(0f);
        powerButton.animate().scaleX(1f).scaleY(1f).alpha(1f).setStartDelay(120).setDuration(650)
                .setInterpolator(new OvershootInterpolator(1.6f)).start();

        status = UiKit.text(this, "", 21, UiKit.TEXT, true);
        status.setGravity(Gravity.CENTER);
        status.setPadding(0, dp(10), 0, dp(4));
        root.addView(status);
        detail = UiKit.text(this, "", 14, UiKit.SUB, false);
        detail.setGravity(Gravity.CENTER);
        detail.setPadding(dp(8), 0, dp(8), dp(8));
        root.addView(detail);

        setupCard = buildSetupCard();
        root.addView(setupCard);
        mirrorCard = buildMirrorCard();
        root.addView(mirrorCard);
        optionsCard = buildOptionsCard();
        root.addView(optionsCard);
        devCard = buildDevCard();
        root.addView(devCard);
        UiKit.fadeUp(setupCard, 220);
        UiKit.fadeUp(mirrorCard, 280);
        UiKit.fadeUp(optionsCard, 360);
        UiKit.fadeUp(devCard, 440);

        versionText = UiKit.text(this, "Version " + BuildInfo.VERSION, 12, 0xFF6B7380, false);
        versionText.setGravity(Gravity.CENTER);
        versionText.setPadding(0, dp(22), 0, dp(6));
        root.addView(versionText);
        TextView foot = UiKit.text(this, "Phone and PC must be on the same Wi-Fi, with Scrcpy Flow open on the PC.", 12, UiKit.SUB, false);
        foot.setGravity(Gravity.CENTER);
        foot.setPadding(dp(10), 0, dp(10), 0);
        root.addView(foot);
        return rootFrame;
    }

    private View ring() {
        View r = new View(this);
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.OVAL);
        d.setStroke(dp(3), 0xFF8B7CFF);
        d.setColor(0x00000000);
        r.setBackground(d);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(dp(190), dp(190));
        lp.gravity = Gravity.CENTER;
        r.setLayoutParams(lp);
        r.setAlpha(0f);
        return r;
    }

    private LinearLayout buildSetupCard() {
        LinearLayout c = card();
        c.addView(UiKit.text(this, "One-time setup", 17, UiKit.WARN, true));
        TextView steps = UiKit.text(this,
                "1. In Developer options, turn on Wireless debugging.\n"
                        + "2. In Scrcpy Flow on your PC: Devices > Show QR code, then scan it from "
                        + "Wireless debugging > Pair device with QR code.\n"
                        + "3. In Scrcpy Flow, click the download button on this phone.\n\n"
                        + "After that, this app can switch Wireless debugging on by itself.",
                14, UiKit.SUB, false);
        steps.setPadding(0, dp(8), 0, dp(14));
        c.addView(steps);
        TextView dev = primaryButton("Open Developer options");
        dev.setOnClickListener(v -> openDeveloperOptions());
        c.addView(dev);
        return c;
    }

    private LinearLayout buildMirrorCard() {
        LinearLayout c = card();
        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(Gravity.CENTER_VERTICAL);
        TextView t = UiKit.text(this, "Mirror to PC", 18, UiKit.TEXT, true);
        head.addView(t, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        mirrorBadge = UiKit.text(this, "", 12, UiKit.GOOD, true);
        mirrorBadge.setPadding(dp(10), dp(3), dp(10), dp(3));
        mirrorBadge.setBackground(UiKit.round(0x263DDC97, dp(10)));
        head.addView(mirrorBadge);
        c.addView(head);

        // Segmented choice: phone screen / second screen
        segFrame = new FrameLayout(this);
        segFrame.setBackground(UiKit.round(UiKit.CARD2, dp(16)));
        LinearLayout.LayoutParams sfp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(52));
        sfp.topMargin = dp(14);
        segFrame.setLayoutParams(sfp);
        segFrame.setPadding(dp(4), dp(4), dp(4), dp(4));
        segHighlight = new View(this);
        segHighlight.setBackground(UiKit.gradient(UiKit.ACCENT, UiKit.PURPLE, dp(13)));
        segHighlight.setElevation(dp(3));
        segFrame.addView(segHighlight, new FrameLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT));
        LinearLayout segRow = new LinearLayout(this);
        segRow.setOrientation(LinearLayout.HORIZONTAL);
        segMirror = segOption("Phone screen", Bridge.MODE_MIRROR);
        segVirtual = segOption("Second screen", Bridge.MODE_VIRTUAL);
        segRow.addView(segMirror, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));
        segRow.addView(segVirtual, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));
        segRow.setElevation(dp(4));
        segFrame.addView(segRow, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        segFrame.post(() -> selectMode(mode, false));
        c.addView(segFrame);

        modeDesc = UiKit.text(this, "", 13, UiKit.SUB, false);
        modeDesc.setPadding(dp(2), dp(10), dp(2), dp(14));
        c.addView(modeDesc);

        startButton = primaryButton("Start on PC");
        startButton.setOnClickListener(v -> startOrStop());
        c.addView(startButton);

        mirrorStatus = UiKit.text(this, "", 13, UiKit.SUB, false);
        mirrorStatus.setGravity(Gravity.CENTER);
        mirrorStatus.setPadding(0, dp(10), 0, 0);
        c.addView(mirrorStatus);
        return c;
    }

    private TextView segOption(String label, final String value) {
        TextView t = UiKit.text(this, label, 14, UiKit.SUB, true);
        t.setGravity(Gravity.CENTER);
        t.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
            selectMode(value, true);
        });
        return t;
    }

    private LinearLayout buildOptionsCard() {
        LinearLayout c = card();
        keepSwitch = styledSwitch("Keep it on automatically");
        keepSwitch.setOnCheckedChangeListener((b, checked) -> {
            if (updating) return;
            AdbWifi.setKeepOn(this, checked);
            if (checked) AdbWifi.ensureOn(this);
            refresh();
        });
        c.addView(keepSwitch);
        TextView hint = UiKit.text(this, "Turns Wireless debugging back on after the phone restarts or reconnects to Wi-Fi.", 13, UiKit.SUB, false);
        hint.setPadding(0, dp(2), 0, dp(14));
        c.addView(hint);
        TextView tile = secondaryButton("Add quick settings tile");
        tile.setOnClickListener(v -> addTile());
        c.addView(tile);
        return c;
    }

    private LinearLayout buildDevCard() {
        LinearLayout c = card();
        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(Gravity.CENTER_VERTICAL);
        TextView t = UiKit.text(this, "Developer mode", 18, UiKit.TEXT, true);
        UiKit.gradientText(t, 0xFFFFC56B, 0xFFFF7AB6);
        head.addView(t, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        devPill = UiKit.text(this, "", 12, UiKit.GOOD, true);
        devPill.setPadding(dp(10), dp(3), dp(10), dp(3));
        head.addView(devPill);
        c.addView(head);

        devHint = UiKit.text(this, "", 13, UiKit.SUB, false);
        devHint.setPadding(0, dp(6), 0, dp(12));
        c.addView(devHint);

        // Main button: turns Developer options (and debugging) on or off
        devMaster = primaryButton("");
        devMaster.setOnClickListener(v -> onDevMaster());
        c.addView(devMaster);

        // Everything below fades out while Developer mode is off
        devBody = new LinearLayout(this);
        devBody.setOrientation(LinearLayout.VERTICAL);
        devBody.setPadding(0, dp(10), 0, 0);
        c.addView(devBody);

        swUsb = styledSwitch("USB debugging");
        swUsb.setOnCheckedChangeListener((b, checked) -> onUsbSwitch(checked));
        devBody.addView(swUsb);
        swAwake = styledSwitch("Stay awake while charging");
        swAwake.setOnCheckedChangeListener((b, checked) -> {
            if (updating) return;
            AdbWifi.putInt(this, Settings.Global.STAY_ON_WHILE_PLUGGED_IN, checked ? 7 : 0);
            Bridge.log(this, "Stay awake while charging: " + (checked ? "ON" : "OFF"));
        });
        devBody.addView(swAwake);

        TextView animTitle = UiKit.text(this, "Phone animation speed", 15, UiKit.TEXT, false);
        animTitle.setPadding(0, dp(14), 0, dp(8));
        devBody.addView(animTitle);
        LinearLayout chipRow = new LinearLayout(this);
        chipRow.setOrientation(LinearLayout.HORIZONTAL);
        for (int i = 0; i < 4; i++) {
            final int idx = i;
            TextView chip = UiKit.text(this, ANIM_LABELS[i], 14, UiKit.SUB, true);
            chip.setGravity(Gravity.CENTER);
            chip.setPadding(0, dp(10), 0, dp(10));
            chip.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
                if (AdbWifi.setAnimationScale(this, ANIM_SCALES[idx])) {
                    Bridge.log(this, "Animation speed: " + ANIM_LABELS[idx]);
                }
                refreshDev();
            });
            UiKit.pressEffect(chip, 0.92f);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            if (i > 0) lp.leftMargin = dp(8);
            chipRow.addView(chip, lp);
            chips[i] = chip;
        }
        devBody.addView(chipRow);
        TextView animHint = UiKit.text(this, "0.5x makes the phone feel snappier, also while mirroring.", 12, UiKit.SUB, false);
        animHint.setPadding(0, dp(6), 0, dp(12));
        devBody.addView(animHint);

        devInfo = UiKit.text(this, "", 13, UiKit.TEXT, false);
        devInfo.setPadding(dp(14), dp(12), dp(14), dp(12));
        devInfo.setBackground(UiKit.round(UiKit.CARD2, dp(14)));
        devInfo.setLineSpacing(0, 1.25f);
        devBody.addView(devInfo);

        TextView logTitle = UiKit.text(this, "Log", 15, UiKit.TEXT, false);
        logTitle.setPadding(0, dp(14), 0, dp(6));
        devBody.addView(logTitle);
        devLog = UiKit.text(this, "", 12, 0xFFB9C0CA, false);
        devLog.setTypeface(Typeface.MONOSPACE);
        devLog.setPadding(dp(14), dp(12), dp(14), dp(12));
        devLog.setBackground(UiKit.round(0xFF0E1116, dp(14)));
        devLog.setTextIsSelectable(true);
        devBody.addView(devLog);
        return c;
    }

    // ================================================================== state

    private void refresh() {
        if (powerButton == null) return;
        boolean perm = AdbWifi.hasPermission(this);
        boolean on = AdbWifi.isOn(this);
        String ip = AdbWifi.wifiIp(this);

        setupCard.setVisibility(perm ? View.GONE : View.VISIBLE);
        mirrorCard.setVisibility(perm ? View.VISIBLE : View.GONE);
        optionsCard.setVisibility(perm ? View.VISIBLE : View.GONE);
        updating = true;
        keepSwitch.setChecked(AdbWifi.keepOn(this));
        updating = false;

        String st;
        String det;
        if (!perm) {
            bigLabel.setText(on ? "ON" : "OFF");
            bigSub.setText("Setup needed");
            st = "Almost there";
            det = "Finish the one-time setup below.";
        } else if (on && ip != null) {
            bigLabel.setText("ON");
            bigSub.setText("Tap to turn off");
            st = "Ready for your PC";
            det = "Phone IP " + ip;
        } else if (on) {
            bigLabel.setText("ON");
            bigSub.setText("Tap to turn off");
            st = "Connect to Wi-Fi";
            det = "Wireless debugging needs the same Wi-Fi as your PC.";
        } else {
            bigLabel.setText("OFF");
            bigSub.setText("Tap to turn on");
            st = "Wireless debugging is off";
            det = ip != null ? "Tap the button to let your PC connect." : "Connect to Wi-Fi first.";
        }
        status.setText(st);
        detail.setText(det);
        if (!st.equals(lastStatus)) {
            if (!lastStatus.isEmpty()) {
                UiKit.nudge(status);
                UiKit.nudge(detail);
            }
            lastStatus = st;
        }
        setPowerState(perm && on);
        refreshMirror(true);
        refreshDev();
    }

    private void setPowerState(boolean active) {
        final int[] target = active ? ON_COLORS : OFF_COLORS;
        if (target[0] != powerColors[0] || target[1] != powerColors[1]) {
            final int[] from = powerColors.clone();
            if (colorAnim != null) colorAnim.cancel();
            final ArgbEvaluator ev = new ArgbEvaluator();
            colorAnim = ValueAnimator.ofFloat(0f, 1f);
            colorAnim.setDuration(450);
            colorAnim.addUpdateListener(a -> {
                float f = (float) a.getAnimatedValue();
                powerColors = new int[]{(int) ev.evaluate(f, from[0], target[0]), (int) ev.evaluate(f, from[1], target[1])};
                powerBg.setColors(powerColors);
            });
            colorAnim.start();
        }
        if (active) startRings();
        else stopRings();
    }

    private void startRings() {
        if (ringAnim1 != null && ringAnim1.isStarted()) return;
        ringAnim1 = ringAnimator(ring1, 0);
        ringAnim2 = ringAnimator(ring2, 1100);
        ringAnim1.start();
        ringAnim2.start();
    }

    private ObjectAnimator ringAnimator(View ring, long delay) {
        ObjectAnimator a = ObjectAnimator.ofPropertyValuesHolder(ring,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.32f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.32f),
                PropertyValuesHolder.ofFloat(View.ALPHA, 0.6f, 0f));
        a.setDuration(2200);
        a.setStartDelay(delay);
        a.setRepeatCount(ValueAnimator.INFINITE);
        a.setInterpolator(new DecelerateInterpolator(1.4f));
        return a;
    }

    private void stopRings() {
        if (ringAnim1 != null) ringAnim1.cancel();
        if (ringAnim2 != null) ringAnim2.cancel();
        ring1.animate().alpha(0f).setDuration(250).start();
        ring2.animate().alpha(0f).setDuration(250).start();
    }

    private void refreshMirror(boolean restartTicker) {
        if (startButton == null) return;
        String st = Bridge.state(this);
        long age = Bridge.stateAgeMs(this);
        String m = Bridge.stateMode(this);
        String dotStr = new String(new char[dots]).replace('\0', '.');
        boolean busy = "waiting".equals(st) || "starting".equals(st) || "stopping".equals(st);

        if ("running".equals(st)) {
            mirrorBadge.setVisibility(View.VISIBLE);
            mirrorBadge.setText("LIVE  " + Bridge.label(m).toUpperCase());
            startButton.setText("Stop mirroring");
            startButton.setBackground(UiKit.ripple(UiKit.gradient(0xFFFF5C6C, 0xFFE0457A, dp(16)), dp(16)));
            mirrorStatus.setText("Your " + Bridge.label(m) + " is showing on the PC.");
        } else {
            mirrorBadge.setVisibility(View.GONE);
            startButton.setText("Start on PC");
            startButton.setBackground(UiKit.ripple(UiKit.gradient(UiKit.ACCENT, UiKit.PURPLE, dp(16)), dp(16)));
            if ("waiting".equals(st) && age > 25000) {
                mirrorStatus.setText("No answer from the PC. Is Scrcpy Flow open, and is this phone saved there?");
            } else if ("waiting".equals(st)) {
                mirrorStatus.setText("Waiting for Scrcpy Flow" + dotStr);
            } else if ("starting".equals(st)) {
                mirrorStatus.setText("Opening on the PC" + dotStr);
            } else if ("stopping".equals(st)) {
                mirrorStatus.setText("Stopping" + dotStr);
            } else if ("error".equals(st)) {
                String d = Bridge.stateDetail(this);
                mirrorStatus.setText("The PC could not start" + (d.isEmpty() ? "." : ": " + d));
            } else {
                mirrorStatus.setText("Scrcpy Flow must be open on your PC.");
            }
        }
        startButton.setEnabled(!busy || age > 25000);
        startButton.setAlpha(startButton.isEnabled() ? 1f : 0.6f);
        if (restartTicker && busy && age <= 25000) {
            handler.removeCallbacks(dotsTicker);
            handler.postDelayed(dotsTicker, 450);
        }
    }

    private void selectMode(String m, boolean animate) {
        mode = Bridge.MODE_VIRTUAL.equals(m) ? Bridge.MODE_VIRTUAL : Bridge.MODE_MIRROR;
        Bridge.setPreferredMode(this, mode);
        int inner = segFrame.getWidth() - segFrame.getPaddingLeft() - segFrame.getPaddingRight();
        int half = Math.max(0, inner / 2);
        ViewGroup.LayoutParams lp = segHighlight.getLayoutParams();
        if (lp.width != half) {
            lp.width = half;
            segHighlight.setLayoutParams(lp);
        }
        float x = Bridge.MODE_VIRTUAL.equals(mode) ? half : 0;
        if (animate) {
            segHighlight.animate().translationX(x).setDuration(340).setInterpolator(new OvershootInterpolator(1.2f)).start();
        } else {
            segHighlight.setTranslationX(x);
        }
        segMirror.setTextColor(Bridge.MODE_MIRROR.equals(mode) ? Color.WHITE : UiKit.SUB);
        segVirtual.setTextColor(Bridge.MODE_VIRTUAL.equals(mode) ? Color.WHITE : UiKit.SUB);
        modeDesc.setText(Bridge.MODE_VIRTUAL.equals(mode)
                ? "Opens a separate screen on the PC. Your phone screen stays free (best on Android 13+)."
                : "Shows and controls your phone screen on the PC.");
        if (animate) UiKit.nudge(modeDesc);
    }

    private void refreshDev() {
        if (devCard == null) return;
        boolean perm = AdbWifi.hasPermission(this);
        boolean devOn = AdbWifi.devOptionsOn(this);
        devCard.setVisibility(perm ? View.VISIBLE : View.GONE);

        devPill.setText(devOn ? "ON" : "OFF");
        devPill.setTextColor(devOn ? UiKit.GOOD : UiKit.SUB);
        devPill.setBackground(UiKit.round(devOn ? 0x263DDC97 : 0x269AA3AF, dp(10)));
        devHint.setText(devOn
                ? "Developer options and debugging are on. Turning them off also disconnects the PC."
                : "Developer options are off, so the PC cannot connect. Tap the button to turn them back on.");
        devMaster.setText(devOn ? "Turn off Developer mode" : "Turn on Developer mode");
        devMaster.setBackground(devOn
                ? UiKit.ripple(outlined(), dp(16))
                : UiKit.ripple(UiKit.gradient(UiKit.ACCENT, UiKit.PURPLE, dp(16)), dp(16)));
        devMaster.setTextColor(devOn ? UiKit.TEXT : Color.WHITE);

        // Fade the details while Developer mode is off (they stay visible, just dimmed)
        if (lastDevOn == null || lastDevOn != devOn) {
            devBody.animate().alpha(devOn ? 1f : 0.35f).setDuration(lastDevOn == null ? 0 : 350).setStartDelay(0).start();
            setEnabledDeep(devBody, devOn);
            if (lastDevOn != null) {
                devMaster.setScaleX(0.94f);
                devMaster.setScaleY(0.94f);
                devMaster.animate().scaleX(1f).scaleY(1f).setDuration(380).setStartDelay(0)
                        .setInterpolator(new OvershootInterpolator(3f)).start();
                UiKit.nudge(devHint);
            }
            lastDevOn = devOn;
        }

        updating = true;
        swUsb.setChecked(AdbWifi.usbDebuggingOn(this));
        swAwake.setChecked(AdbWifi.stayAwakeOn(this));
        updating = false;
        float scale = AdbWifi.animationScale(this);
        for (int i = 0; i < 4; i++) {
            boolean sel = Math.abs(ANIM_SCALES[i] - scale) < 0.01f;
            chips[i].setTextColor(sel ? Color.WHITE : UiKit.SUB);
            chips[i].setBackground(sel
                    ? UiKit.gradient(UiKit.ACCENT, UiKit.PURPLE, dp(12))
                    : UiKit.round(UiKit.CARD2, dp(12)));
        }
        String ip = AdbWifi.wifiIp(this);
        devInfo.setText("Phone IP: " + (ip == null ? "not on Wi-Fi" : ip)
                + "\nDeveloper options: " + (devOn ? "on" : "off")
                + "\nWireless debugging: " + (AdbWifi.isOn(this) ? "on" : "off")
                + "\nKeep it on: " + (AdbWifi.keepOn(this) ? "on" : "off")
                + "\nPC link: " + Bridge.state(this)
                + "\nAndroid " + Build.VERSION.RELEASE + "  (API " + Build.VERSION.SDK_INT + ")"
                + "\nApp version " + BuildInfo.VERSION);
        devLog.setText(Bridge.readLog(this));
    }

    private GradientDrawable outlined() {
        GradientDrawable bg = UiKit.round(UiKit.CARD2, dp(16));
        bg.setStroke(dp(1), UiKit.LINE);
        return bg;
    }

    private static void setEnabledDeep(View v, boolean enabled) {
        v.setEnabled(enabled);
        if (v instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) v;
            for (int i = 0; i < g.getChildCount(); i++) setEnabledDeep(g.getChildAt(i), enabled);
        }
    }

    // ================================================================== actions

    private void toggle() {
        powerButton.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
        if (!AdbWifi.hasPermission(this)) {
            Toast.makeText(this, "Finish the one-time setup with Scrcpy Flow first.", Toast.LENGTH_LONG).show();
            return;
        }
        boolean on = AdbWifi.isOn(this);
        if (!on && !AdbWifi.onWifi(this)) {
            Toast.makeText(this, "Connect to Wi-Fi first.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!AdbWifi.set(this, !on)) {
            Toast.makeText(this, "Android did not allow the change. Try the setup again from Scrcpy Flow.", Toast.LENGTH_LONG).show();
        } else {
            Bridge.log(this, "Wireless debugging turned " + (on ? "off" : "on"));
        }
        handler.postDelayed(this::refresh, 500);
        handler.postDelayed(this::refresh, 1800);
    }

    private void startOrStop() {
        startButton.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
        if ("running".equals(Bridge.state(this))) {
            Bridge.request(this, "stop", Bridge.stateMode(this));
            refresh();
            return;
        }
        if (!AdbWifi.onWifi(this)) {
            Toast.makeText(this, "Connect to the same Wi-Fi as your PC first.", Toast.LENGTH_LONG).show();
            return;
        }
        if (!AdbWifi.devOptionsOn(this) || !AdbWifi.usbDebuggingOn(this)) {
            AdbWifi.putInt(this, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 1);
            AdbWifi.putInt(this, Settings.Global.ADB_ENABLED, 1);
            Bridge.log(this, "Developer mode turned on for mirroring");
        }
        if (!AdbWifi.isOn(this)) {
            AdbWifi.set(this, true);
            Bridge.log(this, "Wireless debugging turned on for mirroring");
        }
        Bridge.request(this, "start", mode);
        refresh();
    }

    /** The big Developer mode button: on = Developer options + USB debugging + Wireless debugging. */
    private void onDevMaster() {
        devMaster.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
        if (!AdbWifi.devOptionsOn(this)) {
            boolean ok = AdbWifi.putInt(this, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 1);
            ok &= AdbWifi.putInt(this, Settings.Global.ADB_ENABLED, 1);
            if (AdbWifi.onWifi(this)) AdbWifi.set(this, true);
            if (ok) {
                Bridge.log(this, "Developer mode ON (debugging on)");
                Toast.makeText(this, "Developer mode is on. Your PC can connect again.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Android did not allow it. Set the app up again from Scrcpy Flow.", Toast.LENGTH_LONG).show();
            }
            refresh();
            return;
        }
        new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                .setTitle("Turn off Developer mode?")
                .setMessage("This turns off Developer options, USB debugging and Wireless debugging, so Scrcpy Flow "
                        + "loses the connection. Come back here and tap Turn on Developer mode whenever you need it again.")
                .setPositiveButton("Turn off", (d, w) -> {
                    AdbWifi.set(this, false);
                    AdbWifi.putInt(this, Settings.Global.ADB_ENABLED, 0);
                    AdbWifi.putInt(this, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0);
                    Bridge.log(this, "Developer mode OFF");
                    refresh();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void onUsbSwitch(final boolean checked) {
        if (updating) return;
        if (checked) {
            AdbWifi.putInt(this, Settings.Global.ADB_ENABLED, 1);
            Bridge.log(this, "USB debugging: ON");
            refreshDev();
            return;
        }
        new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                .setTitle("Turn off USB debugging?")
                .setMessage("Scrcpy Flow will lose the connection to this phone.")
                .setPositiveButton("Turn off", (d, w) -> {
                    AdbWifi.putInt(this, Settings.Global.ADB_ENABLED, 0);
                    Bridge.log(this, "USB debugging: OFF");
                    refresh();
                })
                .setNegativeButton("Cancel", (d, w) -> refreshDev())
                .setOnCancelListener(d -> refreshDev())
                .show();
    }

    private void addTile() {
        if (Build.VERSION.SDK_INT >= 33) {
            StatusBarManager sbm = getSystemService(StatusBarManager.class);
            if (sbm != null) {
                sbm.requestAddTileService(
                        new ComponentName(this, ConnectTile.class),
                        getString(R.string.tile_label),
                        Icon.createWithResource(this, R.drawable.ic_tile),
                        getMainExecutor(),
                        result -> {
                            if (result == StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ALREADY_ADDED) {
                                Toast.makeText(this, "The tile is already in your quick settings.", Toast.LENGTH_SHORT).show();
                            }
                        });
                return;
            }
        }
        Toast.makeText(this, "Pull down quick settings, tap the pencil (edit) and drag \"PC connect\" into place.", Toast.LENGTH_LONG).show();
    }

    private void openDeveloperOptions() {
        try {
            startActivity(new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS));
        } catch (RuntimeException e) {
            Toast.makeText(this, "Settings > About phone: tap Build number (or OS version) 7 times first.", Toast.LENGTH_LONG).show();
        }
    }

    // ================================================================== helpers

    private LinearLayout card() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(18), dp(16), dp(18), dp(18));
        c.setBackground(UiKit.card(this));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(14);
        c.setLayoutParams(lp);
        return c;
    }

    private TextView primaryButton(String label) {
        TextView b = UiKit.text(this, label, 16, Color.WHITE, true);
        b.setGravity(Gravity.CENTER);
        b.setPadding(dp(16), dp(15), dp(16), dp(15));
        b.setBackground(UiKit.ripple(UiKit.gradient(UiKit.ACCENT, UiKit.PURPLE, dp(16)), dp(16)));
        b.setElevation(dp(4));
        b.setClickable(true);
        UiKit.pressEffect(b, 0.96f);
        return b;
    }

    private TextView secondaryButton(String label) {
        TextView b = UiKit.text(this, label, 15, UiKit.TEXT, true);
        b.setGravity(Gravity.CENTER);
        b.setPadding(dp(16), dp(13), dp(16), dp(13));
        GradientDrawable bg = UiKit.round(UiKit.CARD2, dp(16));
        bg.setStroke(dp(1), UiKit.LINE);
        b.setBackground(UiKit.ripple(bg, dp(16)));
        b.setClickable(true);
        UiKit.pressEffect(b, 0.96f);
        return b;
    }

    private Switch styledSwitch(String label) {
        Switch s = new Switch(this);
        s.setText(label);
        s.setTextColor(UiKit.TEXT);
        s.setTextSize(16);
        s.setThumbTintList(UiKit.switchThumb());
        s.setTrackTintList(UiKit.switchTrack());
        s.setPadding(0, dp(8), 0, dp(8));
        return s;
    }

    private int dp(float v) {
        return UiKit.dp(this, v);
    }
}
