package com.italankin.lnch.feature.intentfactory.componenteditor;

import android.widget.TextView;

import com.italankin.lnch.R;
import com.italankin.lnch.feature.intentfactory.flags.IntentFlag;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import static com.italankin.lnch.feature.intentfactory.flags.IntentFlag.getAll;

public class FlagsEditor extends AbstractIntentEditor {

    public FlagsEditor(AppCompatActivity activity) {
        super(activity);
    }

    @Override
    protected void bind() {
        TextView textFlags = activity.findViewById(R.id.intent_flags);
        activity.findViewById(R.id.container_intent_flags).setOnClickListener(v -> {
            showFlagsEdit(textFlags);
        });

        textFlags.setText(flagsToString(result.getFlags()));
    }

    private void showFlagsEdit(TextView textFlags) {
        IntentFlag[] allFlags = getAll();
        CharSequence[] items = new CharSequence[allFlags.length];
        boolean[] checked = new boolean[allFlags.length];
        int flags = result.getFlags();
        for (int i = 0; i < allFlags.length; i++) {
            IntentFlag flag = allFlags[i];
            items[i] = flag.name;
            checked[i] = (flags & flag.value) == flag.value;
        }
        new AlertDialog.Builder(activity)
                .setTitle(R.string.intent_factory_intent_flags)
                .setMultiChoiceItems(items, checked, (dialog, which, isChecked) -> {
                    checked[which] = isChecked;
                })
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    int newFlags = 0;
                    for (int i = 0; i < allFlags.length; i++) {
                        IntentFlag flag = allFlags[i];
                        newFlags |= checked[i] ? flag.value : 0;
                    }
                    textFlags.setText(flagsToString(newFlags));
                    result.setFlags(newFlags);
                })
                .show();
    }

    private static String flagsToString(int flags) {
        StringBuilder sb = new StringBuilder();
        for (IntentFlag flag : IntentFlag.getAll()) {
            if ((flags & flag.value) == flag.value) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(flag.name);
            }
        }
        return sb.toString();
    }
}
