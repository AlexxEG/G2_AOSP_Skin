package com.gmail.alexellingsen.g2aospskin;

import android.content.res.XModuleResources;
import com.gmail.alexellingsen.g2aospskin.hooks.LGEasySettings;
import com.gmail.alexellingsen.g2aospskin.hooks.LGMessenger;
import com.gmail.alexellingsen.g2aospskin.hooks.LGSettings;
import com.gmail.alexellingsen.g2aospskin.utils.SettingsHelper;
import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

@SuppressWarnings("UnusedDeclaration")
public class G2AOSPSkin implements IXposedHookZygoteInit, IXposedHookLoadPackage, IXposedHookInitPackageResources {

    private static String MODULE_PATH = null;
    private static SettingsHelper mSettings;

    @Override
    public void handleInitPackageResources(InitPackageResourcesParam resparam) throws Throwable {
        XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, resparam.res);

        LGEasySettings.handleInitPackageResources(resparam, modRes);
        LGMessenger.handleInitPackageResources(resparam);
        LGSettings.handleInitPackageResources(resparam, modRes);
    }

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
        LGEasySettings.handleLoadPackage(lpparam);
        LGMessenger.handleLoadPackage(lpparam);
        LGSettings.handleLoadPackage(lpparam);
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        MODULE_PATH = startupParam.modulePath;
        mSettings = new SettingsHelper();

        XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, null);

        LGEasySettings.init(mSettings);
        LGMessenger.init(startupParam, modRes);
        LGSettings.init(mSettings);
    }
}
