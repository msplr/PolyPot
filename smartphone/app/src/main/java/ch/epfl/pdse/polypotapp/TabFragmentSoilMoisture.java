package ch.epfl.pdse.polypotapp;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.ParseException;
import java.util.Calendar;

public class TabFragmentSoilMoisture extends Fragment{
    CommunicationManager.DataReadyListener mListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_soil_moisture, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        final LineChart chart = view.findViewById(R.id.graph_soil_moisture);
        final int color = getResources().getColor(android.R.color.holo_blue_light);

        GraphHelper.configureChart(chart, color, 0, 100);

        CommunicationManager communicationManager = CommunicationManager.getInstance(getContext());
        mListener = new CommunicationManager.DataReadyListener() {
            public void onDataReady(JSONArray data, Calendar fromDate, Calendar toDate) {
                try {
                    GraphHelper.updateChartWithData(chart, color, "soil_moisture", data, fromDate, toDate);
                } catch (JSONException|ParseException e) {
                    Snackbar.make(getView(), getString(R.string.error_reception_data), Snackbar.LENGTH_LONG).show();
                }
            }
        };

        communicationManager.addDataReadyListener("soilMoistureListener", mListener);
        communicationManager.getData();
    }
}
