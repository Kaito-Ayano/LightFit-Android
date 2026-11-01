package com.lightfit.app.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.lightfit.app.R;
import com.lightfit.app.data.Exercise;
import com.lightfit.app.data.ExerciseRepository;
import com.lightfit.app.store.SecureStore;

import org.json.JSONObject;

import java.util.Locale;

import java.util.List;

public class PostureFragment extends Fragment {

    private static final int[] QIDS = {R.id.q1, R.id.q2, R.id.q3, R.id.q4, R.id.q5};
    private static final int REQ_CAM = 102;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup vg,
                             @Nullable Bundle b) {
        return inf.inflate(R.layout.fragment_posture, vg, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle b) {
        v.findViewById(R.id.btn_assess).setOnClickListener(x -> assess(v));
        v.findViewById(R.id.btn_ai_camera).setOnClickListener(x -> openCamera());
        showLastScan(v);
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startActivity(new Intent(requireContext(), PostureCameraActivity.class));
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQ_CAM);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_CAM && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startActivity(new Intent(requireContext(), PostureCameraActivity.class));
        }
    }

    private void showLastScan(View v) {
        TextView tv = v.findViewById(R.id.tv_last_scan);
        String last = new SecureStore(requireContext()).get(requireContext(), "posture_scan", "");
        if (last.isEmpty()) return;
        try {
            JSONObject o = new JSONObject(last);
            tv.setText(getString(R.string.lf_scan_last) + ": "
                    + String.format(Locale.US, "%.1f", o.optDouble("tilt", 0))
                    + " / " + String.format(Locale.US, "%.2f", o.optDouble("head", 0)));
        } catch (Exception ignored) { }
    }

    private void assess(View v) {
        int yes = 0;
        for (int id : QIDS) if (((CheckBox) v.findViewById(id)).isChecked()) yes++;
        int score = 100 - yes * 15;

        int bucket = score >= 90 ? R.string.posture_good
                : score >= 70 ? R.string.posture_warn : R.string.posture_bad;

        TextView out = v.findViewById(R.id.assess_result);
        out.setText(getString(R.string.lf_risk_result) + ": " + score + " / 100 · "
                + getString(bucket));

        JSONObject rec = new JSONObject();
        try {
            rec.put("score", score);
            rec.put("yes", yes);
        } catch (Exception ignored) { }
        new SecureStore(requireContext()).put(requireContext(), "posture_selfcheck", rec.toString());

        LinearLayout list = v.findViewById(R.id.assess_result_layout);
        if (list == null) return;
        list.removeAllViews();
        if (yes > 0) {
            List<Exercise> rec2 = ExerciseRepository.byCat(requireContext(), "posture");
            for (Exercise e : rec2) {
                TextView row = new TextView(requireContext());
                row.setText("  " + e.emoji + " " + e.name);
                row.setTextSize(14);
                row.setPadding(8, 6, 8, 6);
                list.addView(row);
            }
        }
    }
}
