package com.italankin.lnch.feature.intentfactory.componenteditor;

import android.content.ComponentName;
import android.widget.TextView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.italankin.lnch.R;
import com.italankin.lnch.feature.intentfactory.componentselector.ComponentSelectorActivity;

public class PackageEditor extends AbstractIntentEditor implements ActivityResultCallback<ComponentName> {

    private final ActivityResultLauncher<Void> selectComponentLauncher;
    private TextView textPackage;

    public PackageEditor(AppCompatActivity activity) {
        super(activity);
        selectComponentLauncher = activity.registerForActivityResult(
                new ComponentSelectorActivity.Contract(), this);
    }

    @Override
    public void bind() {
        textPackage = activity.findViewById(R.id.intent_package);
        activity.findViewById(R.id.container_intent_package).setOnClickListener(v -> {
            showEdit(textPackage, R.string.intent_factory_intent_package, value -> {
                ComponentName cn = result.getComponent();
                if (cn == null && value == null) {
                    result.setComponent(null);
                } else {
                    String packageName = value != null ? value : "";
                    String className = cn != null ? cn.getClassName() : "";
                    result.setClassName(packageName, className);
                }
                update();
            });
        });
        activity.findViewById(R.id.intent_component_select).setOnClickListener(v -> {
            selectComponentLauncher.launch(null);
        });
    }

    @Override
    public void update() {
        ComponentName cn = result.getComponent();
        textPackage.setText(cn != null ? cn.getPackageName() : null);
    }

    @Override
    public void onActivityResult(@Nullable ComponentName componentName) {
        if (componentName != null) {
            result.setComponent(componentName);
            host.requestUpdate();
        }
    }
}
