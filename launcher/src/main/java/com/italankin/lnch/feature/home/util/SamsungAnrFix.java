package com.italankin.lnch.feature.home.util;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;

import com.italankin.lnch.feature.home.HomeActivity;

public class SamsungAnrFix implements Runnable {

    public static void post(Context context) {
        new Handler().post(new SamsungAnrFix(context));
    }

    private static final long MAX_HANDLING_TIME = 1000L;

    private final long start = System.currentTimeMillis();
    private final Context context;

    private SamsungAnrFix(Context context) {
        this.context = context;
    }

    @Override
    public void run() {
        long now = System.currentTimeMillis();
        if (now - start > MAX_HANDLING_TIME) {
            Intent restartIntent = new Intent(context, HomeActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(restartIntent);
            Toast.makeText(context, "Restarting", Toast.LENGTH_LONG).show();
            System.exit(0);
        }
    }
}
