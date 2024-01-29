package sg.edu.np.mad.simplywords;

import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
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

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;
import java.util.Objects;

import sg.edu.np.mad.simplywords.adapter.SummaryAdapter;
import sg.edu.np.mad.simplywords.model.Summary;
import sg.edu.np.mad.simplywords.util.LLMInteraction;
import sg.edu.np.mad.simplywords.viewmodel.SummaryViewModel;

public class SimplifyFragment extends Fragment {
    ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
    Button simplifyTextButton;
    Button simplifyPhotoButton;
    LinearProgressIndicator progressIndicator;
    private SummaryViewModel mSummaryViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_simplify, container, false);

        // Handle image sharing from other apps
        Uri imageUri;
        try {
            assert getArguments() != null;
            imageUri = getArguments().getParcelable("imageUri");
        } catch (AssertionError e) {
            imageUri = null;
        }
        Log.d("SimplifyFragment", "Image URI: " + imageUri);
        if (imageUri != null) {
            TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
            InputImage image;
            try {
                image = InputImage.fromFilePath(requireContext(), imageUri);
                recognizer.process(image)
                        .addOnSuccessListener(visionText -> {
                            String text = visionText.getText();
                            Log.d("SimplifyFragment", "Text from image: " + text);
                            Toast.makeText(getContext(), getString(R.string.simplifying_text), Toast.LENGTH_LONG).show();

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
        }

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

        // Configure the adapter to show the popup window when the user taps on a summary
        adapter.setOnClickListener((position, model) -> {
            // Inflate the layout of the popup window
            LayoutInflater popupInflater = (LayoutInflater) requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View popupView = popupInflater.inflate(R.layout.floating_full_summary, null);

            // Create the popup window
            int width = LinearLayout.LayoutParams.MATCH_PARENT;
            int height = LinearLayout.LayoutParams.WRAP_CONTENT;
            boolean focusable = true; // Let taps outside the popup also dismiss it
            final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

            // Show the popup window
            popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

            TextView textView = popupWindow.getContentView().findViewById(R.id.floating_summary_MainTextView);
            textView.setText(model.getSummarizedText());

            // Toggle between the original and simplified text when the user interacts with the tab layout
            ((TabLayout) popupWindow.getContentView().findViewById(R.id.floating_summary_TabLayout)).addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    if (Objects.equals(tab.getText(), getString(R.string.original_text))) {
                        textView.setText(model.getOriginalText());
                    } else {
                        textView.setText(model.getSummarizedText());
                    }
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                }
            });

            // Dismiss the popup window when the close image is touched
            popupWindow.getContentView().findViewById(R.id.floating_summary_ExitImageView).setOnClickListener(imageView -> popupWindow.dismiss());
        });

        // Handle processing manually entered text
        view.findViewById(R.id.simplify_text_button).setOnClickListener(view1 -> {
            TextInputEditText simplifyEditText = view.findViewById(R.id.simplify_simplifyTextInputEditText);
            String text = Objects.requireNonNull(simplifyEditText.getText()).toString();
            if (text.trim().isEmpty()) {
                Toast.makeText(getContext(), getString(R.string.error_enter_text), Toast.LENGTH_SHORT).show();
                return;
            }

            toggleState(-1);
            new LLMInteraction().generateSummarizedText(requireContext(), text, new LLMInteraction.ResponseCallback() {
                @Override
                public void onSuccess(String summarizedText) {
                    Summary summary = new Summary(text, summarizedText);
                    mSummaryViewModel.insertSummaries(summary);
                    toggleState(100);
                    simplifyEditText.setText("");
                }

                @Override
                public void onError(Exception exception) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                    builder.setMessage(exception.getMessage())
                            .setTitle("Error")
                            .setPositiveButton("OK", (dialog, id) -> {
                                dialog.dismiss();
                                toggleState(0);
                            });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            });
        });

        // Launches the media picker to select an image to process if the user taps on the simplify photo button
        simplifyPhotoButton.setOnClickListener(v -> {
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
                                Toast.makeText(getContext(), getString(R.string.simplifying_text), Toast.LENGTH_LONG).show();

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
}