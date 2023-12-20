package com.italankin.lnch.feature.settings.fonts;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import com.italankin.lnch.R;
import com.italankin.lnch.di.component.ViewModelComponent;
import com.italankin.lnch.feature.base.AppActivity;
import com.italankin.lnch.feature.base.AppViewModelProvider;
import com.italankin.lnch.feature.settings.fonts.events.AddFontEvent;
import com.italankin.lnch.feature.settings.fonts.events.DeleteFontEvent;
import com.italankin.lnch.util.ErrorUtils;
import com.italankin.lnch.util.widget.EditTextAlertDialog;
import com.italankin.lnch.util.widget.LceLayout;
import me.italankin.adapterdelegates.CompositeAdapter;

import java.util.List;

public class FontsActivity extends AppActivity implements FontItemAdapter.Listener {

    public static final String EXTRA_SELECTED_FONT = "selected_font";
    public static final int RESULT_SELECTED_DELETED = RESULT_FIRST_USER;

    private static final String MIME_TYPE = "*/*";

    private FontsViewModel viewModel;

    private LceLayout lce;
    private CompositeAdapter<FontItem> adapter;

    private final ActivityResultLauncher<Void> openFileLauncher = registerForActivityResult(
            new OpenFileContract(), this::showAddDialog);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = AppViewModelProvider.get(this, FontsViewModel.class, ViewModelComponent::fonts);

        setContentView(R.layout.activity_fonts);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        RecyclerView list = findViewById(R.id.list);
        lce = findViewById(R.id.lce);

        adapter = new CompositeAdapter.Builder<FontItem>(this)
                .add(new FontItemAdapter(this))
                .recyclerView(list)
                .setHasStableIds(true)
                .create();

        Drawable drawable = AppCompatResources.getDrawable(this, R.drawable.settings_list_divider);
        DividerItemDecoration decoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        decoration.setDrawable(drawable);
        list.addItemDecoration(decoration);

        viewModel.fontItemsEvents()
                .subscribe(new EventObserver<>() {
                    @Override
                    public void onNext(List<FontItem> fontItems) {
                        onItemsUpdated(fontItems);
                    }
                });
        viewModel.addFontEvents()
                .subscribe(new EventObserver<>() {
                    @Override
                    public void onNext(AddFontEvent event) {
                        if (event instanceof AddFontEvent.FontExistsError) {
                            onAddFontExistsError(((AddFontEvent.FontExistsError) event).fontName);
                        } else if (event instanceof AddFontEvent.FontEmptyNameError) {
                            onAddFontEmptyNameError();
                        } else if (event instanceof AddFontEvent.FontAdded) {
                            onFontAdded();
                        } else if (event instanceof AddFontEvent.InvalidFormatError) {
                            showErrorInvalidFormat();
                        } else if (event instanceof AddFontEvent.FontAddError) {
                            showError(((AddFontEvent.FontAddError) event).error);
                        }
                    }
                });
        viewModel.deleteFontEvents()
                .subscribe(new EventObserver<>() {
                    @Override
                    public void onNext(DeleteFontEvent event) {
                        onFontDeleted(event.reset);
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.settings_fonts, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_add) {
            openFileLauncher.launch(null);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onItemsUpdated(List<FontItem> items) {
        adapter.setDataset(items);
        adapter.notifyDataSetChanged();
        if (items.isEmpty()) {
            lce.empty()
                    .message(R.string.settings_home_laf_appearance_fonts_empty)
                    .show();
        } else {
            lce.showContent();
        }
    }

    private void showError(Throwable e) {
        ErrorUtils.showErrorDialogOrToast(this, e, R.string.settings_home_laf_appearance_fonts_error_generic, this);
    }

    private void onFontDeleted(boolean reset) {
        Toast.makeText(this, R.string.settings_home_laf_appearance_fonts_deleted, Toast.LENGTH_SHORT).show();
        if (reset) {
            setResult(RESULT_SELECTED_DELETED);
        }
    }

    private void onFontAdded() {
        Toast.makeText(this, R.string.settings_home_laf_appearance_fonts_added, Toast.LENGTH_SHORT).show();
    }

    private void showErrorInvalidFormat() {
        Toast.makeText(this, R.string.settings_home_laf_appearance_fonts_error_invalid_format, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onFontSelect(int position, FontItem item) {
        setResult(RESULT_OK, new Intent().putExtra(EXTRA_SELECTED_FONT, item.name));
        finish();
    }

    @Override
    public void onFontDelete(int position, FontItem item) {
        viewModel.deleteFont(item);
    }

    private void onAddFontExistsError(String name) {
        String message = getString(R.string.settings_home_laf_appearance_fonts_error_exists, name);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void onAddFontEmptyNameError() {
        Toast.makeText(this, R.string.settings_home_laf_appearance_fonts_error_empty, Toast.LENGTH_LONG).show();
    }

    private void showAddDialog(@Nullable Uri result) {
        if (result == null) {
            return;
        }
        String defaultName = getFileName(result);

        EditTextAlertDialog.builder(this)
                .setTitle(R.string.settings_home_laf_appearance_fonts_dialog_title)
                .customizeEditText(editText -> {
                    editText.setSingleLine(true);
                    editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(30)});
                    editText.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
                    editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                    editText.setHint(R.string.settings_home_laf_appearance_fonts_dialog_hint);
                    editText.setText(defaultName);
                })
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.settings_home_laf_appearance_fonts_add, (dialog, editText) -> {
                    viewModel.addFont(editText.getText().toString(), result);
                })
                .show(this);
    }

    @Nullable
    private String getFileName(Uri result) {
        Cursor cursor = this.getContentResolver().query(result, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null);
        if (cursor != null) {
            try {
                int columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (columnIndex != -1 && cursor.moveToFirst()) {
                    return cursor.getString(columnIndex);
                }
            } finally {
                cursor.close();
            }
        }
        return null;
    }

    private static class OpenFileContract extends ActivityResultContract<Void, Uri> {

        @NonNull
        @Override
        public Intent createIntent(@NonNull Context context, Void input) {
            return new Intent(Intent.ACTION_OPEN_DOCUMENT)
                    .addCategory(Intent.CATEGORY_OPENABLE)
                    .setType(MIME_TYPE);
        }

        @Override
        public Uri parseResult(int resultCode, @Nullable Intent intent) {
            return resultCode == Activity.RESULT_OK && intent != null ? intent.getData() : null;
        }
    }

    public static class Contract extends ActivityResultContract<Void, Contract.Result> {

        @NonNull
        @Override
        public Intent createIntent(@NonNull Context context, Void unused) {
            return new Intent(context, FontsActivity.class);
        }

        @Override
        public Result parseResult(int resultCode, @Nullable Intent intent) {
            switch (resultCode) {
                case RESULT_OK:
                    return new Result(intent.getStringExtra(EXTRA_SELECTED_FONT), false);
                case RESULT_SELECTED_DELETED:
                    return new Result(null, true);
            }
            return null;
        }

        public static class Result {
            @Nullable
            public final String selected;
            public final boolean selectedDeleted;

            Result(@Nullable String selected, boolean selectedDeleted) {
                this.selected = selected;
                this.selectedDeleted = selectedDeleted;
            }
        }
    }
}
