package sg.edu.np.mad.simplywords;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.tabs.TabLayout;

import sg.edu.np.mad.simplywords.databinding.ActivitySummaryDetailsBinding;
import sg.edu.np.mad.simplywords.util.LLMInteraction;
import sg.edu.np.mad.simplywords.viewmodel.SummaryViewModel;

public class SummaryDetailsActivity extends AppCompatActivity {

    String TAG = "SummaryDetailsActivity";

    SummaryViewModel mSummaryViewModel;
    ActivitySummaryDetailsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySummaryDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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

                    ((TextView) findViewById(R.id.summary_details_translation)).setText("");
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

        // Configures the sheet for translations
        TranslationBottomSheet translationBottomSheet = new TranslationBottomSheet();
        BottomAppBar bottomAppBar = findViewById(R.id.summary_details_bottom_app_bar);
        bottomAppBar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.summary_details_translate) {
                translationBottomSheet.show(getSupportFragmentManager(), translationBottomSheet.getTag());
                return true;
            }
            return false;
        });
    }

    public void onTranslate(String language) {
        // Translates the text
        Log.d(TAG, "Translating to " + language);

        TextView textView = binding.summaryDetailsTranslation;
        textView.setText(getString(R.string.translation_in_progress));
        new LLMInteraction().generateTranslatedText(this, binding.summaryDetailsText.getText(), language, new LLMInteraction.ResponseCallback() {
            @Override
            public void onSuccess(String translatedText) {
                textView.setText(translatedText);
            }

            @Override
            public void onError(Exception exception) {
                // Display an alert dialog with the error
                AlertDialog.Builder builder = new AlertDialog.Builder(SummaryDetailsActivity.this);
                builder.setMessage(exception.getMessage())
                        .setTitle("Error")
                        .setPositiveButton("OK", null);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }
}