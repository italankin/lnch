package com.italankin.lnch.feature.home.fragmentresult;

import android.os.Bundle;

public abstract class DescriptorFragmentResultContract implements FragmentResultContract<String> {
    private static final String DESCRIPTOR_ID = "descriptor_id";

    private final String key;

    protected DescriptorFragmentResultContract(String key) {
        this.key = key;
    }

    public Bundle result(String descriptorId) {
        Bundle bundle = new Bundle();
        bundle.putString(RESULT_KEY, key);
        bundle.putString(DESCRIPTOR_ID, descriptorId);
        return bundle;
    }

    @Override
    public String key() {
        return key;
    }

    @Override
    public String parseResult(Bundle result) {
        return result.getString(DESCRIPTOR_ID);
    }
}
