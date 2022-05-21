package com.italankin.lnch.feature.home.fragmentresult;

import android.os.Bundle;

public interface FragmentResultSender {

    String ARG_REQUEST_KEY = "FragmentResultSender:request_key";

    void sendResult(Bundle result);
}
