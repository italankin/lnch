package com.italankin.lnch.feature.home;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.italankin.lnch.feature.home.apps.AppsFragment;

import java.util.Collections;
import java.util.List;

class HomePagerAdapter extends FragmentStateAdapter {

    private final FragmentManager fragmentManager;
    private List<Class<? extends Fragment>> pages = Collections.emptyList();
    private Fragment[] fragments = new Fragment[0];

    HomePagerAdapter(FragmentActivity activity) {
        super(activity);
        this.fragmentManager = activity.getSupportFragmentManager();
    }

    void setPages(List<Class<? extends Fragment>> pages) {
        if (!this.pages.equals(pages)) {
            this.pages = pages;
            this.fragments = new Fragment[pages.size()];
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Class<? extends Fragment> fragmentClass = pages.get(position);
        try {
            Fragment fragment = fragmentClass.newInstance();
            fragments[position] = fragment;
            return fragment;
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("Unable to create fragment '" +
                    fragmentClass.getName() + "': " + e.getMessage(), e);
        }
    }

    @Override
    public int getItemCount() {
        return pages.size();
    }

    Fragment getFragmentAt(int position) {
        return fragments[position];
    }

    int indexOfFragment(Class<? extends Fragment> fragmentClass) {
        return pages.indexOf(fragmentClass);
    }

    AppsFragment getAppsFragment() {
        return (AppsFragment) getFragmentAt(indexOfFragment(AppsFragment.class));
    }
}
