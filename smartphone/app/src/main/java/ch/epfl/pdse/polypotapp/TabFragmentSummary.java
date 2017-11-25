package ch.epfl.pdse.polypotapp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

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

    }

    public static void initDataUpdate(JSONArray initData) {
        try {
            JSONObject data = initData.getJSONObject(0);
            int soilMoisture = (int) Float.parseFloat(data.getString("soil_moisture"));
            mHumidityText.setText(soilMoisture);
            int temperature = (int) Float.parseFloat(data.getString("temperature"));
            mTemperatureText.setText(temperature);
            int water_level = (int) Float.parseFloat(data.getString("water_level"));
            mWaterLevelText.setText(water_level);
            int luminosity = (int) Float.parseFloat(data.getString("luminosity"));
            mLuminosityText.setText(luminosity);


        }
        catch (final JSONException e) {
            Log.e("ServiceHandler", "No data received from HTTP request");}
    }

}
