package com.gmail.alexellingsen.g2aospskin;

import com.gmail.alexellingsen.g2aospskin.utils.SettingsHelper;

public enum DialogThemes {
    Default, Holo_Dark, Holo_Light;

    public static final String THEME_DEFAULT = "theme_default";
    public static final String THEME_HOLO_DARK = "holo_dark";
    public static final String THEME_HOLO_LIGHT = "holo_light";

    public static DialogThemes getSelectedDialogTheme(SettingsHelper settings) {
        String theme = settings.getString(Prefs.AOSP_THEME_POWER_MENU, DialogThemes.THEME_DEFAULT);

        if (theme.equals(DialogThemes.THEME_DEFAULT)) {
            return DialogThemes.Default;
        } else if (theme.equals(DialogThemes.THEME_HOLO_DARK)) {
            return DialogThemes.Holo_Dark;
        } else if (theme.equals(DialogThemes.THEME_HOLO_LIGHT)) {
            return DialogThemes.Holo_Light;
        } else {
            throw new IllegalArgumentException();
        }
    }
}