package com.italankin.lnch.util.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private final ViewGroup actionContainer;
    private final ViewGroup shortcutContainer;
    private final int horizontalPadding;

    private final int arrowPadding;
    private final int arrowWidth;
    private final int arrowHeight;

    private final int darkArrowColor;
    private final int lightArrowColor;

    private final int[] tmp = new int[2];

    @SuppressLint("InflateParams")
    public ActionPopupWindow(Context context, Picasso picasso) {
        super(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.context = context;
        this.picasso = picasso;
        this.horizontalPadding = context.getResources()
                .getDimensionPixelSize(R.dimen.popup_window_horizontal_padding);
        this.inflater = LayoutInflater.from(context);
        this.contentView = (ViewGroup) inflater.inflate(R.layout.widget_action_popup, null);
        actionContainer = contentView.findViewById(R.id.action_container);
        shortcutContainer = contentView.findViewById(R.id.shortcut_container);
        contentView.getChildAt(0).setClipToOutline(true);
        setContentView(contentView);
        setOutsideTouchable(true);
        setFocusable(true);

        this.arrowPadding = context.getResources()
                .getDimensionPixelSize(R.dimen.popup_window_arrow_padding);
        this.arrowHeight = arrowPadding * 2;
        this.arrowWidth = (int) (arrowHeight * 1.33);
        this.darkArrowColor = context.getColor(R.color.popup_actions_background);
        this.lightArrowColor = context.getColor(R.color.popup_background);
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

    public ActionPopupWindow showAtAnchor(View anchorView, int yoffset, Rect bounds) {
        contentView.measure(MS, MS);
        anchorView.getLocationOnScreen(tmp);

        int contentWidth = contentView.getMeasuredWidth();
        int anchorWidth = anchorView.getMeasuredWidth();
        int widthDiff = (anchorWidth - contentWidth) / 2;
        int xOffset = widthDiff;
        int contentRight = tmp[0] + xOffset + contentWidth;
        boolean beyondLeft = false, beyondRight = false;
        if (tmp[0] + xOffset + widthDiff < horizontalPadding) {
            xOffset = horizontalPadding;
            beyondLeft = true;
        } else if (contentRight > bounds.right - horizontalPadding) {
            xOffset -= (contentRight - bounds.right + horizontalPadding);
            beyondRight = true;
        }

        int anchorHeight = anchorView.getMeasuredHeight();
        int anchorViewBottom = tmp[1] + anchorHeight;
        int contentHeight = contentView.getMeasuredHeight();
        int yOffset = -yoffset;

        int arrowCenter = horizontalPadding;
        if (beyondLeft) {
            arrowCenter += anchorWidth / 2;
        } else if (beyondRight) {
            arrowCenter += Math.abs(xOffset) + anchorWidth / 2;
        } else {
            arrowCenter += contentWidth / 2;
        }
        if (bounds.bottom - anchorViewBottom < contentHeight + yoffset) {
            yOffset = -contentHeight - anchorHeight + yoffset / 2;
            contentView.setPadding(0, 0, 0, arrowPadding);
            contentView.setBackground(new ArrowDrawable(
                    lightArrowColor, arrowCenter, arrowWidth, arrowHeight, true));
        } else {
            int color = actionContainer.getChildCount() > 0 ? darkArrowColor : lightArrowColor;
            contentView.setBackground(new ArrowDrawable(
                    color, arrowCenter, arrowWidth, arrowHeight, false));
        }

        showAsDropDown(anchorView, xOffset, yOffset);
        return this;
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
        private final Paint paint;
        private final Path path;
        private final boolean down;
        private final int height;
        private int xOffset;

        public ArrowDrawable(@ColorInt int color, @Px int centerX, @Px int width, @Px int height, boolean down) {
            this.height = height;
            this.down = down;
            xOffset = centerX - width / 2;

            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(color);

            path = new Path();
            if (down) {
                path.moveTo(0, 0);
                path.lineTo(width, 0);
                path.lineTo(width / 2, height);
            } else {
                path.moveTo(width / 2, 0);
                path.lineTo(width, height);
                path.lineTo(0, height);
            }
            path.close();
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            canvas.save();
            canvas.translate(xOffset, down ? getBounds().height() - height : 0);
            canvas.drawPath(path, paint);
            canvas.restore();
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
