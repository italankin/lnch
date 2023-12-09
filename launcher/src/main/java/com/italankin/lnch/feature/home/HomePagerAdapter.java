package com.italankin.lnch.feature.home;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.Collections;
import java.util.List;

class HomePagerAdapter extends FragmentStateAdapter {

    private List<Class<? extends Fragment>> pages = Collections.emptyList();

    HomePagerAdapter(FragmentActivity activity) {
        super(activity);
    }

    void setPages(List<Class<? extends Fragment>> pages) {
        this.pages = pages;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Class<? extends Fragment> fragmentClass = pages.get(position);
        try {
            return fragmentClass.newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("Unable to create fragment '" +
                    fragmentClass.getName() + "': " + e.getMessage(), e);
        }
    }

    @Override
    public int getItemCount() {
        return pages.size();
    }

    int indexOfFragment(Class<? extends Fragment> fragmentClass) {
        return pages.indexOf(fragmentClass);
    }

    Class<? extends Fragment> getFragmentAt(int position) {
        return pages.get(position);
    }
}
