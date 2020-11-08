package com.italankin.lnch.feature.pin;

import android.app.Activity;
import android.content.pm.LauncherApps;
import android.content.pm.LauncherApps.PinItemRequest;
import android.content.pm.ShortcutInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;

import com.italankin.lnch.LauncherApp;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import timber.log.Timber;

@RequiresApi(api = Build.VERSION_CODES.O)
public class PinItemActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            finish();
            return;
        }
        Parcelable extra = getIntent().getParcelableExtra(LauncherApps.EXTRA_PIN_ITEM_REQUEST);
        PinItemRequest request = extra instanceof PinItemRequest ? (PinItemRequest) extra : null;
        if (request != null) {
            switch (request.getRequestType()) {
                case PinItemRequest.REQUEST_TYPE_SHORTCUT:
                    if (request.isValid() && request.accept()) {
                        ShortcutInfo shortcutInfo = request.getShortcutInfo();
                        if (shortcutInfo == null) {
                            break;
                        }
                        Boolean pinned = LauncherApp.daggerService.main()
                                .getShortcutsRepository()
                                .pinShortcut(shortcutInfo.getPackage(), shortcutInfo.getId())
                                .blockingGet();
                        if (pinned) {
                            Timber.d("Shortcut '%s' pinned", shortcutInfo);
                        } else {
                            Timber.d("Shortcut '%s' already pinned", shortcutInfo);
                        }
                    }
                    break;
                case PinItemRequest.REQUEST_TYPE_APPWIDGET:
                    // TODO
                    break;
            }
        }
        finish();
    }
}
