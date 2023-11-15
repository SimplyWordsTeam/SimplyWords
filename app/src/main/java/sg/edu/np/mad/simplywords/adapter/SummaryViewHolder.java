package sg.edu.np.mad.simplywords.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import sg.edu.np.mad.simplywords.R;

class SummaryViewHolder extends RecyclerView.ViewHolder {
    private final TextView createdAtTextView;
    private final TextView originalTextView;
    private final TextView summarizedTextView;

    public SummaryViewHolder(@NonNull View itemView) {
        super(itemView);
        createdAtTextView = itemView.findViewById(R.id.item_summary_created_at);
        originalTextView = itemView.findViewById(R.id.item_summary_original);
        summarizedTextView = itemView.findViewById(R.id.item_summary_summarized);
    }

    public void bind(long createdAt, String originalText, String summarizedText) {
        Date date = new Date(createdAt);
        String formattedDate = new SimpleDateFormat("dd MMM yyyy (HH:mm a)", Locale.ENGLISH).format(date);
        createdAtTextView.setText(formattedDate);
        originalTextView.setText(originalText);
        summarizedTextView.setText(summarizedText);
    }

    static SummaryViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_summary, parent, false);
        return new SummaryViewHolder(view);
    }
}
