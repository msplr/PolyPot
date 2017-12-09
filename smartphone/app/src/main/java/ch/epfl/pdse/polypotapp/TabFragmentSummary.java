package ch.epfl.pdse.polypotapp;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class TabFragmentSummary extends Fragment {
    private ActivityMain mActivity;

    private String mServer;
    private String mUUID;

    private TextView mWaterLevelText;
    private TextView mTemperatureText;
    private TextView mLuminosityText;
    private TextView mSoilMoistureText;
    private TextView mLatestDataDateText;
    private TextView mLastWateringText;

    public static TabFragmentSummary newInstance(String server, String uuid) {
        TabFragmentSummary f = new TabFragmentSummary();

        Bundle args = new Bundle();
        args.putString("server", server);
        args.putString("uuid", uuid);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mActivity = (ActivityMain) context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        mServer = args.getString("server");
        mUUID = args.getString("uuid");

        return inflater.inflate(R.layout.fragment_summary, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        final SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                CommunicationManager.getDefault(getActivity()).clearCache();
                EventBus.getDefault().post(new CommunicationManager.LatestRequest(mServer, mUUID));
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        // Save for later use
        mWaterLevelText = view.findViewById(R.id.water_level_text);
        mTemperatureText = view.findViewById(R.id.temperature_text);
        mLuminosityText = view.findViewById(R.id.luminosity_text);
        mSoilMoistureText = view.findViewById(R.id.soil_moisture_text);
        mLatestDataDateText = view.findViewById(R.id.latest_data_date_text);
        mLastWateringText = view.findViewById(R.id.last_watering_text);

        EventBus.getDefault().post(new CommunicationManager.LatestRequest(mServer, mUUID));
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe
    public void handleLatestData(CommunicationManager.LatestDataReady event) {
        try {
            // Data part
            JSONObject data = event.response.getJSONObject("data");

            // Soil Moisture
            int soilMoisture = Math.round(Float.parseFloat(data.getString("soil_moisture")));
            mSoilMoistureText.setText(Integer.toString(soilMoisture));

            // Temperature
            int temperature = Math.round(Float.parseFloat(data.getString("temperature")));
            mTemperatureText.setText(Integer.toString(temperature));

            // Water Level
            int water_level = Math.round(Float.parseFloat(data.getString("water_level")));
            mWaterLevelText.setText(Integer.toString(water_level));

            // Luminosity
            int luminosity = Math.round(Float.parseFloat(data.getString("luminosity")));
            mLuminosityText.setText(Integer.toString(luminosity));

            // Date and Time
            SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
            inputDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            SimpleDateFormat outputDateFormat = new SimpleDateFormat(getString(R.string.latest_data_date_format), Locale.US);

            Calendar date = GregorianCalendar.getInstance();
            date.setTime(inputDateFormat.parse(data.getString("datetime")));
            date.setTimeZone(TimeZone.getDefault());

            mLatestDataDateText.setText(outputDateFormat.format(date.getTime()));

            // Command part
            JSONArray commands = event.response.getJSONArray("commands");

            // Search last watering and display it
            for(int i = 0; i < commands.length(); i++) {
                JSONObject command = commands.getJSONObject(i);
                if(command.getString("type").equals("water")) {
                    // Date and Time
                    outputDateFormat = new SimpleDateFormat(getString(R.string.last_watering_format), Locale.US);

                    date.setTime(inputDateFormat.parse(command.getString("datetime")));
                    date.setTimeZone(TimeZone.getDefault());

                    mLastWateringText.setText(outputDateFormat.format(date.getTime()));

                    mActivity.setWaterDate(date.getTime());
                }
            }
        } catch (NullPointerException|JSONException|ParseException e) {
            // Reset to default values
            mSoilMoistureText.setText("");
            mTemperatureText.setText("");
            mWaterLevelText.setText("");
            mLuminosityText.setText("");
            mLatestDataDateText.setText(getString(R.string.latest_data_date_unknown));
            mLastWateringText.setText(R.string.last_watering_unknown);

            // Show an error message
            Snackbar.make(getView(), R.string.reception_summary_error, Snackbar.LENGTH_LONG).show();
        }
    }
}