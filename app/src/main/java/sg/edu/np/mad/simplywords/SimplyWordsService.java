package sg.edu.np.mad.simplywords;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;
import androidx.lifecycle.ViewModelProvider;

import sg.edu.np.mad.simplywords.model.Summary;
import sg.edu.np.mad.simplywords.repo.SummaryRepository;
import sg.edu.np.mad.simplywords.util.LLMInteraction;
import sg.edu.np.mad.simplywords.viewmodel.SummaryViewModel;

public class SimplyWordsService extends Service {
    SummaryOverlay overlay;
    private SummaryRepository mRepository;

    SummaryViewModel mSummaryViewModel;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    public void onCreate() {
        super.onCreate();

        startCustomForeground();
        mRepository= new SummaryRepository(getApplication());
        // Registers the broadcast receiver to receive text from other apps
        IntentFilter overlayDestructionFilter = new IntentFilter("overlay_destroyed");
        registerReceiver(receiver, overlayDestructionFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Unregisters the broadcast receiver when the service is destroyed
        unregisterReceiver(receiver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);



        // Creates and shows the overlay
        overlay = new SummaryOverlay(this);
        overlay.showOverlay();
        overlay.updateProgress(-1);
        String originalText = intent.getStringExtra(Constants.EXTRA_ORIGINAL_TEXT);
        new LLMInteraction().generateSummarizedText(this, originalText, new LLMInteraction.ResponseCallback() {
            @Override
            public void onSuccess(String summarizedText) {
                Summary summary = new Summary((String) summarizedText, summarizedText);
                mRepository.insertSummaries(summary);
                overlay.updateText(summarizedText);
                overlay.updateProgress(100);
            }

            @Override
            public void onError(Exception exception) {
                overlay.updateText("Sorry, something went wrong. Try again.");
                overlay.updateProgress(100);
            }
        });
        return START_NOT_STICKY;
    }

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

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("overlay_destroyed".equals(intent.getAction())) {
                stopSelf();
            }
        }
    };
}