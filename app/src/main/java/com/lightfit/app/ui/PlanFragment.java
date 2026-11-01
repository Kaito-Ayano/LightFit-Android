package com.lightfit.app.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.content.Intent;

import com.lightfit.app.R;
import com.lightfit.app.data.Exercise;
import com.lightfit.app.data.ExerciseRepository;
import com.lightfit.app.logic.PlanEngine;
import com.lightfit.app.store.SecureStore;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class PlanFragment extends Fragment {

    private int mood = 0;   // 1-5
    private int body = 0;   // 1-3

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup vg,
                             @Nullable Bundle b) {
        return inf.inflate(R.layout.fragment_plan, vg, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle b) {
        int[] moodIds = {R.id.mood_1, R.id.mood_2, R.id.mood_3, R.id.mood_4, R.id.mood_5};
        for (int i = 0; i < moodIds.length; i++) {
            final int val = i + 1;
            v.findViewById(moodIds[i]).setOnClickListener(x -> mood = val);
        }
        int[] bodyIds = {R.id.body_1, R.id.body_2, R.id.body_3};
        for (int i = 0; i < bodyIds.length; i++) {
            final int val = i + 1;
            v.findViewById(bodyIds[i]).setOnClickListener(x -> body = val);
        }
        v.findViewById(R.id.btn_generate).setOnClickListener(x -> generate(v));
    }

    /** 训练日志：本地加密存储，仅 {天序号, 分钟}，上限 500 条 */
    private void appendHistory(int minutes) {
        SecureStore s = new SecureStore(requireContext());
        String raw = s.get(requireContext(), "history_log", "[]");
        try {
            org.json.JSONArray arr = new org.json.JSONArray(raw);
            org.json.JSONObject e = new org.json.JSONObject();
            e.put("t", java.time.LocalDate.now().toEpochDay());
            e.put("m", minutes);
            org.json.JSONArray out = new org.json.JSONArray();
            out.put(e);
            for (int i = Math.max(0, arr.length() - 499); i < arr.length(); i++) {
                out.put(arr.get(i));
            }
            s.put(requireContext(), "history_log", out.toString());
        } catch (Exception ignored) { }
    }

    private void generate(View v) {
        if (mood == 0 || body == 0) {
            Toast.makeText(requireContext(), R.string.lf_plan_empty, Toast.LENGTH_SHORT).show();
            return;
        }
        List<Exercise> pool = ExerciseRepository.all(requireContext());
        PlanEngine.Result r = PlanEngine.build(pool, mood, body);
        savePlan(r.items, r.modeKey, 0);
        renderPlan(v, r.items);
        appendHistory(r.minutes);
        Toast.makeText(requireContext(), R.string.lf_saved, Toast.LENGTH_SHORT).show();
    }

    /** 存入今日计划（本地加密）：ids + 分钟数 + 完成进度 */
    private void savePlan(List<Exercise> items, String modeKey, int done) {
        JSONArray ids = new JSONArray();
        int minutes = 0;
        for (Exercise e : items) {
            ids.put(e.id);
            minutes += e.duration;
        }
        JSONObject save = new JSONObject();
        try {
            save.put("mode", modeKey);
            save.put("mood", mood);
            save.put("body", body);
            save.put("ids", ids);
            save.put("minutes", minutes);
            save.put("done", done);
        } catch (Exception ignored) { }
        new SecureStore(requireContext()).put(requireContext(), "today_plan", save.toString());
    }

    /** 渲染计划列表：点击进入播放页，长按移除（v8.0「计划可调整」） */
    private void renderPlan(View v, List<Exercise> items) {
        int minutes = 0;
        for (Exercise e : items) minutes += e.duration;
        ((TextView) v.findViewById(R.id.plan_result_title)).setText(
                getString(R.string.plan_generated) + "  ·  " + minutes
                        + " " + getString(R.string.lf_minutes_unit));
        LinearLayout list = v.findViewById(R.id.plan_list);
        list.removeAllViews();
        for (Exercise e : items) {
            TextView row = new TextView(requireContext());
            row.setText("  " + e.emoji + " " + e.name
                    + "   " + e.duration + " " + getString(R.string.lf_minutes_unit));
            row.setTextSize(15);
            row.setPadding(8, 10, 8, 10);
            row.setOnClickListener(x -> startActivity(
                    new Intent(requireContext(), PlanPlayerActivity.class)));
            row.setOnLongClickListener(x -> {
                removeExercise(v, e.id);
                return true;
            });
            list.addView(row);
        }
    }

    /** 长按移除动作：同步更新今日计划与总分钟数 */
    private void removeExercise(View v, int id) {
        SecureStore s = new SecureStore(requireContext());
        List<Exercise> all = ExerciseRepository.all(requireContext());
        try {
            JSONObject plan = new JSONObject(s.get(requireContext(), "today_plan", "{}"));
            JSONArray ids = plan.optJSONArray("ids");
            if (ids == null) return;
            JSONArray kept = new JSONArray();
            for (int i = 0; i < ids.length(); i++) {
                if (ids.optInt(i) != id) kept.put(ids.optInt(i));
            }
            plan.put("ids", kept);
            plan.put("done", Math.min(plan.optInt("done", 0), kept.length()));
            ((LinearLayout) v.findViewById(R.id.plan_list)).removeAllViews();
            if (kept.length() == 0) {
                s.put(requireContext(), "today_plan", "{}");
                ((TextView) v.findViewById(R.id.plan_result_title)).setText(
                        getString(R.string.lf_plan_empty));
            } else {
                List<Exercise> items = new java.util.ArrayList<>();
                for (Exercise e : all) {
                    boolean found = false;
                    for (int i = 0; i < kept.length(); i++) {
                        if (kept.optInt(i) == e.id) { found = true; break; }
                    }
                    if (found) items.add(e);
                }
                s.put(requireContext(), "today_plan", plan.toString());
                renderPlan(v, items);
            }
            Toast.makeText(requireContext(), R.string.lf_plan_removed, Toast.LENGTH_SHORT).show();
        } catch (Exception ignored) { }
    }
}
