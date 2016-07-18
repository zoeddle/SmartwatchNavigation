package com.example.carola.smartwatchnavigation;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by Carola on 24.05.16.
 */
public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(com.example.carola.smartwatchnavigation.R.xml.preferences);
    }
}
