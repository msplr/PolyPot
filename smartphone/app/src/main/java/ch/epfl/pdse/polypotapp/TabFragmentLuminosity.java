package ch.epfl.pdse.polypotapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;

import java.text.ParseException;

public class TabFragmentLuminosity extends Fragment{
    private LineChart mChart;
    private int mColor;
    private String mNoChartData;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_luminosity, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        mChart = view.findViewById(R.id.graph_luminosity);
        mColor = getResources().getColor(android.R.color.holo_orange_light);
        mNoChartData = getResources().getString(R.string.no_chart_data);

        GraphHelper.configureChart(mChart, mColor, 0, 1200);
        EventBus.getDefault().post(new CommunicationManager.Request(CommunicationManager.RequestType.GET_DATA));
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
    public void handleData(CommunicationManager.DataReady event) {
        try {
            GraphHelper.updateChartWithData(mChart, mColor, "luminosity", event.response, mNoChartData);
        } catch (JSONException|ParseException e) {
            Snackbar.make(getView(), getString(R.string.error_reception_data), Snackbar.LENGTH_LONG).show();
        }
    }
}
