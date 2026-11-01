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

import org.json.JSONObject;

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
    }
}
