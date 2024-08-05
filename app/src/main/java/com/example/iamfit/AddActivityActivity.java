package com.example.iamfit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddActivityActivity extends BaseActivity {

    private static final String API_KEY = "aAggs2us0amxj/9TqfYrAQ==lhAc8lzH7kXFAJjs";
    private static final String API_NINJA_API_KEY = "aAggs2us0amxj/9TqfYrAQ==SUnGUB4272TsIMf8";


    private RadioGroup activityTypeGroup;
    private EditText activitySearchEditText;
    private EditText exerciseTimeEditText;
    private LinearLayout exerciseTimeLayout;
    private TextView caloriesTextView;
    private Button searchButton;
    private Button addButton;
    private int calories = 0;
    private UserDetailsDatabaseHelper dbHelper;
    private String activityName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_activity);
        onCreateDrawer();

        dbHelper = new UserDetailsDatabaseHelper(this);

        activityTypeGroup = findViewById(R.id.activityTypeGroup);
        activitySearchEditText = findViewById(R.id.activitySearchEditText);
        exerciseTimeEditText = findViewById(R.id.exerciseTimeEditText);
        exerciseTimeLayout = findViewById(R.id.exerciseTimeLayout);
        caloriesTextView = findViewById(R.id.caloriesTextView);
        searchButton = findViewById(R.id.searchButton);
        addButton = findViewById(R.id.addButton);

        activityTypeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.exerciseRadioButton) {
                exerciseTimeLayout.setVisibility(View.VISIBLE);
            } else {
                exerciseTimeLayout.setVisibility(View.GONE);
            }
        });

        searchButton.setOnClickListener(v -> searchCalories());
        addButton.setOnClickListener(v -> addActivity());
    }

    private void searchCalories() {
        activityName = activitySearchEditText.getText().toString();
        if (activityName.isEmpty()) {
            Toast.makeText(this, "Please enter an activity", Toast.LENGTH_SHORT).show();
            return;
        }

        if (activityTypeGroup.getCheckedRadioButtonId() == R.id.foodRadioButton) {
            searchFoodCalories();
        } else {
            searchExerciseCalories();
        }
    }

    private void searchFoodCalories() {
        String url = "https://api.calorieninjas.com/v1/nutrition?query=" + activityName;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray items = response.getJSONArray("items");
                        if (items.length() > 0) {
                            JSONObject item = items.getJSONObject(0);
                            calories = item.getInt("calories");
                            caloriesTextView.setText("Calories: " + calories);
                        } else {
                            showErrorDialog("Food could not be found, please try something else");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        showErrorDialog("Error parsing response");
                    }
                },
                error -> showErrorDialog("Error fetching calories")
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("X-Api-Key", API_KEY);
                return headers;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jsonObjectRequest);
    }

    private void searchExerciseCalories() {
        String durationStr = exerciseTimeEditText.getText().toString();
        if (durationStr.isEmpty()) {
            Toast.makeText(this, "Please enter exercise duration", Toast.LENGTH_SHORT).show();
            return;
        }

        UserDetails userDetails = dbHelper.getUserDetails();
        if (userDetails == null) {
            Toast.makeText(this, "User details not found", Toast.LENGTH_SHORT).show();
            return;
        }

        int duration = Integer.parseInt(durationStr);
        float weight = userDetails.getWeight();

        String url = "https://api.api-ninjas.com/v1/caloriesburned?activity=" + activityName +
                "&weight=" + weight + "&duration=" + duration;

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.length() > 0) {
                            JSONObject item = response.getJSONObject(0);
                            calories = item.getInt("total_calories");
                            caloriesTextView.setText("Calories: " + calories);
                        } else {
                            showErrorDialog("Exercise could not be found, please try something else");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        showErrorDialog("Error parsing response");
                    }
                },
                error -> showErrorDialog("Error fetching calories")
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("X-Api-Key", API_NINJA_API_KEY);
                return headers;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jsonArrayRequest);
    }

    private void showErrorDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private void addActivity() {
        if (calories == 0 || activityName.isEmpty()) {
            Toast.makeText(this, "Please search for an activity first", Toast.LENGTH_SHORT).show();
            return;
        }

        String type = activityTypeGroup.getCheckedRadioButtonId() == R.id.foodRadioButton ? "Food" : "Exercise";
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        dbHelper.addActivity(type, date, calories, activityName);

        Toast.makeText(this, "Activity added successfully", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, MainContentActivity.class);
        startActivity(intent);
        finish();
    }
}