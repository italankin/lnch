package com.italankin.lnch.model.descriptor.mutable;

import com.italankin.lnch.model.descriptor.Descriptor;

import java.util.List;

public interface AliasMutableDescriptor<T extends Descriptor> extends MutableDescriptor<T> {

    void setAliases(List<String> aliases);

    List<String> getAliases();
}
