package ch.epfl.pdse.polypotapp;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class CommunicationManager {
    private static CommunicationManager mInstance;
    private static Context mContext;

    private RequestQueue mRequestQueue;

    private CommunicationManager(Context context) {
        mContext = context;
        mRequestQueue = Volley.newRequestQueue(context);
    }

    public static synchronized CommunicationManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new CommunicationManager(context);
        }
        return mInstance;
    }
}
