package com.italankin.lnch.model.repository.search.delegate;

import com.italankin.lnch.model.descriptor.AliasDescriptor;
import com.italankin.lnch.model.descriptor.CustomLabelDescriptor;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.LabelDescriptor;
import com.italankin.lnch.model.repository.search.match.PartialDescriptorMatch;
import com.italankin.lnch.model.repository.search.match.PartialMatch;
import com.italankin.lnch.util.DescriptorUtils;

import java.util.List;

import static com.italankin.lnch.util.SearchUtils.contains;
import static com.italankin.lnch.util.SearchUtils.containsWord;
import static com.italankin.lnch.util.SearchUtils.startsWith;

final class DescriptorSearchUtils {

    static PartialDescriptorMatch test(Descriptor descriptor, String query) {
        if (descriptor instanceof CustomLabelDescriptor) {
            PartialDescriptorMatch match = null;
            CustomLabelDescriptor item = (CustomLabelDescriptor) descriptor;
            String label = item.getLabel();
            String customLabel = item.getCustomLabel();
            if (startsWith(customLabel, query) || startsWith(label, query)) {
                match = new PartialDescriptorMatch(PartialMatch.Type.STARTS_WITH);
            } else if (containsWord(customLabel, query) || containsWord(label, query)) {
                match = new PartialDescriptorMatch(PartialMatch.Type.CONTAINS_WORD);
            } else if (contains(customLabel, query) || contains(label, query)) {
                match = new PartialDescriptorMatch(PartialMatch.Type.CONTAINS);
            }
            if (match != null) {
                match.label = item.getVisibleLabel();
                match.descriptor = descriptor;
                return match;
            }
        } else if (descriptor instanceof LabelDescriptor) {
            PartialDescriptorMatch match = null;
            String label = ((LabelDescriptor) descriptor).getLabel();
            if (startsWith(label, query)) {
                match = new PartialDescriptorMatch(PartialMatch.Type.STARTS_WITH);
            } else if (containsWord(label, query)) {
                match = new PartialDescriptorMatch(PartialMatch.Type.CONTAINS_WORD);
            } else if (contains(label, query)) {
                match = new PartialDescriptorMatch(PartialMatch.Type.CONTAINS);
            }
            if (match != null) {
                match.label = label;
                match.descriptor = descriptor;
                return match;
            }
        }
        if (descriptor instanceof AliasDescriptor) {
            List<String> aliases = ((AliasDescriptor) descriptor).getAliases();
            for (String alias : aliases) {
                PartialDescriptorMatch match = null;
                if (startsWith(alias, query)) {
                    match = new PartialDescriptorMatch(PartialMatch.Type.STARTS_WITH);
                } else if (containsWord(alias, query)) {
                    match = new PartialDescriptorMatch(PartialMatch.Type.CONTAINS_WORD);
                } else if (contains(alias, query)) {
                    match = new PartialDescriptorMatch(PartialMatch.Type.CONTAINS);
                }
                if (match != null) {
                    String visibleLabel = DescriptorUtils.getVisibleLabel(descriptor);
                    match.label = visibleLabel.isEmpty() ? alias : visibleLabel;
                    match.descriptor = descriptor;
                    return match;
                }
            }
        }
        return null;
    }

    private DescriptorSearchUtils() {
        // no instance
    }
}
