package com.italankin.lnch.feature.widgets.util;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.util.SparseIntArray;
import androidx.annotation.RequiresApi;
import com.google.android.material.color.MaterialColors;

/**
 * @see <a href="https://github.com/material-components/material-components-android/blob/master/docs/theming/Color.md">Color mappings</a>
 */
@RequiresApi(Build.VERSION_CODES.S)
public class WidgetColorMapping {

    public static SparseIntArray get(Context context) {
        if (context == context.getApplicationContext()) {
            throw new IllegalArgumentException("Application context should not be used");
        }
        int nightModeFlags = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switch (nightModeFlags) {
            case Configuration.UI_MODE_NIGHT_YES:
                return buildMapping(context, DARK);
            case Configuration.UI_MODE_NIGHT_NO:
                return buildMapping(context, LIGHT);
            default:
            case Configuration.UI_MODE_NIGHT_UNDEFINED:
                return new SparseIntArray(0);
        }
    }

    private static final SparseIntArray LIGHT = colorToAttrMappingLight();
    private static final SparseIntArray DARK = colorToAttrMappingDark();

    private static SparseIntArray buildMapping(Context context, SparseIntArray colorToAttr) {
        SparseIntArray result = new SparseIntArray(colorToAttr.size());
        for (int i = 0; i < colorToAttr.size(); i++) {
            int color = colorToAttr.keyAt(i);
            int attr = colorToAttr.valueAt(i);
            try {
                result.put(color, MaterialColors.getColor(context, attr, color + " -> " + attr));
            } catch (Exception ignored) {
            }
        }
        return result;
    }

    private static SparseIntArray colorToAttrMappingLight() {
        SparseIntArray cm = new SparseIntArray();
        cm.put(android.R.color.system_accent1_600, com.google.android.material.R.attr.colorPrimary);
        cm.put(android.R.color.system_accent1_0, com.google.android.material.R.attr.colorOnPrimary);
        cm.put(android.R.color.system_accent1_100, com.google.android.material.R.attr.colorPrimaryContainer);
        cm.put(android.R.color.system_accent1_900, com.google.android.material.R.attr.colorOnPrimaryContainer);
        cm.put(android.R.color.system_accent1_200, com.google.android.material.R.attr.colorPrimaryInverse);
        cm.put(android.R.color.system_accent2_600, com.google.android.material.R.attr.colorSecondary);
        cm.put(android.R.color.system_accent2_0, com.google.android.material.R.attr.colorOnSecondary);
        cm.put(android.R.color.system_accent2_100, com.google.android.material.R.attr.colorSecondaryContainer);
        cm.put(android.R.color.system_accent2_900, com.google.android.material.R.attr.colorOnSecondaryContainer);
        cm.put(android.R.color.system_accent3_600, com.google.android.material.R.attr.colorTertiary);
        cm.put(android.R.color.system_accent3_0, com.google.android.material.R.attr.colorOnTertiary);
        cm.put(android.R.color.system_accent3_100, com.google.android.material.R.attr.colorTertiaryContainer);
        cm.put(android.R.color.system_accent3_900, com.google.android.material.R.attr.colorOnTertiaryContainer);
        cm.put(android.R.color.system_accent3_100, com.google.android.material.R.attr.colorTertiaryFixed);
        cm.put(android.R.color.system_accent3_200, com.google.android.material.R.attr.colorTertiaryFixedDim);
        cm.put(android.R.color.system_accent3_900, com.google.android.material.R.attr.colorOnTertiaryFixed);
        cm.put(android.R.color.system_accent3_700, com.google.android.material.R.attr.colorOnTertiaryFixedVariant);
        cm.put(android.R.color.system_accent2_100, com.google.android.material.R.attr.colorSecondaryFixed);
        cm.put(android.R.color.system_accent2_200, com.google.android.material.R.attr.colorSecondaryFixedDim);
        cm.put(android.R.color.system_accent2_900, com.google.android.material.R.attr.colorOnSecondaryFixed);
        cm.put(android.R.color.system_accent2_700, com.google.android.material.R.attr.colorOnSecondaryFixedVariant);
        cm.put(android.R.color.system_accent1_100, com.google.android.material.R.attr.colorPrimaryFixed);
        cm.put(android.R.color.system_accent1_200, com.google.android.material.R.attr.colorPrimaryFixedDim);
        cm.put(android.R.color.system_accent1_900, com.google.android.material.R.attr.colorOnPrimaryFixed);
        cm.put(android.R.color.system_accent1_700, com.google.android.material.R.attr.colorOnPrimaryFixedVariant);
        cm.put(android.R.color.system_neutral2_500, com.google.android.material.R.attr.colorOutline);
        cm.put(android.R.color.system_neutral2_200, com.google.android.material.R.attr.colorOutlineVariant);
        cm.put(android.R.color.system_neutral1_900, com.google.android.material.R.attr.colorOnBackground);
        cm.put(android.R.color.system_neutral1_900, com.google.android.material.R.attr.colorOnSurface);
        cm.put(android.R.color.system_neutral2_100, com.google.android.material.R.attr.colorSurfaceVariant);
        cm.put(android.R.color.system_neutral2_700, com.google.android.material.R.attr.colorOnSurfaceVariant);
        cm.put(android.R.color.system_neutral1_800, com.google.android.material.R.attr.colorSurfaceInverse);
        cm.put(android.R.color.system_neutral1_50, com.google.android.material.R.attr.colorOnSurfaceInverse);
        cm.put(android.R.color.system_neutral2_0, com.google.android.material.R.attr.colorSurfaceContainerLowest);
        cm.put(android.R.color.system_neutral2_100, com.google.android.material.R.attr.colorSurfaceContainerHighest);
        return cm;
    }

    private static SparseIntArray colorToAttrMappingDark() {
        SparseIntArray cm = new SparseIntArray();
        cm.put(android.R.color.system_accent1_200, com.google.android.material.R.attr.colorPrimary);
        cm.put(android.R.color.system_accent1_800, com.google.android.material.R.attr.colorOnPrimary);
        cm.put(android.R.color.system_accent1_700, com.google.android.material.R.attr.colorPrimaryContainer);
        cm.put(android.R.color.system_accent1_100, com.google.android.material.R.attr.colorOnPrimaryContainer);
        cm.put(android.R.color.system_accent1_600, com.google.android.material.R.attr.colorPrimaryInverse);
        cm.put(android.R.color.system_accent2_200, com.google.android.material.R.attr.colorSecondary);
        cm.put(android.R.color.system_accent2_800, com.google.android.material.R.attr.colorOnSecondary);
        cm.put(android.R.color.system_accent2_700, com.google.android.material.R.attr.colorSecondaryContainer);
        cm.put(android.R.color.system_accent2_100, com.google.android.material.R.attr.colorOnSecondaryContainer);
        cm.put(android.R.color.system_accent3_200, com.google.android.material.R.attr.colorTertiary);
        cm.put(android.R.color.system_accent3_800, com.google.android.material.R.attr.colorOnTertiary);
        cm.put(android.R.color.system_accent3_700, com.google.android.material.R.attr.colorTertiaryContainer);
        cm.put(android.R.color.system_accent3_100, com.google.android.material.R.attr.colorOnTertiaryContainer);
        cm.put(android.R.color.system_accent3_100, com.google.android.material.R.attr.colorTertiaryFixed);
        cm.put(android.R.color.system_accent3_200, com.google.android.material.R.attr.colorTertiaryFixedDim);
        cm.put(android.R.color.system_accent3_900, com.google.android.material.R.attr.colorOnTertiaryFixed);
        cm.put(android.R.color.system_accent3_700, com.google.android.material.R.attr.colorOnTertiaryFixedVariant);
        cm.put(android.R.color.system_accent2_100, com.google.android.material.R.attr.colorSecondaryFixed);
        cm.put(android.R.color.system_accent2_200, com.google.android.material.R.attr.colorSecondaryFixedDim);
        cm.put(android.R.color.system_accent2_900, com.google.android.material.R.attr.colorOnSecondaryFixed);
        cm.put(android.R.color.system_accent2_700, com.google.android.material.R.attr.colorOnSecondaryFixedVariant);
        cm.put(android.R.color.system_accent1_100, com.google.android.material.R.attr.colorPrimaryFixed);
        cm.put(android.R.color.system_accent1_200, com.google.android.material.R.attr.colorPrimaryFixedDim);
        cm.put(android.R.color.system_accent1_900, com.google.android.material.R.attr.colorOnPrimaryFixed);
        cm.put(android.R.color.system_accent1_700, com.google.android.material.R.attr.colorOnPrimaryFixedVariant);
        cm.put(android.R.color.system_neutral2_400, com.google.android.material.R.attr.colorOutline);
        cm.put(android.R.color.system_neutral2_700, com.google.android.material.R.attr.colorOutlineVariant);
        cm.put(android.R.color.system_neutral1_100, com.google.android.material.R.attr.colorOnBackground);
        cm.put(android.R.color.system_neutral1_100, com.google.android.material.R.attr.colorOnSurface);
        cm.put(android.R.color.system_neutral2_700, com.google.android.material.R.attr.colorSurfaceVariant);
        cm.put(android.R.color.system_neutral2_200, com.google.android.material.R.attr.colorOnSurfaceVariant);
        cm.put(android.R.color.system_neutral1_100, com.google.android.material.R.attr.colorSurfaceInverse);
        cm.put(android.R.color.system_neutral1_800, com.google.android.material.R.attr.colorOnSurfaceInverse);
        cm.put(android.R.color.system_neutral2_900, com.google.android.material.R.attr.colorSurfaceContainerLow);
        return cm;
    }

}
