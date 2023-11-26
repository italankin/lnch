package com.italankin.lnch.util.widget.popup;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.*;
import androidx.appcompat.content.res.AppCompatResources;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.home.fragmentresult.SignalFragmentResultContract;
import com.italankin.lnch.util.ResUtils;
import com.italankin.lnch.util.ViewUtils;
import com.italankin.lnch.util.imageloader.ImageLoader;
import com.italankin.lnch.util.imageloader.cache.Cache;
import com.italankin.lnch.util.imageloader.cache.LruCache;

import java.util.ArrayList;
import java.util.List;

public abstract class ActionPopupFragment extends PopupFragment {

    private static final float DISABLED_ALPHA = 0.33f;

    private final Cache imageLoaderCache = new LruCache(16);
    protected ImageLoader imageLoader;

    protected ViewGroup actionsContainer;
    protected ViewGroup shortcutsContainer;

    private final List<ItemBuilder> actions = new ArrayList<>(1);
    private final List<ItemBuilder> shortcuts = new ArrayList<>(4);

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        imageLoader = new ImageLoader.Builder(context)
                .cache(imageLoaderCache)
                .build();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_action_popup, container, false);
        root = view.findViewById(R.id.popup_root);
        containerRoot = view.findViewById(R.id.popup_container_root);
        itemsContainer = view.findViewById(R.id.popup_item_container);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        actionsContainer = view.findViewById(R.id.action_container);
        shortcutsContainer = view.findViewById(R.id.shortcut_container);
    }

    protected final void dismissWithResult() {
        dismiss();
        Bundle result = new ActionDoneContract().result();
        sendResult(result);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Popup builder
    ///////////////////////////////////////////////////////////////////////////

    protected final void addAction(ItemBuilder item) {
        actions.add(item);
    }

    protected final void addShortcut(ItemBuilder item) {
        shortcuts.add(item);
    }

    protected final void createItemViews() {
        if (shortcuts.isEmpty()) {
            for (ItemBuilder action : actions) {
                addShortcutInternal(action.setIconDrawableTintAttr(android.R.attr.colorAccent));
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
        if (actions.isEmpty() && shortcuts.isEmpty()) {
            addShortcutInternal(new ItemBuilder()
                    .setLabel(R.string.popup_empty)
                    .setEnabled(false));
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
            ViewUtils.onGlobalLayout(imageView, () -> imageLoader.load(item.iconUri)
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
                if (item.label != null) {
                    Toast.makeText(requireContext(), item.label, Toast.LENGTH_SHORT).show();
                }
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
            ViewUtils.onGlobalLayout(labelView, () -> imageLoader.load(item.iconUri)
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

    protected final class ItemBuilder {
        private CharSequence label;
        private Drawable iconDrawable;
        @ColorInt
        private Integer iconDrawableTint;
        private Uri iconUri;
        private boolean enabled = true;
        private View.OnClickListener onClickListener;
        private View.OnLongClickListener onLongClickListener;
        private View.OnClickListener onPinClickListener;

        public ItemBuilder() {
        }

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

    public static class ActionDoneContract extends SignalFragmentResultContract {
        public ActionDoneContract() {
            super("popup_action_done");
        }
    }
}
