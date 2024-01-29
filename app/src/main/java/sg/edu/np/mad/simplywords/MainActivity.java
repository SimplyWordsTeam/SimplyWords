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
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import sg.edu.np.mad.simplywords.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView mAppBar;

    // TODO: Add fragments for other menu items
    HashMap<Integer, Fragment> fragments = new HashMap<Integer, Fragment>() {{
        put(R.id.bottomAppBar_home, new HomeFragment());
        put(R.id.bottomAppBar_simplify, new SimplifyFragment());
        put(R.id.bottomAppBar_settings, new SettingsFragment());
    }};

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // Listens for images shared from other apps
        if (Intent.ACTION_SEND.equals(intent.getAction()) && intent.getType() != null && intent.getType().startsWith("image/")) {
            Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
            Bundle bundle = new Bundle();
            bundle.putParcelable("imageUri", imageUri);
            Objects.requireNonNull(fragments.get(R.id.bottomAppBar_simplify)).setArguments(bundle);

            getSupportFragmentManager().beginTransaction().remove(Objects.requireNonNull(fragments.get(R.id.bottomAppBar_simplify))).commitNow();

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container_view, Objects.requireNonNull(fragments.get(R.id.bottomAppBar_simplify)))
                    .commit();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        checkOverlayPermission();

        // Set the default fragment to home page
        mAppBar = findViewById(R.id.main_navigation);
        mAppBar.setSelectedItemId(R.id.bottomAppBar_home);

        mAppBar.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (fragments.containsKey(id)) {
                Fragment fragment = fragments.get(id);
                assert fragment != null;
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container_view, fragment)
                        .commit();

            } else {
                Toast toast = Toast.makeText(MainActivity.this, item.getTitle() + " pressed", Toast.LENGTH_SHORT);
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
