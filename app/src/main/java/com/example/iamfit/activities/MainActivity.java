package com.example.iamfit.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.iamfit.R;
import com.example.iamfit.utils.ViewPagerAdapter;

public class MainActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "Pref101";
    public static final String HAS_SEEN_INTRO = "Seen101";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if the user has seen the introduction screens before
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        if (settings.getBoolean(HAS_SEEN_INTRO, false)) {
            // User has already seen the intro, proceed to the main activity
            proceedToMainActivity();
        } else {
            // User has not seen the intro, show the introduction screens
            setContentView(R.layout.activity_main);

            ViewPager2 viewPager = findViewById(R.id.viewPager);
            ViewPagerAdapter adapter = new ViewPagerAdapter(this);
            viewPager.setAdapter(adapter);
        }
    }

    private void proceedToMainActivity() {
        Intent intent = new Intent(this, MainContentActivity.class);
        startActivity(intent);
        finish();
    }
}
