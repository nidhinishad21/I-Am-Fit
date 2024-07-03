package com.example.iamfit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

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

            leftButton.setOnClickListener(v -> viewPager.setCurrentItem(page - 1));
            rightButton.setOnClickListener(v -> viewPager.setCurrentItem(page + 1));
        }

        return view;
    }
}