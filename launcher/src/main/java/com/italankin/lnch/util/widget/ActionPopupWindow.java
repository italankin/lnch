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
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Px;
import android.support.annotation.StringRes;
import android.util.TypedValue;
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
import com.italankin.lnch.util.ViewUtils;
import com.squareup.picasso.Picasso;

public class ActionPopupWindow extends PopupWindow {

    private static final float MAX_WIDTH_FACTOR = 0.66f;

    private final Context context;
    private final Picasso picasso;
    private final LayoutInflater inflater;
    private final ViewGroup contentView;
    private final ViewGroup popupContainer;
    private final ViewGroup actionContainer;
    private final ViewGroup shortcutContainer;

    private final int arrowSize;

    private final int darkArrowColor;
    private final int lightArrowColor;

    private final int[] tmp = new int[2];

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
        darkArrowColor = context.getColor(R.color.popup_actions_background);
        lightArrowColor = context.getColor(R.color.popup_background);

        contentView.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                boolean arrowBottom = ((ViewGroup) view).getChildAt(0) == popupContainer;
                float radius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8,
                        view.getResources().getDisplayMetrics());
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
        ImageView imageView = (ImageView) inflater.inflate(R.layout.item_popup_action, actionContainer, false);
        if (item.iconDrawable != null) {
            imageView.setImageDrawable(item.iconDrawable);
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
        imageView.setOnLongClickListener(item.onLongClickListener);
        actionContainer.addView(imageView);
        return this;
    }

    public ActionPopupWindow addShortcut(ItemBuilder item) {
        View view = inflater.inflate(R.layout.item_popup_shortcut, shortcutContainer, false);
        ImageView iconView = view.findViewById(R.id.icon);
        ImageView pinIconView = view.findViewById(R.id.icon_pin);
        TextView labelView = view.findViewById(R.id.label);
        labelView.setText(item.label);
        if (item.iconDrawable != null) {
            Drawable drawable = item.iconDrawable.mutate();
            drawable.setTint(context.getColor(R.color.accent));
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
        if (item.onPinClickListener != null) {
            pinIconView.setVisibility(View.VISIBLE);
            pinIconView.setOnClickListener(v -> {
                item.onPinClickListener.onClick(v);
                dismiss();
            });
        }
        shortcutContainer.addView(view);
        return this;
    }

    @SuppressLint("RtlHardcoded")
    public void showAtAnchor(View anchorView, Rect bounds) {
        int maxWidth = (int) (bounds.width() * MAX_WIDTH_FACTOR);
        contentView.measure(View.MeasureSpec.makeMeasureSpec(maxWidth, View.MeasureSpec.AT_MOST),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
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
        int contentHeight = contentView.getMeasuredHeight() + arrowSize;
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

    public static class ItemBuilder {
        private final Context context;
        private CharSequence label;
        private Drawable iconDrawable;
        private Uri iconUri;
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
        public static final float HEIGHT_FACTOR = .66f;
        private final Paint paint;
        private final Path path;

        public ArrowDrawable(@ColorInt int color, @Px int size, boolean pointDown) {
            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(color);

            path = new Path();
            int height = (int) (size * HEIGHT_FACTOR);
            if (pointDown) {
                path.moveTo(0, 0);
                path.lineTo(size, 0);
                path.lineTo(size / 2, height);
            } else {
                path.moveTo(size / 2, size - height);
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
