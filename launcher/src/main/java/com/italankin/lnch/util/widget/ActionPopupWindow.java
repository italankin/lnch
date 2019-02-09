package com.italankin.lnch.util.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.italankin.lnch.R;
import com.italankin.lnch.util.ResUtils;
import com.italankin.lnch.util.ViewUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.annotation.StringRes;

public class ActionPopupWindow extends PopupWindow {

    private static final float MAX_WIDTH_FACTOR = 0.66f;
    private static final float MAX_HEIGHT_FACTOR = 0.8f;
    private static final float DISABLED_ALPHA = 0.33f;

    private final Context context;
    private final Picasso picasso;
    private final LayoutInflater inflater;
    private final ViewGroup contentView;
    private final ViewGroup popupContainer;
    private final ViewGroup actionContainer;
    private final ViewGroup shortcutContainer;

    private final List<ItemBuilder> actions = new ArrayList<>(1);
    private final List<ItemBuilder> shortcuts = new ArrayList<>(4);

    private final int arrowSize;

    private final int darkArrowColor;
    private final int lightArrowColor;

    @SuppressLint("InflateParams")
    public ActionPopupWindow(Context context, Picasso picasso) {
        super(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.context = context;
        this.picasso = picasso;
        inflater = LayoutInflater.from(context);
        contentView = (ViewGroup) inflater.inflate(R.layout.widget_action_popup, null);
        popupContainer = contentView.findViewById(R.id.popup_container);
        actionContainer = contentView.findViewById(R.id.action_container);
        shortcutContainer = contentView.findViewById(R.id.shortcut_container);
        popupContainer.setClipToOutline(true);
        Resources res = context.getResources();
        setContentView(contentView);
        setOutsideTouchable(true);
        setFocusable(true);
        setElevation(res.getDimensionPixelSize(R.dimen.popup_window_elevation));

        arrowSize = res.getDimensionPixelSize(R.dimen.popup_window_arrow_size);
        darkArrowColor = ResUtils.resolveColor(context, R.attr.colorPopupActionsBackground);
        lightArrowColor = ResUtils.resolveColor(context, R.attr.colorPopupBackground);

        contentView.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                boolean arrowBottom = ((ViewGroup) view).getChildAt(0) == popupContainer;
                float radius = ResUtils.px2dp(view.getContext(), 8);
                Rect rect = new Rect(view.getPaddingLeft(),
                        0,
                        view.getWidth() - view.getPaddingRight(),
                        view.getHeight() - arrowSize);
                if (!arrowBottom) {
                    rect.offset(0, arrowSize);
                }
                outline.setRoundRect(rect, radius);
            }
        });
    }

    public ActionPopupWindow addAction(ItemBuilder item) {
        actions.add(item);
        return this;
    }

    public ActionPopupWindow addShortcut(ItemBuilder item) {
        shortcuts.add(item);
        return this;
    }

    @SuppressLint("RtlHardcoded")
    public void showAtAnchor(View anchorView, Rect bounds) {
        populateItems();

        int maxWidth = (int) (bounds.width() * MAX_WIDTH_FACTOR);
        int maxHeight = (int) (bounds.height() * MAX_HEIGHT_FACTOR);
        contentView.measure(View.MeasureSpec.makeMeasureSpec(maxWidth, View.MeasureSpec.AT_MOST),
                View.MeasureSpec.makeMeasureSpec(maxHeight, View.MeasureSpec.AT_MOST));

        int[] tmp = new int[2];
        anchorView.getLocationOnScreen(tmp);

        int contentWidth = contentView.getMeasuredWidth();
        int anchorWidth = anchorView.getMeasuredWidth();
        int xOffset = (anchorWidth - contentWidth) / 2;
        boolean beyondLeft = false, beyondRight = false;
        if (tmp[0] + xOffset < 0) {
            xOffset = 0;
            beyondLeft = true;
        } else {
            int contentRight = tmp[0] + xOffset + contentWidth;
            if (contentRight > bounds.right) {
                xOffset -= (contentRight - bounds.right);
                beyondRight = true;
            }
        }

        int anchorHeight = anchorView.getMeasuredHeight();
        int additionalVerticalOffset = (anchorView.getPaddingTop() + anchorView.getPaddingTop()) / 2;
        int yOffset = -additionalVerticalOffset;

        int arrowCenter;
        if (beyondLeft) {
            arrowCenter = anchorWidth / 2 - contentView.getPaddingLeft();
        } else if (beyondRight) {
            arrowCenter = Math.abs(xOffset) + anchorWidth / 2 - contentView.getPaddingRight();
        } else {
            arrowCenter = contentWidth / 2 - contentView.getPaddingLeft();
        }
        View arrowView = new View(context);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(arrowSize, arrowSize);
        lp.setMarginStart(arrowCenter - arrowSize / 2);
        int anchorViewBottom = tmp[1] + anchorHeight;
        int contentHeight = contentView.getMeasuredHeight() + arrowSize;
        if (bounds.bottom - anchorViewBottom < contentHeight + additionalVerticalOffset) {
            yOffset = -contentHeight - anchorHeight + additionalVerticalOffset;
            arrowView.setBackground(new ArrowDrawable(lightArrowColor, arrowSize, true));
            contentView.addView(arrowView, lp);
        } else {
            int color = actionContainer.getChildCount() > 0 ? darkArrowColor : lightArrowColor;
            arrowView.setBackground(new ArrowDrawable(color, arrowSize, false));
            contentView.addView(arrowView, 0, lp);
        }

        setWidth(contentWidth);
        showAsDropDown(anchorView, xOffset, yOffset, Gravity.TOP | Gravity.LEFT);
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
        }
        imageView.setOnLongClickListener(item.onLongClickListener);
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
        } else if (item.iconUri != null) {
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
            return setIcon(context.getDrawable(icon));
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

    private static class ArrowDrawable extends Drawable {
        private static final float HEIGHT_FACTOR = .66f;

        private final Paint paint;
        private final Path path;

        private ArrowDrawable(@ColorInt int color, @Px int size, boolean pointDown) {
            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(color);

            path = new Path();
            int height = (int) (size * HEIGHT_FACTOR);
            if (pointDown) {
                path.moveTo(0, 0);
                path.lineTo(size, 0);
                path.lineTo(size / 2f, height);
            } else {
                path.moveTo(size / 2f, size - height);
                path.lineTo(size, size);
                path.lineTo(0, size);
            }
            path.close();
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            canvas.drawPath(path, paint);
        }

        @Override
        public void setAlpha(int alpha) {
            paint.setAlpha(alpha);
        }

        @Override
        public void setColorFilter(@Nullable ColorFilter colorFilter) {
            paint.setColorFilter(colorFilter);
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSPARENT;
        }
    }
}
