package ch.epfl.pdse.polypotapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class CommunicationManager {
    private RequestQueue mRequestQueue;
    private SharedPreferences mPreferences;

    private SimpleDateFormat mDateFormat;
    private Calendar mDate;

    public enum RequestType {
        GET_LATEST, GET_DATA, POST_CONFIGURATION, POST_SETUP
    }

    public abstract class GenericDataReady {
        public JSONObject response = null;
        public VolleyError error = null;

        public void addResponse(JSONObject response) {
            this.response = response;
        }

        public void addError(VolleyError error) {
            this.error = error;
        }
    }

    public class LatestDataReady extends GenericDataReady {}
    public class SetupDataReady extends GenericDataReady {}
    public class DataReady extends GenericDataReady {}

    public static class Request {
        public RequestType type;
        public JSONObject jsonRequest;

        public Request(RequestType type) {
            this.type = type;
            this.jsonRequest = null;
        }

        public Request(RequestType type, JSONObject jsonRequest) {
            this.type = type;
            this.jsonRequest = jsonRequest;
        }
    }

    public CommunicationManager(Context context, Calendar date) {
        mRequestQueue = Volley.newRequestQueue(context.getApplicationContext());
        mRequestQueue.getCache().clear();

        mDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        mDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        mDate = date;

        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        EventBus.getDefault().register(this);
    }

    public void stop() {
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void handleRequest(Request event) {
        String UUID = mPreferences.getString("uuid", "");
        String server = mPreferences.getString("server", "");

        int method = Method.GET;
        String url = server;
        final GenericDataReady dataReady;

        switch (event.type) {
            case GET_LATEST:
                url = server + "/get-latest/" + UUID;
                dataReady = new LatestDataReady();
                break;

            case GET_DATA:
                final Calendar fromDate = (Calendar) mDate.clone();
                fromDate.setTimeZone(TimeZone.getTimeZone("UTC"));
                String from = "from=" + mDateFormat.format(fromDate.getTime());

                final Calendar toDate = (Calendar) mDate.clone();
                toDate.setTimeZone(TimeZone.getTimeZone("UTC"));
                toDate.add(Calendar.DAY_OF_MONTH, 1);
                String to = "to=" + mDateFormat.format(toDate.getTime());

                url = server + "/get-data/" + UUID + "?" + from + "&" + to;
                dataReady = new DataReady();
                break;

            case POST_CONFIGURATION:
                method = Method.POST;
                url = server + "/send-c-and-c/" + UUID;
                dataReady = null;
                break;

            case POST_SETUP:
                method = Method.POST;
                url = "http://192.168.1.1/setup";
                dataReady = new SetupDataReady();
                break;

            default:
                dataReady = null;
                break;
        }

        JsonObjectRequest dataRequest = new JsonObjectRequest(method, url, event.jsonRequest,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if(dataReady != null) {
                            dataReady.addResponse(response);
                            EventBus.getDefault().post(dataReady);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if(dataReady != null) {
                            dataReady.addError(error);
                            EventBus.getDefault().post(dataReady);
                        }
                    }
                }
        );

        mRequestQueue.add(dataRequest);
    }
}