package com.italankin.lnch.di.scope;

import javax.inject.Qualifier;
import javax.inject.Scope;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Scope
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface ViewModelScope {
}
