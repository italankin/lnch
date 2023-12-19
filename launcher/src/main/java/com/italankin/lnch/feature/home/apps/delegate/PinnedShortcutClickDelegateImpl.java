package com.italankin.lnch.feature.home.apps.delegate;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import androidx.annotation.Nullable;
import com.italankin.lnch.model.repository.usage.UsageTracker;
import com.italankin.lnch.model.ui.impl.PinnedShortcutDescriptorUi;
import com.italankin.lnch.util.IntentUtils;

public class PinnedShortcutClickDelegateImpl implements PinnedShortcutClickDelegate {

    private final Context context;
    private final ErrorDelegate errorDelegate;
    private final ItemPopupDelegate itemPopupDelegate;
    private final UsageTracker usageTracker;

    public PinnedShortcutClickDelegateImpl(Context context, ErrorDelegate errorDelegate,
            ItemPopupDelegate itemPopupDelegate, UsageTracker usageTracker) {
        this.context = context;
        this.errorDelegate = errorDelegate;
        this.itemPopupDelegate = itemPopupDelegate;
        this.usageTracker = usageTracker;
    }

    @Override
    public void onPinnedShortcutClick(PinnedShortcutDescriptorUi item) {
        Intent intent = IntentUtils.fromUri(item.uri);
        usageTracker.trackLaunch(item.getDescriptor());
        IntentUtils.safeStartActivity(context, intent, (unused, e) -> errorDelegate.showError(e));
    }

    @Override
    public void onPinnedShortcutLongClick(PinnedShortcutDescriptorUi item, @Nullable View itemView) {
        itemPopupDelegate.showItemPopup(item, itemView);
    }
}
