package com.italankin.lnch.feature.intentfactory.extras;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.italankin.lnch.LauncherApp;
import com.italankin.lnch.R;
import com.italankin.lnch.feature.common.preferences.SupportsOrientationDelegate;
import com.italankin.lnch.model.descriptor.impl.IntentDescriptor;
import com.italankin.lnch.model.repository.prefs.Preferences;
import com.italankin.lnch.util.DialogUtils;
import com.italankin.lnch.util.widget.LceLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IntentExtrasActivity extends AppCompatActivity implements IntentExtrasAdapter.Listener {

    private static final String EXTRA_EXTRAS = "extras";
    private static final String EXTRA_RESULT = "result";

    private final List<IntentExtra> intentExtras = new ArrayList<>();

    private LceLayout lce;
    private IntentExtrasAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Preferences preferences = LauncherApp.daggerService.main().preferences();
        SupportsOrientationDelegate.attach(this, preferences);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_intent_extras);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        lce = findViewById(R.id.lce);

        Bundle bundle = getIntent().getBundleExtra(EXTRA_EXTRAS);
        if (bundle != null) {
            IntentExtra.putAllFrom(bundle, intentExtras);
        }

        RecyclerView list = findViewById(R.id.list);
        adapter = new IntentExtrasAdapter(intentExtras, this);
        list.setAdapter(adapter);

        updateLceState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.intent_extras, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_intent_extras_add) {
            showAddDialog();
            return true;
        } else if (item.getItemId() == R.id.action_intent_extras_save) {
            finishWithResult();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(int position, IntentExtra item) {
        showAddDialog(position, item);
    }

    @Override
    public void onDeleteClick(int position) {
        intentExtras.remove(position);
        adapter.notifyItemRemoved(position);
        updateLceState();
    }

    private void updateLceState() {
        if (intentExtras.isEmpty()) {
            lce.empty()
                    .message(R.string.intent_factory_extras_empty)
                    .show();
        } else {
            lce.showContent();
        }
    }

    private void showAddDialog() {
        showAddDialog(null, null);
    }

    private void showAddDialog(@Nullable Integer position, @Nullable IntentExtra extra) {
        View view = getLayoutInflater().inflate(R.layout.widget_intent_extras, null);

        String[] items = new String[IntentExtra.Type.values().length];
        for (int i = 0; i < IntentExtra.Type.values().length; i++) {
            items[i] = IntentExtra.Type.values()[i].name;
        }
        Spinner spinnerType = view.findViewById(R.id.type);
        spinnerType.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items));
        EditText editKey = view.findViewById(R.id.key);
        EditText editValue = view.findViewById(R.id.value);
        if (extra != null) {
            editKey.setText(extra.key);
            editValue.setText(String.valueOf(extra.value));
            spinnerType.setSelection(Arrays.binarySearch(IntentExtra.Type.values(), extra.type));
        } else {
            spinnerType.setSelection(0);
        }

        AlertDialog alertDialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.intent_factory_extras_dialog_title)
                .setView(view)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.intent_factory_extras_dialog_add, (dialog, which) -> {
                    String key = editKey.getText().toString().trim();
                    if (key.isEmpty()) {
                        Toast.makeText(this, R.string.intent_factory_extras_error_key, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    IntentExtra.Type type = IntentExtra.Type.values()[spinnerType.getSelectedItemPosition()];
                    try {
                        String value = editValue.getText().toString().trim();
                        Object converted = type.convertValue(value);
                        if (position != null) {
                            intentExtras.set(position, new IntentExtra(type, key, converted));
                            adapter.notifyItemChanged(position);
                        } else {
                            intentExtras.add(new IntentExtra(type, key, converted));
                            adapter.notifyItemInserted(intentExtras.size() - 1);
                            updateLceState();
                        }
                    } catch (Exception e) {
                        String text = getString(R.string.intent_factory_extras_error_value, e.toString());
                        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
                    }
                })
                .show();
        DialogUtils.dismissOnDestroy(this, alertDialog);
    }

    private void finishWithResult() {
        Bundle bundle = new Bundle();
        for (IntentExtra intentExtra : intentExtras) {
            intentExtra.putTo(bundle);
        }
        bundle.putBoolean(IntentDescriptor.EXTRA_CUSTOM_INTENT, true);
        Intent data = new Intent().putExtra(EXTRA_RESULT, bundle);
        setResult(RESULT_OK, data);
        finish();
    }

    public static class Contract extends ActivityResultContract<Intent, Bundle> {

        @NonNull
        @Override
        public Intent createIntent(@NonNull Context context, Intent input) {
            return new Intent(context, IntentExtrasActivity.class)
                    .putExtra(EXTRA_EXTRAS, input.getExtras());
        }

        @Nullable
        @Override
        public Bundle parseResult(int resultCode, @Nullable Intent intent) {
            if (resultCode == Activity.RESULT_OK) {
                if (intent == null) {
                    return null;
                }
                return intent.getBundleExtra(EXTRA_RESULT);
            }
            return null;
        }
    }
}
