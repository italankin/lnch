package com.italankin.lnch.feature.home.fragmentresult;

import android.os.Bundle;

public interface FragmentResultContract<T> {

    String RESULT_KEY = "result";

    String key();

    T parseResult(Bundle result);
}
