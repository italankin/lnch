package com.italankin.lnch.model.provider;

import com.italankin.lnch.model.provider.color.ColorProvider;
import com.italankin.lnch.model.provider.color.DominantColorProvider;
import com.italankin.lnch.model.provider.label.LabelProvider;
import com.italankin.lnch.model.provider.label.UppercaseLabelProvider;

public class Preferences {

    public static final String LAYOUT_FLEX = "flex";
    public static final String LAYOUT_GRID = "grid";
    public static final String LAYOUT_LINEAR = "linear";

    public LabelProvider label = new UppercaseLabelProvider();
    public ColorProvider color = new DominantColorProvider();

}
