package com.italankin.lnch.model.descriptor;

import java.util.List;

public interface AliasDescriptor extends Descriptor {

    void setAliases(List<String> aliases);

    List<String> getAliases();
}
