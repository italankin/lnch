package com.italankin.lnch.model.descriptor;

/**
 * A descriptor which can be hidden from home screen
 */
public interface IgnorableDescriptor extends Descriptor {

    void setIgnored(boolean ignored);

    boolean isIgnored();
}
