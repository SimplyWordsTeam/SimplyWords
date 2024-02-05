package sg.edu.np.mad.simplywords;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.slider.Slider;
import com.google.android.material.tabs.TabLayout;

import sg.edu.np.mad.simplywords.databinding.ActivitySummaryDetailsBinding;
import sg.edu.np.mad.simplywords.util.LLMInteraction;
import sg.edu.np.mad.simplywords.viewmodel.SummaryViewModel;

public class SummaryDetailsActivity extends AppCompatActivity {

    String TAG = "SummaryDetailsActivity";

    SummaryViewModel mSummaryViewModel;
    ActivitySummaryDetailsBinding binding;
    boolean isShowingCardView=false;

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
        mSummaryViewModel.getSummaryById(summaryId).observe(this, summary -> textView.setText(summary.getSummarizedText()));

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

        MaterialCardView cardView =findViewById(R.id.summary_details_materialCardView);
        // Configures the sheet for translations
        TranslationBottomSheet translationBottomSheet = new TranslationBottomSheet();
        BottomAppBar bottomAppBar = findViewById(R.id.summary_details_bottom_app_bar);
        bottomAppBar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.summary_details_translate) {
                translationBottomSheet.show(getSupportFragmentManager(), translationBottomSheet.getTag());
                return true;
            } else if (item.getItemId() == R.id.summary_details_text_adjust) {
                if (!isShowingCardView){
                    cardView.setVisibility(View.VISIBLE);
                    isShowingCardView=!isShowingCardView;
                }else{
                    cardView.setVisibility(View.GONE);
                    isShowingCardView=!isShowingCardView;
                }

                return true;
            } else if (item.getItemId() == R.id.summary_details_share) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, textView.getText());

                Intent shareIntent = Intent.createChooser(intent, null);
                startActivity(shareIntent);
            }
            return false;
        });

        Slider fontSizeSlider=findViewById(R.id.summary_details_fontSizeSlider);
        Spinner fontSpinner=findViewById(R.id.summary_details_fontSpinner);
        TextView translatedTextView=findViewById(R.id.summary_details_translation);


        ArrayAdapter<CharSequence> fontSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.font_options, android.R.layout.simple_spinner_item);
        fontSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        TextView summaryTextView=findViewById(R.id.summary_details_text);
        fontSpinner.setAdapter(fontSpinnerAdapter);

        Context context=this;
        fontSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position==0){
                    Typeface typeFace= ResourcesCompat.getFont(context,R.font.lexend);
                    summaryTextView.setTypeface(typeFace);
                    translatedTextView.setTypeface(typeFace);
                } else if (position==1) {
                    Typeface typeFace= ResourcesCompat.getFont(context,R.font.roboto);
                    summaryTextView.setTypeface(typeFace);
                    translatedTextView.setTypeface(typeFace);
                } else if (position==2) {
                    Typeface typeFace= ResourcesCompat.getFont(context,R.font.tinos);
                    summaryTextView.setTypeface(typeFace);
                    translatedTextView.setTypeface(typeFace);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



        fontSizeSlider.setValueFrom(12);
        fontSizeSlider.setValueTo(30);
        fontSizeSlider.setStepSize(3);
        float density=getResources().getDisplayMetrics().scaledDensity;
        float initialFontSizeInPx=textView.getTextSize();
        float initialFontSizeInSp=initialFontSizeInPx/density;

        float nearestStepValue = Math.round(initialFontSizeInSp / fontSizeSlider.getStepSize()) * fontSizeSlider.getStepSize();
        fontSizeSlider.setValue(nearestStepValue);

        fontSizeSlider.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(Slider slider, float value, boolean fromUser) {
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, value);
                translatedTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, value);
            }
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