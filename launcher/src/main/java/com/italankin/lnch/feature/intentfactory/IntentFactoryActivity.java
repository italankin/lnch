package com.italankin.lnch.feature.intentfactory;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.common.preferences.SupportsOrientationDelegate;
import com.italankin.lnch.feature.intentfactory.actions.IntentAction;
import com.italankin.lnch.feature.intentfactory.category.IntentCategory;
import com.italankin.lnch.feature.intentfactory.extras.IntentExtrasActivity;
import com.italankin.lnch.feature.intentfactory.flags.IntentFlag;
import com.italankin.lnch.model.descriptor.impl.IntentDescriptor;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.util.IntentUtils;
import com.italankin.lnch.util.widget.EditTextAlertDialog;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class IntentFactoryActivity extends AppCompatActivity {

    public static Intent editIntent(Context context, IntentDescriptor descriptor) {
        Intent intent = IntentUtils.fromUri(descriptor.intentUri, Intent.URI_INTENT_SCHEME);
        return new Intent(context, IntentFactoryActivity.class)
                .putExtra(EXTRA_INTENT, intent)
                .putExtra(EXTRA_DESCRIPTOR_ID, descriptor.getId())
                .putExtra(EXTRA_LABEL, descriptor.getVisibleLabel());
    }

    @Nullable
    public static IntentFactoryResult getResultExtra(@Nullable Intent data) {
        return data != null ? data.getParcelableExtra(EXTRA_RESULT) : null;
    }

    private static final String EXTRA_INTENT = "intent";
    private static final String EXTRA_DESCRIPTOR_ID = "descriptor_id";
    private static final String EXTRA_LABEL = "label";

    private static final String EXTRA_RESULT = "result";

    private static final int REQUEST_CODE_EDIT_EXTRAS = 0;

    private TextView textTitle;
    private TextView textAction;
    private TextView textPackage;
    private TextView textClass;
    private TextView textData;
    private TextView textType;
    private TextView textFlags;
    private TextView textCategory;
    private TextView textExtras;

    private Intent result = new Intent();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Preferences preferences = LauncherApp.daggerService.main().preferences();
        SupportsOrientationDelegate.attach(this, preferences);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_intent_factory);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.intent_factory_title);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        textTitle = findViewById(R.id.intent_title);
        textTitle.setOnClickListener(v -> {
            showTitleEdit();
        });

        textAction = findViewById(R.id.intent_action);
        findViewById(R.id.container_intent_action).setOnClickListener(v -> {
            showEdit(textAction, R.string.intent_factory_intent_action, result::setAction);
        });
        findViewById(R.id.intent_action_select).setOnClickListener(v -> {
            showActionEdit();
        });

        textPackage = findViewById(R.id.intent_package);
        findViewById(R.id.container_intent_package).setOnClickListener(v -> {
            showEdit(textPackage, R.string.intent_factory_intent_package, value -> {
                ComponentName cn = result.getComponent();
                if (cn == null && value == null) {
                    result.setComponent(null);
                    result.setPackage(null);
                } else {
                    String packageName = value != null ? value : "";
                    result.setClassName(packageName, cn != null ? cn.getClassName() : "");
                    result.setPackage(packageName);
                }
            });
        });
        findViewById(R.id.intent_component_select).setOnClickListener(v -> {
            // TODO
        });

        textClass = findViewById(R.id.intent_class);
        findViewById(R.id.container_intent_class).setOnClickListener(v -> {
            showEdit(textClass, R.string.intent_factory_intent_class, value -> {
                ComponentName cn = result.getComponent();
                if (cn == null && value == null) {
                    result.setComponent(null);
                } else {
                    result.setClassName(cn != null ? cn.getPackageName() : "", value != null ? value : "");
                }
            });
        });

        textData = findViewById(R.id.intent_data);
        findViewById(R.id.container_intent_data).setOnClickListener(v -> {
            showEdit(textData, R.string.intent_factory_intent_data, value -> {
                Uri data = value != null ? Uri.parse(value) : null;
                if (result.getType() != null) {
                    result.setDataAndTypeAndNormalize(data, result.getType());
                } else {
                    result.setDataAndNormalize(data);
                }
            });
        });

        textFlags = findViewById(R.id.intent_flags);
        findViewById(R.id.container_intent_flags).setOnClickListener(v -> {
            showFlagsEdit();
        });

        textType = findViewById(R.id.intent_type);
        findViewById(R.id.container_intent_type).setOnClickListener(v -> {
            showEdit(textType, R.string.intent_factory_intent_type, value -> {
                if (result.getData() != null) {
                    result.setDataAndTypeAndNormalize(result.getData(), value);
                } else {
                    result.setTypeAndNormalize(value);
                }
            });
        });

        textCategory = findViewById(R.id.intent_category);
        findViewById(R.id.container_intent_category).setOnClickListener(v -> {
            showCategoryEdit();
        });

        textExtras = findViewById(R.id.intent_extras);
        findViewById(R.id.container_intent_extras).setOnClickListener(v -> {
            Intent intent = IntentExtrasActivity.intentFromExtras(this, result);
            startActivityForResult(intent, REQUEST_CODE_EDIT_EXTRAS);
        });

        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_INTENT)) {
            result = intent.getParcelableExtra(EXTRA_INTENT);
            fillFromIntent(result, intent.getStringExtra(EXTRA_LABEL));
        } else {
            intent.putExtra(EXTRA_INTENT, result);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.intent_factory, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_intent_factory_save) {
            finishWithResult();
            return true;
        } else if (item.getItemId() == R.id.action_intent_factory_test) {
            testIntent();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_EDIT_EXTRAS && resultCode == RESULT_OK) {
            Bundle extras = IntentExtrasActivity.getResultExtras(data);
            if (extras == null) {
                return;
            }
            result.replaceExtras(extras);
            int size = result.getExtras().size();
            if (size > 0) {
                textExtras.setText(getString(R.string.intent_factory_extras_format, size));
            } else {
                textExtras.setText(null);
            }
        }
    }

    private void showTitleEdit() {
        String text = getTrimmed(textTitle);
        EditTextAlertDialog.builder(this)
                .setTitle(R.string.intent_factory_intent_title)
                .customizeEditText(editText -> {
                    editText.setText(text);
                    editText.setSingleLine(true);
                    editText.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
                    editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                    editText.setSelection(text.length());
                })
                .setPositiveButton(R.string.ok, (dialog, editText) -> {
                    String label = editText.getText().toString().trim();
                    if (!label.isEmpty() && !label.equals(text)) {
                        textTitle.setText(label);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showEdit(TextView textView, @StringRes int title, OnSet callback) {
        String text = getTrimmed(textView);
        EditTextAlertDialog.builder(this)
                .setTitle(title)
                .customizeEditText(editText -> {
                    editText.setText(text);
                    editText.setHint(title);
                    editText.setSingleLine(true);
                    editText.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
                    editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                })
                .setPositiveButton(R.string.ok, (dialog, editText) -> {
                    String label = getTrimmed(editText);
                    if (label.isEmpty()) {
                        textView.setText(null);
                        callback.onSet(null);
                    } else if (!label.equals(text)) {
                        textView.setText(label);
                        callback.onSet(label);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .setNeutralButton(R.string.intent_factory_clear, (dialog, which) -> {
                    textView.setText(null);
                    callback.onSet(null);
                })
                .show();
    }

    private void showActionEdit() {
        CharSequence[] items = new CharSequence[IntentAction.getAll().length];
        for (int i = 0; i < IntentAction.getAll().length; i++) {
            IntentAction action = IntentAction.getAll()[i];
            items[i] = action.name;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.intent_factory_intent_action)
                .setItems(items, (dialog, which) -> {
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

    private void showFlagsEdit() {
        IntentFlag[] allFlags = IntentFlag.getAll();
        CharSequence[] items = new CharSequence[allFlags.length];
        boolean[] checked = new boolean[allFlags.length];
        int flags = result.getFlags();
        for (int i = 0; i < allFlags.length; i++) {
            IntentFlag flag = allFlags[i];
            items[i] = flag.name;
            checked[i] = (flags & flag.value) == flag.value;
        }
        new AlertDialog.Builder(this)
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
                    textFlags.setText(IntentFlag.flagsToString(newFlags));
                    result.setFlags(newFlags);
                })
                .show();
    }

    private void showCategoryEdit() {
        IntentCategory[] allCategories = IntentCategory.getAll();
        CharSequence[] items = new CharSequence[allCategories.length];
        boolean[] checked = new boolean[allCategories.length];
        Set<String> resultCategories = result.getCategories();
        for (int i = 0; i < allCategories.length; i++) {
            IntentCategory category = allCategories[i];
            items[i] = category.name;
            checked[i] = resultCategories != null && resultCategories.contains(category.value);
        }
        new AlertDialog.Builder(this)
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

    private void finishWithResult() {
        if (!validate()) {
            return;
        }
        String label = getTrimmed(textTitle);
        String descriptorId = getIntent().getStringExtra(EXTRA_DESCRIPTOR_ID);
        IntentFactoryResult intentFactoryResult = new IntentFactoryResult(descriptorId, result, label);
        setResult(RESULT_OK, new Intent().putExtra(EXTRA_RESULT, intentFactoryResult));
        finish();
    }

    private boolean validate() {
        String label = getTrimmed(textTitle);
        if (label.isEmpty()) {
            Toast.makeText(this, R.string.intent_factory_intent_error_title, Toast.LENGTH_SHORT).show();
            return false;
        }
        String action = result.getAction();
        String packageName = result.getPackage();
        if ((action == null || action.isEmpty()) && (packageName == null || packageName.isEmpty())) {
            Toast.makeText(this, R.string.intent_factory_intent_error_action, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void fillFromIntent(Intent intent, String label) {
        textTitle.setText(label);
        textAction.setText(intent.getAction());
        ComponentName cn = intent.getComponent();
        if (cn != null) {
            textPackage.setText(cn.getPackageName());
            textClass.setText(cn.getClassName());
        }
        textData.setText(Uri.decode(intent.getData().toString()));
        textType.setText(intent.getType());
        textFlags.setText(IntentFlag.flagsToString(intent.getFlags()));

        Bundle extras = intent.getExtras();
        int size = extras != null ? extras.size() : 0;
        if (size > 0) {
            textExtras.setText(getString(R.string.intent_factory_extras_format, size));
        } else {
            textExtras.setText(null);
        }
    }

    private void testIntent() {
        try {
            startActivity(result);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            try (PrintWriter pw = new PrintWriter(sw)) {
                e.printStackTrace(pw);
            }
            TextView tv = new TextView(this);
            int p = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
            tv.setPadding(p, p, p, p);
            tv.setTextIsSelectable(true);
            tv.setMovementMethod(ScrollingMovementMethod.getInstance());
            tv.setTextSize(11);
            tv.setTypeface(Typeface.MONOSPACE);
            tv.setText(sw.toString());
            new AlertDialog.Builder(this)
                    .setTitle(R.string.intent_factory_intent_error_test)
                    .setView(tv)
                    .show();
        }
    }

    private static String getTrimmed(TextView textView) {
        return textView.getText().toString().trim();
    }

    private interface OnSet {
        void onSet(@Nullable String value);
    }
}
