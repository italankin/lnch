package com.italankin.lnch.feature.intentfactory.componenteditor;

import android.net.Uri;
import android.widget.TextView;

import com.italankin.lnch.R;

import androidx.appcompat.app.AppCompatActivity;

public class DataEditor extends AbstractIntentEditor {

    public DataEditor(AppCompatActivity activity) {
        super(activity);
    }

    @Override
    protected void bind() {
        TextView textData = activity.findViewById(R.id.intent_data);
        activity.findViewById(R.id.container_intent_data).setOnClickListener(v -> {
            showEdit(textData, R.string.intent_factory_intent_data, value -> {
                Uri data = value != null ? Uri.parse(value) : null;
                if (result.getType() != null) {
                    result.setDataAndTypeAndNormalize(data, result.getType());
                } else {
                    result.setDataAndNormalize(data);
                }
            });
        });

        Uri data = result.getData();
        if (data != null) {
            textData.setText(Uri.decode(data.toString()));
        }
    }
}
