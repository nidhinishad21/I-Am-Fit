package com.example.iamfit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "MyPrefsFile2";
    private static final String HAS_SEEN_INTRO = "HasSeenIntro2";

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

            viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    // If the last page is reached, set the flag and proceed to main activity
                    if (position == adapter.getItemCount() - 1) {
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putBoolean(HAS_SEEN_INTRO, true);
                        editor.apply();
                    }
                }
            });
        }
    }

    private void proceedToMainActivity() {
        Intent intent = new Intent(this, MainContentActivity.class);
        startActivity(intent);
        finish();
    }
}
