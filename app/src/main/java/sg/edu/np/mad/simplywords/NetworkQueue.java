package sg.edu.np.mad.simplywords;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class NetworkQueue {
    private static NetworkQueue instance;
    private RequestQueue requestQueue;

    private NetworkQueue(Context context) {
        requestQueue = getRequestQueue(context);
    }

    public static synchronized NetworkQueue getInstance(Context context) {
        if (instance == null) {
            instance = new NetworkQueue(context);
        }
        return instance;
    }

    public RequestQueue getRequestQueue(Context context) {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> request, Context context) {
        getRequestQueue(context).add(request);
    }
}
