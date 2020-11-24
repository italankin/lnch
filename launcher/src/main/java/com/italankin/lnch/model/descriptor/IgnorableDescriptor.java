package com.italankin.lnch.model.descriptor;

public interface IgnorableDescriptor extends Descriptor {

    void setIgnored(boolean ignored);

    boolean isIgnored();
}
