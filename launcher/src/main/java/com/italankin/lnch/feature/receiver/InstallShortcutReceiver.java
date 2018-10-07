package com.italankin.lnch.feature.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.model.repository.apps.AppsRepository;
import com.italankin.lnch.model.repository.apps.actions.AddAction;
import com.italankin.lnch.model.repository.descriptors.model.ShortcutDescriptor;

import timber.log.Timber;

public class InstallShortcutReceiver extends BroadcastReceiver {
    private static final String UNKNOWN_NAME = "???";

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent target = intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
        if (target == null || target.getAction() == null) {
            Timber.e("Invalid intent: %s", intent);
            return;
        }

        if (intent.getCategories() != null
                && intent.getCategories().contains(Intent.CATEGORY_LAUNCHER)
                && Intent.ACTION_MAIN.equals(intent.getAction())) {
            // probably initiated by PlayStore
            Timber.e("Ignoring intent: %s", intent);
            return;
        }
        String name = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
        String uri = target.toUri(0);

        ShortcutDescriptor descriptor = new ShortcutDescriptor();
        descriptor.label = name != null ? name.toUpperCase() : UNKNOWN_NAME;
        descriptor.uri = uri;
        descriptor.color = Color.WHITE;

        AppsRepository apps = LauncherApp.getInstance(context)
                .daggerService
                .main()
                .getAppsRepository();
        Throwable error = apps.edit()
                .enqueue(new AddAction(descriptor))
                .commit()
                .blockingGet();
        if (error == null) {
            Timber.d("Shortcut added: %s", intent);
        } else {
            Timber.e(error, "Commit changes:");
        }
    }
}
