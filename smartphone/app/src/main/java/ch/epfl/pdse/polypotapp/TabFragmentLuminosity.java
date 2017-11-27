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

public class TabFragmentLuminosity extends Fragment{
    CommunicationManager.DataReadyListener mListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_luminosity, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        final LineChart chart = view.findViewById(R.id.graph_luminosity);
        final int color = getResources().getColor(android.R.color.holo_orange_light);
        final String noChartData = getResources().getString(R.string.no_chart_data);

        GraphHelper.configureChart(chart, color, 0, 1200);

        CommunicationManager communicationManager = CommunicationManager.getInstance();
        mListener = new CommunicationManager.DataReadyListener() {
            public void onDataReady(JSONArray data, Calendar fromDate, Calendar toDate) {
                try {
                    GraphHelper.updateChartWithData(chart, color, "luminosity", data, fromDate, toDate, noChartData);
                } catch (JSONException |ParseException e) {
                    Snackbar.make(getView(), getString(R.string.error_reception_data), Snackbar.LENGTH_LONG).show();
                }
            }
        };

        communicationManager.addDataReadyListener("luminosityListener", mListener);
        communicationManager.getData();
    }
}
