package ch.epfl.pdse.polypotapp;

import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.icu.util.GregorianCalendar;
import android.icu.util.TimeZone;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParsePosition;

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
        final TextView humidityText = (TextView) view.findViewById(R.id.humidity_text);
        final TextView dataDateText = (TextView) view.findViewById(R.id.data_date_text);

        CommunicationManager communicationManager = CommunicationManager.getInstance(getContext());

        mListener = new CommunicationManager.SummaryDataReadyListener() {
            public void onDataReady(JSONObject initData) {
                try {
                    // Soil Moisture
                    int soilMoisture = Math.round(Float.parseFloat(initData.getString("soil_moisture")));
                    humidityText.setText(Integer.toString(soilMoisture));

                    // Temperature
                    int temperature = Math.round(Float.parseFloat(initData.getString("temperature")));
                    temperatureText.setText(Integer.toString(temperature));

                    // Water Level
                    int water_level = Math.round(Float.parseFloat(initData.getString("water_level")));
                    waterLevelText.setText(Integer.toString(water_level));

                    // Luminosity
                    int luminosity = Math.round(Float.parseFloat(initData.getString("luminosity")));
                    luminosityText.setText(Integer.toString(luminosity));

                    // Date and Time
                    SimpleDateFormat inputDateFormat = new SimpleDateFormat("YYYY-MM-dd'T'HH:mm:ssXXXXX");
                    SimpleDateFormat outputDateFormat = new SimpleDateFormat("'Data from 'YYYY-MM-dd' 'HH:mm:ss'.'");

                    Calendar date = new GregorianCalendar();
                    inputDateFormat.parse(initData.getString("datetime"), date, new ParsePosition(0));
                    date.setTimeZone(TimeZone.getDefault());

                    dataDateText.setText(outputDateFormat.format(date));
                } catch (final JSONException e) {
                    Snackbar.make(getView(), getString(R.string.error_reception_summary), Snackbar.LENGTH_LONG).show();
                }
            }
        };

        communicationManager.addSummaryDataReadyListener(mListener);
        communicationManager.getLatestData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        CommunicationManager communicationManager = CommunicationManager.getInstance(getContext());
        communicationManager.removeSummaryDataReadyListener(mListener);
    }
}