package sg.edu.np.mad.simplywords.util;

import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import sg.edu.np.mad.simplywords.NetworkQueue;

public class LLMInteraction {
    public interface ResponseCallback {
        void onSuccess(String summarizedText);

        void onError(Exception exception);
    }

    public void generateSummarizedText(Context context, CharSequence originalText, ResponseCallback callback) {
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
                                            .put("content", originalText)
                            )
            );
            data.put("temperature", 1.25);
        } catch (JSONException e) {
            Log.d("LLMInteraction", "Error while creating JSON object: " + e.getMessage());
            callback.onError(e);
        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL, data, response ->
        {
            try {
                String summarizedText = response.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
                callback.onSuccess(summarizedText);
            } catch (JSONException e) {
                Log.d("LLMInteraction", "Error while performing request: " + e.getMessage());
                callback.onError(e);
            }
        }, callback::onError) {
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", "Bearer " + "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");
                return params;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 2, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        NetworkQueue.getInstance(context).addToRequestQueue(request);
    }
}
