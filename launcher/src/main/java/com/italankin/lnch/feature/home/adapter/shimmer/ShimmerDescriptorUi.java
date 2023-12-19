package com.italankin.lnch.feature.home.adapter.shimmer;

import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.mutable.MutableDescriptor;
import com.italankin.lnch.model.ui.DescriptorUi;

import java.util.Collections;
import java.util.Set;

public class ShimmerDescriptorUi implements DescriptorUi {

    public final int widthDp;

    public ShimmerDescriptorUi(int widthDp) {
        this.widthDp = widthDp;
    }

    @Override
    public ShimmerDescriptor getDescriptor() {
        return ShimmerDescriptor.INSTANCE;
    }

    @Override
    public boolean is(DescriptorUi another) {
        return another instanceof ShimmerDescriptorUi;
    }

    @Override
    public boolean deepEquals(DescriptorUi another) {
        return widthDp == ((ShimmerDescriptorUi) another).widthDp;
    }

    private static class ShimmerDescriptor implements Descriptor {
        private static final ShimmerDescriptor INSTANCE = new ShimmerDescriptor();

        @Override
        public String getId() {
            return "shimmer";
        }

        @Override
        public String getOriginalLabel() {
            return "";
        }

        @Override
        public MutableDescriptor<?> toMutable() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<String> getSearchTokens() {
            return Collections.emptySet();
        }
    }
}
