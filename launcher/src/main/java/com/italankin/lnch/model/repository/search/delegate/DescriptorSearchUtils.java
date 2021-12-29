package com.italankin.lnch.model.repository.search.delegate;

import com.italankin.lnch.model.descriptor.AliasDescriptor;
import com.italankin.lnch.model.descriptor.CustomLabelDescriptor;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.LabelDescriptor;
import com.italankin.lnch.model.repository.search.match.PartialMatch;

import java.util.List;

import androidx.annotation.Nullable;

import static com.italankin.lnch.util.SearchUtils.contains;
import static com.italankin.lnch.util.SearchUtils.containsWord;
import static com.italankin.lnch.util.SearchUtils.startsWith;

final class DescriptorSearchUtils {

    @Nullable
    static PartialMatch.Type test(Descriptor descriptor, String query) {
        if (descriptor instanceof CustomLabelDescriptor) {
            CustomLabelDescriptor item = (CustomLabelDescriptor) descriptor;
            String label = item.getLabel();
            String customLabel = item.getCustomLabel();
            if (startsWith(customLabel, query) || startsWith(label, query)) {
                return PartialMatch.Type.STARTS_WITH;
            } else if (containsWord(customLabel, query) || containsWord(label, query)) {
                return PartialMatch.Type.CONTAINS_WORD;
            } else if (contains(customLabel, query) || contains(label, query)) {
                return PartialMatch.Type.CONTAINS;
            }
        }
        if (descriptor instanceof LabelDescriptor) {
            String label = ((LabelDescriptor) descriptor).getLabel();
            if (startsWith(label, query)) {
                return PartialMatch.Type.STARTS_WITH;
            } else if (containsWord(label, query)) {
                return PartialMatch.Type.CONTAINS_WORD;
            } else if (contains(label, query)) {
                return PartialMatch.Type.CONTAINS;
            }
        }
        if (descriptor instanceof AliasDescriptor) {
            List<String> aliases = ((AliasDescriptor) descriptor).getAliases();
            for (String alias : aliases) {
                if (startsWith(alias, query)) {
                    return PartialMatch.Type.STARTS_WITH;
                } else if (containsWord(alias, query)) {
                    return PartialMatch.Type.CONTAINS_WORD;
                } else if (contains(alias, query)) {
                    return PartialMatch.Type.CONTAINS;
                }
            }
        }
        return null;
    }

    private DescriptorSearchUtils() {
        // no instance
    }
}
