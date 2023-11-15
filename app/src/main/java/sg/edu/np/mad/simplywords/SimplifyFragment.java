package sg.edu.np.mad.simplywords;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import sg.edu.np.mad.simplywords.adapter.SummaryAdapter;
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