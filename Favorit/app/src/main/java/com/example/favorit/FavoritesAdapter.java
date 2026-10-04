package com.example.favorit;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.favorit.databinding.ItemFavoriteBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder> {

    public interface OnFavoriteActionListener {
        void onStarToggle(FavoriteItem item, int position);
        void onOpenLink(FavoriteItem item);
        void onShare(FavoriteItem item);
        void onEdit(FavoriteItem item);
        void onDelete(FavoriteItem item);
    }

    private List<FavoriteItem> items = new ArrayList<>();
    private final OnFavoriteActionListener listener;

    public FavoritesAdapter(OnFavoriteActionListener listener) {
        this.listener = listener;
    }

    public void setItems(List<FavoriteItem> newItems) {
        this.items = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFavoriteBinding binding = ItemFavoriteBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new FavoriteViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        holder.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class FavoriteViewHolder extends RecyclerView.ViewHolder {
        private final ItemFavoriteBinding binding;

        public FavoriteViewHolder(@NonNull ItemFavoriteBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(FavoriteItem item, OnFavoriteActionListener listener) {
            Context context = itemView.getContext();

            binding.tvTitle.setText(item.getTitle());

            if (item.getDetails() != null && !item.getDetails().trim().isEmpty()) {
                binding.tvDetails.setText(item.getDetails().trim());
                binding.tvDetails.setVisibility(View.VISIBLE);
            } else {
                binding.tvDetails.setVisibility(View.GONE);
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale.getDefault());
            String dateFormatted = dateFormat.format(new Date(item.getTimestamp()));
            binding.tvDate.setText("تمت الإضافة: " + dateFormatted);

            // Configure Star
            if (item.isFavorite()) {
                binding.btnStar.setImageResource(R.drawable.ic_star_filled);
            } else {
                binding.btnStar.setImageResource(R.drawable.ic_star_border);
            }

            // Category styling
            String category = item.getCategory() != null ? item.getCategory() : "أخرى";
            binding.tvCategoryName.setText(category);

            int bgRes;
            int textRes;
            int iconRes;

            switch (category) {
                case "روابط":
                    bgRes = R.color.cat_link_bg;
                    textRes = R.color.cat_link_text;
                    iconRes = R.drawable.ic_link;
                    break;
                case "أماكن":
                    bgRes = R.color.cat_place_bg;
                    textRes = R.color.cat_place_text;
                    iconRes = R.drawable.ic_place;
                    break;
                case "ملاحظات":
                    bgRes = R.color.cat_note_bg;
                    textRes = R.color.cat_note_text;
                    iconRes = R.drawable.ic_note;
                    break;
                default:
                    bgRes = R.color.cat_other_bg;
                    textRes = R.color.cat_other_text;
                    iconRes = R.drawable.ic_bookmark;
                    break;
            }

            int colorBg = ContextCompat.getColor(context, bgRes);
            int colorText = ContextCompat.getColor(context, textRes);

            binding.layoutCategoryTag.setBackgroundTintList(ColorStateList.valueOf(colorBg));
            binding.tvCategoryName.setTextColor(colorText);
            binding.ivCategoryIcon.setImageResource(iconRes);
            binding.ivCategoryIcon.setImageTintList(ColorStateList.valueOf(colorText));

            // Check if details contains a URL
            String detailsText = item.getDetails() != null ? item.getDetails().trim().toLowerCase(Locale.ROOT) : "";
            boolean isUrl = detailsText.startsWith("http://") || detailsText.startsWith("https://") || detailsText.startsWith("www.");
            binding.btnOpenLink.setVisibility(isUrl ? View.VISIBLE : View.GONE);

            // Listeners
            binding.btnStar.setOnClickListener(v -> {
                if (listener != null) {
                    int pos = getBindingAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        listener.onStarToggle(item, pos);
                    }
                }
            });

            binding.btnOpenLink.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onOpenLink(item);
                }
            });

            binding.btnShare.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onShare(item);
                }
            });

            binding.btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEdit(item);
                }
            });

            binding.btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDelete(item);
                }
            });
        }
    }
}
