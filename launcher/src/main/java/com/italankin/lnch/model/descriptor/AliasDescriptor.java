package com.italankin.lnch.model.descriptor;

import java.util.List;

public interface AliasDescriptor extends Descriptor {

    int MAX_ALIASES = 5;

    void setAliases(List<String> aliases);

    List<String> getAliases();
}
