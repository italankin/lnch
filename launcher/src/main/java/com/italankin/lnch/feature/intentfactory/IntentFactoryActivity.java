package com.italankin.lnch.feature.intentfactory;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
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
import com.italankin.lnch.feature.intentfactory.componenteditor.ActionEditor;
import com.italankin.lnch.feature.intentfactory.componenteditor.CategoryEditor;
import com.italankin.lnch.feature.intentfactory.componenteditor.ClassEditor;
import com.italankin.lnch.feature.intentfactory.componenteditor.DataEditor;
import com.italankin.lnch.feature.intentfactory.componenteditor.ExtrasEditor;
import com.italankin.lnch.feature.intentfactory.componenteditor.FlagsEditor;
import com.italankin.lnch.feature.intentfactory.componenteditor.IntentEditor;
import com.italankin.lnch.feature.intentfactory.componenteditor.PackageEditor;
import com.italankin.lnch.feature.intentfactory.componenteditor.TypeEditor;
import com.italankin.lnch.model.descriptor.impl.IntentDescriptor;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.util.IntentUtils;
import com.italankin.lnch.util.widget.EditTextAlertDialog;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

    private TextView textTitle;

    private final List<IntentEditor> intentEditors = Arrays.asList(
            new ActionEditor(this),
            new PackageEditor(this),
            new ClassEditor(this),
            new DataEditor(this),
            new FlagsEditor(this),
            new TypeEditor(this),
            new CategoryEditor(this),
            new ExtrasEditor(this)
    );

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
        textTitle.setOnClickListener(v -> showTitleEdit());

        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_INTENT)) {
            result = intent.getParcelableExtra(EXTRA_INTENT);
        } else {
            intent.putExtra(EXTRA_INTENT, result);
        }
        if (intent.hasExtra(EXTRA_LABEL)) {
            textTitle.setText(getIntent().getStringExtra(EXTRA_LABEL));
        }

        for (IntentEditor editor : intentEditors) {
            editor.bind(result);
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

    private void showTitleEdit() {
        String label = getTrimmed(textTitle);
        EditTextAlertDialog.builder(this)
                .setTitle(R.string.intent_factory_intent_title)
                .customizeEditText(editText -> {
                    editText.setText(label);
                    editText.setSingleLine(true);
                    editText.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
                    editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                    editText.setSelection(label.length());
                })
                .setPositiveButton(R.string.ok, (dialog, editText) -> {
                    String newLabel = editText.getText().toString().trim();
                    if (!newLabel.isEmpty() && !newLabel.equals(label)) {
                        textTitle.setText(newLabel);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
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
}
