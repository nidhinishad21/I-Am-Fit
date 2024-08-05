package com.example.iamfit.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.iamfit.R;
import com.example.iamfit.models.ActivityItem;
import com.example.iamfit.models.UserDetails;
import com.example.iamfit.utils.UserDetailsDatabaseHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainContentActivity extends BaseActivity {

    private RecyclerView exerciseRecyclerView;
    private RecyclerView foodRecyclerView;
    private TextView totalCaloriesTextView;
    private TextView dateTextView;
    private TextView noExerciseTextView;
    private TextView noFoodTextView;
    private UserDetailsDatabaseHelper dbHelper;
    private FloatingActionButton fabAddActivity;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
    private SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private String currentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_content);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        onCreateDrawer();

        dbHelper = new UserDetailsDatabaseHelper(this);
        exerciseRecyclerView = findViewById(R.id.exerciseRecyclerView);
        foodRecyclerView = findViewById(R.id.foodRecyclerView);
        totalCaloriesTextView = findViewById(R.id.totalCaloriesTextView);
        dateTextView = findViewById(R.id.dateTextView);
        noExerciseTextView = findViewById(R.id.noExerciseTextView);
        noFoodTextView = findViewById(R.id.noFoodTextView);
        fabAddActivity = findViewById(R.id.fabAddActivity);

        exerciseRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        foodRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        fabAddActivity.setOnClickListener(v -> {
            Intent intent = new Intent(MainContentActivity.this, AddActivityActivity.class);
            intent.putExtra("selected_date", currentDate);
            startActivity(intent);
        });

        handleDateAndLoadActivities();
    }

    @Override
    protected void onResume() {
        super.onResume();
        handleDateAndLoadActivities();
    }

    private void handleDateAndLoadActivities() {
        // Check if a specific date was passed
        String selectedDate = getIntent().getStringExtra("selected_date");
        currentDate = (selectedDate != null) ? selectedDate : dbDateFormat.format(new Date());

        try {
            Date date = dbDateFormat.parse(currentDate);
            dateTextView.setText(dateFormat.format(date));
        } catch (Exception e) {
            e.printStackTrace();
            dateTextView.setText(currentDate);
        }

        loadActivities();
    }

    private void loadActivities() {
        List<ActivityItem> exerciseItems = dbHelper.getActivities("Exercise", currentDate);
        List<ActivityItem> foodItems = dbHelper.getActivities("Food", currentDate);

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
        int totalFoodCalories = 0;
        int totalExerciseCalories = 0;

        for (ActivityItem item : foodItems) {
            totalFoodCalories += item.getCalories();
        }
        for (ActivityItem item : exerciseItems) {
            totalExerciseCalories += item.getCalories();
        }

        UserDetails userDetails = dbHelper.getUserDetails();
        int bmr = calculateBMR(userDetails);
        int netCalories = totalFoodCalories - totalExerciseCalories - bmr;

        String summaryText;
        if (netCalories > 0) {
            summaryText = "Excess calories for the day: " + netCalories;
            totalCaloriesTextView.setTextColor(0xFFFF0000);  // Red color
        } else {
            summaryText = "Deficit calories for the day: " + Math.abs(netCalories);
            totalCaloriesTextView.setTextColor(0xFF4CAF50);  // Green color
        }

        totalCaloriesTextView.setText(summaryText);
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

    private class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ViewHolder> {
        private List<ActivityItem> items;
        private boolean isExercise;

        ActivityAdapter(List<ActivityItem> items, boolean isExercise) {
            this.items = items;
            this.isExercise = isExercise;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.activity_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ActivityItem item = items.get(position);
            holder.activityTextView.setText(item.getActivityName());
            holder.caloriesTextView.setText(String.valueOf(item.getCalories()) + " cal");

            if (isExercise) {
                holder.activityTextView.setTextColor(0xFF6200EE);  // Purple color
                holder.caloriesTextView.setTextColor(0xFF6200EE);  // Purple color
            } else {
                holder.activityTextView.setTextColor(0xFFFF9800);  // Orange color
                holder.caloriesTextView.setTextColor(0xFF4CAF50);  // Green color
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