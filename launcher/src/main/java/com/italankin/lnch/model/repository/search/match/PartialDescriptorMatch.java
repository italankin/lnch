package com.italankin.lnch.model.repository.search.match;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import com.italankin.lnch.R;
import com.italankin.lnch.feature.receiver.StartShortcutReceiver;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.model.descriptor.impl.DeepShortcutDescriptor;
import com.italankin.lnch.model.descriptor.impl.IntentDescriptor;
import com.italankin.lnch.model.descriptor.impl.PinnedShortcutDescriptor;
import com.italankin.lnch.model.repository.shortcuts.Shortcut;
import com.italankin.lnch.util.IntentUtils;
import com.italankin.lnch.util.imageloader.resourceloader.PackageIconLoader;
import com.italankin.lnch.util.imageloader.resourceloader.ShortcutIconLoader;

import java.util.Collections;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

public class PartialDescriptorMatch extends PartialMatch implements DescriptorMatch {

    private final Descriptor descriptor;
    private final Kind kind;

    public PartialDescriptorMatch(Descriptor descriptor, Type type, Kind kind) {
        super(type);
        this.descriptor = descriptor;
        this.kind = kind;
        actions.add(Action.INFO);
    }

    public PartialDescriptorMatch(AppDescriptor descriptor, PackageManager packageManager, Type type) {
        super(type);
        this.descriptor = descriptor;
        kind = Kind.APP;
        actions.add(Action.INFO);
        color = descriptor.getVisibleColor();
        intent = packageManager.getLaunchIntentForPackage(descriptor.packageName);
        if (intent != null && descriptor.componentName != null) {
            intent.setComponent(ComponentName.unflattenFromString(descriptor.componentName));
        }
        icon = PackageIconLoader.uriFrom(descriptor.packageName);
        label = descriptor.getVisibleLabel();
    }

    public PartialDescriptorMatch(IntentDescriptor descriptor, Type type) {
        super(type);
        this.descriptor = descriptor;
        kind = Kind.OTHER;
        color = descriptor.getVisibleColor();
        intent = IntentUtils.fromUri(descriptor.intentUri, Intent.URI_INTENT_SCHEME);
        iconRes = R.drawable.ic_launch_intent;
        actions = Collections.emptySet();
        label = descriptor.getVisibleLabel();
    }

    public PartialDescriptorMatch(PinnedShortcutDescriptor descriptor, Type type) {
        super(type);
        this.descriptor = descriptor;
        kind = Kind.SHORTCUT;
        color = descriptor.getVisibleColor();
        intent = IntentUtils.fromUri(descriptor.uri);
        iconRes = R.drawable.ic_shortcut;
        label = descriptor.getVisibleLabel();
    }

    @RequiresApi(api = Build.VERSION_CODES.N_MR1)
    public PartialDescriptorMatch(DeepShortcutDescriptor descriptor, Shortcut shortcut, Type type) {
        super(type);
        this.descriptor = descriptor;
        kind = Kind.SHORTCUT;
        icon = ShortcutIconLoader.uriFrom(shortcut, true);
        label = descriptor.getVisibleLabel();
        intent = StartShortcutReceiver.makeStartIntent(shortcut);
    }

    @Override
    public Descriptor getDescriptor() {
        return descriptor;
    }

    @Nullable
    @Override
    public CharSequence getSubtext(Context context) {
        return null;
    }

    @Override
    public Kind getKind() {
        return kind;
    }

    @Override
    public int hashCode() {
        return descriptor.hashCode();
    }
}
