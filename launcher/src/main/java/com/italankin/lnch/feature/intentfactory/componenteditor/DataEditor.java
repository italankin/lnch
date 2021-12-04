package com.italankin.lnch.feature.intentfactory.componenteditor;

import android.net.Uri;
import android.widget.TextView;

import com.italankin.lnch.R;

import androidx.appcompat.app.AppCompatActivity;

public class DataEditor extends AbstractIntentEditor {

    private TextView textData;

    public DataEditor(AppCompatActivity activity) {
        super(activity);
    }

    @Override
    protected void bind() {
        textData = activity.findViewById(R.id.intent_data);
        activity.findViewById(R.id.container_intent_data).setOnClickListener(v -> {
            showEdit(textData, R.string.intent_factory_intent_data, value -> {
                Uri data = value != null ? Uri.parse(value) : null;
                String type = result.getType();
                if (type != null) {
                    result.setDataAndTypeAndNormalize(data, type);
                } else {
                    result.setDataAndNormalize(data);
                }
                update();
            });
        });
    }

    @Override
    public void update() {
        Uri data = result.getData();
        textData.setText(data != null ? Uri.decode(data.toString()) : null);
    }
}
