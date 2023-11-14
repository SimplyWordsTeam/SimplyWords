package sg.edu.np.mad.simplywords;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SummaryOverlay extends AppCompatActivity {
    private Context context;
    private View view;
    private WindowManager.LayoutParams params;
    private WindowManager windowManager;
    private LayoutInflater layoutInflater;
    public static final String ACTION_SHOW_TEXT = "sg.edu.np.mad.simplywords.SHOW_TEXT";
    public SummaryOverlay(Context context) {
        this.context = context;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            params = new WindowManager.LayoutParams(
                    // Shrink the window to wrap the content rather than filling the screen
                    WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
                    // Display it on top of other application windows
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    // Don't let it grab the input focus
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    // Make the underlying application window visible through any transparent parts
                    PixelFormat.TRANSLUCENT
            );
        }
        //Wrap material theme within the service context
        // Inside your Service
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(context,R.style.Theme_SimplyWords);
        LayoutInflater inflater = LayoutInflater.from(contextThemeWrapper);
        View yourOverlayView = inflater.inflate(R.layout.overlay_summary, null, false);
        this.view=yourOverlayView;
//        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        this.view = layoutInflater.inflate(R.layout.overlay_summary, null);
        view.findViewById(R.id.window_close).setOnClickListener(v -> {
            hide();
        });
        params.gravity = Gravity.TOP;
        this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }



    public void show() {
        try {
            // check if the view is already
            // inflated or present in the window
            if(view.getWindowToken()==null) {
                if(view.getParent()==null) {
                    windowManager.addView(view, params);
                }
            }
        } catch (Exception e) {
            Log.d("SummaryOverlay", e.toString());
        }
    }
    public void hide() {
        try {
            if (view.getWindowToken() != null) {
                if (view.getParent() != null) {
                    windowManager.removeView(view);
                }
            }
        } catch (Exception e) {
            Log.d("SummaryOverlay", e.toString());
        }
    }
    private BroadcastReceiver textReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_SHOW_TEXT.equals(intent.getAction())) {
                String text = intent.getStringExtra("EXTRA_TEXT");
                // Update your overlay's TextView with this text
                TextView textView = view.findViewById(R.id.overlay_summary_text);
                textView.setText(text);
                show(); // Make sure the overlay is visible
            }
        }
    };

    public void registerReceiver() {
        IntentFilter filter = new IntentFilter(ACTION_SHOW_TEXT);
        context.registerReceiver(textReceiver, filter);
    }

    public void unregisterReceiver() {
        context.unregisterReceiver(textReceiver);
    }
    public void updateText(String text){
        if(view!=null){
            TextView textView = view.findViewById(R.id.overlay_summary_text);
            textView.setText(text);
        }
    }
}


