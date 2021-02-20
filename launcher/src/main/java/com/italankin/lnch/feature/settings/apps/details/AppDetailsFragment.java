package com.italankin.lnch.feature.settings.apps.details;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.base.AppFragment;
import com.italankin.lnch.model.descriptor.impl.AppDescriptor;
import com.italankin.lnch.util.IntentUtils;
import com.italankin.lnch.util.PackageUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;

import static com.italankin.lnch.model.descriptor.impl.AppDescriptor.FLAG_SEARCH_SHORTCUTS_VISIBLE;
import static com.italankin.lnch.model.descriptor.impl.AppDescriptor.FLAG_SEARCH_VISIBLE;

public class AppDetailsFragment extends AppFragment implements AppDetailsView {

    public static AppDetailsFragment newInstance(String descriptorId) {
        Bundle args = new Bundle();
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
    private View buttonInfo;
    private View buttonAppAliases;

    private SwitchCompat switchHomeVisibility;
    private SwitchCompat switchSearchVisibility;
    private SwitchCompat switchShortcutsVisibility;

    private Callbacks callbacks;

    @ProvidePresenter
    AppDetailsPresenter providePresenter() {
        return LauncherApp.daggerService.presenters().appDetails();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        callbacks = (Callbacks) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callbacks = null;
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
        switchShortcutsVisibility = view.findViewById(R.id.switch_search_shortcuts_visibility);
        buttonAppAliases = view.findViewById(R.id.app_aliases);

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

        switchShortcutsVisibility.setChecked((descriptor.searchFlags & FLAG_SEARCH_SHORTCUTS_VISIBLE) == FLAG_SEARCH_SHORTCUTS_VISIBLE);
        switchShortcutsVisibility.jumpDrawablesToCurrentState();
        switchShortcutsVisibility.setOnCheckedChangeListener((buttonView, isChecked) -> {
            presenter.setSearchShortcutsVisible(descriptor, isChecked);
        });

        buttonAppAliases.setOnClickListener(v -> {
            if (callbacks != null) {
                callbacks.showAppAliases(descriptor.getId());
            }
        });
    }

    @Override
    public void onError(Throwable e) {
        Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        if (callbacks != null) {
            callbacks.onAppDetailsError();
        }
    }

    public interface Callbacks {
        void showAppAliases(String descriptorId);

        void onAppDetailsError();
    }
}
