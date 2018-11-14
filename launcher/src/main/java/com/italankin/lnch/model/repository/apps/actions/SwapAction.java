package com.italankin.lnch.model.repository.apps.actions;

import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.repository.apps.DescriptorRepository;
import com.italankin.lnch.util.ListUtils;

import java.util.List;

public class SwapAction implements DescriptorRepository.Editor.Action {
    private final int from;
    private final int to;

    public SwapAction(int from, int to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public void apply(List<Descriptor> items) {
        ListUtils.swap(items, from, to);
    }
}
