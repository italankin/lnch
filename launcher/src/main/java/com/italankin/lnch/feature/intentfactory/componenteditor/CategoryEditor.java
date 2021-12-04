package com.italankin.lnch.feature.intentfactory.componenteditor;

import android.widget.TextView;

import com.italankin.lnch.R;
import com.italankin.lnch.feature.intentfactory.category.IntentCategory;

import java.util.HashSet;
import java.util.Set;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class CategoryEditor extends AbstractIntentEditor {

    public CategoryEditor(AppCompatActivity activity) {
        super(activity);
    }

    @Override
    protected void bind() {
        TextView textCategory = activity.findViewById(R.id.intent_category);
        activity.findViewById(R.id.container_intent_category).setOnClickListener(v -> {
            showCategoryEdit(textCategory);
        });

        Set<String> categories = result.getCategories();
        if (categories == null) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (IntentCategory category : IntentCategory.getAll()) {
            if (categories.contains(category.value)) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(category.value);
            }
        }
        textCategory.setText(sb.toString());
    }

    private void showCategoryEdit(TextView textCategory) {
        IntentCategory[] allCategories = IntentCategory.getAll();
        CharSequence[] items = new CharSequence[allCategories.length];
        boolean[] checked = new boolean[allCategories.length];
        Set<String> resultCategories = result.getCategories();
        for (int i = 0; i < allCategories.length; i++) {
            IntentCategory category = allCategories[i];
            items[i] = category.name;
            checked[i] = resultCategories != null && resultCategories.contains(category.value);
        }
        new AlertDialog.Builder(activity)
                .setTitle(R.string.intent_factory_intent_category)
                .setMultiChoiceItems(items, checked, (dialog, which, isChecked) -> {
                    checked[which] = isChecked;
                })
                .setNeutralButton(R.string.intent_factory_clear, (dialog, which) -> {
                    if (result.getCategories() == null) {
                        return;
                    }
                    HashSet<String> categories = new HashSet<>(result.getCategories());
                    for (String s : categories) {
                        result.removeCategory(s);
                    }
                    textCategory.setText(null);
                })
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    StringBuilder title = new StringBuilder();
                    for (int i = 0; i < allCategories.length; i++) {
                        IntentCategory category = allCategories[i];
                        if (checked[i]) {
                            result.addCategory(category.value);
                            if (title.length() > 0) {
                                title.append(", ");
                            }
                            title.append(category.name);
                        } else {
                            result.removeCategory(category.value);
                        }
                    }
                    textCategory.setText(title.toString());
                })
                .show();
    }
}
