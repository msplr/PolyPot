package ch.epfl.pdse.polypotapp;

import android.content.Context;

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
    private static CommunicationManager mInstance;

    private final RequestQueue mRequestQueue;
    private final SimpleDateFormat mDateFormat;

    abstract static class GenericRequest {
        public String server;
        public String uuid;
        public JSONObject jsonRequest;
        public String hint;
        public Calendar date;
    }
    abstract class GenericDataReady {
        public JSONObject response = null;
        public VolleyError error = null;
        public String hint = "";
    }

    static class LatestRequest extends GenericRequest {
        public LatestRequest(String server, String uuid) {
            this.server = server;
            this.uuid = uuid;
        }
    }
    class LatestDataReady extends GenericDataReady {}

    static class DataRequest extends GenericRequest {
        public DataRequest(String server, String uuid, Calendar date) {
            this.server = server;
            this.uuid = uuid;
            this.date = date;
        }
    }
    class DataReady extends GenericDataReady {}

    static class ConfAndCommandsRequest extends GenericRequest {
        public ConfAndCommandsRequest(String server, String uuid, JSONObject jsonRequest, String hint) {
            this.server = server;
            this.uuid = uuid;
            this.jsonRequest = jsonRequest;
            this.hint = hint;
        }
    }
    class ConfAndCommandsDataReady extends GenericDataReady {}

    static class SetupRequest extends GenericRequest {
        public SetupRequest(String server, String uuid, JSONObject jsonRequest) {
            this.server = server;
            this.uuid = uuid;
            this.jsonRequest = jsonRequest;
        }
    }
    class SetupDataReady extends  GenericDataReady {}

    static class SetupPotRequest extends GenericRequest {
        public SetupPotRequest(String server, String uuid, JSONObject jsonRequest) {
            this.server = server;
            this.uuid = uuid;
            this.jsonRequest = jsonRequest;
        }
    }
    class SetupPotDataReady extends GenericDataReady {}

    private CommunicationManager(Context context) {
        mRequestQueue = Volley.newRequestQueue(context.getApplicationContext());

        mDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        mDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        EventBus.getDefault().register(this);
    }

    public static CommunicationManager getDefault(Context context) {
        if(mInstance == null) {
            mInstance = new CommunicationManager(context);
        }
        return mInstance;
    }

    @Subscribe
    public void handleRequest(final GenericRequest event) {
        int method = Method.GET;
        String url = event.server;
        final GenericDataReady dataReady;

        if(event instanceof LatestRequest) {
            url += "/get-latest/" + event.uuid;
            dataReady = new LatestDataReady();
        } else if(event instanceof DataRequest) {
            final Calendar fromDate = (Calendar) event.date.clone();
            fromDate.setTimeZone(TimeZone.getTimeZone("UTC"));
            String from = "from=" + mDateFormat.format(fromDate.getTime());

            final Calendar toDate = (Calendar) event.date.clone();
            toDate.setTimeZone(TimeZone.getTimeZone("UTC"));
            toDate.add(Calendar.DAY_OF_MONTH, 1);
            String to = "to=" + mDateFormat.format(toDate.getTime());

            url += "/get-data/" + event.uuid + "?" + from + "&" + to;
            dataReady = new DataReady();
        } else if(event instanceof ConfAndCommandsRequest) {
            method = Method.POST;
            url += "/send-c-and-c/" + event.uuid;
            dataReady = new ConfAndCommandsDataReady();
        } else if(event instanceof SetupRequest) {
            method = Method.POST;
            url += "/setup";
            dataReady = new SetupDataReady();
        } else if(event instanceof SetupPotRequest) {
            method = Method.POST;
            url = "http://192.168.1.1/setup";
            dataReady = new SetupPotDataReady();
        } else {
            dataReady = null;
        }

        JsonObjectRequest dataRequest = new JsonObjectRequest(method, url, event.jsonRequest,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        dataReady.response = response;
                        dataReady.hint = event.hint;
                        EventBus.getDefault().post(dataReady);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        dataReady.error = error;
                        dataReady.hint = event.hint;
                        EventBus.getDefault().post(dataReady);
                    }
                }
            );

        mRequestQueue.add(dataRequest);
    }
    
    public void clearCache() {
        mRequestQueue.getCache().clear();
    }
}