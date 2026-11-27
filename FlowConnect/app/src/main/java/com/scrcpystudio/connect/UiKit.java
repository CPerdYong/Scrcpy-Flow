package com.scrcpystudio.connect;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

/** Colours, drawables and small animations shared by the app's screens. */
final class UiKit {
    static final int BG = 0xFF0F1115;
    static final int BG_TOP = 0xFF151A2E;
    static final int CARD = 0xF0181C23;
    static final int CARD2 = 0xFF1F2430;
    static final int LINE = 0xFF2A303B;
    static final int TEXT = 0xFFE8EAED;
    static final int SUB = 0xFF9AA3AF;
    static final int ACCENT = 0xFF5B8CFF;
    static final int PURPLE = 0xFF8B5CFF;
    /** Button fills: deep enough that white labels pass 4.5:1 contrast. */
    static final int FILL_A = 0xFF3F6BE6;
    static final int FILL_B = 0xFF7A4FF0;
    static final int GOOD = 0xFF3DDC97;
    static final int WARN = 0xFFFFB547;
    static final int BAD = 0xFFFF5C6C;

    private UiKit() {
    }

    static int dp(Context c, float v) {
        return Math.round(v * c.getResources().getDisplayMetrics().density);
    }

    static GradientDrawable round(int color, float radiusPx) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(radiusPx);
        return d;
    }

    static GradientDrawable gradient(int c1, int c2, float radiusPx) {
        GradientDrawable d = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{c1, c2});
        d.setCornerRadius(radiusPx);
        return d;
    }

    static GradientDrawable card(Context c) {
        GradientDrawable d = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{0xF21C212B, 0xF2161A21});
        d.setCornerRadius(dp(c, 22));
        d.setStroke(dp(c, 1), 0xFF2C3341);
        return d;
    }

    static Drawable ripple(Drawable content, float radiusPx) {
        return new RippleDrawable(ColorStateList.valueOf(0x40FFFFFF), content, round(0xFFFFFFFF, radiusPx));
    }

    /** Dark page background with two soft colour glows. */
    static Drawable background(Context c) {
        GradientDrawable base = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{BG_TOP, BG, BG});
        GradientDrawable glow1 = new GradientDrawable();
        glow1.setGradientType(GradientDrawable.RADIAL_GRADIENT);
        glow1.setColors(new int[]{0x408B5CFF, 0x008B5CFF});
        glow1.setGradientRadius(dp(c, 340));
        glow1.setGradientCenter(1.0f, 0.0f);
        GradientDrawable glow2 = new GradientDrawable();
        glow2.setGradientType(GradientDrawable.RADIAL_GRADIENT);
        glow2.setColors(new int[]{0x305B8CFF, 0x005B8CFF});
        glow2.setGradientRadius(dp(c, 300));
        glow2.setGradientCenter(0.0f, 0.75f);
        return new LayerDrawable(new Drawable[]{base, glow1, glow2});
    }

    static TextView text(Context c, String s, float sp, int color, boolean bold) {
        TextView t = new TextView(c);
        t.setText(s);
        t.setTextSize(sp);
        t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        return t;
    }

    /** Fade and slide a view up into place. */
    static void fadeUp(View v, long delayMs) {
        v.setAlpha(0f);
        v.setTranslationY(dp(v.getContext(), 12));
        v.animate().alpha(1f).translationY(0f).setStartDelay(delayMs).setDuration(260)
                .setInterpolator(new DecelerateInterpolator(2f)).start();
    }

    /** Small fade/slide used when a text changes. */
    static void nudge(View v) {
        v.setAlpha(0.4f);
        v.setTranslationY(dp(v.getContext(), 4));
        v.animate().alpha(1f).translationY(0f).setStartDelay(0).setDuration(200)
                .setInterpolator(new DecelerateInterpolator(2f)).start();
    }

    /** Squish on touch, spring back on release. Clicks still go through. */
    static void pressEffect(View v, final float scale) {
        v.setOnTouchListener((view, ev) -> {
            int a = ev.getActionMasked();
            if (a == MotionEvent.ACTION_DOWN) {
                view.animate().scaleX(scale).scaleY(scale).setDuration(80).setStartDelay(0)
                        .setInterpolator(new DecelerateInterpolator()).start();
            } else if (a == MotionEvent.ACTION_UP || a == MotionEvent.ACTION_CANCEL) {
                view.animate().scaleX(1f).scaleY(1f).setDuration(160).setStartDelay(0)
                        .setInterpolator(new DecelerateInterpolator(1.5f)).start();
            }
            return false;
        });
    }

    static ColorStateList switchThumb() {
        return new ColorStateList(
                new int[][]{new int[]{android.R.attr.state_checked}, new int[]{}},
                new int[]{0xFFFFFFFF, 0xFFB4BBC6});
    }

    static ColorStateList switchTrack() {
        return new ColorStateList(
                new int[][]{new int[]{android.R.attr.state_checked}, new int[]{}},
                new int[]{0xFF7A6CFF, 0xFF3A4150});
    }
}
