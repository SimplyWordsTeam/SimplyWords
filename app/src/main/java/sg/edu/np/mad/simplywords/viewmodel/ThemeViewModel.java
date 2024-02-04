package sg.edu.np.mad.simplywords.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ThemeViewModel extends ViewModel {

    private final MutableLiveData<Integer> currentTheme = new MutableLiveData<>();

    public void setTheme(int themeId) {
        currentTheme.setValue(themeId);
    }

    public LiveData<Integer> getTheme() {
        return currentTheme;
    }
}
