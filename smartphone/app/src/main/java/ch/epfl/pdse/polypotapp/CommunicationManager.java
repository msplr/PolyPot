package ch.epfl.pdse.polypotapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CommunicationManager {
    private static CommunicationManager mInstance;
    private static Context mContext;

    private static JSONArray mSensorData;
    private static JSONArray mInitData;
    private static String mUuid;
    private static String mServer;


    private RequestQueue mRequestQueue;

    private CommunicationManager(Context context) {
        mContext = context;
        mRequestQueue = Volley.newRequestQueue(context);
    //    mRequestQueue.addRequestFinishedListener(new mRequestFinishedListener());

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
        StringRequest initRequest = new StringRequest(Request.Method.GET, mServer + "/get-data/" + mUuid,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject reader = new JSONObject(response.toString());
                            mInitData = reader.getJSONArray("init");
                            MainActivity.initDataUpdate(mInitData);
                        }catch (final JSONException e) {
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });

        mRequestQueue.add(initRequest);
    }

    public void getData() {
        StringRequest dataRequest = new StringRequest(Request.Method.GET, mServer + "get-latest/" + mUuid,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject reader = new JSONObject(response.toString());
                            mSensorData = reader.getJSONArray("data");
                        }catch (final JSONException e) {
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });

        mRequestQueue.add(dataRequest);
    }

    private JSONArray getInitData(){
        return mInitData;
    }

    private JSONArray getSensorData(){
        return mSensorData;
    }


    private LineGraphSeries<DataPoint> getGraphSeries(String date, String sensoryInput) {

        LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
        for (int j = 0; j < mSensorData.length(); j++){
            try {
                JSONObject data = mSensorData.getJSONObject(j);
                String datadate = data.getString("datetime");
                String datadateExtract = datadate.substring(0,10);
                if (datadateExtract.equals(date)){
                    String hour = datadate.substring(11,13);
                    String minutes = datadate.substring(14,16);
                    int x1 = Integer.parseInt(hour);
                    int x2 = Integer.parseInt(minutes);
                    float x = (float)x1 + (float)x2 / 60;
                    int y = (int)Float.parseFloat(data.getString(sensoryInput));

                    series.appendData(new DataPoint(x,y),true, 250);
                }
            }
            catch (final JSONException e) {
                Log.e("ServiceHandler", "No data received from HTTP request");}
        }
        return series;
    }



    private class mRequestFinishedListener<StringRequest> implements RequestQueue.RequestFinishedListener<StringRequest> {
        @Override
        public void onRequestFinished(Request<StringRequest> request) {
            MainActivity.initDataUpdate(mInitData);
        }
    }


}
