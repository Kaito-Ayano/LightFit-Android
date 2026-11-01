package com.lightfit.app.data;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** 从 assets/exercises.json 读取运动库（67 条），按当前语言映射文案 */
public class ExerciseRepository {

    private static String RAW;

    private static synchronized String raw(Context c) {
        if (RAW != null) return RAW;
        try {
            InputStream in = c.getAssets().open("exercises.json");
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) bo.write(buf, 0, n);
            in.close();
            RAW = new String(bo.toByteArray(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            RAW = "[]";
        }
        return RAW;
    }

    public static List<Exercise> all(Context c) {
        return byCat(c, null);
    }

    public static List<Exercise> byCat(Context c, String cat) {
        List<Exercise> out = new ArrayList<>();
        String lang = com.lightfit.app.i18n.LocaleHelper.lang(c);
        boolean gymOnly = "gym".equals(cat);
        try {
            JSONArray arr = new JSONArray(raw(c));
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                Exercise e = Exercise.from(o, lang);
                if (cat == null || "all".equals(cat)) { out.add(e); continue; }
                if (gymOnly && e.gym) { out.add(e); continue; }
                if (!gymOnly && e.cat.equals(cat)) out.add(e);
            }
        } catch (Exception ignored) { }
        return out;
    }

    public static List<Exercise> byIds(Context c, Set<Integer> ids) {
        List<Exercise> out = new ArrayList<>();
        for (Exercise e : all(c)) if (ids.contains(e.id)) out.add(e);
        return out;
    }

    public static Set<String> categories(Context c) {
        Set<String> s = new LinkedHashSet<>();
        for (Exercise e : all(c)) s.add(e.cat);
        return s;
    }
}
