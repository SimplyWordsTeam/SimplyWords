package sg.edu.np.mad.simplywords;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;

import sg.edu.np.mad.simplywords.adapter.SummaryAdapter;
import sg.edu.np.mad.simplywords.model.Summary;
import sg.edu.np.mad.simplywords.util.LLMInteraction;
import sg.edu.np.mad.simplywords.viewmodel.SummaryViewModel;

public class SimplifyFragment extends Fragment {
    private SummaryViewModel mSummaryViewModel;
    ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
    Button simplifyTextButton;
    Button simplifyPhotoButton;
    LinearProgressIndicator progressIndicator;

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

        simplifyTextButton = view.findViewById(R.id.simplify_text_button);
        simplifyPhotoButton = view.findViewById(R.id.simplify_photo_button);
        progressIndicator = view.findViewById(R.id.simplify_progress);

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

                TextView textView = popupWindow.getContentView().findViewById(R.id.floating_summary_MainTextView);
                textView.setText(model.getOriginalText());

                //change the text to the original when the original button is pressed.
                popupWindow.getContentView().findViewById(R.id.floating_summary_OriginalButton).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        TextView textView = popupWindow.getContentView().findViewById(R.id.floating_summary_MainTextView);
                        textView.setText(model.getOriginalText());
                    }
                });
                //change the text to the simplified when the simplified button is pressed.
                popupWindow.getContentView().findViewById(R.id.floating_summary_SimplifyButton).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        TextView textView = popupWindow.getContentView().findViewById(R.id.floating_summary_MainTextView);
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

        view.findViewById(R.id.simplify_text_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextInputEditText simplifyEditText= getView().findViewById(R.id.simplify_simplifyTextInputEditText);
                CharSequence text=simplifyEditText.getText().toString();
                if(text!=null){
                    toggleState(-1);
                    new LLMInteraction().generateSummarizedText(requireContext(), text, new LLMInteraction.ResponseCallback() {
                        @Override
                        public void onSuccess(String summarizedText) {
                            Summary summary = new Summary((String) text, summarizedText);
                            mSummaryViewModel.insertSummaries(summary);
                            sendProcessedText(summarizedText);
                            toggleState(100);
                            simplifyEditText.setText("");

                        }

                        @Override
                        public void onError(Exception exception) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                            builder.setMessage(exception.getMessage())
                                    .setTitle("Error")
                                    .setPositiveButton("OK", (dialog, id) -> dialog.dismiss());
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }
                    });
                }
                else{

                }



            }
        });

        Button button = view.findViewById(R.id.simplify_photo_button);
        button.setOnClickListener(v -> {
            if (pickMedia != null) {
                pickMedia.launch(new PickVisualMediaRequest.Builder().setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build());
            }
        });

        return view;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void toggleState(Integer progress) {
        if (simplifyTextButton != null && simplifyPhotoButton != null && progressIndicator != null) {
            simplifyTextButton.setEnabled(!simplifyTextButton.isEnabled());
            simplifyPhotoButton.setEnabled(!simplifyPhotoButton.isEnabled());

            if (progress >= 0 && progress <= 100) {
                progressIndicator.setProgressCompat(progress, true);
            } else {
                progressIndicator.setIndeterminate(true);
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSummaryViewModel = new ViewModelProvider(requireActivity()).get(SummaryViewModel.class);

        pickMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
                InputImage image;
                try {
                    image = InputImage.fromFilePath(requireContext(), uri);
                    recognizer.process(image)
                            .addOnSuccessListener(visionText -> {
                                String text = visionText.getText();
                                Log.d("SimplifyFragment", "Text from image: " + text);
                                Toast.makeText(getContext(), "Simplifying your text...", Toast.LENGTH_LONG).show();

                                toggleState(-1);
                                new LLMInteraction().generateSummarizedText(getContext(), text, new LLMInteraction.ResponseCallback() {
                                    @Override
                                    public void onSuccess(String summarizedText) {
                                        Summary summary = new Summary(text, summarizedText);
                                        mSummaryViewModel.insertSummaries(summary);
                                        toggleState(100);
                                    }

                                    @Override
                                    public void onError(Exception exception) {
                                        // Display an alert dialog with the error
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                        builder.setMessage(exception.getMessage())
                                                .setTitle("Error")
                                                .setPositiveButton("OK", null);
                                        AlertDialog dialog = builder.create();
                                        dialog.show();
                                        toggleState(0);
                                    }
                                });
                            })
                            .addOnFailureListener(e -> Log.e("SimplifyFragment", "Error processing image", e));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                System.out.println(uri);
            } else {
                System.out.println("No URI");
            }
        });
    }
    private void sendProcessedText(String processedText) {
        Intent intent = new Intent(Constants.ACTION_PROCESS_TEXT);
        intent.putExtra(Constants.EXTRA_PROCESSED_TEXT, processedText);
        requireContext().sendBroadcast(intent);
    }

}