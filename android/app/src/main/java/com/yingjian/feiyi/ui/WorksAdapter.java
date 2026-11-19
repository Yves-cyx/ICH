package com.yingjian.feiyi.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.yingjian.feiyi.R;
import com.yingjian.feiyi.data.Models.Work;
import com.yingjian.feiyi.databinding.ItemWorkBinding;
import java.util.Objects;
import java.util.function.Consumer;

final class WorksAdapter extends ListAdapter<Work, WorksAdapter.Card> {
    private final Consumer<Work> open;

    WorksAdapter(Consumer<Work> open) {
        super(new DiffUtil.ItemCallback<>() {
            @Override public boolean areItemsTheSame(@NonNull Work a, @NonNull Work b) { return a.id == b.id; }
            @Override public boolean areContentsTheSame(@NonNull Work a, @NonNull Work b) {
                return Objects.equals(a.name, b.name) && Objects.equals(a.shape, b.shape)
                        && Objects.equals(a.palette, b.palette) && a.x == b.x && a.y == b.y && a.angle == b.angle;
            }
        });
        this.open = open;
    }

    @NonNull @Override public Card onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        return new Card(ItemWorkBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override public void onBindViewHolder(@NonNull Card card, int position) {
        Work work = getItem(position);
        card.views.name.setText(work.name);
        String role = switch (work.shape) { case "warrior" -> "武生"; case "lady" -> "花旦"; default -> "书生"; };
        String color = switch (work.palette) { case "indigo" -> "靛青"; case "amber" -> "琥珀"; default -> "朱砂"; };
        card.views.meta.setText(card.itemView.getContext().getString(R.string.combined_label, role, color));
        card.views.art.showWork(work, true, false);
        card.views.actions.setOnClickListener(view -> open.accept(work));
    }

    static final class Card extends RecyclerView.ViewHolder {
        final ItemWorkBinding views;
        Card(ItemWorkBinding views) { super(views.getRoot()); this.views = views; }
    }
}
