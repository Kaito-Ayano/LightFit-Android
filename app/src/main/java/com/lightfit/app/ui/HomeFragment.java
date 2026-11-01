package com.lightfit.app.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.lightfit.app.MainActivity;
import com.lightfit.app.R;
import com.lightfit.app.store.SecureStore;

import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDate;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup vg,
                             @Nullable Bundle b) {
        return inf.inflate(R.layout.fragment_home, vg, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle b) {
        SecureStore s = new SecureStore(requireContext());
        String plan = s.get(requireContext(), "today_plan", "");
        TextView state = v.findViewById(R.id.home_plan_state);
        if (plan.isEmpty()) {
            state.setText(R.string.plan_sub);
        } else {
            try {
                JSONObject o = new JSONObject(plan);
                int done = o.optInt("done", 0);
                int total = o.optJSONArray("ids") == null ? 0 : o.optJSONArray("ids").length();
                state.setText(getString(R.string.today_plan) + " · "
                        + done + "/" + total + " " + getString(R.string.done));
            } catch (Exception e) {
                state.setText(R.string.plan_sub);
            }
        }
        v.findViewById(R.id.btn_go_plan).setOnClickListener(x ->
                ((MainActivity) requireActivity()).switchTo(R.id.nav_plan));
        v.findViewById(R.id.btn_go_library).setOnClickListener(x ->
                ((MainActivity) requireActivity()).switchTo(R.id.nav_library));
        v.findViewById(R.id.btn_go_posture).setOnClickListener(x ->
                ((MainActivity) requireActivity()).switchTo(R.id.nav_posture));
        renderWeek(v, s);
    }

    /** 近 7 天训练柱状图 + 汇总（BP 的 Report 模块轻量版） */
    private void renderWeek(View v, SecureStore s) {
        int[] buckets = new int[7];
        int sessions = 0, minutes = 0;
        long today = LocalDate.now().toEpochDay();
        try {
            JSONArray arr = new JSONArray(s.get(requireContext(), "history_log", "[]"));
            for (int i = 0; i < arr.length(); i++) {
                JSONObject e = arr.getJSONObject(i);
                long day = e.optLong("t", -1);
                int m = e.optInt("m", 0);
                int idx = (int) (day - today) + 6;
                if (idx >= 0 && idx < 7) {
                    buckets[idx] += m;
                    sessions++;
                    minutes += m;
                }
            }
        } catch (Exception ignored) { }
        ((com.lightfit.app.ui.BarView) v.findViewById(R.id.week_bar)).setData(buckets);
        TextView stats = v.findViewById(R.id.home_week_stats);
        if (sessions == 0) {
            stats.setText(R.string.lf_week_empty);
        } else {
            stats.setText(sessions + " " + getString(R.string.done) + " · "
                    + minutes + " " + getString(R.string.lf_minutes_unit));
        }
        ((TextView) v.findViewById(R.id.home_streak)).setText(getString(R.string.lf_streak)
                + " " + streak(buckets));
    }

    /** 连续打卡天数：今天有记录则连到今天，否则从昨天起算（宽松口径） */
    private int streak(int[] buckets) {
        int n = 0;
        if (buckets[6] == 0) {
            for (int i = 5; i >= 0 && buckets[i] > 0; i--) n++;
        } else {
            for (int i = 6; i >= 0 && buckets[i] > 0; i--) n++;
        }
        return n;
    }
}
