package sg.edu.np.mad.simplywords;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.util.concurrent.atomic.AtomicBoolean;

import sg.edu.np.mad.simplywords.model.Summary;
import sg.edu.np.mad.simplywords.util.LLMInteraction;
import sg.edu.np.mad.simplywords.viewmodel.SummaryViewModel;

public class SummaryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        // Fetch the highlighted text then start the service to display the overlay
        CharSequence text = getIntent().getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT);
        boolean readOnly = getIntent().getBooleanExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, false);
        Log.d("MainActivity", "Text: " + text + " Read Only: " + readOnly);

        if (text != null) {
            startService();

            SummaryViewModel mSummaryViewModel = new ViewModelProvider(this).get(SummaryViewModel.class);
            Toast.makeText(this, "Simplifying your text...", Toast.LENGTH_LONG).show();
            new LLMInteraction().generateSummarizedText(this, text, new LLMInteraction.ResponseCallback() {
                @Override
                public void onSuccess(String summarizedText) {
                    Summary summary = new Summary((String) text, summarizedText);
                    mSummaryViewModel.insertSummaries(summary);

                    sendProcessedText(summarizedText);
                }

                @Override
                public void onError(Exception exception) {
                    // Display an alert dialog with the error
                    AlertDialog.Builder builder = new AlertDialog.Builder(SummaryActivity.this);
                    builder.setMessage(exception.getMessage())
                            .setTitle("Error")
                            .setPositiveButton("OK", (dialog, id) -> finish());
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            });
        }
    }

    public boolean checkOverlayPermission() {
        AtomicBoolean hasPermission = new AtomicBoolean(false);

        ActivityResultLauncher<Intent> overlayPermissionLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (!Settings.canDrawOverlays(this)) {
                        Log.d("SummaryActivity", "Overlay permission: Not Allowed");
                    } else {
                        Log.d("SummaryActivity", "Overlay permission: OK");
                        hasPermission.set(true);
                    }
                }
        );

        if (!Settings.canDrawOverlays(this)) {
            Log.d("SummaryActivity", "Overlay permission: Not Allowed");
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.permission_title)
                    .setMessage(R.string.permission_message)
                    .setPositiveButton(android.R.string.ok, (dialog, id) -> {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                        overlayPermissionLauncher.launch(intent);
                        dialog.dismiss();
                    });

            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            Log.d("SummaryActivity", "Check OverlayPermission: OK");
            hasPermission.set(true);
        }

        return hasPermission.get();
    }

    public void startService() {
        boolean hasOverlayPermission = checkOverlayPermission();
        if (hasOverlayPermission) {
            Intent intent = new Intent(this, SimplyWordsService.class);
            startForegroundService(intent);
        }
    }

    private void sendProcessedText(String processedText) {
        Intent intent = new Intent(Constants.ACTION_PROCESS_TEXT);
        intent.putExtra(Constants.EXTRA_PROCESSED_TEXT, processedText);
        sendBroadcast(intent);
        finish();
    }
}