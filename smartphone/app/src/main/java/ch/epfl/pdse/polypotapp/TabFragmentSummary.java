package ch.epfl.pdse.polypotapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class TabFragmentSummary extends Fragment {
    private TextView waterLevelText;
    private TextView temperatureText;
    private TextView luminosityText;
    private TextView soilMoistureText;
    private TextView dataDateText;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_summary, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        // Save for later use
        waterLevelText = view.findViewById(R.id.water_level_text);
        temperatureText = view.findViewById(R.id.temperature_text);
        luminosityText = view.findViewById(R.id.luminosity_text);
        soilMoistureText = view.findViewById(R.id.soil_moisture_text);
        dataDateText = view.findViewById(R.id.latest_data_date_text);

        EventBus.getDefault().post(new CommunicationManager.Request(CommunicationManager.RequestType.GET_LATEST));
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
            JSONObject data = event.response.getJSONObject("data");

            // Soil Moisture
            int soilMoisture = Math.round(Float.parseFloat(data.getString("soil_moisture")));
            soilMoistureText.setText(Integer.toString(soilMoisture));

            // Temperature
            int temperature = Math.round(Float.parseFloat(data.getString("temperature")));
            temperatureText.setText(Integer.toString(temperature));

            // Water Level
            int water_level = Math.round(Float.parseFloat(data.getString("water_level")));
            waterLevelText.setText(Integer.toString(water_level));

            // Luminosity
            int luminosity = Math.round(Float.parseFloat(data.getString("luminosity")));
            luminosityText.setText(Integer.toString(luminosity));

            // Date and Time
            SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            inputDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            SimpleDateFormat outputDateFormat = new SimpleDateFormat("'Data from 'yyyy-MM-dd' 'HH:mm:ss'.'");

            Calendar date = GregorianCalendar.getInstance();
            date.setTime(inputDateFormat.parse(data.getString("datetime")));
            date.setTimeZone(TimeZone.getDefault());

            dataDateText.setText(outputDateFormat.format(date.getTime()));
        } catch (NullPointerException|JSONException|ParseException e) {
            Snackbar.make(getView(), getString(R.string.error_reception_summary), Snackbar.LENGTH_LONG).show();
        }
    }
}