package sg.edu.np.mad.simplywords;

import android.app.Application;

import com.google.android.material.color.DynamicColors;

public class SimplyWordsApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Applies Material You's Dynamic Color if applicable.
        DynamicColors.applyToActivitiesIfAvailable(this);




    }
}
