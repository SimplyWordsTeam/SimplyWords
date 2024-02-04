package sg.edu.np.mad.simplywords;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        Preference onboardingPreference = findPreference("onboarding");
        assert onboardingPreference != null;
        onboardingPreference.setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(getActivity(), OnboardingActivity.class);
            intent.putExtra("skipIntro", true);
            startActivity(intent);
            return true;
        });

        Preference flushSharedPreferencesPreference = findPreference("flush_sharedpreferences");
        assert flushSharedPreferencesPreference != null;
        flushSharedPreferencesPreference.setOnPreferenceClickListener(preference -> {
            requireActivity().getSharedPreferences("userPreferences", Context.MODE_PRIVATE).edit().clear().apply();
            Toast.makeText(getContext(), "Successfully requested the cache to be deleted.", Toast.LENGTH_SHORT).show();
            return true;
        });

        // Updates the version number in the settings
        Preference versionPreference = findPreference("version");
        assert versionPreference != null;
        versionPreference.setSummary(BuildConfig.VERSION_NAME);

        return view;
    }
}