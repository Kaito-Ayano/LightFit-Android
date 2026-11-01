package com.lightfit.app.ui;

import android.os.Bundle;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.lightfit.app.R;
import com.lightfit.app.store.SecureStore;

import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** 数据报告（BP Report 模块轻量版）：周 / 月柱状图 + 汇总统计 */
public class ReportActivity extends AppCompatActivity {

    private BarView bar;
    private TextView stats;
    private final List<JSONObject> log = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        bar = findViewById(R.id.report_bar);
        stats = findViewById(R.id.report_stats);

        SecureStore s = new SecureStore(this);
        try {
            JSONArray arr = new JSONArray(s.get(this, "history_log", "[]"));
            for (int i = 0; i < arr.length(); i++) log.add(arr.getJSONObject(i));
        } catch (Exception ignored) { }

        RadioGroup seg = findViewById(R.id.report_seg);
        seg.setOnCheckedChangeListener((g, id) ->
                render(id == R.id.seg_month ? 30 : 7));
        render(7);
    }

    private void render(int window) {
        int[] buckets = new int[window];
        long today = LocalDate.now().toEpochDay();
        int sessions = 0, minutes = 0;
        for (JSONObject e : log) {
            long day = e.optLong("t", -1);
            int m = e.optInt("m", 0);
            int idx = (int) (day - today) + window - 1;
            if (idx >= 0 && idx < window) {
                buckets[idx] += m;
                sessions++;
                minutes += m;
            }
        }
        bar.setData(buckets);

        int active = 0, best = 0, run = 0;
        for (int v : buckets) {
            if (v > 0) {
                active++;
                best = Math.max(best, ++run);
            } else {
                run = 0;
            }
        }
        if (active == 0) {
            stats.setText(R.string.lf_report_empty);
            return;
        }
        stats.setText(getString(R.string.lf_report_sessions) + ": " + sessions + "   "
                + getString(R.string.lf_report_minutes) + ": " + minutes + "\n"
                + getString(R.string.lf_report_active) + ": " + active + "   "
                + getString(R.string.lf_report_best) + ": " + best);
    }
}
