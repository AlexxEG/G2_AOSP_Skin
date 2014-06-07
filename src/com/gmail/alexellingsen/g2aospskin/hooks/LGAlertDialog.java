package com.gmail.alexellingsen.g2aospskin.hooks;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.XResources;
import android.view.ContextThemeWrapper;
import com.gmail.alexellingsen.g2aospskin.Prefs;
import com.gmail.alexellingsen.g2aospskin.utils.SettingsHelper;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class LGAlertDialog {

    public static final String PACKAGE_NAME_LGE = "com.lge.internal";
    public static final String THEME_LGE_DIALOG = "Theme.LGE.Default.Dialog";
    public static final String THEME_LGE_DIALOG_ALERT = "Theme.LGE.Default.Dialog.Alert";

    private static SettingsHelper mSettings;
    private static int mDialogStyleLGE;
    private static int mDialogAlertStyleLGE;

    private static boolean mIsLGDialog = false;

    public static void init(SettingsHelper settings) {
        mSettings = settings;

        mDialogStyleLGE = XResources.getSystem().getIdentifier(THEME_LGE_DIALOG, "style", PACKAGE_NAME_LGE);
        mDialogAlertStyleLGE = XResources.getSystem().getIdentifier(THEME_LGE_DIALOG_ALERT, "style", PACKAGE_NAME_LGE);

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
                                if (!mSettings.getBoolean(Prefs.AOSP_THEME_LG_DIALOG, false))
                                    return;

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

                                if (theme == mDialogStyleLGE || theme == mDialogAlertStyleLGE) {
                                    if (!mSettings.getBoolean(Prefs.AOSP_THEME_LG_DIALOG, false))
                                        return;

                                    Context newContext = new ContextThemeWrapper(context, android.R.style.Theme_Holo_Dialog);

                                    XposedHelpers.setObjectField(alertParams, "mContext", newContext);
                                    XposedHelpers.setIntField(param.thisObject, "mTheme", android.R.style.Theme_Holo_Dialog);

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
