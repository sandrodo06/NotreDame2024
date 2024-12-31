package org.feup.apm.notredame.main;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.feup.apm.notredame.toolbarActivities.exercisesActivity.ExercisesActivity;
import org.feup.apm.notredame.R;
import org.feup.apm.notredame.toolbarActivities.settingsActivity.SettingsActivity;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseActivity extends AppCompatActivity {

    // Sets up the tool bar for all the necessary activities
    protected void setupNavigation() {

        // Maps each button to his activity
        Map<Integer, Class<?>> navigationMap = new HashMap<>();
        navigationMap.put(R.id.HomeBtn, MainActivity.class);
        navigationMap.put(R.id.ExercisesBtn, ExercisesActivity.class);
        navigationMap.put(R.id.SettingsBtn, SettingsActivity.class);

        for (Map.Entry<Integer, Class<?>> entry : navigationMap.entrySet()) {
            LinearLayout button = findViewById(entry.getKey());

            // Sets up the button listener
            button.setOnClickListener(v -> {
                if (!this.getClass().equals(entry.getValue())) {
                    Intent intent = new Intent(this, entry.getValue());
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                }
            });

            // Changes the appearance of the current activity
            if (this.getClass().equals(entry.getValue())) {
                View line = button.getChildAt(0);
                line.setBackgroundColor(Color.parseColor("#64BD45"));

                ImageView icon = (ImageView) button.getChildAt(1);
                icon.setImageTintList(ColorStateList.valueOf(Color.parseColor("#64BD45")));

                TextView text = (TextView) button.getChildAt(2);
                text.setTextColor(Color.parseColor("#64BD45"));
            }
        }
    }
}