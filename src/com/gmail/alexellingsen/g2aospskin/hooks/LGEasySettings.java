package com.gmail.alexellingsen.g2aospskin.hooks;

import android.app.ActionBar;
import android.app.Activity;
import android.content.res.XModuleResources;
import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;
import com.gmail.alexellingsen.g2aospskin.G2AOSPSkin;
import com.gmail.alexellingsen.g2aospskin.Prefs;
import com.gmail.alexellingsen.g2aospskin.R;
import com.gmail.alexellingsen.g2aospskin.utils.SettingsHelper;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import java.util.HashMap;
import java.util.Map;

public class LGEasySettings {

    public static final String PACKAGE = "com.lge.settings.easy";

    private static XModuleResources mModRes;
    private static SettingsHelper mSettings;

    public static void init(SettingsHelper settings, XModuleResources modRes) {
        mModRes = modRes;
        mSettings = settings;
    }

    public static void handleInitPackageResources(InitPackageResourcesParam resparam) {
        if (!resparam.packageName.equals(PACKAGE)) {
            return;
        }

        if (!mSettings.getBoolean(Prefs.AOSP_THEME_SETTINGS, false))
            return;

        Icons.replace(resparam);
    }

    public static void handleLoadPackage(LoadPackageParam lpparam) {
        if (!lpparam.packageName.equals(PACKAGE)) {
            return;
        }

        if (!mSettings.getBoolean(Prefs.AOSP_THEME_SETTINGS, false))
            return;

        G2AOSPSkin.log("Hooking 'createTabs'");
        XposedHelpers.findAndHookMethod(
                PACKAGE + ".EasySettings",
                lpparam.classLoader,
                "createTabs",

                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        Activity activity = (Activity) param.thisObject;

                        activity.setTheme(android.R.style.Theme_Holo);
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Activity activity = (Activity) param.thisObject;

                        for (int i = 0; i < activity.getActionBar().getTabCount(); i++) {
                            ActionBar.Tab tab = activity.getActionBar().getTabAt(i);

                            // Get tab text
                            TextView textView = (TextView) tab.getCustomView().findViewById(
                                    activity.getResources().getIdentifier("tab_text", "id", PACKAGE)
                            );
                            CharSequence text = textView.getText();

                            tab.setCustomView(null);
                            tab.setText(text);
                        }
                    }
                }
        );
        G2AOSPSkin.log("Hooked 'createTabs'");

        fixFlexCustomViewTab(lpparam);
        fixPreferenceFont(lpparam);
    }

    /**
     * Disable the method that makes text in tabs bold when selected.
     * Causes FC on some versions. (CloudyFlex/Flex?)
     */
    public static void fixFlexCustomViewTab(LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod(
                    "com.lge.settings.easy.EasySettings",
                    lpparam.classLoader,
                    "setTabViewUI",
                    int.class,

                    XC_MethodReplacement.DO_NOTHING
            );
        } catch (Throwable e) {
            // Method doesn't exists, fix not needed.

            if (G2AOSPSkin.DEBUG) {
                G2AOSPSkin.log("None dangerous error:");
                XposedBridge.log(e);
            }
        }
    }

    public static void fixPreferenceFont(LoadPackageParam lpparam) {
        XC_MethodHook hook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                View view = (View) param.args[0];
                TextView textView = (TextView) view.findViewById(android.R.id.title);

                // Remove bold style
                Typeface newTT = Typeface.create(textView.getTypeface(), Typeface.NORMAL);
                textView.setTypeface(newTT);
            }
        };

        XposedHelpers.findAndHookMethod(PACKAGE + ".EasySwitchPreference", lpparam.classLoader,
                "onBindView", View.class, hook);

        XposedHelpers.findAndHookMethod("com.lge.settings.display.EasyBrightnessPreference", lpparam.classLoader,
                "onBindView", View.class, hook);
    }

    private static class Icons {
        // Auto generated with a custom external tool, might not be correct.
        static HashMap<String, Integer> icons = new HashMap<String, Integer>() {
            {
                put("daydream_settings_easy", R.drawable.daydream_settings_easy_easy);
                put("ic_accessibility", R.drawable.ic_accessibility_easy);
                put("ic_accessory", R.drawable.ic_accessory_easy);
                put("ic_account_sync", R.drawable.ic_account_sync_easy);
                put("ic_airplane", R.drawable.ic_airplane_easy);
                put("ic_alarm", R.drawable.ic_alarm_easy);
                put("ic_aspectratio", R.drawable.ic_aspectratio_easy);
                put("ic_auto_rotate_screen", R.drawable.ic_auto_rotate_screen_easy);
                put("ic_backup", R.drawable.ic_backup_easy);
                put("ic_backup_reset", R.drawable.ic_backup_reset_easy);
                put("ic_battery", R.drawable.ic_battery_easy);
                put("ic_bluetooth", R.drawable.ic_bluetooth_easy);
                put("ic_brightness", R.drawable.ic_brightness_easy);
                put("ic_call", R.drawable.ic_call_easy);
                put("ic_call_att", R.drawable.ic_call_att_easy);
                put("ic_cloud", R.drawable.ic_cloud_easy);
                put("ic_connectivity", R.drawable.ic_connectivity_easy);
                put("ic_data_usage", R.drawable.ic_data_usage_easy);
                put("ic_date_time", R.drawable.ic_date_time_easy);
                put("ic_develops", R.drawable.ic_develops_easy);
                put("ic_font_size", R.drawable.ic_font_size_easy);
                put("ic_font_type", R.drawable.ic_font_type_easy);
                put("ic_front_key_light", R.drawable.ic_front_key_light_easy);
                put("ic_gentle_vibration", R.drawable.ic_gentle_vibration_easy);
                put("ic_gesture", R.drawable.ic_gesture_easy);
                put("ic_guestmode", R.drawable.ic_guestmode_easy);
                put("ic_hands_free_mode", R.drawable.ic_hands_free_mode_easy);
                put("ic_homeselector", R.drawable.ic_homeselector_easy);
                put("ic_keeponscreen", R.drawable.ic_keeponscreen_easy);
                put("ic_keeponvideo", R.drawable.ic_keeponvideo_easy);
                put("ic_keyboard_language", R.drawable.ic_keyboard_language_easy);
                put("ic_lock", R.drawable.ic_lock_easy);
                put("ic_lockscreen", R.drawable.ic_lockscreen_easy);
                put("ic_manage_apps", R.drawable.ic_manage_apps_easy);
                put("ic_map", R.drawable.ic_map_easy);
                put("ic_menu_display", R.drawable.ic_menu_display_easy);
                put("ic_menu_display_disabled", R.drawable.ic_menu_display_disabled_easy);
                put("ic_menu_etc", R.drawable.ic_menu_etc_easy);
                put("ic_menu_etc_disabled", R.drawable.ic_menu_etc_disabled_easy);
                put("ic_menu_network", R.drawable.ic_menu_network_easy);
                put("ic_menu_network_disabled", R.drawable.ic_menu_network_disabled_easy);
                put("ic_menu_sound", R.drawable.ic_menu_sound_easy);
                put("ic_menu_sound_disabled", R.drawable.ic_menu_sound_disabled_easy);
                put("ic_mode_change", R.drawable.ic_mode_change_easy);
                put("ic_motion_sensor_calibration", R.drawable.ic_motion_sensor_calibration_easy);
                put("ic_multi_user", R.drawable.ic_multi_user_easy);
                put("ic_multitasking", R.drawable.ic_multitasking_easy);
                put("ic_multitasking_slide_aside", R.drawable.ic_multitasking_slide_aside_easy);
                put("ic_networks_setting", R.drawable.ic_networks_setting_easy);
                put("ic_notification_led", R.drawable.ic_notification_led_easy);
                put("ic_notification_sound", R.drawable.ic_notification_sound_easy);
                put("ic_one_hand_operation", R.drawable.ic_one_hand_operation_easy);
                put("ic_phone_info", R.drawable.ic_phone_info_easy);
                put("ic_phone_ringtone", R.drawable.ic_phone_ringtone_easy);
                put("ic_print", R.drawable.ic_print_easy);
                put("ic_quiet_time", R.drawable.ic_quiet_time_easy);
                put("ic_quietmode", R.drawable.ic_quietmode_easy);
                put("ic_ringtone_with_vibration", R.drawable.ic_ringtone_with_vibration_easy);
                put("ic_roaming", R.drawable.ic_roaming_easy);
                put("ic_screen_off_effect", R.drawable.ic_screen_off_effect_easy);
                put("ic_screen_timeout", R.drawable.ic_screen_timeout_easy);
                put("ic_settings_battery", R.drawable.ic_settings_battery_easy);
                put("ic_settings_nfc_payment", R.drawable.ic_settings_nfc_payment_easy);
                put("ic_share_connect", R.drawable.ic_share_connect_easy);
                put("ic_sound", R.drawable.ic_sound_easy);
                put("ic_touch_feedback_system", R.drawable.ic_touch_feedback_system_easy);
                put("ic_vibrate_type", R.drawable.ic_vibrate_type_easy);
                put("ic_vibration_strength", R.drawable.ic_vibration_strength_easy);
                put("ic_volumes", R.drawable.ic_volumes_easy);
                put("ic_wifi", R.drawable.ic_wifi_easy);
                put("ic_wise_ringtone", R.drawable.ic_wise_ringtone_easy);
                put("shortcut_multitasking", R.drawable.shortcut_multitasking_easy);
                put("shortcut_screen_off_effect", R.drawable.shortcut_screen_off_effect_easy);

            }
        };

        public static void replace(InitPackageResourcesParam resparam) {
            for (Map.Entry<String, Integer> set : icons.entrySet()) {
                resparam.res.setReplacement(PACKAGE, "drawable", set.getKey(), mModRes.fwd(set.getValue()));
            }

            G2AOSPSkin.log("Replaced all icons");
        }
    }
}
