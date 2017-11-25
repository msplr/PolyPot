package ch.epfl.pdse.polypotapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class TabFragmentSummary extends Fragment{

    private static TextView mWaterLevelText;
    private static TextView mTemperatureText;
    private static TextView mLuminosityText;
    private static TextView mHumidityText;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_summary, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mWaterLevelText = (TextView) getView().findViewById(R.id.water_level_text);
        mTemperatureText = (TextView) getView().findViewById(R.id.temperature_text);
        mLuminosityText = (TextView) getView().findViewById(R.id.luminosity_text);
        mHumidityText = (TextView) getView().findViewById(R.id.humidity_text);

        CommunicationManager communicationManager = CommunicationManager.getInstance(getContext());
        communicationManager.getLatestData();
    }

    public static void initDataUpdate(JSONObject initData) {
        try {
            int soilMoisture = (int) Float.parseFloat(initData.getString("soil_moisture"));
            mHumidityText.setText(Integer.toString(soilMoisture));
            int temperature = (int) Float.parseFloat(initData.getString("temperature"));
            mTemperatureText.setText(Integer.toString(temperature));
            int water_level = (int) Float.parseFloat(initData.getString("water_level"));
            mWaterLevelText.setText(Integer.toString(water_level));
            int luminosity = (int) Float.parseFloat(initData.getString("luminosity"));
            mLuminosityText.setText(Integer.toString(luminosity));
        }
        catch (final JSONException e) {
            Log.e("ServiceHandler", "No data received from HTTP request");}
    }
}
