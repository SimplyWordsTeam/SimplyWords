package sg.edu.np.mad.simplywords;

import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;

import java.util.ArrayList;

public class StylingFragment extends Fragment {

    ArrayList<Integer> fonts=new ArrayList<Integer>();
    Typeface typeFace;
    Integer themeId;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view=inflater.inflate(R.layout.fragment_adjust_styling,container,false);


        Slider fontSizeSlider=view.findViewById(R.id.adjust_styling_fragment_fontSize_slider);
        Spinner fontSpinner=view.findViewById(R.id.adjust_styling_fragment_font_spinner);
        TextView textStylingPreviewTextView=view.findViewById(R.id.text_styling_preview);
        MaterialButton applyButton=view.findViewById(R.id.adjust_styling_fragment_apply_button);
        ArrayAdapter<CharSequence> fontSpinnerAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.font_options, android.R.layout.simple_spinner_item);
        fontSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fontSpinner.setAdapter(fontSpinnerAdapter);
        fontSpinner.setSelection(0);
        typeFace=ResourcesCompat.getFont(getContext(),R.font.lexend);
        fontSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position==0){
                    Typeface typeFace= ResourcesCompat.getFont(getContext(),R.font.lexend);
                    themeId=  R.style.Theme_SimplyWords_Lexend;
                    textStylingPreviewTextView.setTypeface(typeFace);
                } else if (position==1) {
                    Typeface typeFace= ResourcesCompat.getFont(getContext(),R.font.roboto);
                    themeId=  R.style.Theme_SimplyWords_Roboto;
                    textStylingPreviewTextView.setTypeface(typeFace);
                } else if (position==2) {
                    Typeface typeFace= ResourcesCompat.getFont(getContext(),R.font.tinos);
                    themeId=  R.style.Theme_SimplyWords_Tinos;
                    textStylingPreviewTextView.setTypeface(typeFace);
                }else if (position==3) {
                    Typeface typeFace= ResourcesCompat.getFont(getContext(),R.font.firasans);
                    themeId=  R.style.Theme_SimplyWords_FiraSans;
                    textStylingPreviewTextView.setTypeface(typeFace);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().setTheme(themeId);
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_view,new SettingsFragment())
                        .commit();
            }
        });

        return view;
    }
}
