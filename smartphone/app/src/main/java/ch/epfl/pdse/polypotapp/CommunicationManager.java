package ch.epfl.pdse.polypotapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.util.ArraySet;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CommunicationManager {
    private static CommunicationManager mInstance;

    private Context mContext;
    private RequestQueue mRequestQueue;

    private String mUuid;
    private String mServer;

    private ArraySet<SummaryDataReadyListener> mSummaryDataReadyList;
    private ArraySet<DataReadyListener> mDataReadyList;

    private SimpleDateFormat mDateFormat;

    interface SummaryDataReadyListener {
        void onDataReady(JSONObject summaryData);
    }

    interface DataReadyListener {
        void onDataReady(JSONArray data, Calendar fromDate, Calendar toDate);
    }

    public boolean addSummaryDataReadyListener(SummaryDataReadyListener listener) {
        return mSummaryDataReadyList.add(listener);
    }

    public boolean removeSummaryDataReadyListener(SummaryDataReadyListener listener) {
        return mSummaryDataReadyList.remove(listener);
    }

    public boolean addDataReadyListener(DataReadyListener listener) {
        return mDataReadyList.add(listener);
    }

    public boolean removeDataReadyListener(DataReadyListener listener) {
        return mDataReadyList.remove(listener);
    }

    private CommunicationManager(Context context) {
        mContext = context;
        mRequestQueue = Volley.newRequestQueue(context);

        mSummaryDataReadyList = new ArraySet<SummaryDataReadyListener>();
        mDataReadyList = new ArraySet<DataReadyListener>();

        mDateFormat = new SimpleDateFormat("YYYY-MM-dd'T'HH:mm:ss'Z'");

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        mUuid = preferences.getString("uuid", mContext.getString(R.string.default_uuid));
        mServer = preferences.getString("server", mContext.getString(R.string.default_server));
    }

    public static synchronized CommunicationManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new CommunicationManager(context);
        }
        return mInstance;
    }

    public void getLatestData() {
        StringRequest latestRequest = new StringRequest(Request.Method.GET, mServer + "/get-latest/" + mUuid,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject reader = new JSONObject(response.toString());
                            JSONObject summaryData = reader.getJSONObject("data");

                            for(SummaryDataReadyListener listener : mSummaryDataReadyList) {
                                listener.onDataReady(summaryData);
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
        Log.e("EEEEEEEEEEEEEEE", mDateFormat.format(fromDate)); //TODO: don't work without, try to understand and correct
        fromDate.setTimeZone(TimeZone.GMT_ZONE);
        String from = "from=" + mDateFormat.format(fromDate);

        final Calendar toDate = (Calendar) date.clone();
        Log.e("GGGGGGGGGGGGGGG", mDateFormat.format(toDate)); //TODO: don't work without, try to understand and correct
        toDate.setTimeZone(TimeZone.GMT_ZONE);
        toDate.add(Calendar.DAY_OF_MONTH, 1);
        String to = "to=" + mDateFormat.format(toDate);

        StringRequest dataRequest = new StringRequest(Request.Method.GET, mServer + "/get-data/" + mUuid + "?" + from + "&" + to,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject reader = new JSONObject(response.toString());
                            JSONArray data = reader.getJSONArray("data");

                            for(DataReadyListener listener : mDataReadyList) {
                                listener.onDataReady(data, (Calendar) fromDate.clone(), (Calendar) toDate.clone());
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