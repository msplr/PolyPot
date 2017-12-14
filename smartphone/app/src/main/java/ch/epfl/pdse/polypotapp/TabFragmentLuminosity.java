package ch.epfl.pdse.polypotapp;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;

import java.text.ParseException;

public class TabFragmentLuminosity extends Fragment {
    private ActivityMain mActivity;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private LineChart mChart;
    private int mColor;
    private TextView mDescription;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mActivity = (ActivityMain) context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_luminosity, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        mSwipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mActivity.forceRefresh();
            }
        });

        mChart = view.findViewById(R.id.graph_luminosity);
        mColor = getResources().getColor(R.color.yellow);
        mDescription = view.findViewById(R.id.description_luminosity);
    }

    @Override
    public void onResume() {
        super.onResume();

        EventBus.getDefault().register(this);

        GraphHelper.configureChart(mChart, mColor, 0, 1200);

        updateDescription();
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);

        super.onPause();
    }

    @Subscribe
    public void handleDataLoading(final CommunicationManager.DataLoading event) {
        mSwipeRefreshLayout.setRefreshing(true);
    }

    @Subscribe(sticky = true)
    public void handleDataResponse(CommunicationManager.DataResponse event) {
        try {
            GraphHelper.updateChartWithData(mChart, mColor, "luminosity", getString(R.string.label_luminosity), event.response, mActivity);
        } catch (NullPointerException|JSONException|ParseException e) {
            // Display error on chart
            mChart.clear();
            mChart.setNoDataText(getString(R.string.reception_data_error));

            // Show an error message
            Snackbar.make(getView(), R.string.reception_data_error, Snackbar.LENGTH_LONG).show();
        }

        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Subscribe
    public void handleConfigurationResponse(CommunicationManager.ConfigurationResponse event) {
        if(event.response != null && event.hint.equals("plant")) {
            updateDescription();
        }
    }

    private void updateDescription() {
        mDescription.setText(mActivity.getPlant().luminosityDescription);
    }
}
