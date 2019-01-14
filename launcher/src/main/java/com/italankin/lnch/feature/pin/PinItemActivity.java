package com.italankin.lnch.feature.pin;

import android.app.Activity;
import android.content.pm.LauncherApps;
import android.content.pm.LauncherApps.PinItemRequest;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.O)
public class PinItemActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Parcelable extra = getIntent().getParcelableExtra(LauncherApps.EXTRA_PIN_ITEM_REQUEST);
            PinItemRequest request = extra instanceof PinItemRequest ? (PinItemRequest) extra : null;
            if (request != null && request.getRequestType() == PinItemRequest.REQUEST_TYPE_SHORTCUT) {
                if (request.isValid()) {
                    request.accept();
                }
            }
        }
        finish();
    }
}
