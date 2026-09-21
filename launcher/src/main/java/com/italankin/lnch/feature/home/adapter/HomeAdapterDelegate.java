package com.italankin.lnch.feature.home.adapter;

import android.annotation.SuppressLint;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexboxLayoutManager;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.home.model.Appearance;
import com.italankin.lnch.feature.home.model.UserPrefs;
import com.italankin.lnch.feature.home.util.NotificationDotDrawable;
import com.italankin.lnch.model.descriptor.props.ItemWidth;
import com.italankin.lnch.model.descriptor.props.TextAlign;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.ui.DescriptorUi;
import com.italankin.lnch.util.ResUtils;
import com.italankin.lnch.util.ViewUtils;

import java.util.List;

import me.italankin.adapterdelegates.BaseAdapterDelegate;

public abstract class HomeAdapterDelegate<VH extends HomeAdapterDelegate.ViewHolder<T>, T extends DescriptorUi>
        extends BaseAdapterDelegate<VH, T> {

    private final Params params;
    private UserPrefs.ItemPrefs itemPrefs;

    protected HomeAdapterDelegate() {
        this(Params.DEFAULT);
    }

    protected HomeAdapterDelegate(Params params) {
        this.params = params;
    }

    @Override
    public final void onBind(VH holder, int position, T item) {
        updateHolderView(holder, item);
        holder.bind(item);
    }

    @Override
    public void onBind(@NonNull VH holder, int position, T item, @NonNull List<?> payloads) {
        updateHolderView(holder, item);
        holder.bind(item, payloads);
    }

    @NonNull
    @Override
    public VH onCreate(LayoutInflater inflater, ViewGroup parent) {
        VH holder = super.onCreate(inflater, parent);
        updateHolderView(holder, null);
        return holder;
    }

    @Override
    public long getItemId(int position, T item) {
        return item.getDescriptor().getId().hashCode();
    }

    @Override
    public final boolean isType(int position, Object item) {
        return isType(position, item, params.ignoreVisibility);
    }

    protected abstract boolean isType(int position, Object item, boolean ignoreVisibility);

    void setItemPrefs(UserPrefs.ItemPrefs itemPrefs) {
        this.itemPrefs = itemPrefs;
    }

    private void updateHolderView(VH holder, @Nullable T item) {
        TextView label = holder.getLabel();
        if (label == null || itemPrefs == null) {
            return;
        }
        Appearance appearance = params.applyAppearance ? Appearance.from(item) : Appearance.DEFAULT;
        if (itemPrefs.equals(holder.itemPrefs) && appearance.equals(holder.appearance)) {
            return;
        }
        update(holder, label, itemPrefs, appearance);
        holder.itemPrefs = itemPrefs;
        holder.appearance = appearance;
    }

    protected void update(VH holder, TextView label, UserPrefs.ItemPrefs itemPrefs, Appearance appearance) {
        ViewUtils.setPaddingDp(label, itemPrefs.itemPadding());
        label.setTextSize(itemPrefs.itemTextSize());
        Integer itemShadowColor = itemPrefs.itemShadowColor();
        int shadowColor = itemShadowColor != null
                ? itemShadowColor
                : ResUtils.resolveColor(label.getContext(), R.attr.colorItemShadowDefault);
        label.setShadowLayer(itemPrefs.itemShadowRadius(), label.getShadowDx(),
                label.getShadowDy(), shadowColor);
        label.setTypeface(itemPrefs.typeface());
        updateNotificationDot(holder, itemPrefs);
        updateItemWidth(holder, label, itemPrefs, appearance);
    }

    @SuppressLint("RtlHardcoded")
    private void updateNotificationDot(VH holder, UserPrefs.ItemPrefs itemPrefs) {
        NotificationDotDrawable notificationDot = holder.getNotificationDot();
        if (notificationDot == null) {
            return;
        }
        switch (itemPrefs.notificationDotPosition()) {
            case TOP_LEFT:
                notificationDot.setGravity(Gravity.TOP | Gravity.LEFT);
                break;
            case TOP_RIGHT:
                notificationDot.setGravity(Gravity.TOP | Gravity.RIGHT);
                break;
            case BOT_LEFT:
                notificationDot.setGravity(Gravity.BOTTOM | Gravity.LEFT);
                break;
            case BOT_RIGHT:
                notificationDot.setGravity(Gravity.BOTTOM | Gravity.RIGHT);
                break;
        }
        notificationDot.setColor(itemPrefs.notificationDotColor());
        switch (itemPrefs.notificationDotSize()) {
            case SMALL:
                notificationDot.setSize(NotificationDotDrawable.Size.SMALL);
                break;
            case NORMAL:
                notificationDot.setSize(NotificationDotDrawable.Size.NORMAL);
                break;
            case LARGE:
                notificationDot.setSize(NotificationDotDrawable.Size.LARGE);
                break;
        }
    }

    private void updateItemWidth(VH holder, TextView label, UserPrefs.ItemPrefs itemPrefs, Appearance appearance) {
        View root = holder.getRoot();
        ViewGroup.LayoutParams rootLp = root.getLayoutParams();
        Preferences.ItemWidth itemWidth = appearance.width == null ? params.itemWidthProvider.get(itemPrefs)
                : appearance.width == ItemWidth.FILL_ROW ? Preferences.ItemWidth.MATCH_PARENT
                : Preferences.ItemWidth.WRAP;
        boolean rootLayoutParamsChanged = updateWrapBefore(rootLp,
                itemWidth == Preferences.ItemWidth.FILL_ROW_WRAP_CONTENT);
        if (itemWidth == Preferences.ItemWidth.MATCH_PARENT) {
            if (rootLp.width != ViewGroup.LayoutParams.MATCH_PARENT) {
                rootLp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                rootLayoutParamsChanged = true;
                if (root != label) {
                    ViewGroup.LayoutParams labelLp = label.getLayoutParams();
                    labelLp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    label.setLayoutParams(labelLp);
                }
            }
        } else if (rootLp.width == ViewGroup.LayoutParams.MATCH_PARENT) {
            rootLp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            rootLayoutParamsChanged = true;
            if (root != label) {
                ViewGroup.LayoutParams llp = label.getLayoutParams();
                llp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                label.setLayoutParams(llp);
            }
        }
        TextAlign textAlign = appearance.textAlign;
        if (textAlign == null) {
            switch (params.itemAlignmentProvider.get(itemPrefs)) {
                case START:
                    textAlign = TextAlign.START;
                    break;
                case CENTER:
                    textAlign = TextAlign.CENTER;
                    break;
                case END:
                    textAlign = TextAlign.END;
                    break;
            }
        }
        if (textAlign != null) {
            switch (textAlign) {
                case START:
                    label.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
                    break;
                case CENTER:
                    label.setGravity(Gravity.CENTER);
                    break;
                case END:
                    label.setGravity(Gravity.CENTER_VERTICAL | Gravity.END);
                    break;
            }
        }
        if (rootLayoutParamsChanged) {
            root.setLayoutParams(rootLp);
        }
    }

    private boolean updateWrapBefore(ViewGroup.LayoutParams layoutParams, boolean wrapBefore) {
        if (!(layoutParams instanceof FlexboxLayoutManager.LayoutParams)) {
            return false;
        }
        FlexboxLayoutManager.LayoutParams flexboxLp = (FlexboxLayoutManager.LayoutParams) layoutParams;
        if (flexboxLp.isWrapBefore() == wrapBefore) {
            return false;
        }
        flexboxLp.setWrapBefore(wrapBefore);
        return true;
    }

    public abstract static class ViewHolder<T> extends RecyclerView.ViewHolder {
        UserPrefs.ItemPrefs itemPrefs;
        Appearance appearance;

        protected ViewHolder(View itemView) {
            super(itemView);
        }

        protected abstract void bind(T item);

        protected void bind(T item, List<?> payloads) {
            bind(item);
        }

        protected abstract View getRoot();

        @Nullable
        protected abstract TextView getLabel();

        @Nullable
        protected NotificationDotDrawable getNotificationDot() {
            return null;
        }
    }

    public static class Params {

        public static final Params DEFAULT = new Params(false,
                itemPrefs -> itemPrefs.itemWidth(),
                itemPrefs -> itemPrefs.homeAlignment());
        public static final Provider<Preferences.ItemWidth> ITEM_WIDTH_WRAP = itemPrefs -> Preferences.ItemWidth.WRAP;
        public static final Provider<Preferences.HomeAlignment> ALIGNMENT_FROM_PREFS = itemPrefs -> itemPrefs.homeAlignment();

        final boolean ignoreVisibility;
        final boolean applyAppearance;
        final Provider<Preferences.ItemWidth> itemWidthProvider;
        final Provider<Preferences.HomeAlignment> itemAlignmentProvider;

        public Params(boolean ignoreVisibility,
                Provider<Preferences.ItemWidth> itemWidthProvider,
                Provider<Preferences.HomeAlignment> itemAlignmentProvider) {
            this(ignoreVisibility, true, itemWidthProvider, itemAlignmentProvider);
        }

        public Params(boolean ignoreVisibility,
                boolean applyAppearance,
                Provider<Preferences.ItemWidth> itemWidthProvider,
                Provider<Preferences.HomeAlignment> itemAlignmentProvider) {
            this.ignoreVisibility = ignoreVisibility;
            this.applyAppearance = applyAppearance;
            this.itemWidthProvider = itemWidthProvider;
            this.itemAlignmentProvider = itemAlignmentProvider;
        }

        public interface Provider<T> {
            T get(UserPrefs.ItemPrefs itemPrefs);
        }
    }
}
