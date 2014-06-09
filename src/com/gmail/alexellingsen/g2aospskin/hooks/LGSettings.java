package com.gmail.alexellingsen.g2aospskin.hooks;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XModuleResources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.gmail.alexellingsen.g2aospskin.G2AOSPSkin;
import com.gmail.alexellingsen.g2aospskin.Prefs;
import com.gmail.alexellingsen.g2aospskin.R;
import com.gmail.alexellingsen.g2aospskin.utils.SettingsHelper;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class LGSettings {

    public static final String PACKAGE = "com.android.settings";

    private static XModuleResources mModRes;
    private static SettingsHelper mSettings;

    public static void init(SettingsHelper settings, XModuleResources modRes) {
        mModRes = modRes;
        mSettings = settings;

        try {
            XposedHelpers.findAndHookMethod(
                    "android.preference.Preference",
                    null,
                    "setIcon",
                    int.class,

                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            if (!param.thisObject.getClass().getName().contains("PowerSaveBatteryInfoPreference"))
                                return;

                            if (!mSettings.getBoolean(Prefs.AOSP_THEME_SETTINGS, false))
                                return;

                            XposedHelpers.callMethod(param.thisObject, "setIcon", new Class<?>[]{Drawable.class}, new Object[]{null});

                            param.setResult(true);
                        }
                    }
            );
        } catch (Throwable e) {
            XposedBridge.log(e);
        }

        String[] packages = new String[]{
                "com.android.settings",
                "com.android.settings.quietmode",
                "com.android.settings.vibratecreation",
                "com.android.settings.lockscreen",
                "com.android.settings.handsfreemode",
                "com.android.settings.accounts",
                "com.android.settings.lge"

        };
        final ArrayList<String> packagesList = new ArrayList<String>(Arrays.asList(packages));

        XposedHelpers.findAndHookConstructor(
                "com.android.internal.widget.ActionBarContainer",
                null,
                Context.class,
                AttributeSet.class,

                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Context context = (Context) param.args[0];

                        G2AOSPSkin.log(context.getClass().getPackage().getName());
                        G2AOSPSkin.log(context.getClass().getName());

                        if (packagesList.contains(context.getClass().getPackage().getName()) &&
                                mSettings.getBoolean(Prefs.AOSP_THEME_SETTINGS, false)) {
                            Drawable[] drawables = getDrawables(context);

                            XposedHelpers.setObjectField(param.thisObject, "mBackground", drawables[0]);
                            XposedHelpers.setObjectField(param.thisObject, "mStackedBackground", drawables[1]);
                        }

                        G2AOSPSkin.log("-");
                    }

                    private Drawable[] getDrawables(Context context) {
                        TypedArray a = context.getTheme().obtainStyledAttributes(
                                android.R.style.Widget_Holo_ActionBar_Solid,
                                new int[]{android.R.attr.background, android.R.attr.backgroundStacked});

                        int attributeResourceId1 = a.getResourceId(0, -1);
                        int attributeResourceId2 = a.getResourceId(1, -1);

                        Drawable background = context.getResources().getDrawable(attributeResourceId1);
                        Drawable backgroundStacked = context.getResources().getDrawable(attributeResourceId2);

                        a.recycle();

                        return new Drawable[]{background, backgroundStacked};
                    }
                }
        );
    }

    public static void handleInitPackageResources(InitPackageResourcesParam resparam) {
        if (!resparam.packageName.equals(PACKAGE)) {
            return;
        }

        if (!mSettings.getBoolean(Prefs.AOSP_THEME_SETTINGS, false))
            return;

        Icons.replace(resparam);

        // Replace WiFi signal icons
        resparam.res.setReplacement(PACKAGE, "drawable", "wifi_signal_open", mModRes.fwd(R.drawable.wifi_signal_open));
        resparam.res.setReplacement(PACKAGE, "drawable", "wifi_signal_lock", mModRes.fwd(R.drawable.wifi_signal_lock));
    }

    public static void handleLoadPackage(LoadPackageParam lpparam) {
        if (!lpparam.packageName.equals(PACKAGE)) {
            return;
        }

        if (!mSettings.getBoolean(Prefs.AOSP_THEME_SETTINGS, false))
            return;

        XposedHelpers.findAndHookMethod(
                "com.android.settings.powersave.PowerSaveBatteryInfoPreference",
                lpparam.classLoader,
                "onBindView",
                View.class,

                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        TextView tv = (TextView) XposedHelpers.getObjectField(param.thisObject, "mBatteryLevel");
                        LinearLayout ll = (LinearLayout) tv.getParent();
                        Context context = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");

                        float leftMargin = convertDpToPixels(15, context);

                        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) ll.getLayoutParams();

                        params.setMargins((int) leftMargin,
                                params.topMargin,
                                params.rightMargin,
                                params.bottomMargin);

                        ll.setLayoutParams(params);
                    }

                    private float convertDpToPixels(float dp, Context context) {
                        Resources r = context.getResources();

                        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
                    }
                }
        );

        final String[] activities = new String[]{
                "com.android.settings.Settings",
                "com.android.settings.quietmode.QuietModeMainActivity",
                "com.android.settings.quietmode.QuietModeScheduleSettingsActivity",
                "com.android.settings.quietmode.QuietModeAllowedCallSettingsActivity",
                "com.android.settings.vibratecreation.VibratePicker",
                "com.android.settings.lockscreen.ConfirmLockKnockOn",
                "com.android.settings.lockscreen.ConfirmLockPassword",
                "com.android.settings.lockscreen.ConfirmLockPattern",
                "com.android.settings.handsfreemode.HandsFreeModePreferenceActivity",
                "com.android.settings.accounts.ChooseAccountActivity",
                "com.android.settings.lge.DeviceInfoLgeNetwork",
                "com.android.settings.lge.DeviceInfoLgeStatus",
                "com.android.settings.lge.DeviceInfoLgePhoneIdentity"
        };

        for (String activity : activities) {
            XposedHelpers.findAndHookMethod(
                    activity,
                    lpparam.classLoader,
                    "onCreate",
                    "android.os.Bundle",

                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            PreferenceActivity activity = (PreferenceActivity) param.thisObject;

                            activity.setTheme(android.R.style.Theme_Holo);
                        }
                    }
            );
        }

        String[] dialogs = new String[]{
                "com.android.settings.lge.ScreenOffEffectPopup",
                "com.android.settings.wifi.WifiDialogActivity"
        };

        for (String dialog : dialogs) {
            XposedHelpers.findAndHookMethod(
                    dialog,
                    lpparam.classLoader,
                    "onCreate",
                    Bundle.class,

                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            Activity dialog = (Activity) param.thisObject;

                            dialog.setTheme(android.R.style.Theme_Holo_Dialog);
                        }
                    }
            );
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
                put("ic_launcher_tap_pay", R.drawable.ic_settings_nfc_payment);
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
                put("ic_settings_multitasking_splitview", R.drawable.ic_settings_multitasking);
                put("ic_settings_multitasking_splitview_list", R.drawable.ic_settings_multitasking);
                put("ic_settings_nfc_payment", R.drawable.ic_settings_nfc_payment);
                put("ic_settings_security", R.drawable.ic_settings_security);
                put("ic_settings_share_connect", R.drawable.ic_settings_share_connect);
                put("ic_settings_sound", R.drawable.ic_settings_sound);
                put("ic_settings_storage", R.drawable.ic_settings_storage);
                put("ic_settings_sync", R.drawable.ic_account_sync);
                put("ic_settings_wireless", R.drawable.ic_settings_wireless);
                put("ic_taskmanaser_empty", R.drawable.ic_taskmanaser_empty);
                put("list_accessory", R.drawable.list_accessory);
                put("motion_sensor_calibration", R.drawable.motion_sensor_calibration);
                put("quiet_time", R.drawable.quiet_time);
                put("setting_large_icon", R.drawable.setting_large_icon);
                put("shortcut_accessibility", R.drawable.shortcut_accessibility);
                put("shortcut_accessory", R.drawable.shortcut_accessory);
                put("shortcut_account", R.drawable.shortcut_account);
                put("shortcut_alarm", R.drawable.ic_settings_date_time);
                put("shortcut_apps", R.drawable.shortcut_apps);
                put("shortcut_backup_reset", R.drawable.shortcut_backup_reset);
                put("shortcut_battery", R.drawable.shortcut_battery);
                put("shortcut_bluetooth", R.drawable.shortcut_bluetooth);
                put("shortcut_connectivity", R.drawable.connectivity);
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
                    G2AOSPSkin.log("Couldn't find " + set.getKey());
                }
            }

            G2AOSPSkin.log("Replaced all icons");
        }
    }
}
