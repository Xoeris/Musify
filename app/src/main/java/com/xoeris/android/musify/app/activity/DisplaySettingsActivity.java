package com.xoeris.android.musify.app.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.xoeris.android.musify.R;

@SuppressWarnings("all")
public class DisplaySettingsActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "ThemePrefs";
    private static final String KEY_THEME_MODE = "theme_mode";
    private Spinner themeSpinner;
    private Spinner accentSpinner;
    private String[] themeOptions;
    private String[] accentOptions;
    private int[] themeModes = new int[] {
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
            AppCompatDelegate.MODE_NIGHT_YES,
            AppCompatDelegate.MODE_NIGHT_NO
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_display_settings);

        themeSpinner = findViewById(R.id.themeSpinner);
        accentSpinner = findViewById(R.id.accentSpinner);
        findViewById(R.id.back).setOnClickListener(v -> finish());

        themeOptions = new String[] {
                getString(R.string.follow_system_theme),
                getString(R.string.dark),
                getString(R.string.light)
        };

        accentOptions = new String[] {
                getString(R.string.material_design),
                getString(R.string.self_defined),
                getString(R.string.normal)
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.layout_spinner_item_theme, themeOptions);
        adapter.setDropDownViewResource(R.layout.layout_spinner_item_theme);
        themeSpinner.setAdapter(adapter);

        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(this, R.layout.layout_spinner_item_accent, accentOptions);
        adapter2.setDropDownViewResource(R.layout.layout_spinner_item_accent);
        accentSpinner.setAdapter(adapter2);

        int themeMode = getSavedThemeMode();
        int selectedIndex = 0;
        if (themeMode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
            selectedIndex = 0;
        } else if (themeMode == AppCompatDelegate.MODE_NIGHT_YES) {
            selectedIndex = 1;
        } else {
            selectedIndex = 2;
        }
        themeSpinner.setSelection(selectedIndex);

        themeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            boolean firstSelection = true;
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (firstSelection) {
                    firstSelection = false;
                    return;
                }
                int selectedMode = themeModes[position];
                if (getSavedThemeMode() != selectedMode) {
                    setThemeMode(selectedMode);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setThemeMode(int mode) {
        AppCompatDelegate.setDefaultNightMode(mode);
        saveThemeMode(mode);
        recreate();
    }

    private void saveThemeMode(int mode) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putInt(KEY_THEME_MODE, mode).apply();
    }

    private int getSavedThemeMode() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }
}