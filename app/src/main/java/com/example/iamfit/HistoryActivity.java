package com.example.iamfit;

import android.os.Bundle;

public class HistoryActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        onCreateDrawer();

        // Initialize your views and load history data here
    }
}