package com.italankin.lnch.feature.settings.apps.details;

import static com.italankin.lnch.model.descriptor.impl.AppDescriptor.FLAG_SEARCH_SHORTCUTS_VISIBLE;
import static com.italankin.lnch.model.descriptor.impl.AppDescriptor.FLAG_SEARCH_VISIBLE;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.base.AppFragment;
import com.italankin.lnch.feature.common.dialog.RenameDescriptorDialog;
import com.italankin.lnch.feature.common.dialog.SetColorDescriptorDialog;
import com.italankin.lnch.feature.home.fragmentresult.DescriptorFragmentResultContract;
import com.italankin.lnch.feature.home.fragmentresult.SignalFragmentResultContract;
import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.util.IntentUtils;
import com.italankin.lnch.util.PackageUtils;

public class AppDetailsFragment extends AppFragment implements AppDetailsView {

    public static AppDetailsFragment newInstance(String requestKey, String descriptorId) {
        Bundle args = new Bundle();
        args.putString(ARG_REQUEST_KEY, requestKey);
        args.putString(ARG_DESCRIPTOR_ID, descriptorId);
        AppDetailsFragment fragment = new AppDetailsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private static final String ARG_DESCRIPTOR_ID = "descriptor_id";

    @InjectPresenter
    AppDetailsPresenter presenter;

    private TextView textName;
    private ImageView imageIcon;
    private TextView textPackage;
    private TextView textVisibleName;
    private View buttonInfo;
    private View buttonAppAliases;
    private View buttonRename;
    private View buttonChangeColor;
    private View buttonChangeColorPreview;
    private View buttonChangeBadgeColor;
    private View buttonChangeBadgeColorPreview;

    private SwitchCompat switchHomeVisibility;
    private SwitchCompat switchSearchVisibility;
    private SwitchCompat switchShortcutsVisibility;
    private SwitchCompat switchSearchShortcutsVisibility;

    @ProvidePresenter
    AppDetailsPresenter providePresenter() {
        return LauncherApp.daggerService.presenters().appDetails();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_app_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        textName = view.findViewById(R.id.name);
        imageIcon = view.findViewById(R.id.icon);
        textPackage = view.findViewById(R.id.package_id);
        buttonInfo = view.findViewById(R.id.button_info);
        switchHomeVisibility = view.findViewById(R.id.switch_home_visibility);
        switchSearchVisibility = view.findViewById(R.id.switch_search_visibility);
        switchShortcutsVisibility = view.findViewById(R.id.switch_shortcuts_visibility);
        switchSearchShortcutsVisibility = view.findViewById(R.id.switch_search_shortcuts_visibility);
        buttonAppAliases = view.findViewById(R.id.app_aliases);
        buttonRename = view.findViewById(R.id.action_rename);
        buttonChangeColor = view.findViewById(R.id.action_color);
        buttonChangeColorPreview = view.findViewById(R.id.action_color_preview);
        buttonChangeBadgeColor = view.findViewById(R.id.action_badge_color);
        buttonChangeBadgeColorPreview = view.findViewById(R.id.action_badge_color_preview);
        textVisibleName = view.findViewById(R.id.visible_name);

        String descriptorId = requireArguments().getString(ARG_DESCRIPTOR_ID);
        presenter.loadDescriptor(descriptorId);
    }

    @Override
    public void onDescriptorLoaded(AppDescriptor descriptor) {
        String packageName = descriptor.getPackageName();
        PackageManager packageManager = requireContext().getPackageManager();
        imageIcon.setImageDrawable(PackageUtils.getPackageIcon(packageManager, packageName));
        textName.setText(PackageUtils.getPackageLabel(packageManager, packageName));
        buttonInfo.setOnClickListener(v -> IntentUtils.safeStartAppSettings(requireContext(), packageName, null));
        textPackage.setText(packageName);
        textVisibleName.setText(descriptor.getVisibleLabel());

        switchHomeVisibility.setChecked(!descriptor.ignored);
        switchHomeVisibility.jumpDrawablesToCurrentState();
        switchHomeVisibility.setOnCheckedChangeListener((buttonView, isChecked) -> {
            presenter.setIgnored(descriptor, !isChecked);
        });

        switchSearchVisibility.setChecked((descriptor.searchFlags & FLAG_SEARCH_VISIBLE) == FLAG_SEARCH_VISIBLE);
        switchSearchVisibility.jumpDrawablesToCurrentState();
        switchSearchVisibility.setOnCheckedChangeListener((buttonView, isChecked) -> {
            presenter.setSearchVisible(descriptor, isChecked);
        });

        switchShortcutsVisibility.setChecked(descriptor.showShortcuts);
        switchShortcutsVisibility.jumpDrawablesToCurrentState();
        switchShortcutsVisibility.setOnCheckedChangeListener((buttonView, isChecked) -> {
            presenter.setShortcutsVisible(descriptor, isChecked);
        });

        switchSearchShortcutsVisibility.setChecked(
                (descriptor.searchFlags & FLAG_SEARCH_SHORTCUTS_VISIBLE) == FLAG_SEARCH_SHORTCUTS_VISIBLE
        );
        switchSearchShortcutsVisibility.jumpDrawablesToCurrentState();
        switchSearchShortcutsVisibility.setOnCheckedChangeListener((buttonView, isChecked) -> {
            presenter.setSearchShortcutsVisible(descriptor, isChecked);
        });

        buttonRename.setOnClickListener(v -> {
            new RenameDescriptorDialog(requireContext(), descriptor.getVisibleLabel(),
                    (newLabel) -> setCustomLabel(descriptor, newLabel))
                    .show();
        });

        buttonChangeColor.setOnClickListener(v -> setCustomColor(descriptor));
        updateColorPreview(descriptor);

        buttonChangeBadgeColor.setOnClickListener(v -> setCustomBadgeColor(descriptor));
        updateBadgeColorPreview(descriptor);

        buttonAppAliases.setOnClickListener(v -> {
            sendResult(new ShowAppAliasesContract().result(descriptor.getId()));
        });
    }

    @Override
    public void onError(Throwable e) {
        Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        sendResult(new AppDetailsErrorContract().result());
    }

    private void setCustomLabel(AppDescriptor descriptor, String label) {
        presenter.setCustomLabel(descriptor, label);
        textVisibleName.setText(descriptor.getVisibleLabel());
    }

    private void setCustomColor(AppDescriptor descriptor) {
        new SetColorDescriptorDialog(requireContext(), descriptor.getVisibleColor(),
                newColor -> {
                    presenter.setCustomColor(descriptor, newColor);
                    updateColorPreview(descriptor);
                })
                .show();
    }

    private void setCustomBadgeColor(AppDescriptor descriptor) {
        int color = descriptor.customBadgeColor != null
                ? descriptor.customBadgeColor
                : ContextCompat.getColor(requireContext(), R.color.notification_dot);
        new SetColorDescriptorDialog(requireContext(), color,
                newColor -> {
                    presenter.setCustomBadgeColor(descriptor, newColor);
                    updateBadgeColorPreview(descriptor);
                })
                .show();
    }

    private void updateColorPreview(AppDescriptor descriptor) {
        buttonChangeColorPreview.setBackgroundColor(descriptor.getVisibleColor());
    }

    private void updateBadgeColorPreview(AppDescriptor descriptor) {
        int badgeColor = descriptor.customBadgeColor != null
                ? descriptor.customBadgeColor
                : ContextCompat.getColor(requireContext(), R.color.notification_dot);
        buttonChangeBadgeColorPreview.setBackgroundColor(badgeColor);
    }

    public static class ShowAppAliasesContract extends DescriptorFragmentResultContract {
        public ShowAppAliasesContract() {
            super("show_app_aliases");
        }
    }

    public static class AppDetailsErrorContract extends SignalFragmentResultContract {
        public AppDetailsErrorContract() {
            super("app_details_error");
        }
    }
}
