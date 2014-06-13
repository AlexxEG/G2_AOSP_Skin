package com.gmail.alexellingsen.g2aospskin;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.*;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import com.gmail.alexellingsen.g2aospskin.hooks.LGSettings;
import com.gmail.alexellingsen.g2aospskin.utils.RootFunctions;
import com.gmail.alexellingsen.g2aospskin.utils.SettingsHelper;

import java.util.Arrays;

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

            Preference pSettings = findPreference(Prefs.AOSP_THEME_SETTINGS);

            pSettings.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    killApp(LGSettings.PACKAGE);
                    return true;
                }
            });

            final ListPreference pDialog = (ListPreference) findPreference(Prefs.AOSP_THEME_LG_DIALOG);
            final ListPreference pPowerMenu = (ListPreference) findPreference(Prefs.AOSP_THEME_POWER_MENU);

            pDialog.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String[] values = getResources().getStringArray(R.array.dialogThemeValues);
                    int index = Arrays.asList(values).indexOf(newValue);
                    String entry = getResources().getStringArray(R.array.dialogTheme)[index];

                    pDialog.setSummary(getString(R.string.pref_dialog_summary) + " Selected: " + entry);
                    return true;
                }
            });

            pPowerMenu.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String[] values = getResources().getStringArray(R.array.dialogThemeValues);
                    int index = Arrays.asList(values).indexOf(newValue);
                    String entry = getResources().getStringArray(R.array.dialogTheme)[index];

                    pPowerMenu.setSummary(getString(R.string.pref_power_menu_summary) + " Selected: " + entry);
                    return true;
                }
            });

            pDialog.getOnPreferenceChangeListener().onPreferenceChange(pDialog, pDialog.getValue());
            pPowerMenu.getOnPreferenceChangeListener().onPreferenceChange(pPowerMenu, pPowerMenu.getValue());
        }

        private void killApp(String packageName) {
            if (mSettings.getBoolean(Prefs.KILL_APP_DONT_ASK_AGAIN, false)) {
                RootFunctions.killApp(packageName);
            } else {
                showKillAppConfirmation(packageName);
            }
        }

        private void showKillAppConfirmation(final String packageName) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.alert_dialog_checkbox, null);
            final CheckBox dontAskAgain = (CheckBox) view.findViewById(R.id.chb_dont_ask_again);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setTitle(getString(R.string.title_kill_app))
                    .setMessage(getString(R.string.message_kill_app))
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mSettings.putBoolean(Prefs.KILL_APP_DONT_ASK_AGAIN, dontAskAgain.isChecked());
                            RootFunctions.killApp(packageName);
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setView(view);

            AlertDialog dialog = builder.create();

            dialog.show();
        }
    }
}