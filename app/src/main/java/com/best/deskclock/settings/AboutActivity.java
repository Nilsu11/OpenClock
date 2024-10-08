// SPDX-License-Identifier: GPL-3.0-only

package com.best.deskclock.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;

import com.best.deskclock.BuildConfig;
import com.best.deskclock.R;
import com.best.deskclock.widget.CollapsingToolbarBaseActivity;

/**
 * "About" screen.
 */
public final class AboutActivity extends CollapsingToolbarBaseActivity {

    private static final String PREFS_FRAGMENT_TAG = "about_fragment";

    public static final String KEY_ABOUT_TITLE = "key_about_title";

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, new PrefsFragment(), PREFS_FRAGMENT_TAG)
                    .disallowAddToBackStack()
                    .commit();
        }
    }

    public static class PrefsFragment extends ScreenFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.settings_about);

            setupTitle();
            setupVersion();
            setupWhatsNew();
            setupMainFeatures();
        }

        private void setupTitle() {
            Preference title = findPreference(KEY_ABOUT_TITLE);
            if (title == null) {
                return;
            }
            if (BuildConfig.DEBUG) {
                title.setTitle(R.string.about_debug_app_title);
            } else {
                title.setTitle(R.string.app_label);
            }
        }

        private void setupVersion() {
            Preference version = findPreference("key_about_version");
            if (version == null) {
                return;
            }
            version.setSummary(BuildConfig.VERSION_NAME);
        }

        private void setupWhatsNew() {
            Preference whatsNewPreference = findPreference("key_about_whats_new");
            if (whatsNewPreference == null) {
                return;
            }
            whatsNewPreference.setOnPreferenceClickListener(preference -> {
                String version = BuildConfig.VERSION_NAME;
                if (BuildConfig.DEBUG) {
                    version = version.replace("-debug", "");
                }

                final Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/BlackyHawky/Clock/releases/tag/" + version));
                startActivity(browserIntent);
                return true;
            });
        }

        private void setupMainFeatures() {
            Preference featuresPreference = findPreference("key_about_features");
            if (featuresPreference == null) {
                return;
            }
            featuresPreference.setOnPreferenceClickListener(preference -> {
                final AlertDialog builder = new AlertDialog.Builder(requireContext())
                        .setIcon(R.drawable.ic_about_features)
                        .setTitle(R.string.features_title)
                        .setMessage(R.string.about_dialog_message)
                        .setPositiveButton(R.string.dialog_close, null)
                        .create();
                builder.show();
                return true;
            });
        }
    }

}
