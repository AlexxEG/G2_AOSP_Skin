package com.gmail.alexellingsen.g2aospskin.hooks;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.XModuleResources;
import android.content.res.XResources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.*;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.gmail.alexellingsen.g2aospskin.Prefs;
import com.gmail.alexellingsen.g2aospskin.R;
import com.gmail.alexellingsen.g2aospskin.utils.SettingsHelper;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class PowerMenu {

    public static final String PACKAGE_NAME = "android";
    public static final String PACKAGE_NAME_LGE = "com.lge.internal";
    public static final String PACKAGE_NAME_UI = "com.lge.provider.systemui";

    private static SettingsHelper mSettings;

    public static void init(XModuleResources modRes, SettingsHelper settings) {
        mSettings = settings;

        if (mSettings.getBoolean(Prefs.AOSP_THEME_POWER_MENU, false)) {
            XResources.setSystemWideReplacement(PACKAGE_NAME, "drawable", "ic_lock_power_off", modRes.fwd(R.drawable.ic_lock_power_off));
            XResources.setSystemWideReplacement(PACKAGE_NAME_LGE, "drawable", "ic_lock_restart", modRes.fwd(R.drawable.ic_lock_reboot));
            XResources.setSystemWideReplacement(PACKAGE_NAME, "drawable", "ic_lock_airplane_mode", modRes.fwd(R.drawable.ic_lock_airplane_mode));
            XResources.setSystemWideReplacement(PACKAGE_NAME, "drawable", "ic_lock_airplane_mode_off", modRes.fwd(R.drawable.ic_lock_airplane_mode_off));
            XResources.setSystemWideReplacement(PACKAGE_NAME, "drawable", "ic_audio_vol_mute", modRes.fwd(R.drawable.ic_audio_vol_mute));
            XResources.setSystemWideReplacement(PACKAGE_NAME, "drawable", "ic_audio_ring_notif_vibrate", modRes.fwd(R.drawable.ic_audio_ring_notif_vibrate));
            XResources.setSystemWideReplacement(PACKAGE_NAME, "drawable", "ic_audio_vol", modRes.fwd(R.drawable.ic_audio_vol));
        }
    }

    public static void handleInitPackageResources(final InitPackageResourcesParam resparam, XModuleResources modRes) {
        // Not used yet
    }

    public static void handleLoadPackage(final LoadPackageParam lpparam) {
        if (!lpparam.packageName.equals(PACKAGE_NAME) &&
                !lpparam.packageName.equals(PACKAGE_NAME_UI))
            return;

        // No need to do the hooks if option is not enabled, especially because a reboot
        // is required either way.
        if (!mSettings.getBoolean(Prefs.AOSP_THEME_POWER_MENU, false)) {
            return;
        }

        if (lpparam.packageName.equals(PACKAGE_NAME)) {
            XposedHelpers.findAndHookConstructor(
                    "com.android.internal.policy.impl.GlobalActions$GlobalActionsDialog",
                    lpparam.classLoader,
                    Context.class,
                    "com.android.internal.app.AlertController.AlertParams",

                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            Context context = (Context) param.args[0];
                            ContextThemeWrapper newContext = new ContextThemeWrapper(context, android.R.style.Theme_Holo_Dialog);

                            // Set new context with with AOSP style.
                            param.args[0] = newContext;
                        }
                    }
            );

            XposedHelpers.findAndHookMethod(
                    "com.android.internal.policy.impl.GlobalActions$GlobalActionsDialog",
                    lpparam.classLoader,
                    "onCreate",
                    Bundle.class,

                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            Dialog dialog = (Dialog) param.thisObject;
                            Context context = (Context) XposedHelpers.getObjectField(dialog, "mContext");
                            Resources resources = context.getResources();
                            ListView lv = (ListView) XposedHelpers.callMethod(param.thisObject, "getListView");

                            // Set the divider to a lighter variant.
                            lv.setDivider(resources.getDrawable(android.R.drawable.divider_horizontal_dark));
                        }
                    }
            );

            // Set the text color & font of each item in dialog.
            XC_MethodHook fixTextHook = new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Context context = (Context) param.args[0];
                    View view = (View) param.getResult();
                    Resources resources = context.getResources();

                    TextView textMessage = (TextView) view.findViewById(
                            resources.getIdentifier("message", "id", PACKAGE_NAME)
                    );

                    int color = resources.getColor(android.R.color.primary_text_dark);

                    textMessage.setTextColor(color);
                    textMessage.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
                }
            };

            XposedHelpers.findAndHookMethod(
                    "com.android.internal.policy.impl.GlobalActions$SinglePressAction",
                    lpparam.classLoader,
                    "create",
                    Context.class,
                    View.class,
                    ViewGroup.class,
                    LayoutInflater.class,

                    fixTextHook
            );

            XposedHelpers.findAndHookMethod(
                    "com.android.internal.policy.impl.GlobalActions$ToggleAction",
                    lpparam.classLoader,
                    "create",
                    Context.class,
                    View.class,
                    ViewGroup.class,
                    LayoutInflater.class,

                    fixTextHook
            );
        } else if (lpparam.packageName.equals(PACKAGE_NAME_UI)) {
            XposedHelpers.findAndHookMethod(
                    "com.android.internal.app.AlertController",
                    lpparam.classLoader,
                    "setupTitle",
                    LinearLayout.class,

                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            Window mWindow = (Window) XposedHelpers.getObjectField(param.thisObject, "mWindow");

                            // Check for 'GlobalActions' title which should be the Power menu.
                            if (mWindow.getAttributes().getTitle().equals("GlobalActions")) {
                                XposedHelpers.setObjectField(param.thisObject, "mTitle", null);
                            }
                        }
                    }
            );
        }
    }
}
