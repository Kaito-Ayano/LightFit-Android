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

        TextView title = v.findViewById(R.id.plan_result_title);
        title.setText(getString(R.string.plan_generated)
                + "  ·  " + r.minutes + " " + getString(R.string.lf_minutes_unit));

        LinearLayout list = v.findViewById(R.id.plan_list);
        list.removeAllViews();

        JSONArray ids = new JSONArray();
        for (Exercise e : r.items) {
            ids.put(e.id);
            TextView row = new TextView(requireContext());
            row.setText("  " + e.emoji + " " + e.name
                    + "   " + e.duration + " " + getString(R.string.lf_minutes_unit));
            row.setTextSize(15);
            row.setPadding(8, 10, 8, 10);
            list.addView(row);
        }

        JSONObject save = new JSONObject();
        try {
            save.put("mode", r.modeKey);
            save.put("mood", mood);
            save.put("body", body);
            save.put("ids", ids);
            save.put("done", 0);
        } catch (Exception ignored) { }
        new SecureStore(requireContext()).put(requireContext(), "today_plan", save.toString());
        appendHistory(r.minutes);
        Toast.makeText(requireContext(), R.string.lf_saved, Toast.LENGTH_SHORT).show();
    }
}
