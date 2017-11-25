package ch.epfl.pdse.polypotapp;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;

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

    private static JSONArray sensorData;
    private static JSONArray initData;
    private static String uuid = "c52562b7-a1f1-4729-8a0f-7ee82aae6a10";
    private static String get_url ="https://polypot.0xf00.ch/get-data/";


    private RequestQueue mRequestQueue;

    private CommunicationManager(Context context) {
        mContext = context;
        mRequestQueue = Volley.newRequestQueue(context);

        mRequestQueue.add(dataRequest);
    }

    public static synchronized CommunicationManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new CommunicationManager(context);
        }
        return mInstance;
    }

    private StringRequest dataRequest = new StringRequest(Request.Method.GET, get_url+uuid,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject reader = new JSONObject(response.toString());
                        sensorData = reader.getJSONArray("data");
                        initData = reader.getJSONArray("init");
                    }catch (final JSONException e) {
                    }
                }
            }, new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
        }
    });


    private void setUUID(String new_uuid){
        uuid = new_uuid;
    }
    private void setGetURL(String new_url){
        get_url = new_url;
    }

    private JSONArray getInitData(){
        return initData;
    }

    private JSONArray getSensorData(){
        return sensorData;
    }


    private LineGraphSeries<DataPoint> getGraphSeries(String date, String sensoryInput) {

        LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
        for (int j = 0; j < sensorData.length(); j++){
            try {
                JSONObject data = sensorData.getJSONObject(j);
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

}
