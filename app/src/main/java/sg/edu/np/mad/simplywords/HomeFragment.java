package sg.edu.np.mad.simplywords;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

public class HomeFragment extends Fragment {

    ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
    MaterialCardView simplyWordsCardView ;
    MaterialCardView settingsCardView ;



    public HomeFragment(){
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pickMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                System.out.println(uri);
            } else {
                System.out.println("No URI");
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        view.findViewById(R.id.homePage_TextSimplifier_MaterialCardView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container_view, new SimplifyFragment())
                        .commit();
                BottomNavigationView mAppBar= view.getRootView().findViewById(R.id.main_navigation);
                mAppBar.setSelectedItemId(R.id.bottomAppBar_simplify);
            }
        });
                return view;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

}