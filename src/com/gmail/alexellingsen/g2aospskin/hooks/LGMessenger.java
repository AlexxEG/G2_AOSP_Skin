package com.gmail.alexellingsen.g2aospskin.hooks;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.content.res.XModuleResources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.gmail.alexellingsen.g2aospskin.R;
import de.robv.android.xposed.IXposedHookZygoteInit.StartupParam;
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

    private static int ID = -1;

    private static ImageView conversationShadow;
    private static LinearLayout rightButtonsLand;
    private static LinearLayout rightButtonsPort;

    public static void init(StartupParam startupParam, XModuleResources modRes) throws Throwable {
        mModRes = modRes;

        try {
            XposedHelpers.findAndHookMethod(
                    "android.view.View",
                    null,
                    "setBackground",
                    "android.graphics.drawable.Drawable",

                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            if (param.thisObject instanceof LinearLayout) {
                                LinearLayout thiz = (LinearLayout) param.thisObject;
                                Context context = thiz.getContext();

                                if (context.getClass().getName().equals("com.android.mms.ui.ComposeMessageActivity")) {
                                    if (ID != -1 && thiz.getId() == ID) {
                                        param.args[0] = null;
                                    }
                                }
                            } else if (param.thisObject instanceof EditText) {
                                if (param.thisObject.getClass().getName().contains("ExEditText")) {
                                    EditText thiz = (EditText) param.thisObject;
                                    Context context = thiz.getContext();

                                    TypedArray a = context.getTheme().obtainStyledAttributes(android.R.style.Theme_Holo_Light, new int[]{android.R.attr.editTextBackground});
                                    int attributeResourceId = a.getResourceId(0, android.R.drawable.edit_text);
                                    Drawable drawable = context.getResources().getDrawable(attributeResourceId);
                                    a.recycle();

                                    param.args[0] = drawable;
                                }
                            }
                        }
                    }
            );

            XposedHelpers.findAndHookMethod(
                    "android.view.View",
                    null,
                    "setBackgroundDrawable",
                    "android.graphics.drawable.Drawable",

                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            if (param.thisObject instanceof EditText) {
                                if (param.thisObject.getClass().getName().contains("ExEditText")) {
                                    EditText thiz = (EditText) param.thisObject;
                                    Context context = thiz.getContext();

                                    TypedArray a = context.getTheme().obtainStyledAttributes(android.R.style.Theme_Holo_Light, new int[]{android.R.attr.editTextBackground});
                                    int attributeResourceId = a.getResourceId(0, android.R.drawable.edit_text);
                                    Drawable drawable = context.getResources().getDrawable(attributeResourceId);
                                    a.recycle();

                                    param.args[0] = drawable;
                                }
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
                            param.args[0] = android.R.style.Theme_Holo_Light;
                        }
                    }
                }
        );

        /*XposedHelpers.findAndHookMethod(
                "android.app.Activity",
                null,
                "onCreate",
                Bundle.class,

                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Toast.makeText((Activity) param.thisObject,
                                ((Activity) param.thisObject).getClass().getName(), Toast.LENGTH_SHORT).show();
                    }
                }
        );*/
    }

    public static void handleInitPackageResources(InitPackageResourcesParam resparam) throws Throwable {
        if (!resparam.packageName.equals(PACKAGE)) {
            return;
        }

        resparam.res.hookLayout(PACKAGE, "layout", "compose_bottom_button_area_right", new XC_LayoutInflated() {
            @Override
            public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
                rightButtonsLand = (LinearLayout) liparam.view.findViewById(
                        liparam.res.getIdentifier("right_area_port", "id", PACKAGE)
                );

                rightButtonsPort = (LinearLayout) liparam.view.findViewById(
                        liparam.res.getIdentifier("right_area_land", "id", PACKAGE)
                );
            }
        });

        resparam.res.hookLayout(PACKAGE, "layout", "conversation_list_screen", new XC_LayoutInflated() {
            @Override
            public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
                conversationShadow = (ImageView) liparam.view.findViewById(
                        liparam.res.getIdentifier("msg_list_title_bar_shadow_img", "id", PACKAGE)
                );
                conversationShadow.setVisibility(View.GONE);
            }
        });

        resparam.res.hookLayout(PACKAGE, "layout", "compose_message_activity_header", new XC_LayoutInflated() {
            @Override
            public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
                int[] identifiers = new int[]{
                        liparam.res.getIdentifier("title", "id", PACKAGE),
                        liparam.res.getIdentifier("subtitle", "id", PACKAGE),
                        liparam.res.getIdentifier("subtitle2", "id", PACKAGE)
                };

                for (int id : identifiers) {
                    ((TextView) liparam.view.findViewById(id)).setTextColor(Color.BLACK);
                }
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

                        ID = view.getId();
                    }
                }
        );

        XposedHelpers.findAndHookMethod(
                PACKAGE + ".ui.ComposeMessageActivity",
                lpparam.classLoader,
                "onCreate",
                Bundle.class,

                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        /*rightButtonsLand.setOrientation(LinearLayout.HORIZONTAL);
                        rightButtonsPort.setOrientation(LinearLayout.HORIZONTAL);*/
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
                    "android.os.Bundle",

                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            ((Activity) param.thisObject).setTheme(android.R.style.Theme_Holo_Light);
                        }
                    }
            );
        }

        XC_MethodHook hook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                param.args[0] = new ContextThemeWrapper((android.content.Context) param.args[0],
                        android.R.style.Theme_Holo_Light);
            }

            /*@Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                EditText thiz = (EditText) param.thisObject;
                Context context = thiz.getContext();

                TypedArray a = context.getTheme().obtainStyledAttributes(android.R.style.Theme_Holo_Light, new int[]{android.R.attr.editTextBackground});
                int attributeResourceId = a.getResourceId(0, android.R.drawable.edit_text);
                Drawable drawable = context.getResources().getDrawable(attributeResourceId);
                a.recycle();

                thiz.setBackground(drawable);
            }*/
        };

        XposedHelpers.findAndHookConstructor(
                PACKAGE + ".pinchApi.ExEditText",
                lpparam.classLoader,
                "android.content.Context",

                hook
        );

        XposedHelpers.findAndHookConstructor(
                PACKAGE + ".pinchApi.ExEditText",
                lpparam.classLoader,
                "android.content.Context",
                "android.util.AttributeSet",

                hook
        );

        XposedHelpers.findAndHookConstructor(
                PACKAGE + ".pinchApi.ExEditText",
                lpparam.classLoader,
                "android.content.Context",
                "android.util.AttributeSet",
                "int",

                hook
        );

        XposedBridge.log("Hooked all constructors");
    }
}
