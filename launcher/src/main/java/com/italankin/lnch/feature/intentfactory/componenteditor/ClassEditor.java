package com.italankin.lnch.feature.intentfactory.componenteditor;

import android.content.ComponentName;
import android.widget.TextView;

import com.italankin.lnch.R;

import androidx.appcompat.app.AppCompatActivity;

public class ClassEditor extends AbstractIntentEditor {

    public ClassEditor(AppCompatActivity activity) {
        super(activity);
    }

    @Override
    protected void bind() {
        TextView textClass = activity.findViewById(R.id.intent_class);
        activity.findViewById(R.id.container_intent_class).setOnClickListener(v -> {
            showEdit(textClass, R.string.intent_factory_intent_class, value -> {
                ComponentName cn = result.getComponent();
                if (cn == null && value == null) {
                    result.setComponent(null);
                } else {
                    String packageName = cn != null ? cn.getPackageName() : "";
                    String className = value != null ? value : "";
                    result.setClassName(packageName, className);
                }
            });
        });
    }
}
