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

public class TimerSettingsActivity extends CollapsingToolbarBaseActivity {

    private static final String PREFS_FRAGMENT_TAG = "timer_settings_fragment";

    public static final String KEY_TIMER_RINGTONE = "key_timer_ringtone";
    public static final String KEY_TIMER_CRESCENDO = "key_timer_crescendo_duration";
    public static final String KEY_TIMER_VIBRATE = "key_timer_vibrate";
    public static final String KEY_SORT_TIMERS = "key_sort_timers";
    public static final String KEY_SORT_TIMERS_BY_CREATION_DATE = "0";
    public static final String KEY_SORT_TIMERS_BY_ASCENDING_DURATION = "1";
    public static final String KEY_SORT_TIMERS_BY_DESCENDING_DURATION = "2";
    public static final String KEY_SORT_TIMERS_BY_NAME = "3";
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

        ListPreference mTimerCrescendoPref;
        ListPreference mSortTimersPref;
        ListPreference mDefaultMinutesToAddToTimerPref;
        Preference mTimerRingtonePref;
        Preference mTimerVibratePref;
        SwitchPreferenceCompat mKeepTimerScreenOnPref;
        SwitchPreferenceCompat mTransparentBackgroundPref;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.settings_timer);

            mTimerRingtonePref = findPreference(KEY_TIMER_RINGTONE);
            mTimerCrescendoPref = findPreference(KEY_TIMER_CRESCENDO);
            mTimerVibratePref = findPreference(KEY_TIMER_VIBRATE);
            mSortTimersPref = findPreference(KEY_SORT_TIMERS);
            mDefaultMinutesToAddToTimerPref = findPreference(KEY_DEFAULT_TIME_TO_ADD_TO_TIMER);
            mKeepTimerScreenOnPref = findPreference(KEY_KEEP_TIMER_SCREEN_ON);
            mTransparentBackgroundPref = findPreference(KEY_TRANSPARENT_BACKGROUND_FOR_EXPIRED_TIMER);

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

                case KEY_SORT_TIMERS -> {
                    final ListPreference preference = (ListPreference) pref;
                    final int index = preference.findIndexOfValue((String) newValue);
                    preference.setSummary(preference.getEntries()[index]);
                    requireActivity().setResult(RESULT_OK);
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
            final boolean hasVibrator = ((Vibrator) mTimerVibratePref.getContext()
                    .getSystemService(VIBRATOR_SERVICE)).hasVibrator();
            mTimerVibratePref.setVisible(hasVibrator);
        }

        private void refresh() {
            mTimerRingtonePref.setOnPreferenceClickListener(this);
            mTimerRingtonePref.setSummary(DataModel.getDataModel().getTimerRingtoneTitle());

            mTimerCrescendoPref.setOnPreferenceChangeListener(this);
            mTimerCrescendoPref.setSummary(mTimerCrescendoPref.getEntry());

            mTimerVibratePref.setOnPreferenceChangeListener(this);

            mSortTimersPref.setOnPreferenceChangeListener(this);
            mSortTimersPref.setSummary(mSortTimersPref.getEntry());

            mDefaultMinutesToAddToTimerPref.setOnPreferenceChangeListener(this);
            mDefaultMinutesToAddToTimerPref.setSummary(mDefaultMinutesToAddToTimerPref.getEntry());

            mKeepTimerScreenOnPref.setChecked(DataModel.getDataModel().shouldTimerDisplayRemainOn());
            mKeepTimerScreenOnPref.setOnPreferenceChangeListener(this);

            mTransparentBackgroundPref.setChecked(DataModel.getDataModel().isTimerBackgroundTransparent());
            mTransparentBackgroundPref.setOnPreferenceChangeListener(this);
        }
    }
}
