package sg.edu.np.mad.simplywords;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.view.WindowCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import sg.edu.np.mad.simplywords.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private ActivityResultLauncher<Intent> overlayPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // If launched through the context menu, fetch the highlighted text
        CharSequence text = getIntent().getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT);
        boolean readOnly = getIntent().getBooleanExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, false);
        Log.d("MainActivity", "Text: " + text + " Read Only: " + readOnly);

        checkPermissions();
        startService();

        if (text != null) {
            Toast.makeText(this, "Starting network request...", Toast.LENGTH_SHORT).show();
            String URL = "https://api.openai.com/v1/chat/completions";
            JSONObject data = new JSONObject();
            try {
                data.put("model", "gpt-3.5-turbo");
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
                Log.d("MainActivity", "Data: " + data.toString());
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL, data,
                    response -> {
                        // Display an alert dialog with the response
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        try {
                            builder.setMessage(response.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content"))
                                    .setTitle("Response")
                                    .setPositiveButton("OK", null);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    },
                    error -> {
                        // Display an alert dialog with the error
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setMessage(error.getMessage())
                                .setTitle("Error")
                                .setPositiveButton("OK", null);
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

//        binding.toggleStartButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                Intent intent = new Intent(MainActivity.this, SimplyWordsService.class);
//                startService(intent);
//
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAnchorView(R.id.toggleStartButton)
//                        .setAction("Action", null).show();
//            }
//        });



        BottomNavigationView mAppBar= findViewById(R.id.main_navigation);
        mAppBar.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id==R.id.bottomAppBar_simplify){
                    Toast toast=Toast.makeText(MainActivity.this,"Simplify Selected",Toast.LENGTH_SHORT);
                    toast.show();
                }
                else if (id==R.id.bottomAppBar_home){
                    Toast toast=Toast.makeText(MainActivity.this,"Home Selected",Toast.LENGTH_SHORT);
                    toast.show();
                }
                else{
                    Toast toast=Toast.makeText(MainActivity.this,"Settings Selected",Toast.LENGTH_SHORT);
                    toast.show();
                }

                return true;
            }
        });
    }

    public void startService() {
        // check if the user has already granted
        // the Draw over other apps permission
        if (Settings.canDrawOverlays(this)) {
            // start the service based on the android version
            Intent intent = new Intent(this, SimplyWordsService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }


    public void startHomeActivity(){
        Intent


    }
    //Check if user has allowed  the overlay permission
    public void checkPermissions(){
        overlayPermissionLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (!Settings.canDrawOverlays(this)) {
                        Log.d("MainActivity","Check OverlayPermission: Not Allowed");
                    }
                    else{
                        Log.d("MainActivity","Check OverlayPermission: Allowed");
                    }
                }
        );
        if (!Settings.canDrawOverlays(this))
        {
            Log.d("MainActivity","Check OverlayPermission: Not Allowed");
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("We need permission to display over other screens. You will be told that SimplyWords can access and read content on your screen. This is a standard Android message for apps that can display over other apps. SimplyWords does not access or read any of your content.")
                    .setTitle("Permission required")
                    .setPositiveButton("OK", (dialog, id) -> {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:" + getPackageName()));
                        overlayPermissionLauncher.launch(intent);
                        dialog.dismiss();
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
        }else{
            Log.d("MainActivity","Check OverlayPermission: Allowed");
        }
    }

    public boolean isServiceRunning(){
        ActivityManager manager=(ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (SimplyWordsService.class.getName().equals(service.service)) {
                return true;
            }
        }
        return false;
    }
    

}
