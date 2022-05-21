package com.italankin.lnch.feature.base;

import android.os.Bundle;

import com.arellomobile.mvp.MvpAppCompatFragment;
import com.italankin.lnch.feature.home.fragmentresult.FragmentResultSender;

public abstract class AppFragment extends MvpAppCompatFragment implements FragmentResultSender {

    @Override
    public void sendResult(Bundle result) {
        String requestKey = requireArguments().getString(ARG_REQUEST_KEY);
        getParentFragmentManager().setFragmentResult(requestKey, result);
    }
}
