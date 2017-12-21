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
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.YAxis;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;

import java.text.ParseException;

public class TabFragmentSoilMoisture extends Fragment {
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
        return inflater.inflate(R.layout.fragment_soil_moisture, container, false);
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

        mChart = view.findViewById(R.id.graph_soil_moisture);
        mColor = getResources().getColor(R.color.lightBlue);
        mDescription = view.findViewById(R.id.description_soil_moisture);

        Ad ad = new Ad();
        Button mAdButton = view.findViewById(R.id.ad_button_soil_moisture);
        mAdButton.setText(ad.text);
        mAdButton.setTag(ad.url);
    }

    @Override
    public void onResume() {
        super.onResume();

        EventBus.getDefault().register(this);

        updateLimits();

        GraphHelper.configureChart(mChart, mColor, 0, 100);

        updateDescription();
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);

        super.onPause();
    }

    @Subscribe(sticky = true)
    public void handleDataLoading(final CommunicationManager.DataLoading event) {
        mSwipeRefreshLayout.setRefreshing(true);
    }

    @Subscribe(sticky = true)
    public void handleDataResponse(CommunicationManager.DataResponse event) {
        if(event.response == null) {
            mChart.clear();
            mChart.setNoDataText(getString(R.string.reception_data_error));

            // Show an error message
            Snackbar.make(getView(), R.string.reception_data_error, Snackbar.LENGTH_LONG).show();

            mSwipeRefreshLayout.setRefreshing(false);
            return;
        }

        try {
            GraphHelper.updateChartWithData(mChart, mColor, "soil_moisture", getString(R.string.label_soil_moisture), event.response, mActivity);
        } catch (NullPointerException|JSONException|ParseException e) {
            // Display error on chart
            mChart.clear();
            mChart.setNoDataText(getString(R.string.parsing_data_error));

            // Show an error message
            Snackbar.make(getView(), R.string.parsing_data_error, Snackbar.LENGTH_LONG).show();
        }

        EventBus.getDefault().removeStickyEvent(CommunicationManager.DataLoading.class);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Subscribe
    public void handleConfigurationResponse(CommunicationManager.ConfigurationResponse event) {
        if(event.response != null && event.key.equals("plant")) {
            updateDescription();
        } else if(event.response != null && event.key.equals("target_soil_moisture")) {
            updateLimits();
        }
    }

    private void updateDescription() {
        mDescription.setText(mActivity.getPlant().soilMoistureDescription);
    }

    private void updateLimits() {
        int target = mActivity.getSharedPreferences().getInt("target_soil_moisture", 0);

        YAxis leftAxis = mChart.getAxisLeft();
        LimitLine limitLine = new LimitLine(target, getString(R.string.limit_target_soil_moisture));
        limitLine.setLineWidth(1f);
        limitLine.enableDashedLine(2, 2, 0);
        limitLine.setTextSize(4f);
        leftAxis.addLimitLine(limitLine);
    }
}
