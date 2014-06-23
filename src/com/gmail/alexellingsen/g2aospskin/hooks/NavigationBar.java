package com.gmail.alexellingsen.g2aospskin.hooks;

import android.content.Context;
import android.content.res.XModuleResources;
import android.hardware.input.InputManager;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import com.gmail.alexellingsen.g2aospskin.G2AOSPSkin;
import com.gmail.alexellingsen.g2aospskin.utils.SettingsHelper;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

@SuppressWarnings({"UnusedDeclaration"})
public class NavigationBar {

    private static final String PACKAGE = "com.android.systemui";

    private static XModuleResources mModRes;
    private static SettingsHelper mSettings;

    private static View.OnClickListener mOldListener;

    public static void init(SettingsHelper settings, XModuleResources modRes) {
        mModRes = modRes;
        mSettings = settings;
    }

    public static void handleLoadPackage(LoadPackageParam lpparam) {
        if (!lpparam.packageName.equals(PACKAGE))
            return;

        try {
            XposedHelpers.findAndHookMethod(
                    "com.lge.systemui.navigationbar.NavigationThemeResource",
                    lpparam.classLoader,
                    "updateThemeResources",

                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            // Make the theme engine use chosen theme keys when the navigation bar is transparent or black.
                            // By default it forces the stock key theme otherwise, for some reason...

                            XposedHelpers.setBooleanField(param.thisObject, "mIsTransparent", false);
                            XposedHelpers.setBooleanField(param.thisObject, "mIsBlack", false);
                        }
                    }
            );

            XposedHelpers.findAndHookConstructor(
                    "com.lge.systemui.navigationbar.RecentAppsButton",
                    lpparam.classLoader,
                    Context.class,
                    AttributeSet.class,
                    int.class,
                    boolean.class,

                    new XC_MethodHook() {
                        public boolean dontClick = false;

                        @Override
                        protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
                            final ImageView button = (ImageView) param.thisObject;

                            button.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (dontClick) {
                                        dontClick = false;
                                        return;
                                    }

                                    if (mOldListener != null)
                                        mOldListener.onClick(v);
                                }
                            });

                            button.setOnLongClickListener(new View.OnLongClickListener() {
                                @Override
                                public boolean onLongClick(View v) {
                                    try {
                                        sendMenuClick();

                                        dontClick = true;
                                    } catch (Throwable e) {
                                        G2AOSPSkin.log(e.toString());
                                    }

                                    return true;
                                }

                                void sendMenuClick() {
                                    long downTime = SystemClock.uptimeMillis();
                                    InputManager im = (InputManager) XposedHelpers.callStaticMethod(InputManager.class, "getInstance");

                                    XposedHelpers.callMethod(im, "injectInputEvent",
                                            new KeyEvent(downTime - 50, downTime - 50, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MENU, 0), 0);
                                    XposedHelpers.callMethod(im, "injectInputEvent",
                                            new KeyEvent(downTime - 50, downTime - 25, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MENU, 0), 0);
                                }
                            });
                        }
                    }
            );

            XposedHelpers.findAndHookConstructor(
                    "com.android.systemui.statusbar.phone.PhoneStatusBar",
                    lpparam.classLoader,

                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            mOldListener = (View.OnClickListener) XposedHelpers.getObjectField(param.thisObject, "mRecentsClickListener");
                        }
                    }
            );
        } catch (Throwable e) {
            XposedBridge.log(e);
        }
    }

    public static void handleInitPackageResources(InitPackageResourcesParam resparam) {
        if (!resparam.packageName.equals(PACKAGE))
            return;
    }
}
