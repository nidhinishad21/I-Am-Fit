package com.example.iamfit.utils;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.iamfit.activities.IntroPageFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {

    private static final String[] PAGE_TEXTS = {
            "Welcome to I am Fit.",
            "You will record your activities and food. Once done, we will calculate calorie intake or expenditure for the day using our extensive API.",
            "At the end of the day, you will see a net calorie value. Positive value indicates you are gaining weight and Negative value indicates you are losing weight along with some tips.",
            "Let's get you started. We just need a few details from you to accurately log calories."
    };

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return IntroPageFragment.newInstance(PAGE_TEXTS[position], position);
    }

    @Override
    public int getItemCount() {
        return PAGE_TEXTS.length;
    }
}
