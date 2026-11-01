package com.lightfit.app;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.lightfit.app.i18n.LocaleHelper;
import com.lightfit.app.ui.HomeFragment;
import com.lightfit.app.ui.LibraryFragment;
import com.lightfit.app.ui.PlanFragment;
import com.lightfit.app.ui.PostureFragment;
import com.lightfit.app.ui.ProfileFragment;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView nav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        nav = findViewById(R.id.bottom_nav);
        if (savedInstanceState == null) {
            show(new HomeFragment());
        }
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) show(new HomeFragment());
            else if (id == R.id.nav_library) show(new LibraryFragment());
            else if (id == R.id.nav_plan) show(new PlanFragment());
            else if (id == R.id.nav_posture) show(new PostureFragment());
            else show(new ProfileFragment());
            return true;
        });
    }

    public void show(Fragment f) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, f)
                .commit();
    }

    /** 供子页签跳转 */
    public void switchTo(int menuItemId) {
        nav.setSelectedItemId(menuItemId);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.wrap(base));
    }
}
