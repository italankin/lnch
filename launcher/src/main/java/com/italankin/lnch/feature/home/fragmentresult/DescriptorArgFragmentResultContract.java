package com.italankin.lnch.feature.home.fragmentresult;

import android.os.Bundle;

import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.DescriptorArg;

public abstract class DescriptorArgFragmentResultContract<T extends Descriptor>
        implements FragmentResultContract<DescriptorArg<T>> {

    private static final String DESCRIPTOR = "descriptor";

    private final String key;

    protected DescriptorArgFragmentResultContract(String key) {
        this.key = key;
    }

    @SuppressWarnings("unchecked")
    public Bundle result(T descriptor) {
        return result(descriptor, (Class<T>) descriptor.getClass());
    }

    public Bundle result(Descriptor descriptor, Class<T> type) {
        Bundle bundle = new Bundle();
        bundle.putString(RESULT_KEY, key);
        bundle.putParcelable(DESCRIPTOR, new DescriptorArg<>(descriptor, type));
        return bundle;
    }

    @Override
    public String key() {
        return key;
    }

    @Override
    public DescriptorArg<T> parseResult(Bundle result) {
        return result.getParcelable(DESCRIPTOR);
    }
}
