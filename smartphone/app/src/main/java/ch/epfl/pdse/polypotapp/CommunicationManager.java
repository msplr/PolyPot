package ch.epfl.pdse.polypotapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;

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
    private Context mContext;
    private RequestQueue mRequestQueue;

    private String mUuid;
    private String mServer;

    private SimpleDateFormat mDateFormat;
    private Calendar mDate;

    public enum RequestType {
        GET_LATEST, GET_DATA, POST_CONFIGURATION
    }

    public interface GenericDataReady {
        void addResponse(JSONObject response);
    }

    public class LatestDataReady implements GenericDataReady {
        public JSONObject response;

        @Override
        public void addResponse(JSONObject response) {
            this.response = response;
        }
    }

    public class DataReady implements GenericDataReady {
        public JSONObject response;

        @Override
        public void addResponse(JSONObject response) {
            this.response = response;
        }
    }

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
        mContext = context;
        mRequestQueue = Volley.newRequestQueue(context.getApplicationContext());
        mRequestQueue.getCache().clear();

        mDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        mDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        mDate = date;

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        mUuid = preferences.getString("uuid", mContext.getString(R.string.default_uuid));
        mServer = preferences.getString("server", mContext.getString(R.string.default_server));

        EventBus.getDefault().register(this);
    }

    public void stop() {
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void handleRequest(Request event) {
        int method = Method.GET;
        String url = mServer;
        final GenericDataReady dataReady;

        switch (event.type) {
            case GET_LATEST:
                url = mServer + "/get-latest/" + mUuid;
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

                url = mServer + "/get-data/" + mUuid + "?" + from + "&" + to;
                dataReady = new DataReady();
                break;

            case POST_CONFIGURATION:
                method = Method.POST;
                url = mServer + "/send-c-and-c/" + mUuid;
                dataReady = null;
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
                Snackbar.make(((MainActivity) mContext).getView(), R.string.error_reception_data, Snackbar.LENGTH_LONG).show();
            }
        });

        mRequestQueue.add(dataRequest);
    }
}