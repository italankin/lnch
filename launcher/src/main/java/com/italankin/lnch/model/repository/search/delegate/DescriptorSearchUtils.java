package com.italankin.lnch.model.repository.search.delegate;

import com.italankin.lnch.model.descriptor.CustomLabelDescriptor;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.LabelDescriptor;
import com.italankin.lnch.model.repository.search.match.PartialMatch;

import static com.italankin.lnch.util.SearchUtils.contains;
import static com.italankin.lnch.util.SearchUtils.containsWord;
import static com.italankin.lnch.util.SearchUtils.startsWith;

final class DescriptorSearchUtils {

    static PartialMatch test(Descriptor descriptor, String query) {
        PartialMatch match = null;
        if (descriptor instanceof CustomLabelDescriptor) {
            CustomLabelDescriptor item = (CustomLabelDescriptor) descriptor;
            String label = item.getLabel();
            String customLabel = item.getCustomLabel();
            if (startsWith(customLabel, query) || startsWith(label, query)) {
                match = new PartialMatch(PartialMatch.Type.STARTS_WITH);
            } else if (containsWord(customLabel, query) || containsWord(label, query)) {
                match = new PartialMatch(PartialMatch.Type.CONTAINS_WORD);
            } else if (contains(customLabel, query) || contains(label, query)) {
                match = new PartialMatch(PartialMatch.Type.CONTAINS);
            }
            if (match != null) {
                match.label = item.getVisibleLabel();
                match.descriptor = descriptor;
            }
        } else if (descriptor instanceof LabelDescriptor) {
            String label = ((LabelDescriptor) descriptor).getLabel();
            if (startsWith(label, query)) {
                match = new PartialMatch(PartialMatch.Type.STARTS_WITH);
            } else if (containsWord(label, query)) {
                match = new PartialMatch(PartialMatch.Type.CONTAINS_WORD);
            } else if (contains(label, query)) {
                match = new PartialMatch(PartialMatch.Type.CONTAINS);
            }
            if (match != null) {
                match.label = label;
                match.descriptor = descriptor;
            }
        }
        return match;
    }

    private DescriptorSearchUtils() {
        // no instance
    }
}
