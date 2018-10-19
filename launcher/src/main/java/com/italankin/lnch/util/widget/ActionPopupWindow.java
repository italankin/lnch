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
import android.widget.PopupWindow;
import android.widget.TextView;

import com.italankin.lnch.R;
import com.italankin.lnch.util.ViewUtils;
import com.squareup.picasso.Picasso;

public class ActionPopupWindow extends PopupWindow {

    private static final int MS = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);

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

        arrowSize = res
                .getDimensionPixelSize(R.dimen.popup_window_arrow_size);
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

    public ActionPopupWindow addAction(ActionInfo actionInfo) {
        return addAction(actionInfo.icon, actionInfo.listener, actionInfo.longClickListener);
    }

    public ActionPopupWindow addAction(@DrawableRes int icon, View.OnClickListener listener) {
        return addAction(context.getDrawable(icon), listener, null);
    }

    public ActionPopupWindow addAction(@DrawableRes int icon, View.OnClickListener listener,
            @Nullable View.OnLongClickListener longClickListener) {
        return addAction(context.getDrawable(icon), listener, longClickListener);
    }

    public ActionPopupWindow addAction(Drawable icon, View.OnClickListener listener,
            View.OnLongClickListener longClickListener) {
        ImageView item = (ImageView) inflater.inflate(R.layout.item_popup_action, actionContainer, false);
        item.setImageDrawable(icon);
        item.setOnClickListener(v -> {
            listener.onClick(v);
            dismiss();
        });
        item.setOnLongClickListener(longClickListener);
        actionContainer.addView(item);
        return this;
    }

    public ActionPopupWindow addShortcut(ShortcutInfo shortcutInfo) {
        return addShortcut(shortcutInfo.label, shortcutInfo.icon, shortcutInfo.listener,
                shortcutInfo.longClickListener);
    }

    public ActionPopupWindow addShortcut(@StringRes int label, @DrawableRes int icon,
            View.OnClickListener listener) {
        return addShortcut(context.getText(label), context.getDrawable(icon), listener, null);
    }

    public ActionPopupWindow addShortcut(@StringRes int label, @DrawableRes int icon,
            View.OnClickListener listener, View.OnLongClickListener longClickListener) {
        return addShortcut(context.getText(label), context.getDrawable(icon), listener, longClickListener);
    }

    public ActionPopupWindow addShortcut(CharSequence label, Drawable icon,
            View.OnClickListener listener, View.OnLongClickListener longClickListener) {
        return addShortcut(label, new Icon(icon), listener, longClickListener);
    }

    public ActionPopupWindow addShortcut(CharSequence label, Uri uri,
            View.OnClickListener listener) {
        return addShortcut(label, uri, listener, null);
    }

    public ActionPopupWindow addShortcut(CharSequence label, Uri uri,
            View.OnClickListener listener, View.OnLongClickListener longClickListener) {
        return addShortcut(label, new Icon(uri), listener, longClickListener);
    }

    public ActionPopupWindow addShortcut(CharSequence label, Icon icon,
            View.OnClickListener listener, View.OnLongClickListener longClickListener) {
        View item = inflater.inflate(R.layout.item_popup_shortcut, shortcutContainer, false);
        ImageView imageView = item.findViewById(R.id.icon);
        TextView labelView = item.findViewById(R.id.label);
        labelView.setText(label);
        if (icon.drawable != null) {
            Drawable drawable = icon.drawable.mutate();
            drawable.setTint(context.getColor(R.color.accent));
            imageView.setImageDrawable(drawable);
        } else if (icon.uri != null) {
            ViewUtils.onGlobalLayout(imageView, () -> picasso.load(icon.uri)
                    .resizeDimen(R.dimen.popup_shortcut_icon_size, R.dimen.popup_shortcut_icon_size)
                    .into(imageView));
        }
        item.setOnClickListener(v -> {
            listener.onClick(v);
            dismiss();
        });
        item.setOnLongClickListener(longClickListener);
        shortcutContainer.addView(item);
        return this;
    }

    @SuppressLint("RtlHardcoded")
    public void showAtAnchor(View anchorView, Rect bounds) {
        contentView.measure(MS, MS);
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
        ViewGroup.MarginLayoutParams lp = new ViewGroup.MarginLayoutParams(arrowSize, arrowSize);
        lp.setMarginStart(arrowCenter - arrowSize / 2);
        arrowView.setLayoutParams(lp);
        int anchorViewBottom = tmp[1] + anchorHeight;
        if (bounds.bottom - anchorViewBottom < contentHeight + additionalVerticalOffset) {
            yOffset = -contentHeight - anchorHeight + additionalVerticalOffset;
            arrowView.setBackground(new ArrowDrawable(lightArrowColor, arrowSize, true));
            contentView.addView(arrowView);
        } else {
            int color = actionContainer.getChildCount() > 0 ? darkArrowColor : lightArrowColor;
            arrowView.setBackground(new ArrowDrawable(color, arrowSize, false));
            contentView.addView(arrowView, 0);
        }

        showAsDropDown(anchorView, xOffset, yOffset, Gravity.TOP | Gravity.LEFT);
    }

    public static class ShortcutInfo {
        private final CharSequence label;
        private final Drawable icon;
        private final View.OnClickListener listener;
        private final View.OnLongClickListener longClickListener;

        public ShortcutInfo(CharSequence label, Drawable icon, View.OnClickListener listener,
                View.OnLongClickListener longClickListener) {
            this.label = label;
            this.icon = icon;
            this.listener = listener;
            this.longClickListener = longClickListener;
        }
    }

    public static class ActionInfo {
        private final Drawable icon;
        private final View.OnClickListener listener;
        private final View.OnLongClickListener longClickListener;

        public ActionInfo(Drawable icon, View.OnClickListener listener,
                View.OnLongClickListener longClickListener) {
            this.icon = icon;
            this.listener = listener;
            this.longClickListener = longClickListener;
        }
    }

    private static class Icon {
        private final Drawable drawable;
        private final Uri uri;

        Icon(Drawable drawable) {
            this(drawable, null);
        }

        Icon(Uri uri) {
            this(null, uri);
        }

        Icon(Drawable drawable, Uri uri) {
            this.drawable = drawable;
            this.uri = uri;
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
