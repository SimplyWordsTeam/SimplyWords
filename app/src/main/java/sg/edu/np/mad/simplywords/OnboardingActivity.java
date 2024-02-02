package sg.edu.np.mad.simplywords;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

import sg.edu.np.mad.simplywords.databinding.ActivityOnboardingBinding;
import sg.edu.np.mad.simplywords.onboarding.AdjustFragment;
import sg.edu.np.mad.simplywords.onboarding.WelcomeFragment;

public class OnboardingActivity extends AppCompatActivity {
    ArrayList<Fragment> fragments = new ArrayList<Fragment>() {{
        add(new WelcomeFragment());
        add(new AdjustFragment());
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityOnboardingBinding binding = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Since the activity begins with the first fragment, we can hide the previous button
        Button previousButton = findViewById(R.id.onboarding_previous);
        previousButton.setVisibility(View.GONE);

        // Sets the first fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.onboarding_fragment_container_view, fragments.get(0))
                .commit();

        // Handle navigating through the fragments with the buttons
        Button nextButton = findViewById(R.id.onboarding_next);
        nextButton.setOnClickListener(v -> {
            previousButton.setVisibility(View.VISIBLE);
            int currentFragmentIndex = fragments.indexOf(getSupportFragmentManager().findFragmentById(R.id.onboarding_fragment_container_view));
            Log.d("OnboardingActivity:nextButtonPressed", "Current fragment index: " + currentFragmentIndex);
            if (getSupportFragmentManager().findFragmentById(R.id.onboarding_fragment_container_view) instanceof AdjustFragment) {
                AdjustFragment fragment = (AdjustFragment) getSupportFragmentManager().findFragmentById(R.id.onboarding_fragment_container_view);
                assert fragment != null;
                fragment.saveUserPreferences();
            }
            if (currentFragmentIndex < fragments.size() - 1) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.onboarding_fragment_container_view, fragments.get(currentFragmentIndex + 1))
                        .commit();
            } else {
                finish();
            }
        });
        previousButton.setOnClickListener(v -> {
            int currentFragmentIndex = fragments.indexOf(getSupportFragmentManager().findFragmentById(R.id.onboarding_fragment_container_view));
            Log.d("OnboardingActivity:previousButtonPressed", "Current fragment index: " + currentFragmentIndex);
            if (currentFragmentIndex > 0) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.onboarding_fragment_container_view, fragments.get(currentFragmentIndex - 1))
                        .commit();
            }
            if (currentFragmentIndex - 1 == 0) {
                previousButton.setVisibility(View.GONE);
            }
        });
    }
}