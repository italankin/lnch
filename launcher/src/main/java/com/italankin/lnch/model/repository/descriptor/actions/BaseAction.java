package com.italankin.lnch.model.repository.descriptor.actions;

import androidx.annotation.Nullable;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.mutable.MutableDescriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;
import timber.log.Timber;

import java.util.List;

public abstract class BaseAction implements DescriptorRepository.Editor.Action {

    @SuppressWarnings("unchecked")
    @Nullable
    public <T extends MutableDescriptor<?>> T findById(List<MutableDescriptor<?>> items, String id) {
        for (MutableDescriptor<? extends Descriptor> item : items) {
            if (item.getId().equals(id)) {
                return (T) item;
            }
        }
        Timber.w("findById: no descriptor found for id=%s", id);
        return null;
    }
}
