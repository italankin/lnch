package com.italankin.lnch.util.widget;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.italankin.lnch.R;
import com.italankin.lnch.util.ResUtils;
import com.italankin.lnch.util.ViewUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.appcompat.content.res.AppCompatResources;

public class ActionPopupWindow extends BasePopupWindow {

    private static final float DISABLED_ALPHA = 0.33f;

    private final Picasso picasso;
    private final LayoutInflater inflater;
    private ViewGroup actionContainer;
    private ViewGroup shortcutContainer;

    private final List<ItemBuilder> actions = new ArrayList<>(1);
    private final List<ItemBuilder> shortcuts = new ArrayList<>(4);

    public ActionPopupWindow(Context context, Picasso picasso) {
        super(context);
        this.inflater = LayoutInflater.from(context);
        this.picasso = picasso;
    }

    @Override
    protected void onCreateView(ViewGroup parent) {
        ViewGroup contentView = (ViewGroup) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.widget_action_popup, parent);
        actionContainer = contentView.findViewById(R.id.action_container);
        shortcutContainer = contentView.findViewById(R.id.shortcut_container);
        contentView.findViewById(R.id.popup_container)
                .setClipToOutline(true);
    }

    public ActionPopupWindow addAction(ItemBuilder item) {
        actions.add(item);
        return this;
    }

    public ActionPopupWindow addShortcut(ItemBuilder item) {
        shortcuts.add(item);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showAtAnchor(View anchorView, View parent) {
        populateItems();
        super.showAtAnchor(anchorView, parent);
    }

    /**
     * {@inheritDoc}
     */
    public void showAtAnchor(View anchorView, Rect bounds) {
        populateItems();
        super.showAtAnchor(anchorView, bounds);
    }

    @Override
    protected boolean isDarkArrow() {
        return actionContainer.getChildCount() > 0;
    }

    private void populateItems() {
        for (ItemBuilder action : actions) {
            addActionInternal(action);
        }
        for (ItemBuilder shortcut : shortcuts) {
            addShortcutInternal(shortcut);
        }
        actions.clear();
        shortcuts.clear();
    }

    private void addActionInternal(ItemBuilder item) {
        ImageView imageView = (ImageView) inflater.inflate(R.layout.item_popup_action, actionContainer, false);
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
            imageView.setOnClickListener(v -> {
                item.onClickListener.onClick(v);
                dismiss();
            });
        }
        if (!item.enabled) {
            imageView.setAlpha(DISABLED_ALPHA);
            imageView.setEnabled(false);
        }
        if (item.onLongClickListener != null) {
            imageView.setOnLongClickListener(item.onLongClickListener);
        } else {
            imageView.setOnLongClickListener(v -> {
                Toast.makeText(item.context, item.label, Toast.LENGTH_SHORT).show();
                return true;
            });
        }
        actionContainer.addView(imageView);
    }

    private void addShortcutInternal(ItemBuilder item) {
        View view = inflater.inflate(R.layout.item_popup_shortcut, shortcutContainer, false);
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
            view.setOnClickListener(v -> {
                item.onClickListener.onClick(v);
                dismiss();
            });
        }
        view.setOnLongClickListener(item.onLongClickListener);
        if (!item.enabled) {
            labelView.setAlpha(DISABLED_ALPHA);
            iconView.setAlpha(DISABLED_ALPHA);
            view.setEnabled(false);
        }
        if (item.onPinClickListener != null && item.enabled) {
            pinIconView.setVisibility(View.VISIBLE);
            pinIconView.setOnClickListener(v -> {
                item.onPinClickListener.onClick(v);
                dismiss();
            });
        }
        shortcutContainer.addView(view);
    }

    public static class ItemBuilder {

        private final Context context;
        private CharSequence label;
        private Drawable iconDrawable;
        @ColorInt
        private Integer iconDrawableTint;
        private Uri iconUri;
        private boolean enabled = true;
        private View.OnClickListener onClickListener;
        private View.OnLongClickListener onLongClickListener;
        private View.OnClickListener onPinClickListener;

        public ItemBuilder(Context context) {
            this.context = context;
        }

        public ItemBuilder setLabel(@StringRes int label) {
            return setLabel(context.getText(label));
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
            return setIcon(AppCompatResources.getDrawable(context, icon));
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
            return setIconDrawableTint(ResUtils.resolveColor(context, attr));
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
