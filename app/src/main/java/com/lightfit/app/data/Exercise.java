package com.lightfit.app.data;

import org.json.JSONArray;
import org.json.JSONObject;

/** 运动条目；名称/要点按当前语言取值（运动数据暂无韩语，回落英语） */
public class Exercise {

    public int id;
    public String cat;
    public String emoji;
    public String level;
    public int duration;
    public boolean gym;
    public String name;
    public String tip;

    public static Exercise from(JSONObject o, String lang) {
        Exercise e = new Exercise();
        e.id = o.optInt("id");
        e.cat = o.optString("cat", "general");
        e.emoji = o.optString("emoji", "");
        e.level = o.optString("level", "easy");
        e.duration = o.optInt("duration", 5);
        e.gym = o.optBoolean("gym", false);

        String L = (lang == null || lang.isEmpty()) ? "zh" : lang;
        if ("ko".equals(L)) L = "en";
        String cap = Character.toUpperCase(L.charAt(0)) + L.substring(1);
        e.name = o.optString("name" + cap, o.optString("nameEn", ""));
        e.tip = o.optString("tip" + cap, o.optString("tipEn", ""));
        return e;
    }

    public String levelKey() {
        if ("hard".equals(level)) return "level_hard";
        if ("med".equals(level)) return "level_med";
        return "level_easy";
    }
}
