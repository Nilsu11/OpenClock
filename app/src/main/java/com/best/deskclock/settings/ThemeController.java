// SPDX-License-Identifier: GPL-3.0-only

package com.best.deskclock.settings;

import static com.best.deskclock.settings.InterfaceCustomizationActivity.BLACK_ACCENT_COLOR;
import static com.best.deskclock.settings.InterfaceCustomizationActivity.BLACK_NIGHT_ACCENT_COLOR;
import static com.best.deskclock.settings.InterfaceCustomizationActivity.BLUE_GRAY_ACCENT_COLOR;
import static com.best.deskclock.settings.InterfaceCustomizationActivity.BROWN_ACCENT_COLOR;
import static com.best.deskclock.settings.InterfaceCustomizationActivity.DARK_THEME;
import static com.best.deskclock.settings.InterfaceCustomizationActivity.DEFAULT_ACCENT_COLOR;
import static com.best.deskclock.settings.InterfaceCustomizationActivity.DEFAULT_NIGHT_ACCENT_COLOR;
import static com.best.deskclock.settings.InterfaceCustomizationActivity.GREEN_ACCENT_COLOR;
import static com.best.deskclock.settings.InterfaceCustomizationActivity.INDIGO_ACCENT_COLOR;
import static com.best.deskclock.settings.InterfaceCustomizationActivity.LIGHT_THEME;
import static com.best.deskclock.settings.InterfaceCustomizationActivity.BLUE_GRAY_NIGHT_ACCENT_COLOR;
import static com.best.deskclock.settings.InterfaceCustomizationActivity.BROWN_NIGHT_ACCENT_COLOR;
import static com.best.deskclock.settings.InterfaceCustomizationActivity.GREEN_NIGHT_ACCENT_COLOR;
import static com.best.deskclock.settings.InterfaceCustomizationActivity.INDIGO_NIGHT_ACCENT_COLOR;
import static com.best.deskclock.settings.InterfaceCustomizationActivity.ORANGE_NIGHT_ACCENT_COLOR;
import static com.best.deskclock.settings.InterfaceCustomizationActivity.PINK_NIGHT_ACCENT_COLOR;
import static com.best.deskclock.settings.InterfaceCustomizationActivity.PURPLE_ACCENT_COLOR;
import static com.best.deskclock.settings.InterfaceCustomizationActivity.PURPLE_NIGHT_ACCENT_COLOR;
import static com.best.deskclock.settings.InterfaceCustomizationActivity.RED_NIGHT_ACCENT_COLOR;
import static com.best.deskclock.settings.InterfaceCustomizationActivity.ORANGE_ACCENT_COLOR;
import static com.best.deskclock.settings.InterfaceCustomizationActivity.PINK_ACCENT_COLOR;
import static com.best.deskclock.settings.InterfaceCustomizationActivity.RED_ACCENT_COLOR;
import static com.best.deskclock.settings.InterfaceCustomizationActivity.SYSTEM_THEME;
import static com.best.deskclock.settings.InterfaceCustomizationActivity.YELLOW_ACCENT_COLOR;
import static com.best.deskclock.settings.InterfaceCustomizationActivity.YELLOW_NIGHT_ACCENT_COLOR;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;

import com.best.deskclock.R;
import com.best.deskclock.Utils;
import com.best.deskclock.data.DataModel;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * This class registers ActivityLifecycleCallbacks to collect all activities to a single set.
 * This allows to change the dark mode and the accent color at runtime.
 */
public class ThemeController {
    private static final Set<Activity> activities = Collections.newSetFromMap(new WeakHashMap<>());
    private static boolean initialized = false;
    private static DarkMode darkMode = DarkMode.DEFAULT_DARK_MODE;
    private static AccentColor accentColor = AccentColor.DEFAULT;
    private static AutoNightAccentColor autoNightAccentColor = AutoNightAccentColor.TOGGLED;
    private static NightAccentColor nightAccentColor = NightAccentColor.DEFAULT_NIGHT;
    private static LayoutBackground layoutBackground = LayoutBackground.DEFAULT;
    private static LayoutBorder layoutBorder = LayoutBorder.DEFAULT;
    private static FadeTransitions fadeTransitions = FadeTransitions.DISABLED;
    private static RestoreSettings restoreSettings = RestoreSettings.DONE;

    /**
     * To initialize this class in the application class.
     */
    public static void initialize(Application application) {
        if (!initialized) {
            initialized = true;
            // Registering the callback allows us to listen and react to the lifecycle of every app activity.
            application.registerActivityLifecycleCallbacks(new ActivityCallbacks());
        }
    }

    /**
     * Store a selected dark mode in the static field and trigger recreation for all the activities.
     * @param darkMode Dark mode to use.
     */
    public static void applyDarkMode(DarkMode darkMode) {
        ThemeController.darkMode = darkMode;
        for (Activity activity : activities) {
            ActivityCompat.recreate(activity);
        }
    }

    /**
     * Store a selected accent color in the static field and trigger recreation for all the activities.
     * @param accentColor Accent color to use.
     */
    public static void applyAccentColor(AccentColor accentColor) {
        ThemeController.accentColor = accentColor;
        for (Activity activity : activities) {
            ActivityCompat.recreate(activity);
        }
    }

    /**
     * Allow all activities to be recreated if the "Auto night accent color" switch is toggled.
     * @param autoNightAccentColor the "Auto night accent color" switch is toggled.
     */
    public static void recreateActivityForAutoNightAccentColor(AutoNightAccentColor autoNightAccentColor) {
        ThemeController.autoNightAccentColor = autoNightAccentColor;
        for (Activity activity : activities) {
            // We add a small delay to give the settings switch button a clean animation.
            new Handler().postDelayed(() -> ActivityCompat.recreate(activity), 300);
        }
    }

    /**
     * Store a selected night accent color in the static field and trigger recreation for all the activities.
     * @param nightAccentColor Night accent color to use.
     */
    public static void applyNightAccentColor(NightAccentColor nightAccentColor) {
        ThemeController.nightAccentColor = nightAccentColor;
        for (Activity activity : activities) {
            ActivityCompat.recreate(activity);
        }
    }

    /**
     * Store the boolean value of the "Display card backgrounds" setting in the static field
     * and trigger recreation for all the activities.
     * @param layoutBackground Display or hide backgrounds.
     */
    public static void applyLayoutBackground(LayoutBackground layoutBackground) {
        ThemeController.layoutBackground = layoutBackground;
        for (Activity activity : activities) {
            // We add a small delay to give the settings switch button a clean animation.
            new Handler().postDelayed(() -> ActivityCompat.recreate(activity), 300);
        }
    }

    /**
     * Store the boolean value of the "Display background borders" setting in the static field
     * and trigger recreation for all the activities.
     * @param layoutBorder Display or hide background borders.
     */
    public static void applyLayoutBorderedSettings(LayoutBorder layoutBorder) {
        ThemeController.layoutBorder = layoutBorder;
        for (Activity activity : activities) {
            // We add a small delay to give the settings switch button a clean animation.
            new Handler().postDelayed(() -> ActivityCompat.recreate(activity), 300);
        }
    }

    /**
     * Store the boolean value of the "Enable fade transitions" setting in the static field
     * and trigger recreation for all the activities.
     * @param fadeTransitions Enable or disable fade transitions.
     */
    public static void enableFadeTransitions(FadeTransitions fadeTransitions) {
        ThemeController.fadeTransitions = fadeTransitions;
        for (Activity activity : activities) {
            // We add a small delay to give the settings switch button a clean animation.
            new Handler().postDelayed(() -> ActivityCompat.recreate(activity), 300);
        }
    }

    /**
     * Allow all activities to be recreated if settings are restored.
     * @param restoreSettings Restore is done.
     */
    public static void recreateActivityAfterRestoringSettings(RestoreSettings restoreSettings) {
        ThemeController.restoreSettings = restoreSettings;
        for (Activity activity : activities) {
            ActivityCompat.recreate(activity);
        }
    }

    private static class ActivityCallbacks implements Application.ActivityLifecycleCallbacks {
        @Override
        public void onActivityCreated(@NonNull Activity activity, Bundle savedInstanceState) {
            final String getTheme = DataModel.getDataModel().getTheme();
            final String getColor = DataModel.getDataModel().getAccentColor();
            final String getNightColor = DataModel.getDataModel().getNightAccentColor();

            switch (darkMode) {
                case DEFAULT_DARK_MODE -> {
                    switch (getTheme) {
                        case SYSTEM_THEME ->
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                        case LIGHT_THEME ->
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        case DARK_THEME ->
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    }
                    activities.add(activity);
                }

                case AMOLED -> {
                    if (!getTheme.equals(SYSTEM_THEME)) {
                        activity.setTheme(R.style.AmoledTheme);
                    }
                    activities.add(activity);
                }
            }

            switch (accentColor) {
                case DEFAULT -> {
                    if (getColor.equals(DEFAULT_ACCENT_COLOR)) {
                        activity.setTheme(R.style.DefaultColor);
                    }
                    activities.add(activity);
                }
                case BLACK -> {
                    if (getColor.equals(BLACK_ACCENT_COLOR)) {
                        activity.setTheme(R.style.BlackAccentColor);
                    }
                    activities.add(activity);
                }
                case BLUE_GRAY -> {
                    if (getColor.equals(BLUE_GRAY_ACCENT_COLOR)) {
                        activity.setTheme(R.style.BlueGrayAccentColor);
                    }
                    activities.add(activity);
                }
                case BROWN -> {
                    if (getColor.equals(BROWN_ACCENT_COLOR)) {
                        activity.setTheme(R.style.BrownAccentColor);
                    }
                    activities.add(activity);
                }
                case GREEN -> {
                    if (getColor.equals(GREEN_ACCENT_COLOR)) {
                        activity.setTheme(R.style.GreenAccentColor);
                    }
                    activities.add(activity);
                }
                case INDIGO -> {
                    if (getColor.equals(INDIGO_ACCENT_COLOR)) {
                        activity.setTheme(R.style.IndigoAccentColor);
                    }
                    activities.add(activity);
                }
                case ORANGE -> {
                    if (getColor.equals(ORANGE_ACCENT_COLOR)) {
                        activity.setTheme(R.style.OrangeAccentColor);
                    }
                    activities.add(activity);
                }
                case PINK -> {
                    if (getColor.equals(PINK_ACCENT_COLOR)) {
                        activity.setTheme(R.style.PinkAccentColor);
                    }
                    activities.add(activity);
                }
                case PURPLE -> {
                    if (getColor.equals(PURPLE_ACCENT_COLOR)) {
                        activity.setTheme(R.style.PurpleAccentColor);
                    }
                    activities.add(activity);
                }
                case RED -> {
                    if (getColor.equals(RED_ACCENT_COLOR)) {
                        activity.setTheme(R.style.RedAccentColor);
                    }
                    activities.add(activity);
                }
                case YELLOW -> {
                    if (getColor.equals(YELLOW_ACCENT_COLOR)) {
                        activity.setTheme(R.style.YellowAccentColor);
                    }
                    activities.add(activity);
                }
            }

            if (Objects.requireNonNull(autoNightAccentColor) == AutoNightAccentColor.TOGGLED) {
                activities.add(activity);
            }

            if (Utils.isNight(activity.getResources())) {
                switch (nightAccentColor) {
                    case DEFAULT_NIGHT -> {
                        if (getNightColor.equals(DEFAULT_NIGHT_ACCENT_COLOR)) {
                            activity.setTheme(R.style.DefaultColor);
                        }
                        activities.add(activity);
                    }
                    case NIGHT_BLACK -> {
                        if (getNightColor.equals(BLACK_NIGHT_ACCENT_COLOR)) {
                            activity.setTheme(R.style.BlackAccentColor);
                        }
                        activities.add(activity);
                    }
                    case NIGHT_BLUE_GRAY -> {
                        if (getNightColor.equals(BLUE_GRAY_NIGHT_ACCENT_COLOR)) {
                            activity.setTheme(R.style.BlueGrayAccentColor);
                        }
                        activities.add(activity);
                    }
                    case NIGHT_BROWN -> {
                        if (getNightColor.equals(BROWN_NIGHT_ACCENT_COLOR)) {
                            activity.setTheme(R.style.BrownAccentColor);
                        }
                        activities.add(activity);
                    }
                    case NIGHT_GREEN -> {
                        if (getNightColor.equals(GREEN_NIGHT_ACCENT_COLOR)) {
                            activity.setTheme(R.style.GreenAccentColor);
                        }
                        activities.add(activity);
                    }
                    case NIGHT_INDIGO -> {
                        if (getNightColor.equals(INDIGO_NIGHT_ACCENT_COLOR)) {
                            activity.setTheme(R.style.IndigoAccentColor);
                        }
                        activities.add(activity);
                    }
                    case NIGHT_ORANGE -> {
                        if (getNightColor.equals(ORANGE_NIGHT_ACCENT_COLOR)) {
                            activity.setTheme(R.style.OrangeAccentColor);
                        }
                        activities.add(activity);
                    }
                    case NIGHT_PINK -> {
                        if (getNightColor.equals(PINK_NIGHT_ACCENT_COLOR)) {
                            activity.setTheme(R.style.PinkAccentColor);
                        }
                        activities.add(activity);
                    }
                    case NIGHT_PURPLE -> {
                        if (getNightColor.equals(PURPLE_NIGHT_ACCENT_COLOR)) {
                            activity.setTheme(R.style.PurpleAccentColor);
                        }
                        activities.add(activity);
                    }
                    case NIGHT_RED -> {
                        if (getNightColor.equals(RED_NIGHT_ACCENT_COLOR)) {
                            activity.setTheme(R.style.RedAccentColor);
                        }
                        activities.add(activity);
                    }
                    case NIGHT_YELLOW -> {
                        if (getNightColor.equals(YELLOW_NIGHT_ACCENT_COLOR)) {
                            activity.setTheme(R.style.YellowAccentColor);
                        }
                        activities.add(activity);
                    }
                }
            }

            switch (layoutBackground) {
                case DEFAULT, TRANSPARENT -> activities.add(activity);
            }

            switch (layoutBorder) {
                case DEFAULT, BORDERED -> activities.add(activity);
            }

            switch (fadeTransitions) {
                case DISABLED, ENABLED -> activities.add(activity);
            }

            if (Objects.requireNonNull(restoreSettings) == RestoreSettings.DONE) {
                activities.add(activity);
            }
        }

        @Override
        public void onActivityDestroyed(@NonNull Activity activity) {
            activities.remove(activity);
        }

        // Methods implemented by default
        @Override
        public void onActivityStarted(@NonNull Activity activity) {}

        @Override
        public void onActivityResumed(@NonNull Activity activity) {}

        @Override
        public void onActivityPaused(@NonNull Activity activity) {}

        @Override
        public void onActivityStopped(@NonNull Activity activity) {}

        @Override
        public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {}

    }

    public enum DarkMode {DEFAULT_DARK_MODE, AMOLED}
    public enum AccentColor {DEFAULT, BLACK, BLUE_GRAY, BROWN, GREEN, INDIGO, ORANGE, PINK, PURPLE, RED, YELLOW}
    public enum AutoNightAccentColor {TOGGLED}
    public enum NightAccentColor {DEFAULT_NIGHT, NIGHT_BLACK, NIGHT_BLUE_GRAY, NIGHT_BROWN, NIGHT_GREEN,
        NIGHT_INDIGO, NIGHT_ORANGE, NIGHT_PINK, NIGHT_PURPLE, NIGHT_RED, NIGHT_YELLOW}
    public enum LayoutBackground {DEFAULT, TRANSPARENT}
    public enum LayoutBorder {DEFAULT, BORDERED}
    public enum FadeTransitions {DISABLED, ENABLED}
    public enum RestoreSettings {DONE}
}
