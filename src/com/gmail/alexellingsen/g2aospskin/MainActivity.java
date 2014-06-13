package com.gmail.alexellingsen.g2aospskin;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import com.gmail.alexellingsen.g2aospskin.hooks.LGSettings;
import com.gmail.alexellingsen.g2aospskin.utils.RootFunctions;
import com.gmail.alexellingsen.g2aospskin.utils.SettingsHelper;

import java.util.Arrays;

public class MainActivity extends PrefsActivity {

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(getApplicationContext(), PrefsActivity.class);
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
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

            Preference pXposedShortcut = findPreference("shortcut_xposed");

            pXposedShortcut.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent("de.robv.android.xposed.installer.OPEN_SECTION");
                    intent.setPackage("de.robv.android.xposed.installer");
                    intent.putExtra("section", "install");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    return true;
                }
            });
        }

        private void killApp(String packageName) {
            if (mSettings.getBoolean(Prefs.KILL_APP_SHOW_POPUP, true)) {
                showKillAppConfirmation(packageName);
            } else {
                if (mSettings.getBoolean(Prefs.KILL_APP, false)) {
                    RootFunctions.killApp(packageName);
                }
            }
        }

        private void showKillAppConfirmation(final String packageName) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.alert_dialog_checkbox, null);
            final CheckBox dontAskAgain = (CheckBox) view.findViewById(R.id.chb_dont_ask_again);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setTitle(getString(R.string.title_kill_app))
                    .setMessage(getString(R.string.message_kill_app))
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mSettings.putBoolean(Prefs.KILL_APP_SHOW_POPUP, !dontAskAgain.isChecked());
                            mSettings.putBoolean(Prefs.KILL_APP, true);
                            RootFunctions.killApp(packageName);
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mSettings.putBoolean(Prefs.KILL_APP_SHOW_POPUP, !dontAskAgain.isChecked());
                            mSettings.putBoolean(Prefs.KILL_APP, false);
                            dialog.dismiss();
                        }
                    })
                    .setView(view);

            AlertDialog dialog = builder.create();

            dialog.show();
        }
    }
}