package com.gmail.alexellingsen.g2aospskin.hooks;

import android.app.Activity;
import android.content.Context;
import android.content.res.XModuleResources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.gmail.alexellingsen.g2aospskin.R;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import java.util.ArrayList;

public class LGMessenger {

    private static final String PACKAGE = "com.android.mms";

    private static XModuleResources mModRes;
    private static int mID = -1;
    private static ImageView mConversationShadow;

    public static void init(XModuleResources modRes) throws Throwable {
        mModRes = modRes;

        try {
            XposedHelpers.findAndHookMethod(
                    "android.view.View",
                    null,
                    "setBackground",
                    Drawable.class,

                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            if (param.thisObject instanceof LinearLayout) {
                                LinearLayout thiz = (LinearLayout) param.thisObject;
                                Context context = thiz.getContext();

                                if (context.getClass().getName().equals("com.android.mms.ui.ComposeMessageActivity")) {
                                    if (mID != -1 && thiz.getId() == mID) {
                                        param.args[0] = null;
                                    }
                                }
                            } else if (param.thisObject.getClass().getName().equals("com.android.mms.pinchApi.ExEditText")) {
                                param.args[0] = null;
                            }
                        }
                    }
            );

            XposedHelpers.findAndHookMethod(
                    "android.view.View",
                    null,
                    "setBackgroundDrawable",
                    Drawable.class,

                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            if (param.thisObject.getClass().getName().equals("com.android.mms.pinchApi.ExEditText")) {
                                param.args[0] = null;
                            }
                        }
                    }
            );
        } catch (Throwable ignored) {
            XposedBridge.log("Couldn't hook LinearLayout in init");
        }

        XposedHelpers.findAndHookMethod(
                "android.view.ContextThemeWrapper",
                null,
                "setTheme",
                int.class,

                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        if (param.thisObject.getClass().getPackage().getName().contains("com.android.mms.ui")) {
                            param.args[0] = android.R.style.Theme_Holo_Light_DarkActionBar;
                        }
                    }
                }
        );
    }

    public static void handleInitPackageResources(InitPackageResourcesParam resparam) throws Throwable {
        if (!resparam.packageName.equals(PACKAGE)) {
            return;
        }

        resparam.res.hookLayout(PACKAGE, "layout", "conversation_list_screen", new XC_LayoutInflated() {
            @Override
            public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
                mConversationShadow = (ImageView) liparam.view.findViewById(
                        liparam.res.getIdentifier("msg_list_title_bar_shadow_img", "id", PACKAGE)
                );
                mConversationShadow.setVisibility(View.GONE);
            }
        });

        resparam.res.setReplacement(PACKAGE, "drawable", "ic_add", mModRes.fwd(R.drawable.ic_action_content_new));
        resparam.res.setReplacement(PACKAGE, "drawable", "ic_menu_add", mModRes.fwd(R.drawable.ic_action_content_new));
        resparam.res.setReplacement(PACKAGE, "drawable", "ic_menu_call", mModRes.fwd(R.drawable.ic_action_device_access_call));
        resparam.res.setReplacement(PACKAGE, "drawable", "ic_menu_composemsg", mModRes.fwd(R.drawable.ic_action_content_new_email));
    }

    public static void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals(PACKAGE)) {
            return;
        }

        XposedHelpers.findAndHookMethod(
                PACKAGE + ".ui.ComposeMessageFragment",
                lpparam.classLoader,
                "initResourceRefs",

                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        ViewGroup view = (ViewGroup) XposedHelpers.getObjectField(param.thisObject, "mEditTextLayout");

                        mID = view.getId();
                    }
                }
        );

        ArrayList<String> classes = new ArrayList<String>();

        classes.add(PACKAGE + ".ui.ConversationList");
        classes.add(PACKAGE + ".ui.ComposeMessageActivity");
        classes.add(PACKAGE + ".ui.SmsPreference");
        classes.add(PACKAGE + ".ui.FloatingWrapper");

        for (String c : classes) {
            XposedHelpers.findAndHookMethod(
                    c,
                    lpparam.classLoader,
                    "onCreate",
                    Bundle.class,

                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            ((Activity) param.thisObject).setTheme(android.R.style.Theme_Holo_Light_DarkActionBar);
                        }
                    }
            );
        }

        XC_MethodHook hook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                param.args[0] = new ContextThemeWrapper((android.content.Context) param.args[0],
                        android.R.style.Theme_Holo_Light_DarkActionBar);
            }
        };

        XposedHelpers.findAndHookConstructor(
                PACKAGE + ".pinchApi.ExEditText",
                lpparam.classLoader,
                Context.class,

                hook
        );

        XposedHelpers.findAndHookConstructor(
                PACKAGE + ".pinchApi.ExEditText",
                lpparam.classLoader,
                Context.class,
                AttributeSet.class,

                hook
        );

        XposedHelpers.findAndHookConstructor(
                PACKAGE + ".pinchApi.ExEditText",
                lpparam.classLoader,
                Context.class,
                AttributeSet.class,
                int.class,

                hook
        );

        XposedBridge.log("Hooked all constructors");
    }
}
