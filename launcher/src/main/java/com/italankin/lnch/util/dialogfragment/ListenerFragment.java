package com.italankin.lnch.util.dialogfragment;

import java.io.Serializable;

import androidx.fragment.app.Fragment;

public interface ListenerFragment<T> extends Serializable {
    T get(Fragment parentFragment);
}
