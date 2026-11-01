package com.lightfit.app.ui;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.lightfit.app.R;
import com.lightfit.app.data.Exercise;
import com.lightfit.app.data.ExerciseRepository;
import com.lightfit.app.store.SecureStore;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 训练播放页（BP Correct -> Coach 闭环的落地点）。
 * 逐动作倒计时 + 要点提示；全部完成标记今日计划完成。
 */
public class PlanPlayerActivity extends AppCompatActivity {

    private final List<Exercise> items = new ArrayList<>();
    private int index = 0;
    private int totalSec = 0;      // 当前动作总秒数
    private int remainSec = 0;
    private CountDownTimer timer;
    private boolean running = false;

    private TextView tvEmoji, tvName, tvTip, tvCount, tvIdx;
    private ProgressBar progress;
    private Button btnStart;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        tvEmoji = findViewById(R.id.player_emoji);
        tvName = findViewById(R.id.player_name);
        tvTip = findViewById(R.id.player_tip);
        tvCount = findViewById(R.id.player_count);
        tvIdx = findViewById(R.id.player_idx);
        progress = findViewById(R.id.player_progress);
        findViewById(R.id.btn_prev).setOnClickListener(v -> move(-1));
        findViewById(R.id.btn_next).setOnClickListener(v -> nextAction());
        btnStart = findViewById(R.id.btn_start_pause);
        btnStart.setOnClickListener(v -> toggleTimer());

        loadPlan();
    }

    private void loadPlan() {
        SecureStore s = new SecureStore(this);
        String raw = s.get(this, "today_plan", "");
        Set<Integer> ids = new HashSet<>();
        try {
            JSONObject plan = new JSONObject(raw);
            JSONArray arr = plan.optJSONArray("ids");
            if (arr == null) throw new Exception("no ids");
            for (int i = 0; i < arr.length(); i++) ids.add(arr.optInt(i));
        } catch (Exception e) {
            Toast.makeText(this, R.string.lf_plan_empty, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        for (Exercise ex : ExerciseRepository.all(this)) {
            if (ids.contains(ex.id)) items.add(ex);
        }
        if (items.isEmpty()) {
            Toast.makeText(this, R.string.lf_plan_empty, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        showIndex();
    }

    private void showIndex() {
        Exercise e = items.get(index);
        tvEmoji.setText(e.emoji);
        tvName.setText(e.name);
        tvTip.setText(e.tip);
        tvIdx.setText((index + 1) + " / " + items.size());
        totalSec = remainSec = e.duration * 60;
        updateCount();
        stopTimer();
        btnStart.setText(R.string.lf_player_start);
        running = false;
    }

    private void updateCount() {
        tvCount.setText(remainSec + "s / " + totalSec + "s");
        progress.setProgress(totalSec <= 0 ? 0 : (int) ((totalSec - remainSec) * 1000L / totalSec));
    }

    private void toggleTimer() {
        if (running) {
            stopTimer();
            btnStart.setText(R.string.lf_player_start);
        } else {
            startTimer();
            btnStart.setText(R.string.lf_player_pause);
        }
    }

    private void startTimer() {
        running = true;
        timer = new CountDownTimer(remainSec * 1000L + 100, 500) {
            @Override
            public void onTick(long ms) {
                remainSec = (int) ((ms + 999) / 1000);
                updateCount();
            }

            @Override
            public void onFinish() {
                running = false;
                nextAction();
            }
        }.start();
    }

    private void stopTimer() {
        if (timer != null) timer.cancel();
        timer = null;
        running = false;
    }

    private void nextAction() {
        if (index + 1 < items.size()) {
            index++;
            showIndex();
        } else {
            markDone();
        }
    }

    private void move(int delta) {
        int ni = index + delta;
        if (ni < 0 || ni >= items.size()) {
            Toast.makeText(this, R.string.lf_player_prev, Toast.LENGTH_SHORT).show();
            return;
        }
        index = ni;
        showIndex();
    }

    private void markDone() {
        stopTimer();
        SecureStore s = new SecureStore(this);
        try {
            JSONObject plan = new JSONObject(s.get(this, "today_plan", "{}"));
            plan.put("done", items.size());
            plan.put("completed_t", System.currentTimeMillis());
            s.put(this, "today_plan", plan.toString());
        } catch (Exception ignored) { }
        Toast.makeText(this, R.string.lf_player_finish, Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
    }
}
