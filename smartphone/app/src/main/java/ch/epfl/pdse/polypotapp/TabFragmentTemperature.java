package ch.epfl.pdse.polypotapp;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONArray;
import org.json.JSONException;

public class TabFragmentTemperature extends Fragment{
    CommunicationManager.DataReadyListener mListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_temperature, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        final GraphView graph = (GraphView) getView().findViewById(R.id.graph_temperature);

        graph.getGridLabelRenderer().setLabelFormatter(new GraphHelper.DateFormatter());
        graph.getGridLabelRenderer().setNumHorizontalLabels(6);

        //graph.getViewport().setMinY(0);
        //graph.getViewport().setMaxY(30);
        //graph.getViewport().setYAxisBoundsManual(true);
        graph.getGridLabelRenderer().setNumVerticalLabels(5);

        CommunicationManager communicationManager = CommunicationManager.getInstance(getContext());

        mListener = new CommunicationManager.DataReadyListener() {
            public void onDataReady(JSONArray data) {
                try {
                    LineGraphSeries<DataPoint> series = GraphHelper.extractSeries(data, "temperature");
                    series.setColor(getResources().getColor(android.R.color.holo_red_light));
                    graph.removeAllSeries();
                    graph.addSeries(series);
                } catch (final JSONException e) {
                    Snackbar.make(getView(), getString(R.string.error_reception_data), Snackbar.LENGTH_LONG).show();
                }
            }
        };

        communicationManager.addDataReadyListener(mListener);
        communicationManager.getData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        CommunicationManager communicationManager = CommunicationManager.getInstance(getContext());
        communicationManager.removeDataReadyListener(mListener);
    }
}
