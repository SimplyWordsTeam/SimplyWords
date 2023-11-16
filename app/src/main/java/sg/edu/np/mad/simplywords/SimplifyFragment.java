package sg.edu.np.mad.simplywords;

import android.content.Context;
import android.media.MediaSync;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import sg.edu.np.mad.simplywords.adapter.SummaryAdapter;
import sg.edu.np.mad.simplywords.model.Summary;
import sg.edu.np.mad.simplywords.viewmodel.SummaryViewModel;

public class SimplifyFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    private SummaryViewModel mSummaryViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_simplify, container, false);



        // Inflate the layout for this fragment
        RecyclerView recyclerView = view.findViewById(R.id.simplify_RecentActivityRecyclerView);
        final SummaryAdapter adapter = new SummaryAdapter(new SummaryAdapter.SummaryDiff());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mSummaryViewModel.getAllSummaries().observe(getViewLifecycleOwner(), summaries -> {
            Log.d("HistoryFragment", "Summaries size: " + summaries.size());
            adapter.submitList(summaries);
        });

        adapter.setOnClickListener(new SummaryAdapter.OnClickListener() {
            @Override
            public void onClick(int position, Summary model) {


                // inflate the layout of the popup window
                LayoutInflater inflater = (LayoutInflater)
                        requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View popupView = inflater.inflate(R.layout.floating_full_summary, null);

                // create the popup window
                int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                boolean focusable = true; // lets taps outside the popup also dismiss it
                final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

                // show the popup window
                // which view you pass in doesn't matter, it is only used for the window tolken
                popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

                TextView textView=popupWindow.getContentView().findViewById(R.id.floating_summary_MainTextView);
                textView.setText(model.getOriginalText());

                //change the text to the original when the original button is pressed.
                popupWindow.getContentView().findViewById(R.id.floating_summary_OriginalButton).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        TextView textView=popupWindow.getContentView().findViewById(R.id.floating_summary_MainTextView);
                        textView.setText(model.getOriginalText());
                    }
                });
                //change the text to the simplified when the simplified button is pressed.
                popupWindow.getContentView().findViewById(R.id.floating_summary_SimplifyButton).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        TextView textView=popupWindow.getContentView().findViewById(R.id.floating_summary_MainTextView);
                        textView.setText(model.getSummarizedText());
                    }
                });

                // dismiss the popup window when touched
                popupWindow.getContentView().findViewById(R.id.floating_summary_ExitImageView).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        popupWindow.dismiss();
                    }
                });

            }
        });
        return view;
    }
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        mSummaryViewModel = new ViewModelProvider(requireActivity()).get(SummaryViewModel.class);


    }


}