package ch.epfl.pdse.polypotapp;

import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

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
        final LineChart chart = (LineChart) view.findViewById(R.id.graph_temperature);
        final int color = getResources().getColor(android.R.color.holo_red_light);

        YAxis yAxis = chart.getAxisLeft();
        yAxis.setAxisMinimum(0);
        yAxis.setAxisMaximum(30);

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new GraphHelper.DateAxisFormatter());
        xAxis.setLabelCount(7,true);

        Description description = new Description();
        description.setText("");
        chart.setDescription(description);

        chart.getLegend().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.setNoDataTextColor(color);

        CommunicationManager communicationManager = CommunicationManager.getInstance(getContext());
        mListener = new CommunicationManager.DataReadyListener() {
            public void onDataReady(JSONArray data, Calendar fromDate, Calendar toDate) {
                try {
                    fromDate.setTimeZone(TimeZone.getDefault());
                    fromDate.add(Calendar.MINUTE, 5);
                    toDate.setTimeZone(TimeZone.getDefault());
                    toDate.add(Calendar.MINUTE, 5);

                    ArrayList<Entry> entries = GraphHelper.extractSeries(data, "temperature");

                    if(entries.size() == 0) {
                        chart.clear();
                    } else {
                        LineDataSet dataSet = new LineDataSet(entries, "Label");
                        dataSet.setColor(color);
                        dataSet.setDrawValues(false);
                        dataSet.setLineWidth(2f);
                        dataSet.setDrawCircles(false);

                        LineData lineData = new LineData(dataSet);
                        chart.setData(lineData);
                    }

                    XAxis xAxis = chart.getXAxis();
                    xAxis.setAxisMinimum(fromDate.getTimeInMillis());
                    xAxis.setAxisMaximum(toDate.getTimeInMillis());

                    chart.invalidate();
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

