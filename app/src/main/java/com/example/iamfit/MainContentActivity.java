package com.example.iamfit;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainContentActivity extends BaseActivity {

    private DrawerLayout drawer;
    private RecyclerView exerciseRecyclerView;
    private RecyclerView foodRecyclerView;
    private TextView totalCaloriesTextView;
    private TextView noExerciseTextView;
    private TextView noFoodTextView;
    private UserDetailsDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_content);
        onCreateDrawer();

        dbHelper = new UserDetailsDatabaseHelper(this);

        exerciseRecyclerView = findViewById(R.id.exerciseRecyclerView);
        foodRecyclerView = findViewById(R.id.foodRecyclerView);
        totalCaloriesTextView = findViewById(R.id.totalCaloriesTextView);
        noExerciseTextView = findViewById(R.id.noExerciseTextView);
        noFoodTextView = findViewById(R.id.noFoodTextView);

        exerciseRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        foodRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton fabAddActivity = findViewById(R.id.fabAddActivity);
        fabAddActivity.setOnClickListener(v -> {
            Intent intent = new Intent(MainContentActivity.this, AddActivityActivity.class);
            startActivity(intent);
        });

        loadActivities();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadActivities();
    }

    private void loadActivities() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        List<ActivityItem> exerciseItems = dbHelper.getActivities("Exercise", today);
        List<ActivityItem> foodItems = dbHelper.getActivities("Food", today);

        if (exerciseItems.isEmpty()) {
            exerciseRecyclerView.setVisibility(View.GONE);
            noExerciseTextView.setVisibility(View.VISIBLE);
        } else {
            exerciseRecyclerView.setVisibility(View.VISIBLE);
            noExerciseTextView.setVisibility(View.GONE);
            exerciseRecyclerView.setAdapter(new ActivityAdapter(exerciseItems, true));
        }

        if (foodItems.isEmpty()) {
            foodRecyclerView.setVisibility(View.GONE);
            noFoodTextView.setVisibility(View.VISIBLE);
        } else {
            foodRecyclerView.setVisibility(View.VISIBLE);
            noFoodTextView.setVisibility(View.GONE);
            foodRecyclerView.setAdapter(new ActivityAdapter(foodItems, false));
        }

        updateCalorieSummary(exerciseItems, foodItems);
    }

    private void updateCalorieSummary(List<ActivityItem> exerciseItems, List<ActivityItem> foodItems) {
        int totalCalories = 0;
        for (ActivityItem item : foodItems) {
            totalCalories = totalCalories + item.getCalories();
        }

        for (ActivityItem item : exerciseItems) {
            totalCalories = totalCalories - item.getCalories();
        }

        UserDetails userDetails = dbHelper.getUserDetails();
        int bmr = calculateBMR(userDetails);
        int netCalories = totalCalories - bmr;

        String summaryText;
        if (netCalories > 0) {
            summaryText = "Excess calories today: " + netCalories  + " . Please exercise more today.";
        } else {
            summaryText = "Deficit calories today: " + Math.abs(netCalories)  + " . Please eat more.";
        }

        totalCaloriesTextView.setText(summaryText);
    }

    private int calculateAge(Date dateOfBirth) {
        Calendar today = Calendar.getInstance();
        Calendar birthDate = Calendar.getInstance();
        birthDate.setTime(dateOfBirth);
        int age = today.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR);
        if (today.get(Calendar.DAY_OF_YEAR) < birthDate.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }
        return age;
    }

    private int calculateBMR(UserDetails userDetails) {
        int age = calculateAge(userDetails.getDateOfBirth());
        float weight = userDetails.getWeight(); // in kg
        float height = userDetails.getHeight(); // in cm
        String gender = userDetails.getGender();

        // Mifflin-St Jeor Equation
        double bmr;
        if (gender.equalsIgnoreCase("male")) {
            bmr = (10 * weight) + (6.25 * height) - (5 * age) + 5;
        } else {
            bmr = (10 * weight) + (6.25 * height) - (5 * age) - 161;
        }

        return (int) Math.round(bmr);
    }

    private class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ViewHolder> {
        private List<ActivityItem> items;
        private boolean isExercise;

        ActivityAdapter(List<ActivityItem> items, boolean isExercise) {
            this.items = items;
            this.isExercise = isExercise;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.activity_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            ActivityItem item = items.get(position);
            holder.activityTextView.setText(item.getActivityName());
            holder.caloriesTextView.setText(String.valueOf(Math.abs(item.getCalories())));

            if (isExercise) {
                holder.activityTextView.setTextColor(getResources().getColor(R.color.purple_500));
                holder.caloriesTextView.setTextColor(getResources().getColor(R.color.purple_500));
            } else {
                holder.activityTextView.setTextColor(getResources().getColor(R.color.orange_500));
                holder.caloriesTextView.setTextColor(getResources().getColor(R.color.green_500));
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView activityTextView;
            TextView caloriesTextView;

            ViewHolder(View itemView) {
                super(itemView);
                activityTextView = itemView.findViewById(R.id.activityTextView);
                caloriesTextView = itemView.findViewById(R.id.caloriesTextView);
            }
        }
    }
}