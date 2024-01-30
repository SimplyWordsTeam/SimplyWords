package sg.edu.np.mad.simplywords;

import android.content.Context;
import android.graphics.PixelFormat;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.sql.Time;
import java.time.Duration;

public class SummaryOverlay extends AppCompatActivity {
    private View view;
    private WindowManager.LayoutParams params;
    private WindowManager windowManager;

    public SummaryOverlay(Context context) {
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                // Display the window on top of other application windowss
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                // Add flags to alter the window appearance and behaviors
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL|
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                // Make the underlying application window visible through any transparent parts
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.CENTER;
        // Wrap the Material theme within the service context
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(context, R.style.Theme_SimplyWords);
        LayoutInflater layoutInflater = LayoutInflater.from(contextThemeWrapper);
        this.view = layoutInflater.inflate(R.layout.overlay_summary, null, false);
        this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        // Close the overlay when the close button is clicked
        view.findViewById(R.id.window_close).setOnClickListener(v -> hideOverlay());

        // Enable scrolling for the text view
        ((TextView) view.findViewById(R.id.overlay_summary_text)).setMovementMethod(new ScrollingMovementMethod());

        view.findViewById(R.id.overlay_summary_drag).setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;


            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:
                        Log.d("SummaryOverlay", "Drag Button is pressed");
                        // Record the initial position when touch is started
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;

                    case MotionEvent.ACTION_UP:
                        Log.d("SummaryOverlay", "Drag Button is released");
                        // Action for touch release
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        Log.d("SummaryOverlay", "Drag Button is Moving");
                        // Calculate the X and Y coordinates of the view
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);

                        // Update the layout with new X & Y coordinate
                        windowManager.updateViewLayout(view, params);
                        return true;
                }
                return false;
            }
        });
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


