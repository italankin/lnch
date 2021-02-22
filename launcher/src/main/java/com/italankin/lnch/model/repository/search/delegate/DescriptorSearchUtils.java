package com.italankin.lnch.model.repository.search.delegate;

import com.italankin.lnch.model.descriptor.AliasDescriptor;
import com.italankin.lnch.model.descriptor.CustomLabelDescriptor;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.LabelDescriptor;
import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.model.descriptor.impl.IntentDescriptor;
import com.italankin.lnch.model.descriptor.impl.PinnedShortcutDescriptor;
import com.italankin.lnch.model.repository.search.match.Match;
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
                match = new PartialDescriptorMatch(descriptor, PartialMatch.Type.STARTS_WITH, kindOf(descriptor));
            } else if (containsWord(customLabel, query) || containsWord(label, query)) {
                match = new PartialDescriptorMatch(descriptor, PartialMatch.Type.CONTAINS_WORD, kindOf(descriptor));
            } else if (contains(customLabel, query) || contains(label, query)) {
                match = new PartialDescriptorMatch(descriptor, PartialMatch.Type.CONTAINS, kindOf(descriptor));
            }
            if (match != null) {
                match.label = item.getVisibleLabel();
                return match;
            }
        } else if (descriptor instanceof LabelDescriptor) {
            PartialDescriptorMatch match = null;
            String label = ((LabelDescriptor) descriptor).getLabel();
            if (startsWith(label, query)) {
                match = new PartialDescriptorMatch(descriptor, PartialMatch.Type.STARTS_WITH, kindOf(descriptor));
            } else if (containsWord(label, query)) {
                match = new PartialDescriptorMatch(descriptor, PartialMatch.Type.CONTAINS_WORD, kindOf(descriptor));
            } else if (contains(label, query)) {
                match = new PartialDescriptorMatch(descriptor, PartialMatch.Type.CONTAINS, kindOf(descriptor));
            }
            if (match != null) {
                match.label = label;
                return match;
            }
        }
        if (descriptor instanceof AliasDescriptor) {
            List<String> aliases = ((AliasDescriptor) descriptor).getAliases();
            for (String alias : aliases) {
                PartialDescriptorMatch match = null;
                if (startsWith(alias, query)) {
                    match = new PartialDescriptorMatch(descriptor, PartialMatch.Type.STARTS_WITH, kindOf(descriptor));
                } else if (containsWord(alias, query)) {
                    match = new PartialDescriptorMatch(descriptor, PartialMatch.Type.CONTAINS_WORD, kindOf(descriptor));
                } else if (contains(alias, query)) {
                    match = new PartialDescriptorMatch(descriptor, PartialMatch.Type.CONTAINS, kindOf(descriptor));
                }
                if (match != null) {
                    String visibleLabel = DescriptorUtils.getVisibleLabel(descriptor);
                    match.label = visibleLabel.isEmpty() ? alias : visibleLabel;
                    return match;
                }
            }
        }
        return null;
    }

    private static Match.Kind kindOf(Descriptor descriptor) {
        if (descriptor instanceof AppDescriptor) {
            return Match.Kind.APP;
        }
        if (descriptor instanceof PinnedShortcutDescriptor) {
            return Match.Kind.SHORTCUT;
        }
        if (descriptor instanceof IntentDescriptor) {
            return Match.Kind.SHORTCUT;
        }
        return Match.Kind.OTHER;
    }

    private DescriptorSearchUtils() {
        // no instance
    }
}
