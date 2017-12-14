package ch.epfl.pdse.polypotapp;

import android.content.Context;

import com.android.volley.Cache;
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
import java.util.Date;
import java.util.Locale;
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
        public Object value;
        public Date fromDate;
        public Date toDate;
    }
    abstract class GenericLoading {}
    abstract class GenericResponse {
        public JSONObject response = null;
        public VolleyError error = null;
        public String hint = "";
        public Object value = null;
    }

    static class LatestRequest extends GenericRequest {
        public LatestRequest(String server, String uuid) {
            this.server = server;
            this.uuid = uuid;
        }
    }
    class LatestLoading extends GenericLoading {}
    class LatestResponse extends GenericResponse {}

    static class DataRequest extends GenericRequest {
        public DataRequest(String server, String uuid, Date fromDate, Date toDate) {
            this.server = server;
            this.uuid = uuid;
            this.fromDate = fromDate;
            this.toDate = toDate;
        }
    }
    class DataLoading extends GenericLoading {}
    class DataResponse extends GenericResponse {}

    static class StatsRequest extends GenericRequest {
        public StatsRequest(String server, String uuid, Date fromDate, Date toDate) {
            this.server = server;
            this.uuid = uuid;
            this.fromDate = fromDate;
            this.toDate = toDate;
        }
    }
    class StatsLoading extends GenericLoading {}
    class StatsResponse extends GenericResponse {}

    static class ConfigurationRequest extends GenericRequest {
        public ConfigurationRequest(String server, String uuid, JSONObject jsonRequest, String hint, Object value) {
            this.server = server;
            this.uuid = uuid;
            this.jsonRequest = jsonRequest;
            this.hint = hint;
            this.value = value;
        }
    }
    class ConfigurationResponse extends GenericResponse {}

    static class CommandsRequest extends GenericRequest {
        public CommandsRequest(String server, String uuid, JSONObject jsonRequest, String hint) {
            this.server = server;
            this.uuid = uuid;
            this.jsonRequest = jsonRequest;
            this.hint = hint;
        }
    }
    class CommandsResponse extends GenericResponse {}

    static class SetupRequest extends GenericRequest {
        public SetupRequest(String server, String uuid, JSONObject jsonRequest) {
            this.server = server;
            this.uuid = uuid;
            this.jsonRequest = jsonRequest;
        }
    }
    class SetupResponse extends  GenericResponse {}

    static class SetupPotRequest extends GenericRequest {
        public SetupPotRequest(String server, String uuid, JSONObject jsonRequest) {
            this.server = server;
            this.uuid = uuid;
            this.jsonRequest = jsonRequest;
        }
    }
    class SetupPotResponse extends GenericResponse {}
    
    private CommunicationManager(Context context) {
        mRequestQueue = Volley.newRequestQueue(context.getApplicationContext());

        mDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        mDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        EventBus.getDefault().register(this);
    }

    public static CommunicationManager create(Context context) {
        if(mInstance == null) {
            mInstance = new CommunicationManager(context);
        }
        return mInstance;
    }

    public static CommunicationManager getDefault() {
        return mInstance;
    }

    @Subscribe
    public void handleRequest(final GenericRequest event) {
        int method = Method.GET;
        String url = event.server;
        GenericLoading loading = null;
        final GenericResponse dataReady;

        if(event instanceof LatestRequest) {
            url += "/get-latest/" + event.uuid;
            loading = new LatestLoading();
            dataReady = new LatestResponse();
        } else if(event instanceof DataRequest) {
            String from = "from=" + mDateFormat.format(event.fromDate);
            String to = "to=" + mDateFormat.format(event.toDate);

            url += "/get-data/" + event.uuid + "?" + from + "&" + to;
            loading = new DataLoading();
            dataReady = new DataResponse();
        } else if(event instanceof StatsRequest) {
            String from = "from=" + mDateFormat.format(event.fromDate);
            String to = "to=" + mDateFormat.format(event.toDate);

            url += "/get-data/" + event.uuid + "?" + from + "&" + to;
            loading = new StatsLoading();
            dataReady = new StatsResponse();
        } else if(event instanceof ConfigurationRequest) {
            method = Method.POST;
            url += "/send-c-and-c/" + event.uuid;
            dataReady = new ConfigurationResponse();
        } else if(event instanceof CommandsRequest) {
            method = Method.POST;
            url += "/send-c-and-c/" + event.uuid;
            dataReady = new CommandsResponse();
        } else if(event instanceof SetupRequest) {
            method = Method.POST;
            url += "/setup";
            dataReady = new SetupResponse();
        } else if(event instanceof SetupPotRequest) {
            method = Method.POST;
            url = "http://192.168.1.1/setup";
            dataReady = new SetupPotResponse();
        } else {
            dataReady = null;
        }

        Cache.Entry cached = mRequestQueue.getCache().get(url);
        if(loading != null && (cached == null || cached.isExpired())) {
            EventBus.getDefault().post(loading);
        }

        JsonObjectRequest dataRequest = new JsonObjectRequest(method, url, event.jsonRequest,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        dataReady.response = response;
                        dataReady.hint = event.hint;
                        dataReady.value = event.value;
                        EventBus.getDefault().postSticky(dataReady);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        dataReady.error = error;
                        dataReady.hint = event.hint;
                        dataReady.value = event.value;
                        EventBus.getDefault().postSticky(dataReady);
                    }
                }
            );

        mRequestQueue.add(dataRequest);
    }
    
    public void clearCache() {
        mRequestQueue.getCache().clear();
    }
}