package com.lightfit.app.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

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
        ver.setText(getString(R.string.lf_version) + ": LightFit v1.0 (Java)");

        v.findViewById(R.id.btn_reset).setOnClickListener(x -> {
            new SecureStore(requireContext()).clear(requireContext());
            LocaleHelper.prefs(requireContext()).edit().clear().apply();
            Toast.makeText(requireContext(), R.string.lf_reset_done, Toast.LENGTH_SHORT).show();
        });
    }
}
