package sg.edu.np.mad.simplywords;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import sg.edu.np.mad.simplywords.model.Summary;
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

            Toast.makeText(this, "Simplifying your text...", Toast.LENGTH_LONG).show();
            String URL = "https://api.openai.com/v1/chat/completions";

            // Creates the JSON object to be sent to the API
            JSONObject data = new JSONObject();
            try {
                data.put("model", "gpt-3.5-turbo-1106");
                data.put(
                        "messages",
                        new JSONArray()
                                .put(
                                        new JSONObject()
                                                .put("role", "system")
                                                .put("content", "You are part of a system and you are tasked to simplify language of a text input. You are expected to give an output of the summarized text, and the output will be directly shown to the user; therefore, do not give any comments, remarks, opinions, or analysis on the task you are given to do.\n" +
                                                        "\n" +
                                                        "The target audience that you will be rewriting the text input for are seniors who are not tech literate. They have trouble understanding user interfaces because of the complex language used in them, the confusing layouts, and complicated iconography. Your job is to take complicated phrases in the text input and simplify them such that it is easy for them to understand. Jargon, technical terms, and metaphors are common bottlenecks for the target audience. Avoid changing information that would change the meaning of the text input, but synonyms are acceptable.\n" +
                                                        "\n" +
                                                        "The following demonstrate the original (O) and simplified (S) variants of sentences you are expected to replicate:\n" +
                                                        "\n" +
                                                        "O: Company A was hit overnight by a Distributed Denial of Service (DDoS) attack, upsetting millions of visitors and causing stocks to crash. Company B, an independent cybersecurity analyst company, has mentioned that DDoS attacks are more common in recent days and many mission-critical organizations have experienced an increasing occurrence of such attacks.\n" +
                                                        "S: Company A experienced a cyberattack which overwhelms its servers that led to millions of customers losing confidence in them and having a poorer reputation. Company B, which looks at how cyberattacks work, has said that the cyberattack has been more frequent recently, and many important companies are experiencing it in recent times.\n" +
                                                        "\n" +
                                                        "O: Click on \"Find out how\" and sign in. Once done, navigate to the Services section in the user portal and tap on Request assistance.\n" +
                                                        "S: Find the words \"Find out how\" and tap on it. Follow the instructions on the screen, typing out all the required information, then sign in. After you are signed in, scroll and find the Services section, then find the words \"Request assistance\". Tap on it to continue.")
                                )
                                .put(
                                        new JSONObject()
                                                .put("role", "user")
                                                .put("content", text)
                                )
                );
                data.put("temperature", 1.25);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

            SummaryViewModel mSummaryViewModel = new ViewModelProvider(this).get(SummaryViewModel.class);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL, data, response ->
            {
                try {
                    String summarizedText = response.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");

                    Summary summary = new Summary((String) text, summarizedText);
                    mSummaryViewModel.insertSummaries(summary);

                    sendProcessedText(summarizedText);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }, error -> {
                // Display an alert dialog with the error
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(error.getMessage())
                        .setTitle("Error")
                        .setPositiveButton("OK", (dialog, id) -> finish());
                AlertDialog dialog = builder.create();
                dialog.show();
            }) {
                public Map<String, String> getHeaders() {
                    Map<String, String> params = new HashMap<>();
                    params.put("Authorization", "Bearer " + "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");
                    return params;
                }
            };

            request.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            NetworkQueue.getInstance(this).addToRequestQueue(request);
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