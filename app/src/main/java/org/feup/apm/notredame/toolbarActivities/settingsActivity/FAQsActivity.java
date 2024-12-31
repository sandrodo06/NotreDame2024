package org.feup.apm.notredame.toolbarActivities.settingsActivity;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.feup.apm.notredame.R;
import org.feup.apm.notredame.main.BaseActivity;

import java.util.ArrayList;
import java.util.List;

public class FAQsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_faqs);
        EdgeToEdge.enable(this);

        // =========================================================================================
        // Toolbar setup
        setupNavigation();

        // Initialize RecyclerView
        RecyclerView faqRecyclerView = findViewById(R.id.faqRecyclerView);
        faqRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Populate FAQ list
        List<FAQ> faqList = new ArrayList<>();
        faqList.add(new FAQ("What is this app?", "This app is designed to help you track and improve your posture using data from a posture sensor. It provides real-time feedback and insights based on your posture and movement throughout the day."));
        faqList.add(new FAQ("How do I track my posture?", "Once your posture sensor is connected, the app will begin tracking your posture in real time. You’ll receive alerts and reminders if your posture is misaligned."));
        faqList.add(new FAQ("Can the app give me real-time feedback?", "Yes, the app provides real-time feedback through notifications and visual cues when it detects poor posture. You can track improvements through daily or weekly reports in the app’s dashboard."));
        faqList.add(new FAQ("How accurate is the posture tracking?", "The posture tracking is highly accurate thanks to the precision of the posture sensor. It measures your body movements and angles to provide precise assessments of your posture and alignment."));
        faqList.add(new FAQ("Where can I find support?", "Support is available under the 'Settings' section of the app. You can contact customer service for troubleshooting, FAQs, and further assistance."));
        faqList.add(new FAQ("Posture sensor not syncing with the app?", "If your posture sensor isn't syncing, make sure it's charged and within range of your phone. You can also try turning Bluetooth off and on, restarting your phone, or reinstalling the app. If the issue persists, contact support."));
        faqList.add(new FAQ("Is the app suitable for all users?", "Yes, the app is suitable for anyone looking to improve their posture, but it's particularly beneficial for those who spend long hours sitting or have posture-related issues. Please consult your doctor before using if you have specific health conditions related to posture."));

        // Set Adapter
        FAQAdapter faqAdapter = new FAQAdapter(faqList);
        faqRecyclerView.setAdapter(faqAdapter);

        // Setup back button
        findViewById(R.id.backButton).setOnClickListener(v -> {
            Intent intent = new Intent(FAQsActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
    }
}
