package com.italankin.lnch.feature.home.fragmentresult;

import android.os.Bundle;

public abstract class SignalFragmentResultContract implements FragmentResultContract<Void> {
    private final String key;

    protected SignalFragmentResultContract(String key) {
        this.key = key;
    }

    public Bundle result() {
        Bundle result = new Bundle();
        result.putString(RESULT_KEY, key);
        return result;
    }

    @Override
    public String key() {
        return key;
    }

    @Override
    public Void parseResult(Bundle result) {
        return null;
    }
}
