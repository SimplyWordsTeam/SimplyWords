package sg.edu.np.mad.simplywords;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

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
