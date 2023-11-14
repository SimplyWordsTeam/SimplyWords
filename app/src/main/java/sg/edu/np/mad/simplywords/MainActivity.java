package sg.edu.np.mad.simplywords;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import sg.edu.np.mad.simplywords.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        checkOverlayPermission();

        // Set the default fragment to home page
        BottomNavigationView mAppBar = findViewById(R.id.main_navigation);
        mAppBar.setSelectedItemId(R.id.bottomAppBar_home);

        HashMap<Integer, Fragment> fragments = new HashMap<>();
        fragments.put(R.id.bottomAppBar_home, new HomePageFragment());
        // TODO: Add fragments for other menu items
        mAppBar.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (fragments.containsKey(id)) {
                Fragment fragment = fragments.get(id);
                assert fragment != null;
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container_view, fragment)
                        .commit();
            } else {
                String message;
                if (id == R.id.bottomAppBar_simplify) {
                    message = "Simplify selected";
                } else if (id == R.id.bottomAppBar_home) {
                    message = "Home selected";
                } else {
                    message = "Settings selected";
                }

                Toast toast = Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT);
                toast.show();
            }
            return true;
        });
    }

    public void checkOverlayPermission() {
        AtomicBoolean hasPermission = new AtomicBoolean(false);

        ActivityResultLauncher<Intent> overlayPermissionLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (!Settings.canDrawOverlays(this)) {
                        Log.d("MainActivity", "Overlay permission: Not Allowed");
                    } else {
                        Log.d("MainActivity", "Overlay permission: OK");
                        hasPermission.set(true);
                    }
                }
        );

        if (!Settings.canDrawOverlays(this)) {
            Log.d("MainActivity", "Overlay permission: Not Allowed");
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
            Log.d("MainActivity", "Check OverlayPermission: OK");
            hasPermission.set(true);
        }

        hasPermission.get();
    }
}
