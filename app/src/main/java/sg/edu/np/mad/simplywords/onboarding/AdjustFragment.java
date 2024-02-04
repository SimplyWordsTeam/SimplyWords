package sg.edu.np.mad.simplywords.onboarding;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.slider.Slider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

import sg.edu.np.mad.simplywords.Constants;
import sg.edu.np.mad.simplywords.R;

public class AdjustFragment extends Fragment {

    String TAG = "AdjustFragment";
    Integer selectedSimplificationLevel = 0;
    ArrayList<String> selectedTopics = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_adjust_simplification, container, false);

        // Makes the preview text scrollable
        TextView previewText = view.findViewById(R.id.adjust_preview);
        previewText.setMovementMethod(new ScrollingMovementMethod());

        // Listens for changes in the slider and updates the simplification level
        Slider simplificationSlider = view.findViewById(R.id.slider);
        TextView sliderLabel = view.findViewById(R.id.adjust_slider_label);
        HashMap<Float, String> simplificationLevels = new HashMap<Float, String>() {{
            put(0f, requireActivity().getString(R.string.very_simple));
            put(1f, requireActivity().getString(R.string.simple));
            put(2f, requireActivity().getString(R.string.detailed));
        }};
        simplificationSlider.addOnChangeListener((slider, value, fromUser) -> {
            Log.d(TAG, "Simplification level changed to: " + simplificationLevels.get(value));
            switch (Objects.requireNonNull(simplificationLevels.get(value))) {
                case "Very simple":
                    previewText.setText(R.string.very_simple_example);
                    break;
                case "Simple":
                    previewText.setText(R.string.simple_example);
                    break;
                case "Detailed":
                    previewText.setText(R.string.detailed_example);
                    break;
            }
            sliderLabel.setText(simplificationLevels.get(value));
            selectedSimplificationLevel = (int) value;
        });

        // Inflates the topics chip group with the topics
        ChipGroup topicsChipGroup = view.findViewById(R.id.topics_chips);
        String[] topics = getResources().getStringArray(R.array.topics);
        for (String topic : topics) {
            Chip chip = new Chip(requireContext());
            chip.setText(topic);
            chip.setCheckedIconVisible(true);
            chip.setCheckable(true);
            chip.setId(View.generateViewId());
            chip.setOnClickListener(v -> {
                if (chip.isChecked()) {
                    selectedTopics.add(topic);
                } else {
                    selectedTopics.remove(topic);
                }
            });

            topicsChipGroup.addView(chip);
        }

        return view;
    }

    public void saveUserPreferences() {
        SharedPreferences preferences = requireActivity().getSharedPreferences("userPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("has_configured", true);
        editor.putInt("simplification_level", selectedSimplificationLevel);
        editor.putStringSet("topics", new HashSet<>(selectedTopics));
        editor.apply();
    }
}