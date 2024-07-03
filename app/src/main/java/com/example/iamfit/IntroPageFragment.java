package com.example.iamfit;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import java.util.Calendar;

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
                    String dob = dateOfBirthButton.getText().toString();

                    if (TextUtils.isEmpty(weightStr) || TextUtils.isEmpty(heightStr) || dob.equals("Select Date of Birth")) {
                        Toast.makeText(getContext(), "Please fill in all details", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double weight = Double.parseDouble(weightStr);
                    double height = Double.parseDouble(heightStr);

                    // Save to SQLite Database
                    saveUserDetails(weight, height, dob);

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

    private void saveUserDetails(double weight, double height, String dateOfBirth) {
        UserDetailsDatabaseHelper dbHelper = new UserDetailsDatabaseHelper(getContext());
        dbHelper.insertUserDetails(weight, height, dateOfBirth);
    }

    private void proceedToMainActivity() {
        Intent intent = new Intent(getActivity(), MainContentActivity.class);
        startActivity(intent);
        getActivity().finish();
    }
}
