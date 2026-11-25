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

final class CatalogAdapter extends ListAdapter<CatalogAdapter.Slot, CatalogAdapter.Card> {
    record Slot(long key, Heritage entry) {}

    private final CatalogCoverLoader covers;
    private final Consumer<Heritage> open;

    CatalogAdapter(CatalogCoverLoader covers, Consumer<Heritage> open) {
        super(new DiffUtil.ItemCallback<>() {
            @Override public boolean areItemsTheSame(@NonNull Slot a, @NonNull Slot b) { return a.key() == b.key(); }

            @Override public boolean areContentsTheSame(@NonNull Slot first, @NonNull Slot second) {
                Heritage a = first.entry();
                Heritage b = second.entry();
                if (a == null || b == null) return Objects.equals(a, b);
                return Objects.equals(a.title, b.title)
                        && Objects.equals(a.region, b.region) && Objects.equals(a.category, b.category)
                        && Objects.equals(a.artKey, b.artKey) && Objects.equals(a.coverUrl, b.coverUrl);
            }
        });
        this.covers = covers;
        this.open = open;
    }

    @NonNull @Override public Card onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        return new Card(ItemHeritageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override public void onBindViewHolder(@NonNull Card card, int position) {
        Heritage item = getItem(position).entry();
        covers.bind(card.views.cover, card.views.art, item);
        var root = card.views.getRoot();
        root.setOnClickListener(item == null ? null : view -> open.accept(item));
        root.setClickable(item != null);
        root.setFocusable(item != null);
        if (item == null) {
            card.views.title.setText(R.string.catalog_empty_slot);
            card.views.meta.setText("");
            return;
        }
        card.views.title.setText(item.title);
        card.views.meta.setText(metaLabel(card.itemView, item));
    }

    @Override public void onViewRecycled(@NonNull Card card) {
        card.views.cover.setTag(null);
        super.onViewRecycled(card);
    }

    static String metaLabel(android.view.View view, Heritage item) {
        String region = item.region == null ? "" : item.region.trim();
        String category = item.category == null ? "" : item.category.trim();
        if (region.isEmpty()) return category;
        if (category.isEmpty()) return region;
        return view.getContext().getString(R.string.combined_label, region, category);
    }

    static final class Card extends RecyclerView.ViewHolder {
        final ItemHeritageBinding views;
        Card(ItemHeritageBinding views) { super(views.getRoot()); this.views = views; }
    }
}
