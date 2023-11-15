package sg.edu.np.mad.simplywords.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import sg.edu.np.mad.simplywords.R;

public class RecentsAdapter extends RecyclerView.Adapter<RecentsAdapter.ViewHolder> {


    private String[] localDataSet;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView timeTextView;
        private final TextView summaryTextView;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            timeTextView = (TextView) view.findViewById(R.id.item_recent_Time_TextView);
            summaryTextView = (TextView) view.findViewById(R.id.item_recent_Summary_TextView);
        }

        public TextView getTimeTextView() {
            return timeTextView;
        }
        public TextView getSummaryTextView(){
            return summaryTextView;
        }
    }

    /**
     * Initialize the dataset of the Adapter
     *
     * @param dataSet String[] containing the data to populate views to be used
     * by RecyclerView
     */
    public RecentsAdapter(String[] dataSet) {
        localDataSet = dataSet;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_recent, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.getSummaryTextView().setText(localDataSet[position]);
        viewHolder.getTimeTextView().setText("SET SOMETHING HERE");

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return localDataSet.length;
    }

}
