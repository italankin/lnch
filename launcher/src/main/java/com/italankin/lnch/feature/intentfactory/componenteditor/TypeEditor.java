package com.italankin.lnch.feature.intentfactory.componenteditor;

import android.widget.TextView;

import com.italankin.lnch.R;

import androidx.appcompat.app.AppCompatActivity;

public class TypeEditor extends AbstractIntentEditor {

    public TypeEditor(AppCompatActivity activity) {
        super(activity);
    }

    @Override
    protected void bind() {
        TextView textType = activity.findViewById(R.id.intent_type);
        activity.findViewById(R.id.container_intent_type).setOnClickListener(v -> {
            showEdit(textType, R.string.intent_factory_intent_type, value -> {
                if (result.getData() != null) {
                    result.setDataAndTypeAndNormalize(result.getData(), value);
                } else {
                    result.setTypeAndNormalize(value);
                }
            });
        });

        textType.setText(result.getType());
    }
}
