package com.italankin.lnch.model.repository.apps.actions;

import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.repository.apps.AppsRepository;

import java.util.List;

public class RunnableAction implements AppsRepository.Editor.Action {
    private final Runnable runnable;

    public RunnableAction(Runnable runnable) {
        this.runnable = runnable;
    }

    @Override
    public void apply(List<Descriptor> items) {
        runnable.run();
    }
}
