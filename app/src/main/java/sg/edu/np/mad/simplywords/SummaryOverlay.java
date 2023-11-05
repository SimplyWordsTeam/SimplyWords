package sg.edu.np.mad.simplywords;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class SummaryOverlay {
    private Context context;
    private View view;
    private WindowManager.LayoutParams params;
    private WindowManager windowManager;
    private LayoutInflater layoutInflater;

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

        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.view = layoutInflater.inflate(R.layout.overlay_summary, null);
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
}
