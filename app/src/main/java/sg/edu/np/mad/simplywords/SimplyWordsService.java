package sg.edu.np.mad.simplywords;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class SimplyWordsService extends Service {
    public SimplyWordsService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}