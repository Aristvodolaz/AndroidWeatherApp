package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.myapplication.fragments.AppFragment;
import com.example.myapplication.fragments.HistoryFragment;

public class MyViewPadgerAdapter extends FragmentStateAdapter {
    public MyViewPadgerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new HistoryFragment();
            case 1:
                return new AppFragment();
            default:
                return new HistoryFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
