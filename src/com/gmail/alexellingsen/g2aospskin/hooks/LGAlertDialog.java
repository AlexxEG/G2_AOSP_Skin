package com.gmail.alexellingsen.g2aospskin.hooks;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.XResources;
import android.view.ContextThemeWrapper;
import com.gmail.alexellingsen.g2aospskin.DialogThemes;
import com.gmail.alexellingsen.g2aospskin.G2AOSPSkin;
import com.gmail.alexellingsen.g2aospskin.utils.SettingsHelper;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import java.util.ArrayList;

public class LGAlertDialog {

    public static final String PACKAGE_NAME_LGE = "com.lge.internal";
    public static final String THEME_LGE_DIALOG = "Theme.LGE.Default.Dialog";
    public static final String THEME_LGE_DIALOG_ALERT = "Theme.LGE.Default.Dialog.Alert";
    public static final String THEME_LGE_WHITE_DIALOG = "Theme.LGE.White.Dialog";
    public static final String THEME_LGE_WHITE_DIALOG_ALERT = "Theme.LGE.White.Dialog.Alert";

    private static SettingsHelper mSettings;
    private static ArrayList<Integer> mLGEThemes;
    private static boolean mIsLGDialog = false;
    private static DialogThemes mDialogTheme;

    public static void init(SettingsHelper settings) {
        mDialogTheme = DialogThemes.getSelectedDialogTheme(settings);

        if (mDialogTheme == DialogThemes.Default) {
            return;
        }

        mSettings = settings;
        mLGEThemes = new ArrayList<Integer>();

        mLGEThemes.add(XResources.getSystem().getIdentifier(THEME_LGE_DIALOG, "style", PACKAGE_NAME_LGE));
        mLGEThemes.add(XResources.getSystem().getIdentifier(THEME_LGE_DIALOG_ALERT, "style", PACKAGE_NAME_LGE));
        mLGEThemes.add(XResources.getSystem().getIdentifier(THEME_LGE_WHITE_DIALOG, "style", PACKAGE_NAME_LGE));
        mLGEThemes.add(XResources.getSystem().getIdentifier(THEME_LGE_WHITE_DIALOG_ALERT, "style", PACKAGE_NAME_LGE));

        try {
            XposedHelpers.findAndHookMethod(
                    "android.app.AlertDialog$Builder",
                    null,
                    "create",

                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            // Set window background to transparent on LG dialogs.
                            if (mIsLGDialog) {
                                AlertDialog dialog = (AlertDialog) param.getResult();

                                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

                                mIsLGDialog = false;
                            }
                        }

                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            try {
                                int theme = XposedHelpers.getIntField(param.thisObject, "mTheme");
                                Object alertParams = XposedHelpers.getObjectField(param.thisObject, "P");
                                Context context = (Context) XposedHelpers.getObjectField(alertParams, "mContext");

                                String title = (String) XposedHelpers.getObjectField(alertParams, "mTitle");

                                G2AOSPSkin.log("AlertDialog title: " + title);
                                G2AOSPSkin.log("AlertDialog theme: " + theme);

                                if (mLGEThemes.contains(theme)) {
                                    int newTheme;

                                    if (mDialogTheme == DialogThemes.Holo_Dark) {
                                        newTheme = android.R.style.Theme_Holo_Dialog;
                                    } else {
                                        newTheme = android.R.style.Theme_Holo_Light_Dialog;
                                    }

                                    Context newContext = new ContextThemeWrapper(context, newTheme);

                                    XposedHelpers.setObjectField(alertParams, "mContext", newContext);
                                    XposedHelpers.setIntField(param.thisObject, "mTheme", newTheme);

                                    mIsLGDialog = true;
                                }
                            } catch (Throwable ignored) {
                            }
                        }
                    }
            );
        } catch (Throwable ignored) {
        }
    }

    public static void handleLoadPackage(LoadPackageParam lpparam) {
        // Not used yet
    }

    public static void handleInitPackageResources(InitPackageResourcesParam resparam) {
        // Not used yet
    }
}
