// SPDX-License-Identifier: GPL-3.0-only

package com.best.deskclock.utils;

import static com.best.deskclock.DeskClockApplication.getDefaultSharedPreferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextClock;
import android.widget.TextView;

import com.best.deskclock.AnalogClock;
import com.best.deskclock.R;
import com.best.deskclock.data.DataModel;
import com.best.deskclock.data.SettingsDAO;

public class ScreensaverUtils {

    /**
     * For screensavers to set whether the digital or analog clock should be displayed.
     * Returns the view to be displayed.
     *
     * @param digitalClock if the view concerned is the digital clock
     * @param analogClock  if the view concerned is the analog clock
     */
    public static void setScreensaverClockStyle(Context context, View digitalClock, View analogClock) {
        final DataModel.ClockStyle screensaverClockStyle = SettingsDAO.getScreensaverClockStyle(getDefaultSharedPreferences(context));
        switch (screensaverClockStyle) {
            case ANALOG -> {
                final boolean isTablet = ThemeUtils.isTablet();
                analogClock.getLayoutParams().height = ThemeUtils.convertDpToPixels(isTablet ? 300 : 220, context);
                analogClock.getLayoutParams().width = ThemeUtils.convertDpToPixels(isTablet ? 300 : 220, context);
                digitalClock.setVisibility(View.GONE);
                analogClock.setVisibility(View.VISIBLE);
                return;
            }
            case DIGITAL -> {
                digitalClock.setVisibility(View.VISIBLE);
                analogClock.setVisibility(View.GONE);
                return;
            }
        }

        throw new IllegalStateException("unexpected clock style: " + screensaverClockStyle);
    }

    /**
     * For screensaver, dim the color.
     */
    public static void dimScreensaverView(Context context, View view, int color) {
        String colorFilter = getScreensaverColorFilter(context, color);
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);

        paint.setColorFilter(new PorterDuffColorFilter(Color.parseColor(colorFilter), PorterDuff.Mode.SRC_IN));

        view.setLayerType(View.LAYER_TYPE_HARDWARE, paint);
    }

    /**
     * For screensaver, calculate the color filter to use to dim the color.
     *
     * @param color the color selected in the screensaver color picker
     */
    public static String getScreensaverColorFilter(Context context, int color) {
        final SharedPreferences prefs = getDefaultSharedPreferences(context);
        final int brightnessPercentage = SettingsDAO.getScreensaverBrightness(prefs);

        if (SettingsDAO.areScreensaverClockDynamicColors(prefs)) {
            color = context.getColor(R.color.md_theme_inversePrimary);
        }

        String colorFilter = String.format("%06X", 0xFFFFFF & color);
        // The alpha channel should range from 16 (10 hex) to 192 (C0 hex).
        String alpha = String.format("%02X", 16 + (192 * brightnessPercentage / 100));

        colorFilter = "#" + alpha + colorFilter;

        return colorFilter;
    }

    /**
     * For screensaver, configure the clock that is visible to display seconds. The clock that is not visible never
     * displays seconds to avoid it scheduling unnecessary ticking runnable.
     *
     * @param digitalClock if the view concerned is the digital clock
     * @param analogClock  if the view concerned is the analog clock
     */
    public static void setScreensaverClockSecondsEnabled(Context context, TextClock digitalClock, AnalogClock analogClock) {
        final SharedPreferences prefs = getDefaultSharedPreferences(context);
        final boolean areScreensaverClockSecondsDisplayed = SettingsDAO.areScreensaverClockSecondsDisplayed(prefs);
        final DataModel.ClockStyle screensaverClockStyle = SettingsDAO.getScreensaverClockStyle(prefs);
        switch (screensaverClockStyle) {
            case ANALOG -> {
                setScreensaverTimeFormat(digitalClock, false);
                analogClock.enableSeconds(areScreensaverClockSecondsDisplayed);
                return;
            }
            case DIGITAL -> {
                analogClock.enableSeconds(false);
                setScreensaverTimeFormat(digitalClock, areScreensaverClockSecondsDisplayed);
                return;
            }
        }

        throw new IllegalStateException("unexpected clock style: " + screensaverClockStyle);
    }

    /**
     * For screensaver, format the digital clock to be bold and/or italic or not.
     *
     * @param screensaverDigitalClock TextClock to format
     * @param includeSeconds          whether seconds are displayed or not
     */
    public static void setScreensaverTimeFormat(TextClock screensaverDigitalClock, boolean includeSeconds) {
        final Context context = screensaverDigitalClock.getContext();
        final SharedPreferences prefs = getDefaultSharedPreferences(context);
        final boolean isScreensaverDigitalClockInBold = SettingsDAO.isScreensaverDigitalClockInBold(prefs);
        final boolean isScreensaverDigitalClockInItalic = SettingsDAO.isScreensaverDigitalClockInItalic(prefs);

        screensaverDigitalClock.setFormat12Hour(ClockUtils.get12ModeFormat(context, 0.4f, includeSeconds));
        screensaverDigitalClock.setFormat24Hour(ClockUtils.get24ModeFormat(context, includeSeconds));

        if (isScreensaverDigitalClockInBold && isScreensaverDigitalClockInItalic) {
            screensaverDigitalClock.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD_ITALIC));
        } else if (isScreensaverDigitalClockInBold) {
            screensaverDigitalClock.setTypeface(Typeface.DEFAULT_BOLD);
        } else if (isScreensaverDigitalClockInItalic) {
            screensaverDigitalClock.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
        } else {
            screensaverDigitalClock.setTypeface(Typeface.DEFAULT);
        }
    }

    /**
     * For screensaver, format the date and the next alarm to be bold and/or italic or not.
     *
     * @param date Date to format
     */
    public static void setScreensaverDateFormat(Context context, TextView date) {
        final SharedPreferences prefs = getDefaultSharedPreferences(context);
        final boolean isScreensaverDateInBold = SettingsDAO.isScreensaverDateInBold(prefs);
        final boolean isScreensaverDateInItalic = SettingsDAO.isScreensaverDateInItalic(prefs);

        if (isScreensaverDateInBold && isScreensaverDateInItalic) {
            date.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD_ITALIC));
        } else if (isScreensaverDateInBold) {
            date.setTypeface(Typeface.DEFAULT_BOLD);
        } else if (isScreensaverDateInItalic) {
            date.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
        } else {
            date.setTypeface(Typeface.DEFAULT);
        }
    }

    /**
     * For screensaver, format the date and the next alarm to be bold and/or italic or not.
     *
     * @param nextAlarm Next alarm to format
     */
    public static void setScreensaverNextAlarmFormat(TextView nextAlarm) {
        final Context context = nextAlarm.getContext();
        final SharedPreferences prefs = getDefaultSharedPreferences(context);

        if (SettingsDAO.isScreensaverNextAlarmInBold(prefs) && SettingsDAO.isScreensaverNextAlarmInItalic(prefs)) {
            nextAlarm.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD_ITALIC));
        } else if (SettingsDAO.isScreensaverNextAlarmInBold(prefs)) {
            nextAlarm.setTypeface(Typeface.DEFAULT_BOLD);
        } else if (SettingsDAO.isScreensaverNextAlarmInItalic(prefs)) {
            nextAlarm.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
        } else {
            nextAlarm.setTypeface(Typeface.DEFAULT);
        }
    }

    /**
     * For screensaver, set the margins and the style of the clock.
     */
    public static void setScreensaverMarginsAndClockStyle(final Context context, final View clock) {
        final SharedPreferences prefs = getDefaultSharedPreferences(context);
        final View mainClockView = clock.findViewById(R.id.main_clock);
        final boolean isTablet = ThemeUtils.isTablet();
        final boolean isLandscape = ThemeUtils.isLandscape();

        // Margins
        final int mainClockMarginLeft = ThemeUtils.convertDpToPixels(isTablet ? 20 : 16, context);
        final int mainClockMarginRight = ThemeUtils.convertDpToPixels(isTablet ? 20 : 16, context);
        final int mainClockMarginTop = ThemeUtils.convertDpToPixels(isTablet
                ? isLandscape ? 32 : 48
                : isLandscape ? 16 : 24, context);
        final int mainClockMarginBottom = ThemeUtils.convertDpToPixels(isTablet ? 20 : 16, context);
        final ViewGroup.MarginLayoutParams paramsForMainClock = (ViewGroup.MarginLayoutParams) mainClockView.getLayoutParams();
        paramsForMainClock.setMargins(mainClockMarginLeft, mainClockMarginTop, mainClockMarginRight, mainClockMarginBottom);
        mainClockView.setLayoutParams(paramsForMainClock);

        final int digitalClockMarginBottom = ThemeUtils.convertDpToPixels(isTablet ? -18 : -8, context);
        final ViewGroup.MarginLayoutParams paramsForDigitalClock = (ViewGroup.MarginLayoutParams) mainClockView.getLayoutParams();
        paramsForMainClock.setMargins(0, 0, 0, digitalClockMarginBottom);
        mainClockView.setLayoutParams(paramsForDigitalClock);

        final int analogClockMarginBottom = ThemeUtils.convertDpToPixels(isLandscape
                ? 5
                : isTablet ? 18 : 14, context);
        final ViewGroup.MarginLayoutParams paramsForAnalogClock = (ViewGroup.MarginLayoutParams) mainClockView.getLayoutParams();
        paramsForMainClock.setMargins(0, 0, 0, analogClockMarginBottom);
        mainClockView.setLayoutParams(paramsForAnalogClock);

        // Style
        final AnalogClock analogClock = mainClockView.findViewById(R.id.analog_clock);
        final TextClock textClock = mainClockView.findViewById(R.id.digital_clock);
        final TextView date = mainClockView.findViewById(R.id.date);
        final TextView nextAlarmIcon = mainClockView.findViewById(R.id.nextAlarmIcon);
        final TextView nextAlarm = mainClockView.findViewById(R.id.nextAlarm);
        final int screenSaverClockColorPicker = SettingsDAO.getScreensaverClockColorPicker(prefs);
        final int screensaverDateColorPicker = SettingsDAO.getScreensaverDateColorPicker(prefs);
        final int screensaverNextAlarmColorPicker = SettingsDAO.getScreensaverNextAlarmColorPicker(prefs);

        setScreensaverClockStyle(context, textClock, analogClock);
        dimScreensaverView(context, textClock, screenSaverClockColorPicker);
        dimScreensaverView(context, analogClock, screenSaverClockColorPicker);
        dimScreensaverView(context, date, screensaverDateColorPicker);
        dimScreensaverView(context, nextAlarmIcon, screensaverNextAlarmColorPicker);
        dimScreensaverView(context, nextAlarm, screensaverNextAlarmColorPicker);
        setScreensaverClockSecondsEnabled(context, textClock, analogClock);
        setScreensaverDateFormat(context, date);
        ClockUtils.setClockIconTypeface(nextAlarmIcon);
        setScreensaverNextAlarmFormat(nextAlarm);
    }
}
