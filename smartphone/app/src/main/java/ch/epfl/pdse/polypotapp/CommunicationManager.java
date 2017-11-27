package ch.epfl.pdse.polypotapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

public class CommunicationManager {
    private static CommunicationManager mInstance;

    private Context mContext;
    private RequestQueue mRequestQueue;

    private HashMap<String,SummaryDataReadyListener> mSummaryDataReadyList;
    private HashMap<String,DataReadyListener> mDataReadyList;

    private String mUuid;
    private String mServer;

    private SimpleDateFormat mDateFormat;

    interface SummaryDataReadyListener {
        void onDataReady(JSONObject summaryData);
    }

    interface DataReadyListener {
        void onDataReady(JSONArray data, Calendar fromDate, Calendar toDate);
    }

    public SummaryDataReadyListener addSummaryDataReadyListener(String key, SummaryDataReadyListener listener) {
        return mSummaryDataReadyList.put(key, listener);
    }

    public SummaryDataReadyListener removeSummaryDataReadyListener(String key) {
        return mSummaryDataReadyList.remove(key);
    }

    public DataReadyListener addDataReadyListener(String key, DataReadyListener listener) {
        return mDataReadyList.put(key, listener);
    }

    public DataReadyListener removeDataReadyListener(String key) {
        return mDataReadyList.remove(key);
    }

    private CommunicationManager(Context context) {
        mContext = context;
        mRequestQueue = Volley.newRequestQueue(context.getApplicationContext());

        mSummaryDataReadyList = new HashMap<>();
        mDataReadyList = new HashMap<>();

        mDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        mDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        mUuid = preferences.getString("uuid", mContext.getString(R.string.default_uuid));
        mServer = preferences.getString("server", mContext.getString(R.string.default_server));
    }

    public static synchronized CommunicationManager createInstance(Context context) {
        if (mInstance == null) {
            mInstance = new CommunicationManager(context);
        }
        return mInstance;
    }

    public static synchronized CommunicationManager getInstance() {
        return mInstance;
    }

    public void updateInstance(Context context) {
        mContext = context;
    }

    public void getLatestData() {
        JsonObjectRequest latestRequest = new JsonObjectRequest(Request.Method.GET, mServer + "/get-latest/" + mUuid, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject summaryData = response.getJSONObject("data");

                            for(String key : mSummaryDataReadyList.keySet()) {
                                mSummaryDataReadyList.get(key).onDataReady(summaryData);
                            }
                        } catch (final JSONException e) {
                            Snackbar.make(((MainActivity) mContext).getView(), mContext.getString(R.string.error_reception_summary), Snackbar.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Snackbar.make(((MainActivity) mContext).getView(), R.string.error_reception_summary, Snackbar.LENGTH_LONG).show();
                    }
                });
        mRequestQueue.add(latestRequest);
    }

    public void getData() {
        Calendar date = ((MainActivity) mContext).getDate();

        if(date == null) {
            return;
        }

        final Calendar fromDate = (Calendar) date.clone();
        fromDate.setTimeZone(TimeZone.getTimeZone("UTC"));
        String from = "from=" + mDateFormat.format(fromDate.getTime());

        final Calendar toDate = (Calendar) date.clone();
        toDate.setTimeZone(TimeZone.getTimeZone("UTC"));
        toDate.add(Calendar.DAY_OF_MONTH, 1);
        String to = "to=" + mDateFormat.format(toDate.getTime());

        JsonObjectRequest dataRequest = new JsonObjectRequest(Request.Method.GET, mServer + "/get-data/" + mUuid + "?" + from + "&" + to, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray data = response.getJSONArray("data");

                            for(String key : mDataReadyList.keySet()) {
                                mDataReadyList.get(key).onDataReady(data, (Calendar) fromDate.clone(), (Calendar) toDate.clone());
                            }
                        } catch (final JSONException e) {
                            Snackbar.make(((MainActivity) mContext).getView(), R.string.error_reception_data, Snackbar.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                        public void onErrorResponse(VolleyError error) {
                        Snackbar.make(((MainActivity) mContext).getView(), R.string.error_reception_data, Snackbar.LENGTH_LONG).show();
                    }
                });

        mRequestQueue.add(dataRequest);
    }
}