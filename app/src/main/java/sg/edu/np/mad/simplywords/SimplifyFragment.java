package sg.edu.np.mad.simplywords;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import sg.edu.np.mad.simplywords.adapter.SummaryAdapter;
import sg.edu.np.mad.simplywords.model.MutableLong;
import sg.edu.np.mad.simplywords.model.Summary;
import sg.edu.np.mad.simplywords.util.LLMInteraction;
import sg.edu.np.mad.simplywords.viewmodel.SummaryViewModel;

public class SimplifyFragment extends Fragment {
    ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
    ActivityResultLauncher<String> requestCameraPermissionLauncher;
    ActivityResultLauncher<Intent> takePictureLauncher;
    ActivityResultLauncher<Intent> requestStoragePermissionLauncher;
    String  currentImagePath;// This string stores the path of the photo that is being taken by the user through the camera. It ise reset to null after it is processed.
    CameraManager cameraManager;
    Button simplifyTextButton;
    Button simplifyPhotoButton;
    Button simplifyCameraPhotoButton;
    Button recentActivitySortButton;
    Spinner recentsPeriodFilterSpinner;
    RecyclerView recentsActivityRecyclerView;
    Boolean hasCamera;
    Boolean recentsIsLatestToEarliest;//  current sorting order for recents
    Integer recentsPeriodFilter;// current period filter for recents
    LinearProgressIndicator progressIndicator;
    private SummaryViewModel mSummaryViewModel;
    SummaryAdapter summaryAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_simplify, container, false);
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        //register the ActivityResult to request for camera permission
        requestCameraPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (isGranted) {
                        Log.d("SimplifyFragment", "Camera permission granted");
                        // Permission is granted. Continue with camera operation.
                    } else {
                        Log.d("SimplifyFragment", "Camera permission denied");
                        // Permission denied. Consider guiding the user to app settings.
                    }
                });

        //register the ActivityResultsContract to launch the camera
        takePictureLauncher=registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),result->{
            Log.d("SimplifyFragment", "takePictureLauncher: received call back");
            if (result.getResultCode() == Activity.RESULT_OK ) {
                Log.d("SimplifyFragment", "takePictureLauncher: operation was successful");

                try{
                    File imageFile = new File(currentImagePath);
                    Log.d("SimplifyFragment", "takePictureLauncher: got image file...");
                    Bitmap imageBitMap= BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                    Log.d("SimplifyFragment", "takePictureLauncher: converted file to bitmap...");

                    try {
                        InputImage image = InputImage.fromBitmap(imageBitMap,0);
                        Log.d("SimplifyFragment", "takePictureLauncher: converted BitMap to mlkit InputImage..."+image);

                        recognizer.process(image).addOnSuccessListener(new OnSuccessListener<Text>() {
                            @Override
                            public void onSuccess(Text visionText) {
                                Log.d("SimplifyFragment", "takePictureLauncher: Successfully recognized text from image...");

                                String text=visionText.getText();
                                Log.d("SimplifyFragment", "Text from image: " + text);

                                Toast.makeText(getContext(), getString(R.string.simplifying_text), Toast.LENGTH_LONG).show();

                                toggleState(-1);
                                new LLMInteraction().generateSummarizedText(getContext(), text, new LLMInteraction.ResponseCallback() {
                                    @Override
                                    public void onSuccess(String summarizedText) {
                                        Log.d("SimplifyFragment", "takePictureLauncher: successfully generating summarised text");
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

                            }
                        });
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                }catch (NullPointerException e){
                    throw new RuntimeException(e);
                }
                currentImagePath=null;
            }
        });


        //If the user has a camera, hasCamera is set to true. Otherwise false.
        hasCamera = checkCameraHardware(getContext());

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


        recentsPeriodFilterSpinner = view.findViewById(R.id.simplify_RecentActivitySpinner);
        ArrayAdapter<CharSequence> recentsFilterAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.time_filter_options, android.R.layout.simple_spinner_item);
        recentsFilterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        recentsPeriodFilterSpinner.setAdapter(recentsFilterAdapter);
        recentsPeriodFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                recentsPeriodFilter=position;
                refreshRecentsSort();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        recentsPeriodFilterSpinner.setSelection(0);

        // Inflate the layout for this fragment
        recentsActivityRecyclerView = view.findViewById(R.id.simplify_RecentActivityRecyclerView);
        summaryAdapter = new SummaryAdapter(new SummaryAdapter.SummaryDiff());



        //assign the views to their holders as a class attribute
        simplifyTextButton = view.findViewById(R.id.simplify_text_button);
        simplifyPhotoButton = view.findViewById(R.id.simplify_photo_button);
        simplifyCameraPhotoButton=view.findViewById(R.id.simplify_camera_photo_button);
        progressIndicator = view.findViewById(R.id.simplify_progress);
        recentActivitySortButton=view.findViewById(R.id.simplify_RecentActivitySortButton);

        recentsIsLatestToEarliest=Boolean.FALSE;

        recentActivitySortButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recentsIsLatestToEarliest=!recentsIsLatestToEarliest;
                refreshRecentsSort();
                recentActivitySortButton.setSelected(!recentsIsLatestToEarliest);
                if(recentsIsLatestToEarliest){
                    recentActivitySortButton.setBackgroundResource(R.drawable.baseline_filter_list_24_inverted);
                }else{
                    recentActivitySortButton.setBackgroundResource(R.drawable.baseline_filter_list_24);
                }

            }
        });
//        disable the camera button if no camera is detected
        if(!hasCamera){
            simplifyCameraPhotoButton.setEnabled(false);
        }



        // Configure the adapter to show the popup window when the user taps on a summary
        summaryAdapter.setOnClickListener((position, model) -> {
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

        simplifyCameraPhotoButton.setOnClickListener(v -> {
            Log.d("SimplifyFragment", "Running checkCameraPermission()...");
            checkCameraPermission();

            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                Log.d("SimplifyFragment", "Permission Granted");


                try {
                    Log.d("SimplifyFragment", "Instantiating image file");
                    File photoFile = createImageFile();  // Create the file for the photo
                    Uri photoURI = FileProvider.getUriForFile(getActivity(),
                            getActivity().getApplicationContext().getPackageName() + ".provider",
                            photoFile);
                    Log.d("SimplifyFragment", "Setting intents");
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                    if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                        Log.d("SimplifyFragment", "Launching Camera...");
                        takePictureLauncher.launch(intent); // Launch the camera activity
                    } else {
                        Log.d("SimplifyFragment", "No app to handle camera intent");
                    }
                } catch (IOException exception) {
                    Log.d("SimplifyFragment", "Failed to create image file", exception);
                }
            } else {
                Log.d("SimplifyFragment", "Camera permission not granted");
            }
        });


        return view;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void toggleState(Integer progress) {
        if(hasCamera){
            if (simplifyTextButton != null && simplifyPhotoButton != null && simplifyCameraPhotoButton!=null&& progressIndicator != null) {
                simplifyTextButton.setEnabled(!simplifyTextButton.isEnabled());
                simplifyPhotoButton.setEnabled(!simplifyPhotoButton.isEnabled());
                simplifyCameraPhotoButton.setEnabled(!simplifyCameraPhotoButton.isEnabled());
                if (progress >= 0 && progress <= 100) {
                    progressIndicator.setProgressCompat(progress, true);
                } else {
                    progressIndicator.setIndeterminate(true);
                }
            }
        }else{
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

    }
    /** Check if this device has a camera */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }
    public void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.d("SimplifyFragment", "Camera permission: Not Allowed");
            requestCameraPermission();
        } else {
            Log.d("SimplifyFragment", "Camera permission: OK");
            // Camera permission is already granted, proceed with camera operation.
        }
    }
    public void requestCameraPermission(){
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package"+ getActivity().getPackageName()));
        requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
    }

    public File createImageFile() throws IOException{
        String timeStamp=new SimpleDateFormat("yyyyMMdd_HHmmss",Locale.getDefault()).format(new Date());
        String imageFileName= "SimplyWords_"+timeStamp+"_";
        File storageDir=getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image =File.createTempFile(imageFileName,".jpg",storageDir);
        currentImagePath=image.getAbsolutePath();
        return image;
    }
    public void refreshRecentsSort(){

        recentsActivityRecyclerView.setAdapter(summaryAdapter);
        recentsActivityRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mSummaryViewModel.getAllSummaries().observe(getViewLifecycleOwner(), summaries -> {
            Log.d("HistoryFragment", "Summaries size: " + summaries.size());
            MutableLong startOfPeriodMutableLong=new MutableLong();
            MutableLong endOfPeriodMutableLong=new MutableLong();

            if(recentsPeriodFilter==0){ //Current Selected filter is this week

                //First day of the current week
                Calendar calendar=Calendar.getInstance();
                calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                startOfPeriodMutableLong.setLong(calendar.getTimeInMillis());

                //Last Day of the current Week
                calendar.add(Calendar.WEEK_OF_YEAR, 1);
                calendar.add(Calendar.MILLISECOND, -1);
                endOfPeriodMutableLong.setLong(calendar.getTimeInMillis());
            } else if (recentsPeriodFilter==1) {//Current Selected filter is Last 3 Weeks

                //First day of the week 3 weeks ago.
                Calendar calendar=Calendar.getInstance();
                calendar.add(Calendar.WEEK_OF_YEAR,-3);
                calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                startOfPeriodMutableLong.setLong(calendar.getTimeInMillis());

                //Last day of last week
                calendar=Calendar.getInstance();
                calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                calendar.add(Calendar.MILLISECOND,-1);
                endOfPeriodMutableLong.setLong(calendar.getTimeInMillis());



            }else if (recentsPeriodFilter==2){ //Current Selected filter is Last 3 Months

                //First day of the month 3 months ago
                Calendar calendar=Calendar.getInstance();
                calendar.add(Calendar.MONTH,-3);
                calendar.set(Calendar.DAY_OF_MONTH,1);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                startOfPeriodMutableLong.setLong(calendar.getTimeInMillis());

                //Last Day of last month
                calendar=Calendar.getInstance();
                calendar.set(Calendar.DAY_OF_MONTH,1);
                calendar.add(Calendar.MILLISECOND,-1);
                endOfPeriodMutableLong.setLong(calendar.getTimeInMillis());
            } else if (recentsPeriodFilter==3) {
                summaryAdapter.submitList(summaries);
                return;
            } else{
                Log.d("SimplifyFragment", "Period filter: option index not within range ");
            }

            List<Summary> filteredSummaries = new ArrayList<>();

            for (Summary summary: summaries) {
                if(summary.getCreatedAt()>=startOfPeriodMutableLong.getLong() && summary.getCreatedAt() <= endOfPeriodMutableLong.getLong()){
                    filteredSummaries.add(summary);
                }

            }
            if (!recentsIsLatestToEarliest){
                Collections.reverse(filteredSummaries);
            }
            summaryAdapter.submitList(filteredSummaries);
        });


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