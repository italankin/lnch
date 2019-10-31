/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.italankin.lnch.model.repository.shortcuts.backport;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.italankin.lnch.model.repository.shortcuts.Shortcut;
import com.italankin.lnch.util.picasso.PackageResourceHandler;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

class ShortcutBackport implements Shortcut {
    private final static String USE_PACKAGE = "shortcut_backport_use_package";
    private final static String EXTRA_SHORTCUT_ID = "shortcut_backport_id";

    static Intent stripPackage(Intent intent) {
        intent = new Intent(intent);
        if (!intent.getBooleanExtra(ShortcutBackport.USE_PACKAGE, true)) {
            intent.setPackage(null);
        }
        intent.removeExtra(ShortcutBackport.USE_PACKAGE);
        return intent;
    }

    private final Context context;
    private final String packageName;

    private final String id;
    private final boolean enabled;
    private final int iconRes;
    private final String shortLabel;
    private final String longLabel;
    private final String disabledMessage;

    private final Intent mIntent;

    ShortcutBackport(Context context, Resources resources, String packageName, XmlResourceParser parseXml)
            throws XmlPullParserException, IOException {
        this.context = context;
        this.packageName = packageName;

        HashMap<String, String> xmlData = new HashMap<>();
        for (int i = 0; i < parseXml.getAttributeCount(); i++) {
            xmlData.put(parseXml.getAttributeName(i), parseXml.getAttributeValue(i));
        }

        id = xmlData.get("shortcutId");
        enabled = !xmlData.containsKey("enabled") || xmlData.get("enabled").toLowerCase().equals("true");

        if (xmlData.containsKey("icon")) {
            String icon = xmlData.get("icon");
            int resId = resources.getIdentifier(icon, null, packageName);
            iconRes = resId == 0 ? Integer.parseInt(icon.substring(1)) : resId;
        } else {
            iconRes = 0;
        }

        shortLabel = xmlData.containsKey("shortcutShortLabel")
                ? getString(resources, xmlData.get("shortcutShortLabel"))
                : "";
        longLabel = xmlData.containsKey("shortcutLongLabel")
                ? getString(resources, xmlData.get("shortcutLongLabel"))
                : shortLabel;
        disabledMessage = xmlData.containsKey("shortcutDisabledMessage")
                ? getString(resources, xmlData.get("shortcutDisabledMessage"))
                : "";

        HashMap<String, String> xmlDataIntent = new HashMap<>();
        HashMap<String, String> xmlDataExtras = new HashMap<>();
        HashMap<String, String> extras = new HashMap<>();
        int startDepth = parseXml.getDepth();
        do {
            if (parseXml.nextToken() != XmlPullParser.START_TAG) {
                continue;
            }
            String xmlName = parseXml.getName();
            if (xmlName.equals("intent")) {
                xmlDataIntent.clear();
                extras.clear();
                for (int i = 0; i < parseXml.getAttributeCount(); i++) {
                    xmlDataIntent.put(parseXml.getAttributeName(i), parseXml.getAttributeValue(i));
                }
            } else if (xmlName.equals("extra")) {
                xmlDataExtras.clear();
                for (int i = 0; i < parseXml.getAttributeCount(); i++) {
                    xmlDataExtras.put(parseXml.getAttributeName(i), parseXml.getAttributeValue(i));
                }
                if (xmlDataExtras.containsKey("name") && xmlDataExtras.containsKey("value")) {
                    extras.put(xmlDataExtras.get("name"), xmlDataExtras.get("value"));
                }
            }
        } while (parseXml.getDepth() > startDepth);

        String action = xmlDataIntent.containsKey("action")
                ? xmlDataIntent.get("action")
                : Intent.ACTION_MAIN;

        boolean useTargetPackage = xmlDataIntent.containsKey("targetPackage");
        String targetPackage = useTargetPackage
                ? xmlDataIntent.get("targetPackage")
                : packageName;

        mIntent = new Intent(action)
                .setPackage(targetPackage)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME)
                .putExtra(EXTRA_SHORTCUT_ID, id);

        if (xmlDataIntent.containsKey("targetClass")) {
            mIntent.setComponent(new ComponentName(targetPackage, xmlDataIntent.get("targetClass")));
        }

        if (xmlDataIntent.containsKey("data")) {
            mIntent.setData(Uri.parse(xmlDataIntent.get("data")));
        }

        for (Map.Entry<String, String> entry : extras.entrySet()) {
            mIntent.putExtra(entry.getKey(), entry.getValue());
        }

        mIntent.putExtra(USE_PACKAGE, useTargetPackage);
    }

    @Override
    public Uri getIconUri() {
        return PackageResourceHandler.uriFrom(packageName, iconRes);
    }

    @Override
    public String getPackageName() {
        return packageName;
    }

    @Override
    public boolean start(Rect bounds, Bundle options) {
        try {
            context.startActivity(stripPackage(mIntent));
            return true;
        } catch (Exception e) {
            Timber.w(e, "start:");
            return false;
        }
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public CharSequence getShortLabel() {
        CharSequence label = shortLabel;
        return label != null ? label : getLongLabel();
    }

    @Override
    public CharSequence getLongLabel() {
        CharSequence longLabel = this.longLabel;
        return longLabel != null ? longLabel : "";
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean isDynamic() {
        return false;
    }

    @Override
    public int getRank() {
        return 1;
    }

    @Override
    public CharSequence getDisabledMessage() {
        return disabledMessage;
    }

    public Intent getIntent() {
        return mIntent;
    }

    private static String getString(Resources resources, @Nullable String value) {
        if (value == null || value.length() < 2) {
            return "";
        }
        return resources.getString(Integer.valueOf(value.substring(1)));
    }
}
