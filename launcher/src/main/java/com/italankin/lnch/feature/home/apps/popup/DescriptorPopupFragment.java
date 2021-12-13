package com.italankin.lnch.feature.home.apps.popup;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.home.apps.FragmentResults;
import com.italankin.lnch.feature.home.apps.delegate.CustomizeDelegate;
import com.italankin.lnch.feature.home.apps.delegate.ErrorDelegate;
import com.italankin.lnch.feature.home.apps.delegate.ErrorDelegateImpl;
import com.italankin.lnch.feature.home.apps.delegate.ShortcutStarterDelegate;
import com.italankin.lnch.feature.home.apps.delegate.ShortcutStarterDelegateImpl;
import com.italankin.lnch.model.descriptor.Descriptor;
import com.italankin.lnch.model.descriptor.PackageDescriptor;
import com.italankin.lnch.model.descriptor.impl.DeepShortcutDescriptor;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.repository.shortcuts.Shortcut;
import com.italankin.lnch.model.repository.shortcuts.ShortcutsRepository;
import com.italankin.lnch.model.ui.CustomLabelDescriptorUi;
import com.italankin.lnch.model.ui.DescriptorUi;
import com.italankin.lnch.model.ui.InFolderDescriptorUi;
import com.italankin.lnch.model.ui.RemovableDescriptorUi;
import com.italankin.lnch.model.ui.impl.AppDescriptorUi;
import com.italankin.lnch.model.ui.impl.DeepShortcutDescriptorUi;
import com.italankin.lnch.model.ui.util.DescriptorUiFactory;
import com.italankin.lnch.util.IntentUtils;
import com.italankin.lnch.util.ListUtils;
import com.italankin.lnch.util.NumberUtils;
import com.italankin.lnch.util.PackageUtils;
import com.italankin.lnch.util.ResUtils;
import com.italankin.lnch.util.ViewUtils;
import com.italankin.lnch.util.widget.popup.PopupFragment;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;

public class DescriptorPopupFragment extends PopupFragment {

    public static DescriptorPopupFragment newInstance(
            DescriptorUi descriptorUi,
            String requestKey,
            @Nullable View anchorView) {
        Rect anchor = ViewUtils.getViewBounds(anchorView);
        return newInstance(descriptorUi, requestKey, anchor);
    }

    public static DescriptorPopupFragment newInstance(
            DescriptorUi descriptorUi,
            String requestKey,
            @Nullable Rect anchor) {
        DescriptorPopupFragment fragment = new DescriptorPopupFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_ANCHOR, anchor);
        args.putString(ARG_DESCRIPTOR_ID, descriptorUi.getDescriptor().getId());
        args.putString(ARG_REQUEST_KEY, requestKey);
        if (descriptorUi instanceof InFolderDescriptorUi) {
            args.putString(ARG_FOLDER_ID, ((InFolderDescriptorUi) descriptorUi).getFolderId());
        }
        fragment.setArguments(args);
        return fragment;
    }

    private static final String ARG_DESCRIPTOR_ID = "descriptor_id";
    private static final String ARG_FOLDER_ID = "folder_id";
    private static final String ARG_REQUEST_KEY = "request_key";

    private static final String BACKSTACK_NAME = "descriptor_popup";
    private static final String TAG = "descriptor_popup";

    private static final float DISABLED_ALPHA = 0.33f;

    private Picasso picasso;
    private ShortcutsRepository shortcutsRepository;
    private Preferences preferences;

    private ViewGroup actionsContainer;
    private ViewGroup shortcutsContainer;

    private ErrorDelegate errorDelegate;
    private ShortcutStarterDelegate shortcutStarterDelegate;

    private final List<ItemBuilder> actions = new ArrayList<>(1);
    private final List<ItemBuilder> shortcuts = new ArrayList<>(4);

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        picasso = LauncherApp.daggerService.main().picassoFactory().create(context);
        preferences = LauncherApp.daggerService.main().preferences();
        shortcutsRepository = LauncherApp.daggerService.main().shortcutsRepository();
    }

    @Override
    protected String getPopupBackstackName() {
        return BACKSTACK_NAME;
    }

    @Override
    protected String getPopupTag() {
        return TAG;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) super.onCreateView(inflater, container, savedInstanceState);
        inflater.inflate(R.layout.partial_descriptor_popup, itemsContainer, true);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        actionsContainer = view.findViewById(R.id.action_container);
        shortcutsContainer = view.findViewById(R.id.shortcut_container);

        Context context = requireContext();
        errorDelegate = new ErrorDelegateImpl(context);
        CustomizeDelegate customizeDelegate = () -> {
            Bundle result = new Bundle();
            result.putString(FragmentResults.RESULT, FragmentResults.Customize.KEY);
            sendResult(result);
            dismiss();
        };
        shortcutStarterDelegate = new ShortcutStarterDelegateImpl(context, errorDelegate, customizeDelegate);

        load();
    }

    private void load() {
        Bundle args = requireArguments();
        String descriptorId = args.getString(ARG_DESCRIPTOR_ID);
        Descriptor descriptor = LauncherApp.daggerService.main()
                .descriptorRepository()
                .findById(Descriptor.class, descriptorId);
        DescriptorUi item = DescriptorUiFactory.createItem(descriptor);
        if (item instanceof InFolderDescriptorUi) {
            String folderId = args.getString(ARG_FOLDER_ID);
            ((InFolderDescriptorUi) item).setFolderId(folderId);
        }
        if (item instanceof AppDescriptorUi) {
            showAppPopup((AppDescriptorUi) item);
        } else {
            showItemPopup(item);
        }
        populateItems();
        showPopup();
    }

    private void showAppPopup(AppDescriptorUi item) {
        Context context = requireContext();
        boolean uninstallAvailable = !PackageUtils.isSystem(context.getPackageManager(), item.packageName);
        ItemBuilder infoItem = new ItemBuilder()
                .setLabel(R.string.popup_app_info)
                .setIcon(R.drawable.ic_app_info)
                .setOnClickListener(v -> {
                    startAppSettings(item.getDescriptor(), v);
                });
        ItemBuilder uninstallItem = new ItemBuilder()
                .setLabel(R.string.popup_app_uninstall)
                .setIcon(R.drawable.ic_action_delete)
                .setOnClickListener(v -> {
                    startUninstall(item);
                });
        ItemBuilder removeFromFolderItem = new ItemBuilder()
                .setIcon(R.drawable.ic_action_remove_from_folder)
                .setLabel(R.string.customize_item_remove_from_folder)
                .setOnClickListener(v -> {
                    removeFromFolder(item);
                });

        List<Shortcut> shortcuts = getShortcuts(item);
        if (item.getFolderId() != null) {
            if (shortcuts.isEmpty()) {
                addShortcut(removeFromFolderItem.setIconDrawableTintAttr(R.attr.colorAccent));
            } else {
                addAction(removeFromFolderItem);
            }
        }
        addAction(infoItem);
        if (uninstallAvailable) {
            addAction(uninstallItem);
        }
        for (Shortcut shortcut : shortcuts) {
            addShortcut(new ItemBuilder()
                    .setLabel(shortcut.getShortLabel())
                    .setIcon(shortcut.getIconUri())
                    .setEnabled(shortcut.isEnabled())
                    .setOnClickListener(v -> {
                        startShortcut(shortcut, v);
                    })
                    .setOnPinClickListener(v -> {
                        pinShortcut(shortcut);
                    })
            );
        }
    }

    private void showItemPopup(DescriptorUi item) {
        Context context = requireContext();
        if (item.getDescriptor() instanceof PackageDescriptor) {
            addAction(new ItemBuilder()
                    .setIcon(R.drawable.ic_app_info)
                    .setLabel(R.string.popup_app_info)
                    .setOnClickListener(v -> {
                        PackageDescriptor descriptor = (PackageDescriptor) item.getDescriptor();
                        startAppSettings(descriptor, v);
                    })
            );
        }
        if (item instanceof RemovableDescriptorUi) {
            addAction(new ItemBuilder()
                    .setIcon(R.drawable.ic_action_delete)
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
        if (item instanceof InFolderDescriptorUi && ((InFolderDescriptorUi) item).getFolderId() != null) {
            addShortcut(new ItemBuilder()
                    .setIcon(R.drawable.ic_action_remove_from_folder)
                    .setIconDrawableTintAttr(R.attr.colorAccent)
                    .setLabel(R.string.customize_item_remove_from_folder)
                    .setOnClickListener(v -> {
                        removeFromFolder(((InFolderDescriptorUi) item));
                    })
            );
        }
    }
    ///////////////////////////////////////////////////////////////////////////
    // Actions
    ///////////////////////////////////////////////////////////////////////////

    private void startShortcut(Shortcut shortcut, View v) {
        shortcutStarterDelegate.startShortcut(shortcut, v);
        Bundle result = new Bundle();
        result.putString(FragmentResults.RESULT, FragmentResults.OnActionHandled.KEY);
        sendResult(result);
        dismiss();
    }

    private void startUninstall(AppDescriptorUi item) {
        Intent intent = PackageUtils.getUninstallIntent(item.packageName);
        if (IntentUtils.safeStartActivity(requireContext(), intent)) {
            Bundle result = new Bundle();
            result.putString(FragmentResults.RESULT, FragmentResults.OnActionHandled.KEY);
            sendResult(result);
            dismiss();
        } else {
            errorDelegate.showError(R.string.error);
        }
    }

    private void startAppSettings(PackageDescriptor item, View bounds) {
        IntentUtils.safeStartAppSettings(requireContext(), item.getPackageName(), bounds);
        Bundle result = new Bundle();
        result.putString(FragmentResults.RESULT, FragmentResults.OnActionHandled.KEY);
        sendResult(result);
        dismiss();
    }

    private void removeItemImmediate(RemovableDescriptorUi item) {
        Bundle result = new Bundle();
        result.putString(FragmentResults.RESULT, FragmentResults.RemoveItem.KEY);
        result.putString(FragmentResults.RemoveItem.DESCRIPTOR_ID, item.getDescriptor().getId());
        sendResult(result);
        dismiss();
    }

    private void removeFromFolder(InFolderDescriptorUi item) {
        Bundle result = new Bundle();
        result.putString(FragmentResults.RESULT, FragmentResults.RemoveFromFolder.KEY);
        result.putString(FragmentResults.RemoveFromFolder.DESCRIPTOR_ID, item.getDescriptor().getId());
        result.putString(FragmentResults.RemoveFromFolder.FOLDER_ID, item.getFolderId());
        sendResult(result);
        dismiss();
    }

    private void pinShortcut(Shortcut shortcut) {
        Bundle result = new Bundle();
        result.putString(FragmentResults.RESULT, FragmentResults.PinShortcut.KEY);
        result.putString(FragmentResults.PinShortcut.PACKAGE_NAME, shortcut.getPackageName());
        result.putString(FragmentResults.PinShortcut.SHORTCUT_ID, shortcut.getId());
        sendResult(result);
        dismiss();
    }

    private void sendResult(Bundle result) {
        String requestKey = requireArguments().getString(ARG_REQUEST_KEY);
        getParentFragmentManager().setFragmentResult(requestKey, result);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Popup builder
    ///////////////////////////////////////////////////////////////////////////

    private void addAction(ItemBuilder item) {
        actions.add(item);
    }

    private void addShortcut(ItemBuilder item) {
        shortcuts.add(item);
    }

    private void populateItems() {
        if (shortcuts.isEmpty()) {
            for (ItemBuilder action : actions) {
                addShortcutInternal(action.setIconDrawableTintAttr(R.attr.colorAccent));
            }
        } else {
            for (ItemBuilder action : actions) {
                addActionInternal(action);
            }
            for (ItemBuilder shortcut : shortcuts) {
                addShortcutInternal(shortcut);
            }
        }
        if (actionsContainer.getChildCount() == 0) {
            containerRoot.setArrowColors(ResUtils.resolveColor(requireContext(), R.attr.colorPopupBackground));
        }
        actions.clear();
        shortcuts.clear();
    }

    private void addActionInternal(ItemBuilder item) {
        ImageView imageView = (ImageView) getLayoutInflater()
                .inflate(R.layout.item_popup_action, actionsContainer, false);
        if (item.iconDrawable != null) {
            Drawable drawable = item.iconDrawable.mutate();
            if (item.iconDrawableTint != null) {
                drawable.setTint(item.iconDrawableTint);
            }
            imageView.setImageDrawable(drawable);
        } else if (item.iconUri != null) {
            ViewUtils.onGlobalLayout(imageView, () -> picasso.load(item.iconUri)
                    .resizeDimen(R.dimen.popup_action_icon_size, R.dimen.popup_action_icon_size)
                    .centerInside()
                    .into(imageView));
        }
        if (item.onClickListener != null) {
            imageView.setOnClickListener(item.onClickListener);
        }
        if (!item.enabled) {
            imageView.setAlpha(DISABLED_ALPHA);
            imageView.setEnabled(false);
        }
        if (item.onLongClickListener != null) {
            imageView.setOnLongClickListener(item.onLongClickListener);
        } else {
            imageView.setOnLongClickListener(v -> {
                Toast.makeText(requireContext(), item.label, Toast.LENGTH_SHORT).show();
                return true;
            });
        }
        actionsContainer.addView(imageView);
    }

    private void addShortcutInternal(ItemBuilder item) {
        View view = getLayoutInflater().inflate(R.layout.item_popup_shortcut, shortcutsContainer, false);
        ImageView iconView = view.findViewById(R.id.icon);
        ImageView pinIconView = view.findViewById(R.id.icon_pin);
        TextView labelView = view.findViewById(R.id.label);
        labelView.setText(item.label);
        if (item.iconDrawable != null) {
            Drawable drawable = item.iconDrawable.mutate();
            if (item.iconDrawableTint != null) {
                drawable.setTint(item.iconDrawableTint);
            }
            iconView.setImageDrawable(drawable);
            iconView.setVisibility(View.VISIBLE);
        } else if (item.iconUri != null) {
            iconView.setVisibility(View.VISIBLE);
            ViewUtils.onGlobalLayout(labelView, () -> picasso.load(item.iconUri)
                    .resizeDimen(R.dimen.popup_shortcut_icon_size, R.dimen.popup_shortcut_icon_size)
                    .centerInside()
                    .into(iconView));
        }
        if (item.onClickListener != null) {
            view.setOnClickListener(item.onClickListener);
        }
        view.setOnLongClickListener(item.onLongClickListener);
        if (!item.enabled) {
            labelView.setAlpha(DISABLED_ALPHA);
            iconView.setAlpha(DISABLED_ALPHA);
            view.setEnabled(false);
        }
        if (item.onPinClickListener != null && item.enabled) {
            pinIconView.setVisibility(View.VISIBLE);
            pinIconView.setOnClickListener(item.onPinClickListener);
        }
        shortcutsContainer.addView(view);
    }

    private List<Shortcut> getShortcuts(AppDescriptorUi item) {
        if (!item.getDescriptor().showShortcuts) {
            return Collections.emptyList();
        }
        List<Shortcut> shortcuts = shortcutsRepository.getShortcuts(item.getDescriptor());
        if (shortcuts.isEmpty()) {
            return Collections.emptyList();
        }
        if (preferences.get(Preferences.SHORTCUTS_SORT_MODE) == Preferences.ShortcutsSortMode.REVERSED) {
            shortcuts = ListUtils.reversedCopy(shortcuts);
        }
        int max = NumberUtils.parseInt(preferences.get(Preferences.MAX_DYNAMIC_SHORTCUTS), -1);
        if (max < 0 || shortcuts.size() <= max) {
            return shortcuts;
        }
        List<Shortcut> result = new ArrayList<>(shortcuts.size());
        for (Shortcut shortcut : shortcuts) {
            if (!shortcut.isDynamic() || max-- > 0) {
                result.add(shortcut);
            }
        }
        return result;
    }

    private class ItemBuilder {
        private CharSequence label;
        private Drawable iconDrawable;
        @ColorInt
        private Integer iconDrawableTint;
        private Uri iconUri;
        private boolean enabled = true;
        private View.OnClickListener onClickListener;
        private View.OnLongClickListener onLongClickListener;
        private View.OnClickListener onPinClickListener;

        public ItemBuilder setLabel(@StringRes int label) {
            return setLabel(requireContext().getText(label));
        }

        public ItemBuilder setLabel(CharSequence label) {
            this.label = label;
            return this;
        }

        public ItemBuilder setIcon(Uri uri) {
            this.iconUri = uri;
            return this;
        }

        public ItemBuilder setIcon(@DrawableRes int icon) {
            return setIcon(AppCompatResources.getDrawable(requireContext(), icon));
        }

        public ItemBuilder setIcon(Drawable icon) {
            this.iconDrawable = icon;
            return this;
        }

        public ItemBuilder setIconDrawableTint(@ColorInt int tint) {
            this.iconDrawableTint = tint;
            return this;
        }

        public ItemBuilder setIconDrawableTintAttr(@AttrRes int attr) {
            return setIconDrawableTint(ResUtils.resolveColor(requireContext(), attr));
        }

        public ItemBuilder setEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public ItemBuilder setOnClickListener(View.OnClickListener listener) {
            this.onClickListener = listener;
            return this;
        }

        public ItemBuilder setOnLongClickListener(View.OnLongClickListener listener) {
            this.onLongClickListener = listener;
            return this;
        }

        public ItemBuilder setOnPinClickListener(View.OnClickListener listener) {
            this.onPinClickListener = listener;
            return this;
        }
    }
}
