package com.example.favorit;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.favorit.databinding.ActivityMainBinding;
import com.example.favorit.databinding.DialogAddEditFavoriteBinding;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class MainActivity extends AppCompatActivity implements FavoritesAdapter.OnFavoriteActionListener {

    private ActivityMainBinding binding;
    private DatabaseHelper dbHelper;
    private FavoritesAdapter adapter;

    private String selectedCategory = "الكل";
    private String searchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(this);

        setupRecyclerView();
        setupSearch();
        setupCategoryFilters();
        setupFab();

        insertInitialSampleDataIfEmpty();
        loadData();
    }

    private void setupRecyclerView() {
        adapter = new FavoritesAdapter(this);
        binding.rvFavorites.setLayoutManager(new LinearLayoutManager(this));
        binding.rvFavorites.setAdapter(adapter);
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s != null ? s.toString() : "";
                if (!searchQuery.isEmpty()) {
                    binding.btnClearSearch.setVisibility(View.VISIBLE);
                } else {
                    binding.btnClearSearch.setVisibility(View.GONE);
                }
                loadData();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.btnClearSearch.setOnClickListener(v -> {
            binding.etSearch.setText("");
            searchQuery = "";
            binding.btnClearSearch.setVisibility(View.GONE);
            loadData();
        });
    }

    private void setupCategoryFilters() {
        binding.chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds == null || checkedIds.isEmpty()) {
                selectedCategory = "الكل";
            } else {
                int id = checkedIds.get(0);
                if (id == R.id.filterLink) {
                    selectedCategory = "روابط";
                } else if (id == R.id.filterPlace) {
                    selectedCategory = "أماكن";
                } else if (id == R.id.filterNote) {
                    selectedCategory = "ملاحظات";
                } else if (id == R.id.filterOther) {
                    selectedCategory = "أخرى";
                } else {
                    selectedCategory = "الكل";
                }
            }
            loadData();
        });
    }

    private void setupFab() {
        binding.fabAdd.setOnClickListener(v -> showAddOrEditDialog(null));
    }

    private void insertInitialSampleDataIfEmpty() {
        List<FavoriteItem> currentItems = dbHelper.getFilteredItems("الكل", "");
        if (currentItems.isEmpty()) {
            long now = System.currentTimeMillis();
            dbHelper.insertItem(new FavoriteItem("موقع محرك البحث جوجل", "https://www.google.com", "روابط", now, true));
            dbHelper.insertItem(new FavoriteItem("برج خليفة - دبي", "أحد أجمل المعالم السياحية والأماكن المفضلة للزيارة", "أماكن", now - 3600000, true));
            dbHelper.insertItem(new FavoriteItem("قائمة الأهداف الأسبوعية", "1. إنهاء تطوير التطبيق\n2. قراءة كتاب جديد\n3. ممارسة الرياضة", "ملاحظات", now - 7200000, false));
        }
    }

    private void loadData() {
        List<FavoriteItem> list = dbHelper.getFilteredItems(selectedCategory, searchQuery);
        adapter.setItems(list);

        String countText = getString(R.string.total_items, list.size());
        binding.tvCount.setText(countText);

        if (list.isEmpty()) {
            binding.layoutEmpty.setVisibility(View.VISIBLE);
            binding.rvFavorites.setVisibility(View.GONE);
        } else {
            binding.layoutEmpty.setVisibility(View.GONE);
            binding.rvFavorites.setVisibility(View.VISIBLE);
        }
    }

    private void showAddOrEditDialog(FavoriteItem itemToEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        DialogAddEditFavoriteBinding dialogBinding = DialogAddEditFavoriteBinding.inflate(LayoutInflater.from(this));
        builder.setView(dialogBinding.getRoot());

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        boolean isEdit = (itemToEdit != null);
        if (isEdit) {
            dialogBinding.tvDialogTitle.setText(R.string.edit_favorite);
            dialogBinding.etTitle.setText(itemToEdit.getTitle());
            dialogBinding.etDetails.setText(itemToEdit.getDetails());

            String category = itemToEdit.getCategory();
            if (category == null) category = "أخرى";

            switch (category) {
                case "روابط":
                    dialogBinding.chipLink.setChecked(true);
                    break;
                case "أماكن":
                    dialogBinding.chipPlace.setChecked(true);
                    break;
                case "ملاحظات":
                    dialogBinding.chipNote.setChecked(true);
                    break;
                default:
                    dialogBinding.chipOther.setChecked(true);
                    break;
            }
        } else {
            dialogBinding.tvDialogTitle.setText(R.string.add_favorite);
        }

        dialogBinding.btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialogBinding.btnSave.setOnClickListener(v -> {
            String title = dialogBinding.etTitle.getText() != null ? dialogBinding.etTitle.getText().toString().trim() : "";
            String details = dialogBinding.etDetails.getText() != null ? dialogBinding.etDetails.getText().toString().trim() : "";

            if (title.isEmpty()) {
                dialogBinding.inputLayoutTitle.setError(getString(R.string.error_empty_title));
                return;
            } else {
                dialogBinding.inputLayoutTitle.setError(null);
            }

            String category;
            int checkedChipId = dialogBinding.chipGroupCategory.getCheckedChipId();
            if (checkedChipId == R.id.chipPlace) {
                category = "أماكن";
            } else if (checkedChipId == R.id.chipNote) {
                category = "ملاحظات";
            } else if (checkedChipId == R.id.chipOther) {
                category = "أخرى";
            } else {
                category = "روابط";
            }

            if (isEdit) {
                itemToEdit.setTitle(title);
                itemToEdit.setDetails(details);
                itemToEdit.setCategory(category);
                dbHelper.updateItem(itemToEdit);
                Snackbar.make(binding.getRoot(), R.string.item_updated, Snackbar.LENGTH_SHORT).show();
            } else {
                FavoriteItem newItem = new FavoriteItem(title, details, category, System.currentTimeMillis(), true);
                dbHelper.insertItem(newItem);
                Snackbar.make(binding.getRoot(), R.string.item_added, Snackbar.LENGTH_SHORT).show();
            }

            dialog.dismiss();
            loadData();
        });

        dialog.show();
    }

    @Override
    public void onStarToggle(FavoriteItem item, int position) {
        boolean newStatus = !item.isFavorite();
        item.setFavorite(newStatus);
        dbHelper.updateFavoriteStatus(item.getId(), newStatus);
        loadData();
    }

    @Override
    public void onOpenLink(FavoriteItem item) {
        String url = item.getDetails() != null ? item.getDetails().trim() : "";
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "تعذر فتح الرابط", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onShare(FavoriteItem item) {
        String shareText = item.getTitle() + "\n" + (item.getDetails() != null ? item.getDetails() : "");
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share)));
    }

    @Override
    public void onEdit(FavoriteItem item) {
        showAddOrEditDialog(item);
    }

    @Override
    public void onDelete(FavoriteItem item) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_confirm_title)
                .setMessage(R.string.delete_confirm_msg)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    dbHelper.deleteItem(item.getId());
                    loadData();
                    Snackbar.make(binding.getRoot(), R.string.item_deleted, Snackbar.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
