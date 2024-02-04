package sg.edu.np.mad.simplywords.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import sg.edu.np.mad.simplywords.BuildConfig;
import sg.edu.np.mad.simplywords.Constants;
import sg.edu.np.mad.simplywords.NetworkQueue;

public class LLMInteraction {
    String TAG = "LLMInteraction";

    public void generateSummarizedText(Context context, CharSequence originalText, ResponseCallback callback) {
        String API_KEY = BuildConfig.OPENAI_KEY;

        // Creates the JSON object to be sent to the API
        SharedPreferences preferences = context.getSharedPreferences("userPreferences", Context.MODE_PRIVATE);
        String prompt = preferences.getBoolean("has_configured", false) ? Constants.configureSummarizePrompt(preferences) : Constants.LLM_SUMMARIZE_PROMPT;

        JSONObject data = new JSONObject();
        try {
            data.put("model", "gpt-3.5-turbo-1106");
            data.put(
                    "messages",
                    new JSONArray()
                            .put(
                                    new JSONObject()
                                            .put("role", "system")
                                            .put("content", prompt)
                            )
                            .put(
                                    new JSONObject()
                                            .put("role", "user")
                                            .put("content", originalText)
                            )
            );
            data.put("temperature", 1.25);
            Log.d(TAG, "JSON object created: " + data);
        } catch (JSONException e) {
            Log.d(TAG, "Error while creating JSON object: " + e.getMessage());
            callback.onError(e);
        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, Constants.LLM_ENDPOINT, data, response ->
        {
            try {
                String summarizedText = response.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");

                if (summarizedText.contains("[ERRAMBIG]")) {
                    String technicalReason = summarizedText.substring(summarizedText.indexOf("[REASON_TECHNICAL] = ") + 21, summarizedText.indexOf("[REASON] = "));
                    String friendlyReason = summarizedText.substring(summarizedText.indexOf("[REASON] = ") + 11);
                    Log.e(TAG, "ERRAMBIG response: " + technicalReason);
                    throw new Exception(friendlyReason);
                }

                callback.onSuccess(summarizedText);
            } catch (Exception e) {
                Log.d(TAG, "Error while performing request: " + e.getMessage());
                callback.onError(e);
            }
        }, callback::onError) {
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", "Bearer " + API_KEY);
                return params;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        NetworkQueue.getInstance(context).addToRequestQueue(request, context);
    }

    public void generateTranslatedText(Context context, CharSequence originalText, String language, ResponseCallback callback) {
        String API_KEY = BuildConfig.OPENAI_KEY;

        // Creates the JSON object to be sent to the API
        JSONObject data = new JSONObject();
        try {
            data.put("model", "gpt-4-0125-preview");
            data.put(
                    "messages",
                    new JSONArray()
                            .put(
                                    new JSONObject()
                                            .put("role", "system")
                                            .put("content", Constants.configureTranslatePrompt(language))
                            )
                            .put(
                                    new JSONObject()
                                            .put("role", "user")
                                            .put("content", originalText)
                            )
            );
            data.put("temperature", 1.25);
            Log.d(TAG, "JSON object created: " + data);
        } catch (JSONException e) {
            Log.d(TAG, "Error while creating JSON object: " + e.getMessage());
            callback.onError(e);
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, Constants.LLM_ENDPOINT, data, response ->
        {
            try {
                String translatedText = response.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");

                callback.onSuccess(translatedText);
            } catch (Exception e) {
                Log.d(TAG, "Error while performing request: " + e.getMessage());
                callback.onError(e);
            }
        }, callback::onError) {
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", "Bearer " + API_KEY);
                return params;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        NetworkQueue.getInstance(context).addToRequestQueue(request, context);
    }

    public interface ResponseCallback {
        void onSuccess(String summarizedText);

        void onError(Exception exception);
    }
}
