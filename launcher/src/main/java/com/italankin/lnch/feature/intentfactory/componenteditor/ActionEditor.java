package com.italankin.lnch.feature.intentfactory.componenteditor;

import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.intentfactory.actions.IntentAction;

public class ActionEditor extends AbstractIntentEditor {

    private TextView textAction;

    public ActionEditor(AppCompatActivity activity) {
        super(activity);
    }

    @Override
    public void bind() {
        textAction = activity.findViewById(R.id.intent_action);
        activity.findViewById(R.id.container_intent_action).setOnClickListener(v -> {
            showEdit(textAction, R.string.intent_factory_intent_action, result::setAction);
        });
        activity.findViewById(R.id.intent_action_select).setOnClickListener(v -> {
            showActionEdit(textAction);
        });
    }

    @Override
    public void update() {
        textAction.setText(result.getAction());
    }

    private void showActionEdit(TextView textAction) {
        new MaterialAlertDialogBuilder(activity)
                .setTitle(R.string.intent_factory_intent_action)
                .setItems(createItems(), (dialog, which) -> {
                    IntentAction action = IntentAction.getAll()[which];
                    textAction.setText(action.value);
                    result.setAction(action.value);
                })
                .setNeutralButton(R.string.intent_factory_clear, (dialog, which) -> {
                    textAction.setText(null);
                    result.setAction(null);
                })
                .show();
    }

    private CharSequence[] createItems() {
        CharSequence[] items = new CharSequence[IntentAction.getAll().length];
        for (int i = 0; i < IntentAction.getAll().length; i++) {
            IntentAction action = IntentAction.getAll()[i];
            items[i] = action.name;
        }
        return items;
    }
}
