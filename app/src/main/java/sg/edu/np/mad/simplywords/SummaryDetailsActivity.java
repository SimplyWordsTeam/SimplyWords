package sg.edu.np.mad.simplywords;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;

import sg.edu.np.mad.simplywords.viewmodel.SummaryViewModel;

public class SummaryDetailsActivity extends AppCompatActivity {

    String TAG = "SummaryDetailsActivity";

    SummaryViewModel mSummaryViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary_details);

        // Closes the activity when the back button is pressed
        MaterialToolbar appBar = findViewById(R.id.summary_details_app_bar);
        appBar.setNavigationOnClickListener(v -> finish());

        // Gets the summary ID from the intent
        int summaryId = getIntent().getIntExtra("summaryId", -1);
        if (summaryId == -1) {
            // If the summary ID is not found, finish the activity
            finish();
            return;
        }

        // Fills the text view with the simplified text
        Log.d(TAG, "Summary ID: " + summaryId);
        TextView textView = findViewById(R.id.summary_details_text);
        mSummaryViewModel = new ViewModelProvider(this).get(SummaryViewModel.class);
        mSummaryViewModel.getSummaryById(summaryId).observe(this, summary -> {
            textView.setText(summary.getSummarizedText());
        });

        // Listens for changes to the tabs to toggle between the simplified and original text
        TabLayout tabLayout = findViewById(R.id.summary_details_tab_layout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mSummaryViewModel.getSummaryById(summaryId).observe(SummaryDetailsActivity.this, summary -> {
                    if (summary == null) {
                        return;
                    }
                    if (tab.getText() == getString(R.string.simplified_text)) {
                        textView.setText(summary.getSummarizedText());
                    } else {
                        textView.setText(summary.getOriginalText());
                    }
                });
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Do nothing
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Do nothing
            }
        });
    }
}