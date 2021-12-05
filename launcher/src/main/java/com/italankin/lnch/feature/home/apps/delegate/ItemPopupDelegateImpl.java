package com.italankin.lnch.feature.home.apps.delegate;

import android.content.Context;
import android.view.View;

import com.italankin.lnch.R;
import com.italankin.lnch.model.ui.CustomLabelDescriptorUi;
import com.italankin.lnch.model.ui.DescriptorUi;
import com.italankin.lnch.model.ui.RemovableDescriptorUi;
import com.italankin.lnch.model.ui.impl.DeepShortcutDescriptorUi;
import com.italankin.lnch.util.IntentUtils;
import com.italankin.lnch.util.widget.ActionPopupWindow;
import com.squareup.picasso.Picasso;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

public abstract class ItemPopupDelegateImpl implements ItemPopupDelegate {

    private final Context context;
    private final Picasso picasso;
    private final PopupDelegate popupDelegate;

    public ItemPopupDelegateImpl(Context context, Picasso picasso, PopupDelegate popupDelegate) {
        this.context = context;
        this.picasso = picasso;
        this.popupDelegate = popupDelegate;
    }

    protected abstract void removeItemImmediate(RemovableDescriptorUi item);

    @Override
    public void showItemPopup(DescriptorUi item, @Nullable View anchor) {
        ActionPopupWindow popup = new ActionPopupWindow(context, picasso);
        if (item instanceof DeepShortcutDescriptorUi) {
            popup.addShortcut(new ActionPopupWindow.ItemBuilder(context)
                    .setIcon(R.drawable.ic_app_info)
                    .setIconDrawableTintAttr(R.attr.colorAccent)
                    .setLabel(R.string.popup_app_info)
                    .setOnClickListener(v -> {
                        String packageName = ((DeepShortcutDescriptorUi) item).packageName;
                        IntentUtils.safeStartAppSettings(context, packageName, v);
                    })
            );
        }
        if (item instanceof RemovableDescriptorUi) {
            popup.addShortcut(new ActionPopupWindow.ItemBuilder(context)
                    .setIcon(R.drawable.ic_action_delete)
                    .setIconDrawableTintAttr(R.attr.colorAccent)
                    .setLabel(R.string.customize_item_delete)
                    .setOnClickListener(v -> {
                        String visibleLabel = ((CustomLabelDescriptorUi) item).getVisibleLabel();
                        String message = context.getString(R.string.popup_delete_message, visibleLabel);
                        new AlertDialog.Builder(context)
                                .setTitle(R.string.popup_delete_title)
                                .setMessage(message)
                                .setNegativeButton(R.string.cancel, null)
                                .setPositiveButton(R.string.popup_delete_action, (dialog, which) -> {
                                    removeItemImmediate((RemovableDescriptorUi) item);
                                })
                                .show();
                    })
            );
        }
        popupDelegate.showPopupWindow(popup, anchor);
    }
}
