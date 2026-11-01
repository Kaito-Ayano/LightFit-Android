package com.lightfit.app.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.lightfit.app.R;
import com.lightfit.app.i18n.LocaleHelper;
import com.lightfit.app.store.SecureStore;

public class ProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup vg,
                             @Nullable Bundle b) {
        return inf.inflate(R.layout.fragment_profile, vg, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle b) {
        String cur = LocaleHelper.lang(requireContext());
        int[] ids = {R.id.lang_zh, R.id.lang_en, R.id.lang_ru, R.id.lang_kk, R.id.lang_ko};
        String[] codes = {"zh", "en", "ru", "kk", "ko"};
        for (int i = 0; i < ids.length; i++) {
            final String code = codes[i];
            RadioButton rb = v.findViewById(ids[i]);
            if ((cur.isEmpty() && "zh".equals(code)) || code.equals(cur)) rb.setChecked(true);
            rb.setOnClickListener(x -> {
                LocaleHelper.setLang(requireContext(), code);
                requireActivity().recreate();
            });
        }

        TextView ver = v.findViewById(R.id.tv_version);
        ver.setText(getString(R.string.lf_version) + ": LightFit v1.4 (Java)");

        v.findViewById(R.id.btn_reset).setOnClickListener(x -> {
            new SecureStore(requireContext()).clear(requireContext());
            LocaleHelper.prefs(requireContext()).edit().clear().apply();
            Toast.makeText(requireContext(), R.string.lf_reset_done, Toast.LENGTH_SHORT).show();
        });

        loadProfile(v);
        v.findViewById(R.id.btn_save_profile).setOnClickListener(x -> saveProfile(v));
    }

    private void loadProfile(View v) {
        String raw = new SecureStore(requireContext()).get(requireContext(), "profile", "");
        if (raw.isEmpty()) return;
        try {
            JSONObject o = new JSONObject(raw);
            ((EditText) v.findViewById(R.id.et_nick)).setText(o.optString("nick", ""));
            double h = o.optDouble("h", 0), w = o.optDouble("w", 0);
            if (h > 0) ((EditText) v.findViewById(R.id.et_h)).setText(String.valueOf((int) h));
            if (w > 0) ((EditText) v.findViewById(R.id.et_w)).setText(String.valueOf((int) w));
            int g = o.optInt("goal", 0);
            if (g == 1) ((RadioButton) v.findViewById(R.id.goal_posture2)).setChecked(true);
            else if (g == 2) ((RadioButton) v.findViewById(R.id.goal_relax)).setChecked(true);
            showBmi(v, w, h);
        } catch (Exception ignored) { }
    }

    private void saveProfile(View v) {
        double h, w;
        try {
            h = Double.parseDouble(((EditText) v.findViewById(R.id.et_h)).getText().toString());
            w = Double.parseDouble(((EditText) v.findViewById(R.id.et_w)).getText().toString());
        } catch (Exception e) {
            Toast.makeText(requireContext(), R.string.lf_bmi_bad, Toast.LENGTH_SHORT).show();
            return;
        }
        if (h < 50 || h > 250 || w < 10 || w > 400) {
            Toast.makeText(requireContext(), R.string.lf_bmi_bad, Toast.LENGTH_SHORT).show();
            return;
        }
        int goal = 0;
        if (((RadioButton) v.findViewById(R.id.goal_posture2)).isChecked()) goal = 1;
        else if (((RadioButton) v.findViewById(R.id.goal_relax)).isChecked()) goal = 2;
        JSONObject o = new JSONObject();
        try {
            o.put("nick", ((EditText) v.findViewById(R.id.et_nick)).getText().toString().trim());
            o.put("h", h);
            o.put("w", w);
            o.put("goal", goal);
        } catch (Exception ignored) { }
        new SecureStore(requireContext()).put(requireContext(), "profile", o.toString());
        showBmi(v, w, h);
        Toast.makeText(requireContext(), R.string.lf_saved, Toast.LENGTH_SHORT).show();
    }

    private void showBmi(View v, double w, double h) {
        TextView tv = v.findViewById(R.id.tv_bmi);
        if (h <= 0 || w <= 0) {
            tv.setText("");
            return;
        }
        double bmi = w / ((h / 100.0) * (h / 100.0));
        int cat;
        if (bmi < 18.5) cat = R.string.lf_bmi_low;
        else if (bmi < 24) cat = R.string.lf_bmi_ok;
        else if (bmi < 28) cat = R.string.lf_bmi_high;
        else cat = R.string.lf_bmi_fat;
        tv.setText(getString(R.string.lf_bmi) + ": "
                + String.format(java.util.Locale.US, "%.1f", bmi) + " · " + getString(cat));
    }
}
