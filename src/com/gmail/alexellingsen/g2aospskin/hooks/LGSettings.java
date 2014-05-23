package com.gmail.alexellingsen.g2aospskin.hooks;

import android.content.res.Resources;
import android.content.res.XModuleResources;
import android.preference.PreferenceActivity;
import com.gmail.alexellingsen.g2aospskin.R;
import com.gmail.alexellingsen.g2aospskin.utils.SettingsHelper;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import java.util.HashMap;
import java.util.Map;

public class LGSettings {

    private static final String PACKAGE = "com.android.settings";

    private static XModuleResources mModRes;
    private static SettingsHelper mSettings;

    public static void init(SettingsHelper settings) {
        mSettings = settings;
    }

    public static void handleInitPackageResources(InitPackageResourcesParam resparam, XModuleResources modRes) {
        if (!resparam.packageName.equals(PACKAGE)) {
            return;
        }

        mModRes = modRes;

        Icons.replace(resparam);
    }

    public static void handleLoadPackage(LoadPackageParam lpparam) {
        if (!lpparam.packageName.equals(PACKAGE)) {
            return;
        }

        if (lpparam.packageName.equals(PACKAGE)) {
            XposedBridge.log("Hooking 'onCreate'");
            XposedHelpers.findAndHookMethod(
                    PACKAGE + ".Settings",
                    lpparam.classLoader,
                    "onCreate",
                    "android.os.Bundle",

                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            PreferenceActivity activity = (PreferenceActivity) param.thisObject;

                            activity.setTheme(android.R.style.Theme_Holo);

                            XposedBridge.log("'onCreate' running");
                        }
                    }
            );
            XposedBridge.log("Hooked 'onCreate'");
        }
    }

    private static class Icons {
        // Auto generated with a custom external tool, might not be correct.
        static HashMap<String, Integer> icons = new HashMap<String, Integer>() {
            {
                put("btn_delete_disabled", R.drawable.btn_delete_disabled);
                put("btn_delete_normal", R.drawable.btn_delete_normal);
                put("btn_delete_pressed", R.drawable.btn_delete_pressed);
                put("connectivity", R.drawable.connectivity);
                put("daydream_settings_list", R.drawable.daydream_settings_list);
                put("ic_access_easy_access", R.drawable.ic_access_easy_access);
                put("ic_accessory", R.drawable.ic_accessory);
                put("ic_airplane", R.drawable.ic_airplane);
                put("ic_call_att", R.drawable.ic_call_att);
                put("ic_cloud", R.drawable.ic_cloud);
                put("ic_guestmode", R.drawable.ic_guestmode);
                put("ic_home_white", R.drawable.ic_home_white);
                put("ic_lockscreen_white", R.drawable.ic_lockscreen_white);
                put("ic_multi_user", R.drawable.ic_multi_user);
                put("ic_network", R.drawable.ic_network);
                put("ic_notification_led", R.drawable.ic_notification_led);
                put("ic_one_hand_operation", R.drawable.ic_one_hand_operation);
                put("ic_p2p_storage", R.drawable.ic_p2p_storage);
                put("ic_print", R.drawable.ic_print);
                put("ic_print_list", R.drawable.ic_print_list);
                put("ic_print_shortcuts", R.drawable.ic_print_shortcuts);
                put("ic_quietmode", R.drawable.ic_quietmode);
                put("ic_settings_about", R.drawable.ic_settings_about);
                put("ic_settings_accessibility", R.drawable.ic_settings_accessibility);
                put("ic_settings_applications", R.drawable.ic_settings_applications);
                put("ic_settings_backup", R.drawable.ic_settings_backup);
                put("ic_settings_battery", R.drawable.ic_settings_battery);
                put("ic_settings_bluetooth2", R.drawable.ic_settings_bluetooth2);
                put("ic_settings_call_settings", R.drawable.ic_settings_call_settings);
                put("ic_settings_data_usage", R.drawable.ic_settings_data_usage);
                put("ic_settings_date_time", R.drawable.ic_settings_date_time);
                put("ic_settings_development", R.drawable.ic_settings_development);
                put("ic_settings_display", R.drawable.ic_settings_display);
                put("ic_settings_gesture", R.drawable.ic_settings_gesture);
                put("ic_settings_language", R.drawable.ic_settings_language);
                put("ic_settings_location", R.drawable.ic_settings_location);
                put("ic_settings_multitasking", R.drawable.ic_settings_multitasking);
                put("ic_settings_multitasking_splitview_list", R.drawable.ic_settings_multitasking);
                put("ic_settings_nfc_payment", R.drawable.ic_settings_nfc_payment);
                put("ic_settings_security", R.drawable.ic_settings_security);
                put("ic_settings_share_connect", R.drawable.ic_settings_share_connect);
                put("ic_settings_sound", R.drawable.ic_settings_sound);
                put("ic_settings_storage", R.drawable.ic_settings_storage);
                put("ic_settings_sync", R.drawable.ic_settings_sync);
                put("ic_settings_wireless", R.drawable.ic_settings_wireless);
                put("ic_taskmanaser_empty", R.drawable.ic_taskmanaser_empty);
                put("list_accessory", R.drawable.list_accessory);
                put("motion_sensor_calibration", R.drawable.motion_sensor_calibration);
                put("quiet_time", R.drawable.quiet_time);
                put("setting_large_icon", R.drawable.setting_large_icon);
                put("shortcut_accessibility", R.drawable.shortcut_accessibility);
                put("shortcut_accessory", R.drawable.shortcut_accessory);
                put("shortcut_account", R.drawable.shortcut_account);
                put("shortcut_apps", R.drawable.shortcut_apps);
                put("shortcut_backup_reset", R.drawable.shortcut_backup_reset);
                put("shortcut_battery", R.drawable.shortcut_battery);
                put("shortcut_bluetooth", R.drawable.shortcut_bluetooth);
                put("shortcut_data_usage", R.drawable.shortcut_data_usage);
                put("shortcut_develops", R.drawable.shortcut_develops);
                put("shortcut_display", R.drawable.shortcut_display);
                put("shortcut_gesture", R.drawable.shortcut_gesture);
                put("shortcut_language", R.drawable.shortcut_language);
                put("shortcut_location", R.drawable.shortcut_location);
                put("shortcut_lockscreen", R.drawable.shortcut_lockscreen);
                put("shortcut_multitasking", R.drawable.shortcut_multitasking);
                put("shortcut_networks_setting", R.drawable.shortcut_networks_setting);
                put("shortcut_one_handed_operation", R.drawable.shortcut_one_handed_operation);
                put("shortcut_phone_info", R.drawable.shortcut_phone_info);
                put("shortcut_security", R.drawable.shortcut_security);
                put("shortcut_share_connect", R.drawable.shortcut_share_connect);
                put("shortcut_sound", R.drawable.shortcut_sound);
                put("shortcut_storage", R.drawable.shortcut_storage);
                put("shortcut_wifi", R.drawable.shortcut_wifi);
                put("shortcuts_multi_user", R.drawable.shortcuts_multi_user);
            }
        };

        public static void replace(InitPackageResourcesParam resparam) {
            for (Map.Entry<String, Integer> set : icons.entrySet()) {
                try {
                    resparam.res.setReplacement(PACKAGE, "drawable", set.getKey(), mModRes.fwd(set.getValue()));
                } catch (Resources.NotFoundException e) {
                    XposedBridge.log("Couldn't find " + set.getKey());
                }
            }

            XposedBridge.log("Replaced all icons");
        }
    }
}
