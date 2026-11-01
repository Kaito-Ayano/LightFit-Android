package com.lightfit.app.ui;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.lightfit.app.R;
import com.lightfit.app.data.Exercise;
import com.lightfit.app.data.ExerciseRepository;

import java.util.List;

public class LibraryFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup vg,
                             @Nullable Bundle b) {
        return inf.inflate(R.layout.fragment_library, vg, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle b) {
        LinearLayout chips = v.findViewById(R.id.lib_chips);
        LinearLayout list = v.findViewById(R.id.lib_list);

        String[] cats = {"all", "neck", "posture", "gym"};
        int[] labels = {R.string.lf_lib_all, R.string.cat_neck, R.string.cat_posture,
                R.string.lf_lib_gym};

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 12, 0);

        for (int i = 0; i < cats.length; i++) {
            final String cat = cats[i];
            MaterialButton mb = new MaterialButton(requireContext(), null,
                    com.google.android.material.R.attr.materialButtonOutlinedStyle);
            mb.setText(labels[i]);
            mb.setAllCaps(false);
            mb.setLayoutParams(lp);
            mb.setOnClickListener(x -> render(list, cat));
            chips.addView(mb);
        }
        render(list, "all");
    }

    private void render(LinearLayout list, String cat) {
        list.removeAllViews();
        List<Exercise> items = ExerciseRepository.byCat(requireContext(), cat);
        for (Exercise e : items) {
            MaterialButton card = new MaterialButton(requireContext(), null,
                    com.google.android.material.R.attr.materialButtonOutlinedStyle);
            card.setAllCaps(false);
            card.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            card.setPadding(20, 14, 20, 14);
            String level = getString(getResources().getIdentifier(e.levelKey(),
                    "string", requireContext().getPackageName()));
            card.setText(e.emoji + "  " + e.name + "\n" + e.duration + " "
                    + getString(R.string.lf_minutes_unit) + " · " + level);
            card.setOnClickListener(x -> showDetail(e));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 6, 0, 6);
            card.setLayoutParams(lp);
            list.addView(card);
        }
        if (items.isEmpty()) {
            TextView empty = new TextView(requireContext());
            empty.setText(R.string.lf_plan_empty);
            list.addView(empty);
        }
    }

    private void showDetail(Exercise e) {
        String level = getString(getResources().getIdentifier(e.levelKey(),
                "string", requireContext().getPackageName()));
        new AlertDialog.Builder(requireContext())
                .setTitle(e.emoji + " " + e.name)
                .setMessage(getString(R.string.lf_level) + ": " + level + " · "
                        + e.duration + " " + getString(R.string.lf_minutes_unit)
                        + "\n\n" + e.tip)
                .setPositiveButton(R.string.done, null)
                .show();
    }
}
