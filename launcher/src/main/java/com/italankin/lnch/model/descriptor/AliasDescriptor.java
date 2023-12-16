package com.italankin.lnch.model.descriptor;

import java.util.List;

/**
 * A descriptor with search aliases
 */
public interface AliasDescriptor extends Descriptor {

    int MAX_ALIASES = 5;

    List<String> getAliases();
}
