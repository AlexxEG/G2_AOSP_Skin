package com.gmail.alexellingsen.g2aospskin;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import com.gmail.alexellingsen.g2aospskin.utils.SettingsHelper;

public class MainActivity extends PreferenceActivity {

    private SettingsHelper mSettings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSettings = new SettingsHelper(this);

        getFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new MainFragment())
                .commit();
    }

    class MainFragment extends PreferenceFragment {

        @SuppressWarnings("deprecation")
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            PreferenceManager prefMgr = getPreferenceManager();
            prefMgr.setSharedPreferencesName(Prefs.NAME);
            prefMgr.setSharedPreferencesMode(MODE_WORLD_READABLE);

            addPreferencesFromResource(R.xml.preferences);
        }
    }
}
