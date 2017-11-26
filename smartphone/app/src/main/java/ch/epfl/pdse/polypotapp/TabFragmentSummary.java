package ch.epfl.pdse.polypotapp;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class TabFragmentSummary extends Fragment {
    CommunicationManager.SummaryDataReadyListener mListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_summary, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Save for later use
        final TextView waterLevelText = (TextView) view.findViewById(R.id.water_level_text);
        final TextView temperatureText = (TextView) view.findViewById(R.id.temperature_text);
        final TextView luminosityText = (TextView) view.findViewById(R.id.luminosity_text);
        final TextView soil_moistureText = (TextView) view.findViewById(R.id.soil_moisture_text);
        final TextView dataDateText = (TextView) view.findViewById(R.id.data_date_text);

        CommunicationManager communicationManager = CommunicationManager.getInstance(getContext());

        mListener = new CommunicationManager.SummaryDataReadyListener() {
            public void onDataReady(JSONObject summaryData) {
                try {
                    // Soil Moisture
                    int soilMoisture = Math.round(Float.parseFloat(summaryData.getString("soil_moisture")));
                    soil_moistureText.setText(Integer.toString(soilMoisture));

                    // Temperature
                    int temperature = Math.round(Float.parseFloat(summaryData.getString("temperature")));
                    temperatureText.setText(Integer.toString(temperature));

                    // Water Level
                    int water_level = Math.round(Float.parseFloat(summaryData.getString("water_level")));
                    waterLevelText.setText(Integer.toString(water_level));

                    // Luminosity
                    int luminosity = Math.round(Float.parseFloat(summaryData.getString("luminosity")));
                    luminosityText.setText(Integer.toString(luminosity));

                    // Date and Time
                    SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                    inputDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                    SimpleDateFormat outputDateFormat = new SimpleDateFormat("'Data from 'yyyy-MM-dd' 'HH:mm:ss'.'");

                    Calendar date = GregorianCalendar.getInstance();
                    date.setTime(inputDateFormat.parse(summaryData.getString("datetime")));
                    date.setTimeZone(TimeZone.getDefault());

                    dataDateText.setText(outputDateFormat.format(date.getTime()));
                } catch (JSONException|ParseException e) {
                    Snackbar.make(getView(), getString(R.string.error_reception_summary), Snackbar.LENGTH_LONG).show();
                }
            }
        };

        communicationManager.addSummaryDataReadyListener("summaryListener", mListener);
        communicationManager.getLatestData();
    }
}