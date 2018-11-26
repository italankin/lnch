package com.italankin.lnch.util.dialogfragment;

import android.app.Activity;

import java.io.Serializable;

public interface ListenerActivity<T> extends Serializable {
    T get(Activity activity);
}
