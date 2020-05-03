package com.italankin.lnch.feature.home;

import android.view.ViewGroup;

import com.italankin.lnch.feature.home.apps.AppsFragment;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

class HomePagerAdapter extends FragmentStatePagerAdapter {

    private static final int PAGE_COUNT = 1;
    static final int POSITION_APPS = 0;

    private final Fragment[] fragments = new Fragment[PAGE_COUNT];

    HomePagerAdapter(@NonNull FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        if (position == POSITION_APPS) {
            return new AppsFragment();
        } else {
            throw new IllegalArgumentException("unknown position=" + position);
        }
    }

    Fragment getFragmentAt(int position) {
        return fragments[position];
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        fragments[position] = fragment;
        return fragment;
    }

    AppsFragment getAppsFragment() {
        return (AppsFragment) fragments[POSITION_APPS];
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }
}
