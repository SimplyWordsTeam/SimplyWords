package sg.edu.np.mad.simplywords.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import sg.edu.np.mad.simplywords.model.Summary;

public class SummaryAdapter extends ListAdapter<Summary, SummaryViewHolder> {
    private OnClickListener onClickListener;

    public SummaryAdapter(@NonNull DiffUtil.ItemCallback<Summary> callback) {
        super(callback);
    }

    @NonNull
    @Override
    public SummaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return SummaryViewHolder.create(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull SummaryViewHolder holder, int position) {
        Summary summary = getItem(position);
        holder.bind(summary.getCreatedAt(), summary.getOriginalText(), summary.getSummarizedText());
        holder.itemView.setOnClickListener(view -> {
            if (onClickListener != null) {
                onClickListener.onClick(holder.getAdapterPosition(), summary);
            }
        });
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }


    public interface OnClickListener {
        void onClick(int position, Summary model);
    }

    public static class SummaryDiff extends DiffUtil.ItemCallback<Summary> {
        @Override
        public boolean areItemsTheSame(@NonNull Summary oldItem, @NonNull Summary newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Summary oldItem, @NonNull Summary newItem) {
            return oldItem.getOriginalText().equals(newItem.getOriginalText());
        }
    }
}
