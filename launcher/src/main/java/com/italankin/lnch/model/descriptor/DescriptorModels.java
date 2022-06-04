package com.italankin.lnch.model.descriptor;

import com.italankin.lnch.model.repository.store.json.model.DescriptorJson;
import com.italankin.lnch.model.ui.DescriptorUi;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
public @interface DescriptorModels {

    Class<? extends DescriptorJson> json();

    Class<? extends DescriptorUi> ui();
}
