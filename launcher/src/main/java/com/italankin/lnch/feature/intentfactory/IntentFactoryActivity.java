package com.italankin.lnch.feature.intentfactory;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.common.preferences.SupportsOrientationDelegate;
import com.italankin.lnch.feature.intentfactory.componenteditor.*;
import com.italankin.lnch.feature.intentfactory.flags.IntentFlag;
import com.italankin.lnch.model.descriptor.impl.IntentDescriptor;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.model.ui.impl.IntentDescriptorUi;
import com.italankin.lnch.util.DialogUtils;
import com.italankin.lnch.util.ViewUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

public class IntentFactoryActivity extends AppCompatActivity implements IntentEditor.Host {

    private static final String EXTRA_DESCRIPTOR_ID = "descriptor_id";
    private static final String EXTRA_INTENT = "intent";

    private static final String EXTRA_RESULT = "result";

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

    // intents without an action will always have ACTION_VIEW as a default anyway
    private Intent result = new Intent(Intent.ACTION_VIEW)
            .setFlags(IntentFlag.DEFAULT_FLAGS);

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

        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_INTENT)) {
            result = intent.getParcelableExtra(EXTRA_INTENT);
        } else {
            intent.putExtra(EXTRA_INTENT, result);
        }
        result.putExtra(IntentDescriptor.EXTRA_CUSTOM_INTENT, true);

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

    @Override
    public void requestUpdate() {
        for (IntentEditor editor : intentEditors) {
            editor.update();
        }
    }

    private void finishWithResult() {
        if (!validate()) {
            return;
        }
        String descriptorId = getIntent().getStringExtra(EXTRA_DESCRIPTOR_ID);
        IntentFactoryResult intentFactoryResult = new IntentFactoryResult(descriptorId, result);
        setResult(RESULT_OK, new Intent().putExtra(EXTRA_RESULT, intentFactoryResult));
        finish();
    }

    private boolean validate() {
        String action = result.getAction();
        ComponentName cn = result.getComponent();
        if ((action == null || action.isEmpty()) &&
                (cn == null || cn.getPackageName().isEmpty() || cn.getClassName().isEmpty())) {
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
            ViewUtils.setPaddingDp(tv, 8);
            tv.setTextIsSelectable(true);
            tv.setMovementMethod(ScrollingMovementMethod.getInstance());
            tv.setTextSize(11);
            tv.setTypeface(Typeface.MONOSPACE);
            tv.setText(sw.toString());
            AlertDialog alertDialog = new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.intent_factory_intent_error_test)
                    .setView(tv)
                    .show();
            DialogUtils.dismissOnDestroy(this, alertDialog);
        }
    }

    public static class EditContract extends ActivityResultContract<IntentDescriptorUi, IntentFactoryResult> {
        @NonNull
        @Override
        public Intent createIntent(@NonNull Context context, IntentDescriptorUi input) {
            return new Intent(context, IntentFactoryActivity.class)
                    .putExtra(EXTRA_DESCRIPTOR_ID, input.getDescriptor().getId())
                    .putExtra(EXTRA_INTENT, input.intent);
        }

        @Override
        public IntentFactoryResult parseResult(int resultCode, @Nullable Intent intent) {
            return resultCode == RESULT_OK && intent != null ? intent.getParcelableExtra(EXTRA_RESULT) : null;
        }
    }

    public static class CreateContract extends ActivityResultContract<Void, IntentFactoryResult> {
        @NonNull
        @Override
        public Intent createIntent(@NonNull Context context, Void input) {
            return new Intent(context, IntentFactoryActivity.class);
        }

        @Override
        public IntentFactoryResult parseResult(int resultCode, @Nullable Intent intent) {
            return resultCode == RESULT_OK && intent != null ? intent.getParcelableExtra(EXTRA_RESULT) : null;
        }
    }
}
