package com.italankin.lnch.feature.intentfactory.componenteditor;

import android.net.Uri;
import android.widget.TextView;

import com.italankin.lnch.R;

import androidx.appcompat.app.AppCompatActivity;

public class TypeEditor extends AbstractIntentEditor {

    private TextView textType;

    public TypeEditor(AppCompatActivity activity) {
        super(activity);
    }

    @Override
    protected void bind() {
        textType = activity.findViewById(R.id.intent_type);
        activity.findViewById(R.id.container_intent_type).setOnClickListener(v -> {
            showEdit(textType, R.string.intent_factory_intent_type, value -> {
                Uri data = result.getData();
                if (data != null) {
                    result.setDataAndTypeAndNormalize(data, value);
                } else {
                    result.setTypeAndNormalize(value);
                }
                update();
            });
        });
    }

    @Override
    public void update() {
        textType.setText(result.getType());
    }
}
