package com.italankin.lnch.feature.intentfactory.componenteditor;

import android.content.ComponentName;
import android.widget.TextView;

import com.italankin.lnch.R;

import androidx.appcompat.app.AppCompatActivity;

public class PackageEditor extends AbstractIntentEditor {

    public PackageEditor(AppCompatActivity activity) {
        super(activity);
    }

    @Override
    public void bind() {
        TextView textPackage = activity.findViewById(R.id.intent_package);
        activity.findViewById(R.id.container_intent_package).setOnClickListener(v -> {
            showEdit(textPackage, R.string.intent_factory_intent_package, value -> {
                ComponentName cn = result.getComponent();
                if (cn == null && value == null) {
                    result.setComponent(null);
                    result.setPackage(null);
                } else {
                    String packageName = value != null ? value : "";
                    String className = cn != null ? cn.getClassName() : "";
                    result.setClassName(packageName, className);
                    result.setPackage(packageName);
                }
            });
        });
        activity.findViewById(R.id.intent_component_select).setOnClickListener(v -> {
            // TODO
        });

        ComponentName cn = result.getComponent();
        if (cn != null) {
            textPackage.setText(cn.getPackageName());
        }
    }
}
