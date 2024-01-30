package sg.edu.np.mad.simplywords.onboarding;

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

import java.util.HashMap;
import java.util.Objects;

import sg.edu.np.mad.simplywords.R;

public class AdjustFragment extends Fragment {

    String TAG = "AdjustFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_adjust, container, false);

        // Makes the preview text scrollable
        TextView previewText = view.findViewById(R.id.adjust_preview);
        previewText.setMovementMethod(new ScrollingMovementMethod());

        // Listens for changes in the slider and updates the simplification level
        Slider simplificationSlider = view.findViewById(R.id.slider);
        TextView sliderLabel = view.findViewById(R.id.adjust_slider_label);
        HashMap<Float, String> simplificationLevels = new HashMap<Float, String>() {{
            put(0f, requireActivity().getString(R.string.very_simple));
            put(0.5f, requireActivity().getString(R.string.simple));
            put(1f, requireActivity().getString(R.string.detailed));
        }};
        simplificationSlider.addOnChangeListener((slider, value, fromUser) -> {
            Log.d(TAG, "Simplification level change to: " + simplificationLevels.get(value));
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

            topicsChipGroup.addView(chip);
        }

        return view;
    }

    public void saveUserPreferences() {
        // TODO: Update user preferences to include simplification level and topics
    }
}