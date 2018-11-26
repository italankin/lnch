package com.italankin.lnch.feature.settings.base;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.arellomobile.mvp.MvpDelegate;
import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.di.service.DaggerService;

public abstract class AppPreferenceFragment extends BasePreferenceFragment {

    private boolean mIsStateSaved;
    private MvpDelegate<? extends AppPreferenceFragment> mMvpDelegate;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getMvpDelegate().onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();

        mIsStateSaved = false;

        getMvpDelegate().onAttach();
    }

    public void onResume() {
        super.onResume();

        mIsStateSaved = false;

        getMvpDelegate().onAttach();
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mIsStateSaved = true;

        getMvpDelegate().onSaveInstanceState(outState);
        getMvpDelegate().onDetach();
    }

    @Override
    public void onStop() {
        super.onStop();

        getMvpDelegate().onDetach();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        getMvpDelegate().onDetach();
        getMvpDelegate().onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //We leave the screen and respectively all fragments will be destroyed
        if (requireActivity().isFinishing()) {
            getMvpDelegate().onDestroy();
            return;
        }

        // When we rotate device isRemoving() return true for fragment placed in backstack
        // http://stackoverflow.com/questions/34649126/fragment-back-stack-and-isremoving
        if (mIsStateSaved) {
            mIsStateSaved = false;
            return;
        }

        // See https://github.com/Arello-Mobile/Moxy/issues/24
        boolean anyParentIsRemoving = false;
        Fragment parent = getParentFragment();
        while (!anyParentIsRemoving && parent != null) {
            anyParentIsRemoving = parent.isRemoving();
            parent = parent.getParentFragment();
        }

        if (isRemoving() || anyParentIsRemoving) {
            getMvpDelegate().onDestroy();
        }
    }

    /**
     * @return The {@link MvpDelegate} being used by this Fragment.
     */
    public MvpDelegate getMvpDelegate() {
        if (mMvpDelegate == null) {
            mMvpDelegate = new MvpDelegate<>(this);
        }

        return mMvpDelegate;
    }

    protected DaggerService daggerService() {
        return LauncherApp.getInstance(requireContext()).daggerService;
    }
}
