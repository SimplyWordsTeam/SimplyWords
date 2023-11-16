package sg.edu.np.mad.simplywords;

import android.content.Context;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.progressindicator.LinearProgressIndicator;

public class SummaryOverlay extends AppCompatActivity {
    private final View view;
    private final WindowManager.LayoutParams params;
    private final WindowManager windowManager;

    public SummaryOverlay(Context context) {
        params = new WindowManager.LayoutParams(
                // Display the window on top of other application windows
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                // Add flags to alter the window appearance and behavior
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                // Make the underlying application window visible through any transparent parts
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.START;

        // Wrap the Material theme within the service context
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(context, R.style.Theme_SimplyWords);
        LayoutInflater layoutInflater = LayoutInflater.from(contextThemeWrapper);
        this.view = layoutInflater.inflate(R.layout.overlay_summary, null, false);
        this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        // Close the overlay when the close button is clicked
        view.findViewById(R.id.window_close).setOnClickListener(v -> hideOverlay());
    }

    public void showOverlay() {
        try {
            // Add the view if it's not already inflated or present in the window
            if (view.getWindowToken() == null || view.getParent() == null) {
                windowManager.addView(view, params);
            }
        } catch (Exception e) {
            Log.d("SummaryOverlay", e.toString());
        }
    }

    public void hideOverlay() {
        try {
            if (view.getWindowToken() != null && view.getParent() != null) {
                windowManager.removeView(view);
            }
        } catch (Exception e) {
            Log.d("SummaryOverlay", e.toString());
        }
    }

    public void updateText(String text) {
        if (view != null) {
            TextView textView = view.findViewById(R.id.overlay_summary_text);
            textView.setText(text);
        }
    }

    public void updateProgress(Integer progress) {
        LinearProgressIndicator progressIndicator = view.findViewById(R.id.overlay_summary_progress);
        if (progress >= 0 && progress <= 100) {
            progressIndicator.setProgressCompat(progress, true);
        } else {
            progressIndicator.setIndeterminate(true);
        }
    }
}


