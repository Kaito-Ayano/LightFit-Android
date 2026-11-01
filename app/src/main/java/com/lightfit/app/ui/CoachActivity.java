package com.lightfit.app.ui;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.lightfit.app.R;
import com.lightfit.app.data.Exercise;
import com.lightfit.app.data.ExerciseRepository;
import com.lightfit.app.store.SecureStore;

import org.json.JSONObject;

import java.util.List;
import java.util.Locale;

/**
 * AI 健康顾问（离线规则版）：本地知识库匹配，不上传、不联网。
 * 对应 BP：Coach — AI health advisor（v1 为离线规则实现，敏感零外泄）。
 */
public class CoachActivity extends AppCompatActivity {

    private LinearLayout chat;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coach);
        chat = findViewById(R.id.chat);
        EditText input = findViewById(R.id.coach_input);
        Button send = findViewById(R.id.btn_send);

        addBubble(greet(), false);

        int[][] chips = {
                {R.string.lf_coach_q_neck, 1},
                {R.string.lf_coach_q_plan, 2},
                {R.string.lf_coach_q_posture, 3},
                {R.string.lf_coach_q_mood, 4},
        };
        LinearLayout chipRow = findViewById(R.id.coach_chips);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 10, 0);
        for (int[] c : chips) {
            final int topic = c[1];
            MaterialButton mb = new MaterialButton(this, null,
                    com.google.android.material.R.attr.materialButtonOutlinedStyle);
            mb.setText(c[0]);
            mb.setAllCaps(false);
            mb.setLayoutParams(lp);
            mb.setOnClickListener(v -> answerFor(topic, null));
            chipRow.addView(mb);
        }

        send.setOnClickListener(v -> {
            String q = input.getText().toString().trim();
            if (q.isEmpty()) return;
            addBubble(q, true);
            input.setText("");
            answerFor(0, q);
        });
    }

    private String greet() {
        SecureStore s = new SecureStore(this);
        try {
            JSONObject plan = new JSONObject(s.get(this, "today_plan", "{}"));
            int mood = plan.optInt("mood", 0);
            int body = plan.optInt("body", 0);
            if (mood == 0 && body == 0) return getString(R.string.lf_coach_greet_hi);
            if (mood <= 2 || body == 1) return getString(R.string.lf_coach_greet_tired);
            if (mood >= 4) return getString(R.string.lf_coach_greet_great);
            return getString(R.string.lf_coach_greet_hi);
        } catch (Exception e) {
            return getString(R.string.lf_coach_greet_hi);
        }
    }

    private void answerFor(int topic, String raw) {
        String q = raw == null ? "" : raw.toLowerCase(Locale.ROOT);
        boolean neck = topic == 1 || q.contains("neck") || q.contains("shoulder")
                || q.contains("颈") || q.contains("肩");
        boolean posture = topic == 3 || q.contains("posture") || q.contains("体态")
                || q.contains("驼") || q.contains("坐姿");
        boolean mood = topic == 4 || q.contains("mood") || q.contains("stress")
                || q.contains("情绪") || q.contains("累") || q.contains("压力");
        boolean plan = topic == 2 || q.contains("plan") || q.contains("train")
                || q.contains("练") || q.contains("计划");

        final String answer;
        if (neck) {
            answer = getString(R.string.lf_coach_a_neck, topNames("neck"));
        } else if (posture) {
            answer = getString(R.string.lf_coach_a_posture, topNames("posture"));
        } else if (mood) {
            answer = getString(R.string.lf_coach_a_mood);
        } else if (plan) {
            answer = getString(R.string.lf_coach_a_plan);
        } else {
            answer = getString(R.string.lf_coach_a_fallback);
        }
        addBubble(answer, false);
    }

    private String topNames(String cat) {
        List<Exercise> list = ExerciseRepository.byCat(this, cat);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(2, list.size()); i++) {
            if (sb.length() > 0) sb.append("、");
            sb.append(list.get(i).name);
        }
        return sb.length() == 0 ? "—" : sb.toString();
    }

    private void addBubble(String text, boolean isUser) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(15);
        tv.setPadding(28, 22, 28, 22);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 6, 0, 6);
        int maxW = getResources().getDisplayMetrics().widthPixels * 7 / 10;
        lp.width = maxW;
        tv.setLayoutParams(lp);
        tv.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        if (isUser) {
            tv.setGravity(Gravity.END);
            tv.setBackgroundColor(Color.rgb(224, 238, 235));
            tv.setTextColor(Color.rgb(15, 65, 64));
            tv.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
        } else {
            tv.setGravity(Gravity.START);
            tv.setBackgroundColor(Color.rgb(22, 50, 79));
            tv.setTextColor(Color.WHITE);
        }
        chat.addView(tv, lp);
        ScrollView sv = findViewById(R.id.chat_scroll);
        if (sv != null) sv.post(() -> sv.fullScroll(ScrollView.FOCUS_DOWN));
    }
}
