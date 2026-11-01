package com.lightfit.app.logic;

import com.lightfit.app.data.Exercise;

import java.util.ArrayList;
import java.util.List;

/**
 * 离线规则引擎（对应 BP 核心闭环：Check-in -> Recommend）。
 * 情绪 1-5（1 最差），身体 1-3（1 疲惫）。
 * 模式：easy（低气压日）/ standard / challenge（状态好）。
 */
public final class PlanEngine {

    public static class Result {
        public String modeKey;   // mode_easy / mode_standard / mode_challenge
        public int minutes;
        public List<Exercise> items = new ArrayList<>();
    }

    private PlanEngine() { }

    public static Result build(List<Exercise> pool, int mood, int body) {
        Result r = new Result();
        int budget;
        if (mood <= 2 || body == 1) {
            r.modeKey = "mode_easy";
            budget = 8;
        } else if (mood >= 4 && body == 3) {
            r.modeKey = "mode_challenge";
            budget = 20;
        } else {
            r.modeKey = "mode_standard";
            budget = 15;
        }

        // 优先级：颈椎 -> 体态 -> 肩 -> 腰背 -> 其余补齐
        String[] order = {"neck", "posture", "shoulder", "waist", "back"};
        addByCats(r, pool, order, budget);
        if (r.items.size() < 5) {
            addByCats(r, pool, new String[]{(String) null}, budget);
        }
        for (Exercise e : r.items) r.minutes += e.duration;
        return r;
    }

    private static void addByCats(Result r, List<Exercise> pool, String[] cats, int budget) {
        int sum = 0;
        for (Exercise e : r.items) sum += e.duration;
        for (String cat : cats) {
            for (Exercise e : pool) {
                if (r.items.size() >= 6 || sum >= budget) return;
                if (cat != null && !e.cat.equals(cat)) continue;
                if (sum + e.duration > budget) continue;
                boolean dup = false;
                for (Exercise x : r.items) if (x.id == e.id) dup = true;
                if (dup) continue;
                r.items.add(e);
                sum += e.duration;
            }
        }
    }
}
