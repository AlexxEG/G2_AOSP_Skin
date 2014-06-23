package com.gmail.alexellingsen.g2aospskin;

import android.content.res.XModuleResources;
import com.gmail.alexellingsen.g2aospskin.hooks.*;
import com.gmail.alexellingsen.g2aospskin.utils.SettingsHelper;
import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

@SuppressWarnings("UnusedDeclaration")
public class G2AOSPSkin implements IXposedHookZygoteInit, IXposedHookLoadPackage, IXposedHookInitPackageResources {

    public static final boolean DEBUG = true;
    public static final String TAG = "[G2AOSPSkin]";

    private static String MODULE_PATH = null;
    private static SettingsHelper mSettings;

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        MODULE_PATH = startupParam.modulePath;
        mSettings = new SettingsHelper();

        XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, null);

        LGAlertDialog.init(mSettings);
        LGEasySettings.init(mSettings, modRes);
        LGMessenger.init(mSettings, modRes);
        LGSettings.init(mSettings, modRes);
        NavigationBar.init(mSettings, modRes);
        PowerMenu.init(mSettings, modRes);

        XposedBridge.log(TAG + " Debug enabled: " + DEBUG);
    }

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
        LGAlertDialog.handleLoadPackage(lpparam);
        LGEasySettings.handleLoadPackage(lpparam);
        LGMessenger.handleLoadPackage(lpparam);
        LGSettings.handleLoadPackage(lpparam);
        NavigationBar.handleLoadPackage(lpparam);
        PowerMenu.handleLoadPackage(lpparam);
    }

    @Override
    public void handleInitPackageResources(InitPackageResourcesParam resparam) throws Throwable {
        LGAlertDialog.handleInitPackageResources(resparam);
        LGEasySettings.handleInitPackageResources(resparam);
        LGMessenger.handleInitPackageResources(resparam);
        LGSettings.handleInitPackageResources(resparam);
        NavigationBar.handleInitPackageResources(resparam);
        PowerMenu.handleInitPackageResources(resparam);
    }

    public static void log(String msg) {
        if (DEBUG) {
            XposedBridge.log(TAG + " " + msg);
        }
    }

    public static void log(String tag, String msg) {
        if (DEBUG) {
            XposedBridge.log("[" + tag + "]" + " " + msg);
        }
    }
}
