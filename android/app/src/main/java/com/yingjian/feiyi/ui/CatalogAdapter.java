package com.yingjian.feiyi.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.yingjian.feiyi.R;
import com.yingjian.feiyi.data.Models.Heritage;
import com.yingjian.feiyi.databinding.ItemHeritageBinding;
import java.util.Objects;
import java.util.function.Consumer;

final class CatalogAdapter extends ListAdapter<Heritage, CatalogAdapter.Card> {
    private final Consumer<Heritage> open;

    CatalogAdapter(Consumer<Heritage> open) {
        super(new DiffUtil.ItemCallback<>() {
            @Override public boolean areItemsTheSame(@NonNull Heritage a, @NonNull Heritage b) { return a.id == b.id; }
            @Override public boolean areContentsTheSame(@NonNull Heritage a, @NonNull Heritage b) {
                return Objects.equals(a.title, b.title) && Objects.equals(a.summary, b.summary)
                        && Objects.equals(a.region, b.region) && Objects.equals(a.category, b.category)
                        && Objects.equals(a.artKey, b.artKey);
            }
        });
        this.open = open;
    }

    @NonNull @Override public Card onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        return new Card(ItemHeritageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override public void onBindViewHolder(@NonNull Card card, int position) {
        Heritage item = getItem(position);
        card.views.art.setArtwork(item.artKey);
        card.views.title.setText(item.title);
        card.views.meta.setText(card.itemView.getContext().getString(R.string.combined_label, item.category, item.region));
        card.views.summary.setText(item.summary);
        card.views.getRoot().setOnClickListener(view -> open.accept(item));
    }

    static final class Card extends RecyclerView.ViewHolder {
        final ItemHeritageBinding views;
        Card(ItemHeritageBinding views) { super(views.getRoot()); this.views = views; }
    }
}
