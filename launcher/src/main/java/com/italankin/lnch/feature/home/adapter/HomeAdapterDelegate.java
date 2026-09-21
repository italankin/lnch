package com.italankin.lnch.feature.home.adapter;

import android.annotation.SuppressLint;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.italankin.lnch.R;
import com.italankin.lnch.feature.home.model.Appearance;
import com.italankin.lnch.feature.home.model.UserPrefs;
import com.italankin.lnch.feature.home.util.NotificationDotDrawable;
import com.italankin.lnch.model.descriptor.props.ItemWidth;
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
        updateItemLayout(holder, label, itemPrefs, appearance);
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

    private void updateItemLayout(VH holder, TextView label, UserPrefs.ItemPrefs itemPrefs, Appearance appearance) {
        ItemWidth width = appearance.width;
        if (width == null) {
            switch (params.itemWidthProvider.get(itemPrefs)) {
                case MATCH_PARENT:
                    width = ItemWidth.FILL_ROW;
                    break;
                case FILL_ROW_WRAP_CONTENT:
                    width = ItemWidth.FILL_ROW_CONTENT_WIDTH;
                    break;
                case WRAP:
                default:
                    width = ItemWidth.WRAP_CONTENT;
                    break;
            }
        }
        int containerWidth = width == ItemWidth.WRAP_CONTENT
                ? ViewGroup.LayoutParams.WRAP_CONTENT
                : ViewGroup.LayoutParams.MATCH_PARENT;
        int labelWidth = width == ItemWidth.FILL_ROW
                ? ViewGroup.LayoutParams.MATCH_PARENT
                : ViewGroup.LayoutParams.WRAP_CONTENT;

        int gravity;
        if (appearance.textAlign != null) {
            switch (appearance.textAlign) {
                case CENTER:
                    gravity = Gravity.CENTER;
                    break;
                case END:
                    gravity = Gravity.CENTER_VERTICAL | Gravity.END;
                    break;
                case START:
                default:
                    gravity = Gravity.CENTER_VERTICAL | Gravity.START;
                    break;
            }
        } else {
            switch (params.itemAlignmentProvider.get(itemPrefs)) {
                case CENTER:
                    gravity = Gravity.CENTER;
                    break;
                case END:
                    gravity = Gravity.CENTER_VERTICAL | Gravity.END;
                    break;
                default:
                    gravity = Gravity.CENTER_VERTICAL | Gravity.START;
                    break;
            }
        }
        ViewGroup.LayoutParams containerLp = holder.itemRootContainer.getLayoutParams();
        if (containerLp.width != containerWidth) {
            containerLp.width = containerWidth;
            holder.itemRootContainer.setLayoutParams(containerLp);
        }
        FrameLayout.LayoutParams labelLp = (FrameLayout.LayoutParams) label.getLayoutParams();
        if (labelLp.width != labelWidth || labelLp.gravity != gravity) {
            labelLp.width = labelWidth;
            labelLp.gravity = gravity;
            label.setLayoutParams(labelLp);
        }
        label.setGravity(gravity);
    }

    public abstract static class ViewHolder<T> extends RecyclerView.ViewHolder {
        final View itemRootContainer;
        UserPrefs.ItemPrefs itemPrefs;
        Appearance appearance;

        protected ViewHolder(View itemView) {
            super(itemView);
            itemRootContainer = itemView.findViewById(R.id.itemRootContainer);
        }

        protected abstract void bind(T item);

        protected void bind(T item, List<?> payloads) {
            bind(item);
        }

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
