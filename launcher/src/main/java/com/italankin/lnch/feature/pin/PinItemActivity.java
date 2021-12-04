package com.italankin.lnch.feature.pin;

import android.app.Activity;
import android.content.Context;
import android.content.pm.LauncherApps;
import android.content.pm.LauncherApps.PinItemRequest;
import android.content.pm.ShortcutInfo;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.R;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.O)
public class PinItemActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            finish();
            return;
        }
        LauncherApps launcherApps = (LauncherApps) getSystemService(Context.LAUNCHER_APPS_SERVICE);
        PinItemRequest request = launcherApps.getPinItemRequest(getIntent());
        if (request != null) {
            switch (request.getRequestType()) {
                case PinItemRequest.REQUEST_TYPE_SHORTCUT:
                    if (request.isValid() && request.accept()) {
                        ShortcutInfo shortcutInfo = request.getShortcutInfo();
                        if (shortcutInfo == null) {
                            break;
                        }
                        Boolean pinned = LauncherApp.daggerService.main()
                                .shortcutsRepository()
                                .pinShortcut(shortcutInfo.getPackage(), shortcutInfo.getId())
                                .onErrorReturnItem(false)
                                .blockingGet();
                        if (pinned) {
                            String text = getString(R.string.deep_shortcut_pinned, shortcutInfo.getShortLabel());
                            Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
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
