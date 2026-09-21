package com.italankin.lnch.feature.home.apps.popup;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.home.repository.EditModeState;
import com.italankin.lnch.feature.home.repository.HomeDescriptorsState;
import com.italankin.lnch.feature.home.repository.HomeEntry;
import com.italankin.lnch.model.descriptor.props.ItemWidth;
import com.italankin.lnch.model.descriptor.props.TextAlign;
import com.italankin.lnch.model.repository.descriptor.actions.SetLayoutAction;
import com.italankin.lnch.model.ui.CustomLayoutDescriptorUi;
import com.italankin.lnch.model.ui.DescriptorUi;
import com.italankin.lnch.util.widget.popup.PopupFragment;
import com.italankin.lnch.util.widget.popup.PopupFrameView;

public class ItemAppearancePopupFragment extends PopupFragment {

    public static ItemAppearancePopupFragment newInstance(
            DescriptorUi descriptorUi,
            String requestKey,
            @Nullable Rect anchor) {
        ItemAppearancePopupFragment fragment = new ItemAppearancePopupFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_ANCHOR, anchor);
        args.putString(ARG_DESCRIPTOR_ID, descriptorUi.getDescriptor().getId());
        args.putString(ARG_REQUEST_KEY, requestKey);
        fragment.setArguments(args);
        return fragment;
    }

    private static final String ARG_DESCRIPTOR_ID = "descriptor_id";
    private static final String BACKSTACK_NAME = "item_appearance_popup";
    private static final String TAG = "item_appearance_popup";

    private HomeDescriptorsState homeDescriptorsState;
    private EditModeState editModeState;
    private String descriptorId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        homeDescriptorsState = LauncherApp.daggerService.main().homeDescriptorState();
        editModeState = LauncherApp.daggerService.main().editModeState();
        descriptorId = requireArguments().getString(ARG_DESCRIPTOR_ID);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        View content = inflater.inflate(R.layout.popup_item_appearance, itemsContainer, false);
        MaterialButtonToggleGroup widthGroup = content.findViewById(R.id.item_appearance_width);
        MaterialButtonToggleGroup alignGroup = content.findViewById(R.id.item_appearance_align);
        HomeEntry<CustomLayoutDescriptorUi> entry = homeDescriptorsState.find(CustomLayoutDescriptorUi.class, descriptorId);
        if (entry != null) {
            ItemWidth width = entry.item.getCustomWidth();
            widthGroup.check(width == null ? R.id.item_width_default
                    : width == ItemWidth.WRAP_CONTENT ? R.id.item_width_content : R.id.item_width_fill);
            TextAlign align = entry.item.getCustomTextAlign();
            alignGroup.check(align == null ? R.id.item_align_default
                    : align == TextAlign.START ? R.id.item_align_start
                      : align == TextAlign.CENTER ? R.id.item_align_center : R.id.item_align_end);
        }
        widthGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) {
                return;
            }
            HomeEntry<CustomLayoutDescriptorUi> current = homeDescriptorsState.find(CustomLayoutDescriptorUi.class, descriptorId);
            if (current != null) {
                ItemWidth width = checkedId == R.id.item_width_content ? ItemWidth.WRAP_CONTENT
                        : checkedId == R.id.item_width_fill ? ItemWidth.FILL_ROW : null;
                if (current.item.getCustomWidth() != width) {
                    current.item.setCustomWidth(width);
                    updateAppearance(current.item);
                }
            }
        });
        alignGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) {
                return;
            }
            HomeEntry<CustomLayoutDescriptorUi> current = homeDescriptorsState.find(CustomLayoutDescriptorUi.class, descriptorId);
            if (current != null) {
                TextAlign align = checkedId == R.id.item_align_start ? TextAlign.START
                        : checkedId == R.id.item_align_center ? TextAlign.CENTER
                          : checkedId == R.id.item_align_end ? TextAlign.END : null;
                if (current.item.getCustomTextAlign() != align) {
                    current.item.setCustomTextAlign(align);
                    updateAppearance(current.item);
                }
            }
        });
        itemsContainer.addView(content);
        containerRoot.setClickable(true);
        return root;
    }

    private void updateAppearance(CustomLayoutDescriptorUi item) {
        editModeState.addAction(new SetLayoutAction(item.getDescriptor(), item.getCustomWidth(), item.getCustomTextAlign()));
        homeDescriptorsState.updateItem(item);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setPopupLocations(PopupFrameView.Location.TOP, PopupFrameView.Location.BOTTOM);
        showPopup();
    }

    @Override
    protected String getPopupBackstackName() {
        return BACKSTACK_NAME;
    }

    @Override
    protected String getPopupTag() {
        return TAG;
    }
}
