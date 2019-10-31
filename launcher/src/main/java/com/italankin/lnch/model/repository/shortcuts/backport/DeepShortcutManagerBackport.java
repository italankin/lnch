package com.italankin.lnch.model.repository.shortcuts.backport;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.Process;

import androidx.annotation.Nullable;

import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

class DeepShortcutManagerBackport {

    static List<ShortcutBackport> getForPackage(Context context, PackageManager pm, LauncherApps launcherApps,
            @Nullable ComponentName activity, String packageName) {
        List<ShortcutBackport> shortcutInfos = new ArrayList<>();
        List<LauncherActivityInfo> infoList = launcherApps.getActivityList(packageName, Process.myUserHandle());
        for (LauncherActivityInfo info : infoList) {
            if (activity == null || activity.equals(info.getComponentName())) {
                parsePackageXml(context, pm, info.getComponentName().getPackageName(),
                        info.getComponentName(), shortcutInfos);
            }
        }
        return shortcutInfos;
    }

    private static void parsePackageXml(Context context, PackageManager pm, String packageName,
            ComponentName activity, List<ShortcutBackport> shortcutInfos) {
        String resource = null;
        String currentActivity = "";
        String searchActivity = activity.getClassName();

        Map<String, String> parsedData = new HashMap<>();

        try {
            Resources resourcesForApplication = pm.getResourcesForApplication(packageName);
            AssetManager assets = resourcesForApplication.getAssets();
            XmlResourceParser parseXml = assets.openXmlResourceParser("AndroidManifest.xml");

            int eventType;
            while ((eventType = parseXml.nextToken()) != XmlPullParser.END_DOCUMENT) {
                if (eventType != XmlPullParser.START_TAG) {
                    continue;
                }
                String name = parseXml.getName();
                if ("activity".equals(name) || "activity-alias".equals(name)) {
                    parsedData.clear();
                    for (int i = 0; i < parseXml.getAttributeCount(); i++) {
                        parsedData.put(parseXml.getAttributeName(i), parseXml.getAttributeValue(i));
                    }
                    if (parsedData.containsKey("name")) {
                        currentActivity = parsedData.get("name");
                    }
                } else if (name.equals("meta-data") && currentActivity.equals(searchActivity)) {
                    parsedData.clear();
                    for (int i = 0; i < parseXml.getAttributeCount(); i++) {
                        parsedData.put(parseXml.getAttributeName(i), parseXml.getAttributeValue(i));
                    }
                    if (parsedData.containsKey("name") &&
                            parsedData.get("name").equals("android.app.shortcuts") &&
                            parsedData.containsKey("resource")) {
                        resource = parsedData.get("resource");
                    }
                }
            }
            parseXml.close();

            if (resource != null) {
                int resId = resourcesForApplication.getIdentifier(resource, null, packageName);
                parseXml = resourcesForApplication.getXml(resId == 0
                        ? Integer.parseInt(resource.substring(1))
                        : resId);

                while ((eventType = parseXml.nextToken()) != XmlPullParser.END_DOCUMENT) {
                    if (eventType != XmlPullParser.START_TAG) {
                        continue;
                    }
                    if (!parseXml.getName().equals("shortcut")) {
                        continue;
                    }
                    ShortcutBackport info = parseShortcut(context, resourcesForApplication,
                            packageName, parseXml);
                    if (info != null && info.getId() != null) {
                        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(
                                ShortcutBackport.stripPackage(info.getIntent()), 0);
                        for (ResolveInfo ri : resolveInfos) {
                            if (ri.isDefault || ri.activityInfo.exported) {
                                shortcutInfos.add(info);
                                break;
                            }
                        }
                    }
                }
                parseXml.close();
            }
        } catch (Exception e) {
            Timber.e(e, "parsePackageXml:");
        }
    }

    private static ShortcutBackport parseShortcut(Context context, Resources resources,
            String packageName, XmlResourceParser parseXml) {
        try {
            return new ShortcutBackport(context, resources, packageName, parseXml);
        } catch (Exception e) {
            Timber.e(e, "parseShortcut:");
        }
        return null;
    }

    private DeepShortcutManagerBackport() {
        // no instance
    }
}
