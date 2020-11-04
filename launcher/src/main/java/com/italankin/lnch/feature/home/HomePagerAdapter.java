package com.italankin.lnch.feature.home;

import android.view.ViewGroup;

import com.italankin.lnch.feature.home.apps.AppsFragment;

import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.fragment.app.FragmentTransaction;

class HomePagerAdapter extends FragmentStatePagerAdapter {

    private final FragmentManager fragmentManager;
    private List<Class<? extends Fragment>> pages = Collections.emptyList();
    private Fragment[] fragments = new Fragment[0];

    HomePagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.fragmentManager = fragmentManager;
    }

    void setPages(List<Class<? extends Fragment>> pages) {
        if (!this.pages.equals(pages)) {
            removeFragments();
            this.pages = pages;
            this.fragments = new Fragment[pages.size()];
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        fragments[position] = fragment;
        return fragment;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        Class<? extends Fragment> fragmentClass = pages.get(position);
        try {
            return fragmentClass.newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("Unable to create fragment '" +
                    fragmentClass.getName() + "': " + e.getMessage(), e);
        }
    }

    @Override
    public int getCount() {
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

    private void removeFragments() {
        FragmentTransaction transaction = null;
        for (Fragment fragment : fragments) {
            if (fragment != null) {
                if (transaction == null) {
                    transaction = fragmentManager.beginTransaction();
                }
                transaction.remove(fragment);
            }
        }
        if (transaction != null) {
            transaction.commitNowAllowingStateLoss();
        }
    }
}
