package org.feup.apm.notredame.toolbarActivities.settingsActivity;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;

import org.feup.apm.notredame.R;
import org.feup.apm.notredame.main.BaseActivity;

public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        // =========================================================================================
        // Toolbar setup
        setupNavigation();

        // =========================================================================================
        // Setup the buttons
        findViewById(R.id.newsButton).setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, NewsActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.supportButton).setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, SupportActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.faqsButton).setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, FAQsActivity.class);
            startActivity(intent);
        });
    }
}
