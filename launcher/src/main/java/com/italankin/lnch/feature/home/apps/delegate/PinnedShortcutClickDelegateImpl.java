package com.italankin.lnch.feature.home.apps.delegate;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.italankin.lnch.R;
import com.italankin.lnch.model.ui.impl.PinnedShortcutDescriptorUi;
import com.italankin.lnch.util.IntentUtils;

import androidx.annotation.Nullable;

public class PinnedShortcutClickDelegateImpl implements PinnedShortcutClickDelegate {

    private final Context context;
    private final ErrorDelegate errorDelegate;
    private final ItemPopupDelegate itemPopupDelegate;

    public PinnedShortcutClickDelegateImpl(Context context, ErrorDelegate errorDelegate,
            ItemPopupDelegate itemPopupDelegate) {
        this.context = context;
        this.errorDelegate = errorDelegate;
        this.itemPopupDelegate = itemPopupDelegate;
    }

    @Override
    public void onPinnedShortcutClick(PinnedShortcutDescriptorUi item) {
        Intent intent = IntentUtils.fromUri(item.uri);
        if (!IntentUtils.safeStartActivity(context, intent)) {
            errorDelegate.showError(R.string.error);
        }
    }

    @Override
    public void onPinnedShortcutLongClick(PinnedShortcutDescriptorUi item, @Nullable View itemView) {
        itemPopupDelegate.showItemPopup(item, itemView);
    }
}
