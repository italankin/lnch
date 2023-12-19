package com.italankin.lnch.feature.home.adapter.error;

import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.mutable.MutableDescriptor;
import com.italankin.lnch.model.ui.DescriptorUi;

public class ErrorDescriptorUi implements DescriptorUi {

    public final Throwable error;

    public ErrorDescriptorUi(Throwable error) {
        this.error = error;
    }

    @Override
    public Descriptor getDescriptor() {
        return ErrorDescriptor.INSTANCE;
    }

    @Override
    public boolean is(DescriptorUi another) {
        return false;
    }

    @Override
    public boolean deepEquals(DescriptorUi another) {
        return false;
    }

    private static class ErrorDescriptor implements Descriptor {
        static final ErrorDescriptor INSTANCE = new ErrorDescriptor();

        @Override
        public String getId() {
            return "error";
        }

        @Override
        public String getOriginalLabel() {
            return "";
        }

        @Override
        public MutableDescriptor<?> toMutable() {
            throw new UnsupportedOperationException();
        }
    }
}
