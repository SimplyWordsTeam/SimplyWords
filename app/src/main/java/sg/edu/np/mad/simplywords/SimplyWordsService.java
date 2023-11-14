package sg.edu.np.mad.simplywords;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

public class SimplyWordsService extends Service {

    SummaryOverlay overlay;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startCustomForeground();
        } else {
            startForeground(1, new Notification());
        }

        // Registers the broadcast receiver to receive text from other apps
        IntentFilter filter = new IntentFilter(Constants.ACTION_PROCESS_TEXT);
        registerReceiver(textReceiver, filter);

        // Creates and shows the overlay
        overlay = new SummaryOverlay(this);
        overlay.showOverlay();
        overlay.updateProgress(-1);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Unregisters the broadcast receiver when the service is destroyed
        unregisterReceiver(textReceiver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void startCustomForeground() {
        String CHANNEL_ID = "simplywords_service_channel";
        String CHANNEL_NAME = "SimplyWords Background Service";
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_MIN);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(channel);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(getString(R.string.notification_message))
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }

    private final BroadcastReceiver textReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Constants.ACTION_PROCESS_TEXT.equals(intent.getAction())) {
                String text = intent.getStringExtra(Constants.EXTRA_PROCESSED_TEXT);
                if (overlay != null) {
                    overlay.updateText(text);
                    overlay.updateProgress(100);
                }
            }
        }
    };
}