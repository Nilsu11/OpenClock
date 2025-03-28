// SPDX-License-Identifier: GPL-3.0-only

package com.best.deskclock;

import static com.best.deskclock.DeskClockApplication.getDefaultSharedPreferences;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.best.deskclock.settings.PermissionsManagementActivity;

public class FirstLaunch extends AppCompatActivity {

    public static final String KEY_IS_FIRST_LAUNCH = "key_is_first_launch";

    TextView mAppTitle;
    TextView mAppVersion;
    TextView mMainFeaturesText;
    TextView mImportantInfoText;
    Button mNowButton;
    Button mLaterButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = getDefaultSharedPreferences(this);

        setContentView(R.layout.first_launch_activity);

        mAppTitle = findViewById(R.id.first_launch_app_title);
        mAppVersion = findViewById(R.id.first_launch_app_version);
        mMainFeaturesText = findViewById(R.id.first_launch_main_features_text);
        mImportantInfoText = findViewById(R.id.first_launch_important_info_text);
        mNowButton = findViewById(R.id.now_button);
        mLaterButton = findViewById(R.id.later_button);

        setupTitle();

        setupVersion();

        setupMainFeaturesText();

        setupImportantInfoMessage();

        mNowButton.setOnClickListener(v -> {
            sharedPreferences.edit().putBoolean(KEY_IS_FIRST_LAUNCH, false).apply();
            finish();
            startActivity(new Intent(this, DeskClock.class));
            startActivity(new Intent(this, PermissionsManagementActivity.class));
        });

        mLaterButton.setOnClickListener(v -> {
            sharedPreferences.edit().putBoolean(KEY_IS_FIRST_LAUNCH, false).apply();
            finish();
            startActivity(new Intent(this, DeskClock.class));
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showDialogToQuit();
            }
        });
    }

    /**
     * Automatically sets the application title according to whether it's the debug version or not.
     */
    private void setupTitle() {
        if (BuildConfig.DEBUG) {
            mAppTitle.setText(R.string.about_debug_app_title);
        } else {
            mAppTitle.setText(R.string.app_label);
        }
    }

    /**
     * Automatically sets the application version according to whether it's the debug version or not.
     */
    private void setupVersion() {
        String versionNumber = BuildConfig.VERSION_NAME;
        mAppVersion.setText(getString(R.string.first_launch_version, versionNumber));
    }

    /**
     * Shows a dialog asking the user whether or not to quit the application.
     */
    private void showDialogToQuit() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.first_launch_dialog_title))
                .setIcon(R.drawable.ic_logout)
                .setMessage(getString(R.string.first_launch_dialog_message))
                .setPositiveButton(android.R.string.yes, (dialog, which) -> finishAffinity())
                .setNegativeButton(android.R.string.no, null)
                .setCancelable(false)
                .show();
    }

    /**
     * Points to the GitHub page where you can view all the application's features.
     */
    private void setupMainFeaturesText() {
        String link = ("<a href=\"https://github.com/BlackyHawky/Clock#features-\">"
                + getString(R.string.first_launch_main_feature_link) + "</a>");

        Spanned mainFeaturesMessage = Html.fromHtml(getString(R.string.first_launch_main_feature_message, link));

        mMainFeaturesText.setText(mainFeaturesMessage);
        mMainFeaturesText.setMovementMethod(LinkMovementMethod.getInstance());
    }

    /**
     * Define an important message for the first launch.
     */
    private void setupImportantInfoMessage() {
        String android14message = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            android14message = getString(R.string.first_launch_important_info_message_for_SDK34);
        }
        Spanned importantInfoMessage = Html.fromHtml(getString(R.string.first_launch_important_info_message, android14message));
        mImportantInfoText.setText(importantInfoMessage);
    }

}
