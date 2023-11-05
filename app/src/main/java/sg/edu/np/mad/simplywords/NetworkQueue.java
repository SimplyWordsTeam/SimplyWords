package sg.edu.np.mad.simplywords;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class NetworkQueue {
    private static NetworkQueue instance;
    private RequestQueue requestQueue;
    private static Context ctx;

    private NetworkQueue(Context context) {
        ctx = context;
        requestQueue = getRequestQueue();
    }

    public static synchronized NetworkQueue getInstance(Context context) {
        if (instance == null) {
            instance = new NetworkQueue(context);
        }
        return instance;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
}
