package com.italankin.lnch.model.repository.descriptor.actions;

import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.repository.descriptor.DescriptorRepository;

import java.util.List;
import java.util.NoSuchElementException;

abstract class BaseAction implements DescriptorRepository.Editor.Action {

    @SuppressWarnings("unchecked")
    <T> T findById(List<Descriptor> items, String id) {
        for (Descriptor item : items) {
            if (item.getId().equals(id)) {
                return (T) item;
            }
        }
        throw new NoSuchElementException("No descriptor found for id=" + id);
    }
}
