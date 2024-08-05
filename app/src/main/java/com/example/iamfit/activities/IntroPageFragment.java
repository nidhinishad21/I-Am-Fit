package com.example.iamfit.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.iamfit.R;
import com.example.iamfit.utils.UserDetailsDatabaseHelper;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class IntroPageFragment extends Fragment {

    private static final String ARG_TEXT = "arg_text";
    private static final String ARG_PAGE = "arg_page";

    public static IntroPageFragment newInstance(String text, int page) {
        IntroPageFragment fragment = new IntroPageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TEXT, text);
        args.putInt(ARG_PAGE, page);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_intro_page, container, false);

        TextView textView = view.findViewById(R.id.textView);
        EditText weightInput = view.findViewById(R.id.weightInput);
        EditText heightInput = view.findViewById(R.id.heightInput);
        Button dateOfBirthButton = view.findViewById(R.id.dateOfBirthButton);
        Button leftButton = view.findViewById(R.id.leftButton);
        Button rightButton = view.findViewById(R.id.rightButton);
        RadioGroup radioGroup = view.findViewById(R.id.genderGroup);

        if (getArguments() != null) {
            textView.setText(getArguments().getString(ARG_TEXT));
            int page = getArguments().getInt(ARG_PAGE);

            ViewPager2 viewPager = getActivity().findViewById(R.id.viewPager);
            if (page == 0) {
                leftButton.setVisibility(View.GONE);
            } else {
                leftButton.setVisibility(View.VISIBLE);
            }

            if (page == 3) { // This is the last page
                weightInput.setVisibility(View.VISIBLE);
                heightInput.setVisibility(View.VISIBLE);
                dateOfBirthButton.setVisibility(View.VISIBLE);
                radioGroup.setVisibility(View.VISIBLE);

                dateOfBirthButton.setOnClickListener(v -> {
                    Calendar calendar = Calendar.getInstance();
                    int year = calendar.get(Calendar.YEAR);
                    int month = calendar.get(Calendar.MONTH);
                    int day = calendar.get(Calendar.DAY_OF_MONTH);

                    DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view1, selectedYear, selectedMonth, selectedDay) -> {
                        dateOfBirthButton.setText(selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear);
                    }, year, month, day);
                    datePickerDialog.show();
                });

                rightButton.setText("Finish");
                rightButton.setOnClickListener(v -> {
                    String weightStr = weightInput.getText().toString();
                    String heightStr = heightInput.getText().toString();
                    DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
                    String gender = radioGroup.getCheckedRadioButtonId() == R.id.maleRadioButton ? "Male" : "Female";
                    Date dob = null;
                    try {
                        dob = formatter.parse(dateOfBirthButton.getText().toString());
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }

                    if (TextUtils.isEmpty(weightStr) || TextUtils.isEmpty(heightStr) || dob.equals("Select Date of Birth")) {
                        Toast.makeText(getContext(), "Please fill in all details", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    float weight = Float.parseFloat(weightStr);
                    float height = Float.parseFloat(heightStr);

                    // Save to SQLite Database
                    saveUserDetails(weight, height, dob, gender);

                    // Proceed to main activity
                    proceedToMainActivity();
                });
            } else {
                rightButton.setOnClickListener(v -> viewPager.setCurrentItem(page + 1));
            }

            leftButton.setOnClickListener(v -> viewPager.setCurrentItem(page - 1));
        }

        return view;
    }

    private void saveUserDetails(float weight, float height, Date dateOfBirth, String gender) {
        UserDetailsDatabaseHelper dbHelper = new UserDetailsDatabaseHelper(getContext());
        dbHelper.addUserDetails(weight, height, dateOfBirth, gender);
    }

    private void proceedToMainActivity() {
        SharedPreferences settings = this.getActivity().getSharedPreferences(MainActivity.PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(MainActivity.HAS_SEEN_INTRO, true);
        editor.apply();
        Intent intent = new Intent(getActivity(), MainContentActivity.class);
        startActivity(intent);
        getActivity().finish();
    }
}
