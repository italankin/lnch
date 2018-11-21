package com.italankin.lnch.util.dialogfragment;

import android.support.v4.app.Fragment;

import java.io.Serializable;

public interface ListenerFragment<T> extends Serializable {
    T get(Fragment parentFragment);
}
