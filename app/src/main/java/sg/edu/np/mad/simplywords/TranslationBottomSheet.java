package sg.edu.np.mad.simplywords;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputLayout;

public class TranslationBottomSheet extends BottomSheetDialogFragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.sheet_translation, container, false);

        Button translateButton = view.findViewById(R.id.sheet_translation_button);
        TextInputLayout languageInputLayout = view.findViewById(R.id.translation_language_text_input_layout);
        AutoCompleteTextView languageTextInput = view.findViewById(R.id.translation_language_text_input_edit_text);
        translateButton.setOnClickListener(v -> {
            if (languageTextInput.getText().toString().isEmpty()) {
                languageInputLayout.setError(getString(R.string.no_language_error));
            } else {
                languageInputLayout.setError(null);

                // Calls the method in the parent activity to translate the text
                ((SummaryDetailsActivity) requireActivity()).onTranslate(languageTextInput.getText().toString());

                languageTextInput.setText("");
                dismiss();
            }
        });

        return view;
    }
}
