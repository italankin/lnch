package com.italankin.lnch.model.repository.descriptor.actions;

import com.italankin.lnch.model.descriptor.mutable.MutableDescriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import com.italankin.lnch.util.ListUtils;

import java.util.List;

public class MoveAction implements DescriptorRepository.Editor.Action {
    private final int from;
    private final int to;

    public MoveAction(int from, int to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public void apply(List<MutableDescriptor<?>> items) {
        ListUtils.move(items, from, to);
    }
}
