package com.italankin.lnch.feature.intentfactory.componenteditor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.italankin.lnch.R;
import com.italankin.lnch.feature.intentfactory.extras.IntentExtrasActivity;
import com.italankin.lnch.model.descriptor.impl.IntentDescriptor;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ExtrasEditor extends AbstractIntentEditor implements ActivityResultCallback<Bundle> {

    private final ActivityResultLauncher<Intent> editExtrasLauncher;
    private TextView textExtras;

    public ExtrasEditor(AppCompatActivity activity) {
        super(activity);
        editExtrasLauncher = activity.registerForActivityResult(new IntentExtrasActivity.Contract(), this);
    }

    @Override
    protected void bind() {
        textExtras = activity.findViewById(R.id.intent_extras);
        activity.findViewById(R.id.container_intent_extras).setOnClickListener(v -> {
            editExtrasLauncher.launch(result);
        });
    }

    @Override
    public void update() {
        Bundle extras = result.getExtras();
        int size = extras != null ? extras.size() : 0;
        if (size > 0) {
            if (result.hasExtra(IntentDescriptor.EXTRA_CUSTOM_INTENT)) {
                size--;
            }
            textExtras.setText(activity.getString(R.string.intent_factory_extras_format, size));
        } else {
            textExtras.setText(null);
        }
    }

    @Override
    public void onActivityResult(@Nullable Bundle extras) {
        if (extras == null) {
            return;
        }
        result.replaceExtras(extras);
        update();
    }
}
