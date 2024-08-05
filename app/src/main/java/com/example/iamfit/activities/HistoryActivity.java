package com.example.iamfit.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.iamfit.R;
import com.example.iamfit.models.UserDetails;
import com.example.iamfit.utils.UserDetailsDatabaseHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends BaseActivity {

    private RecyclerView historyRecyclerView;
    private Button datePickerButton;
    private FloatingActionButton fabAddActivity;
    private UserDetailsDatabaseHelper dbHelper;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
    private SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        onCreateDrawer();

        dbHelper = new UserDetailsDatabaseHelper(this);
        historyRecyclerView = findViewById(R.id.historyRecyclerView);
        datePickerButton = findViewById(R.id.datePickerButton);
        fabAddActivity = findViewById(R.id.fabAddActivity);

        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        datePickerButton.setOnClickListener(v -> showDatePicker());

        fabAddActivity.setOnClickListener(v -> {
            Intent intent = new Intent(HistoryActivity.this, AddActivityActivity.class);
            intent.putExtra("selected_date", dbDateFormat.format(new Date())); // Default to current date
            startActivity(intent);
        });

        loadHistory(30); // Load last 30 days by default
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHistory(30); // Reload data when returning to this activity
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year1, monthOfYear, dayOfMonth);
                    loadHistory(selectedDate.getTime());
                }, year, month, day);
        datePickerDialog.show();
    }

    private void loadHistory(int days) {
        Calendar endDate = Calendar.getInstance();
        Calendar startDate = Calendar.getInstance();
        startDate.add(Calendar.DAY_OF_MONTH, -days + 1);

        List<HistoryItem> historyItems = new ArrayList<>();

        while (!startDate.after(endDate)) {
            Date currentDate = startDate.getTime();
            HistoryItem item = getHistoryForDate(currentDate);
            historyItems.add(item);
            startDate.add(Calendar.DAY_OF_MONTH, 1);
        }

        // Sort the list in descending order (most recent date first)
        Collections.sort(historyItems, (item1, item2) -> item2.date.compareTo(item1.date));

        historyRecyclerView.setAdapter(new HistoryAdapter(historyItems));
    }

    private void loadHistory(Date date) {
        HistoryItem item = getHistoryForDate(date);
        historyRecyclerView.setAdapter(new HistoryAdapter(Collections.singletonList(item)));
    }

    private HistoryItem getHistoryForDate(Date date) {
        String dateString = dbDateFormat.format(date);
        int foodCalories = dbHelper.getTotalCaloriesForDate(dateString, "Food");
        int exerciseCalories = dbHelper.getTotalCaloriesForDate(dateString, "Exercise");

        if (foodCalories == 0 && exerciseCalories == 0) {
            return new HistoryItem(date, 0, 0, 0, false);
        }

        UserDetails userDetails = dbHelper.getUserDetails();
        int bmr = calculateBMR(userDetails);
        int netCalories = foodCalories - exerciseCalories - bmr;

        return new HistoryItem(date, foodCalories, exerciseCalories, netCalories, true);
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

    private class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
        private List<HistoryItem> historyItems;

        HistoryAdapter(List<HistoryItem> historyItems) {
            this.historyItems = historyItems;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            HistoryItem item = historyItems.get(position);
            holder.dateTextView.setText(dateFormat.format(item.date));

            if (item.hasData) {
                holder.foodCaloriesRow.setVisibility(View.VISIBLE);
                holder.exerciseCaloriesRow.setVisibility(View.VISIBLE);
                holder.netCaloriesRow.setVisibility(View.VISIBLE);
                holder.noDataTextView.setVisibility(View.GONE);

                holder.foodCaloriesTextView.setText(String.valueOf(item.foodCalories));
                holder.exerciseCaloriesTextView.setText(String.valueOf(item.exerciseCalories));

                String netCaloriesText = item.netCalories > 0
                        ? String.format("+%d", item.netCalories)
                        : String.format("%d", item.netCalories);
                holder.netCaloriesTextView.setText(netCaloriesText);
                holder.netCaloriesTextView.setTextColor(item.netCalories > 0 ? 0xFFFF0000 : 0xFF4CAF50);
            } else {
                holder.foodCaloriesRow.setVisibility(View.GONE);
                holder.exerciseCaloriesRow.setVisibility(View.GONE);
                holder.netCaloriesRow.setVisibility(View.GONE);
                holder.noDataTextView.setVisibility(View.VISIBLE);
            }

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(HistoryActivity.this, MainContentActivity.class);
                intent.putExtra("selected_date", dbDateFormat.format(item.date));
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return historyItems.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView dateTextView, foodCaloriesTextView, exerciseCaloriesTextView, netCaloriesTextView, noDataTextView;
            View foodCaloriesRow, exerciseCaloriesRow, netCaloriesRow;

            ViewHolder(View itemView) {
                super(itemView);
                dateTextView = itemView.findViewById(R.id.dateTextView);
                foodCaloriesTextView = itemView.findViewById(R.id.foodCaloriesTextView);
                exerciseCaloriesTextView = itemView.findViewById(R.id.exerciseCaloriesTextView);
                netCaloriesTextView = itemView.findViewById(R.id.netCaloriesTextView);
                noDataTextView = itemView.findViewById(R.id.noDataTextView);
                foodCaloriesRow = itemView.findViewById(R.id.foodCaloriesRow);
                exerciseCaloriesRow = itemView.findViewById(R.id.exerciseCaloriesRow);
                netCaloriesRow = itemView.findViewById(R.id.netCaloriesRow);
            }
        }
    }

    private static class HistoryItem {
        Date date;
        int foodCalories;
        int exerciseCalories;
        int netCalories;
        boolean hasData;

        HistoryItem(Date date, int foodCalories, int exerciseCalories, int netCalories, boolean hasData) {
            this.date = date;
            this.foodCalories = foodCalories;
            this.exerciseCalories = exerciseCalories;
            this.netCalories = netCalories;
            this.hasData = hasData;
        }
    }
}