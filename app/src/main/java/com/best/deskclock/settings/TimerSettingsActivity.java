// SPDX-License-Identifier: GPL-3.0-only

package com.best.deskclock.settings;

import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;

import androidx.annotation.NonNull;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.SwitchPreferenceCompat;
import androidx.preference.TwoStatePreference;

import com.best.deskclock.R;
import com.best.deskclock.Utils;
import com.best.deskclock.data.DataModel;
import com.best.deskclock.ringtone.RingtonePickerActivity;
import com.best.deskclock.widget.CollapsingToolbarBaseActivity;

import java.util.Objects;

public class TimerSettingsActivity extends CollapsingToolbarBaseActivity {

    private static final String PREFS_FRAGMENT_TAG = "timer_settings_fragment";

    public static final String KEY_TIMER_RINGTONE = "key_timer_ringtone";
    public static final String KEY_TIMER_CRESCENDO = "key_timer_crescendo_duration";
    public static final String KEY_TIMER_VIBRATE = "key_timer_vibrate";
    public static final String KEY_DEFAULT_TIME_TO_ADD_TO_TIMER = "key_default_time_to_add_to_timer";
    public static final String KEY_KEEP_TIMER_SCREEN_ON = "key_keep_timer_screen_on";
    public static final String KEY_TRANSPARENT_BACKGROUND_FOR_EXPIRED_TIMER =
            "key_transparent_background_for_expired_timer";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, new PrefsFragment(), PREFS_FRAGMENT_TAG)
                    .disallowAddToBackStack()
                    .commit();
        }
    }

    public static class PrefsFragment extends ScreenFragment implements
            Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

        Preference mTimerVibrate;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.settings_timer);

            mTimerVibrate = findPreference(KEY_TIMER_VIBRATE);

            hidePreferences();
        }

        @Override
        public void onResume() {
            super.onResume();

            refresh();
        }

        @Override
        public boolean onPreferenceChange(Preference pref, Object newValue) {
            switch (pref.getKey()) {
                case KEY_TIMER_RINGTONE -> pref.setSummary(DataModel.getDataModel().getTimerRingtoneTitle());

                case KEY_TIMER_CRESCENDO, KEY_DEFAULT_TIME_TO_ADD_TO_TIMER -> {
                    final ListPreference preference = (ListPreference) pref;
                    final int index = preference.findIndexOfValue((String) newValue);
                    preference.setSummary(preference.getEntries()[index]);
                }

                case KEY_TIMER_VIBRATE -> {
                    final TwoStatePreference timerVibratePref = (TwoStatePreference) pref;
                    DataModel.getDataModel().setTimerVibrate(timerVibratePref.isChecked());
                    Utils.setVibrationTime(requireContext(), 50);
                }

                case KEY_KEEP_TIMER_SCREEN_ON, KEY_TRANSPARENT_BACKGROUND_FOR_EXPIRED_TIMER ->
                        Utils.setVibrationTime(requireContext(), 50);
            }

            return true;
        }

        @Override
        public boolean onPreferenceClick(@NonNull Preference pref) {
            final Context context = getActivity();
            if (context == null) {
                return false;
            }
            if (pref.getKey().equals(KEY_TIMER_RINGTONE)) {
                startActivity(RingtonePickerActivity.createTimerRingtonePickerIntent(context));
                return true;
            }

            return false;
        }

        private void hidePreferences() {
            final boolean hasVibrator = ((Vibrator) mTimerVibrate.getContext()
                    .getSystemService(VIBRATOR_SERVICE)).hasVibrator();
            mTimerVibrate.setVisible(hasVibrator);
        }

        private void refresh() {
            final Preference timerRingtonePref = findPreference(KEY_TIMER_RINGTONE);
            Objects.requireNonNull(timerRingtonePref).setOnPreferenceClickListener(this);
            timerRingtonePref.setSummary(DataModel.getDataModel().getTimerRingtoneTitle());

            final ListPreference timerCrescendoPref = findPreference(KEY_TIMER_CRESCENDO);
            Objects.requireNonNull(timerCrescendoPref).setOnPreferenceChangeListener(this);
            timerCrescendoPref.setSummary(timerCrescendoPref.getEntry());

            mTimerVibrate.setOnPreferenceChangeListener(this);

            final ListPreference defaultMinutesToAddToTimerPref = findPreference(KEY_DEFAULT_TIME_TO_ADD_TO_TIMER);
            Objects.requireNonNull(defaultMinutesToAddToTimerPref).setOnPreferenceChangeListener(this);
            defaultMinutesToAddToTimerPref.setSummary(defaultMinutesToAddToTimerPref.getEntry());

            final SwitchPreferenceCompat keepTimerScreenOnPref = findPreference(KEY_KEEP_TIMER_SCREEN_ON);
            Objects.requireNonNull(keepTimerScreenOnPref).setChecked(
                    DataModel.getDataModel().shouldTimerDisplayRemainOn()
            );
            keepTimerScreenOnPref.setOnPreferenceChangeListener(this);

            final SwitchPreferenceCompat transparentBackgroundPref =
                    findPreference(KEY_TRANSPARENT_BACKGROUND_FOR_EXPIRED_TIMER);
            Objects.requireNonNull(transparentBackgroundPref).setChecked(
                    DataModel.getDataModel().isTimerBackgroundTransparent()
            );
            transparentBackgroundPref.setOnPreferenceChangeListener(this);
        }
    }
}
