package com.lightfit.app.i18n;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import java.util.Locale;

/** 轻量应用内多语言：zh / en / ru / kk / ko，空值跟随系统 */
public final class LocaleHelper {

    private static final String PREF = "lightfit_prefs";
    private static final String KEY_LANG = "lang";

    private LocaleHelper() { }

    public static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public static String lang(Context c) {
        return prefs(c).getString(KEY_LANG, "");
    }

    public static void setLang(Context c, String code) {
        prefs(c).edit().putString(KEY_LANG, code).apply();
    }

    public static Context wrap(Context base) {
        String tag = lang(base);
        Locale loc = (tag == null || tag.isEmpty())
                ? Locale.getDefault()
                : Locale.forLanguageTag(tag);
        Configuration cfg = new Configuration();
        cfg.setLocale(loc);
        return base.createConfigurationContext(cfg);
    }
}
