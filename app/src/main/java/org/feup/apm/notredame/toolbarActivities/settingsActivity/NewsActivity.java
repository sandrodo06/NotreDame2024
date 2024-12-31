package org.feup.apm.notredame.toolbarActivities.settingsActivity;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;

import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.feup.apm.notredame.R;
import org.feup.apm.notredame.main.BaseActivity;


public class NewsActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_news);
        EdgeToEdge.enable(this);

        // =========================================================================================
        // Toolbar setup
        setupNavigation();

        // =========================================================================================
        // Setup back button
        findViewById(R.id.backButton).setOnClickListener(v -> {
            Intent intent = new Intent(NewsActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        // Loads the news html code
        WebView newsWebView = findViewById(R.id.newsWebView);
        newsWebView.setWebViewClient(new WebViewClient());
        newsWebView.loadUrl("file:///android_asset/index.html");
    }
}
