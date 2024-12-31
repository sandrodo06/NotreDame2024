package org.feup.apm.notredame.toolbarActivities.settingsActivity;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;

import org.feup.apm.notredame.R;
import org.feup.apm.notredame.main.BaseActivity;

public class SupportActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_support);
        EdgeToEdge.enable(this);

        // =========================================================================================
        // Toolbar setup
        setupNavigation();

        // =========================================================================================
        // Setup back button
        findViewById(R.id.backButton).setOnClickListener(v -> {
            Intent intent = new Intent(SupportActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

    }
}
