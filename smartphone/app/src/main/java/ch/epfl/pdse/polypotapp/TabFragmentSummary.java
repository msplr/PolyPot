package ch.epfl.pdse.polypotapp;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class TabFragmentSummary extends Fragment {
    private ActivityMain mActivity;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private boolean mLatestRefreshing;
    private boolean mStatsRefreshing;

    private TextView mWaterLevelText;
    private TextView mTemperatureText;
    private TextView mSoilMoistureText;
    private TextView mLuminosityText;
    private TextView mLastWateringText;
    private TextView mPlantStatusText;
    private TextView mLatestDataDateText;

    private HashMap<String, Float> mStats;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mActivity = (ActivityMain) context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_summary, container, false);
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

        mLatestRefreshing = false;
        mStatsRefreshing = false;

        // Save for later use
        mWaterLevelText = view.findViewById(R.id.water_level_text);
        mTemperatureText = view.findViewById(R.id.temperature_text);
        mSoilMoistureText = view.findViewById(R.id.soil_moisture_text);
        mLuminosityText = view.findViewById(R.id.luminosity_text);
        mLastWateringText = view.findViewById(R.id.last_watering_text);
        mPlantStatusText = view.findViewById(R.id.plant_status_text);
        mLatestDataDateText = view.findViewById(R.id.latest_data_date_text);
    }

    @Override
    public void onResume() {
        super.onResume();

        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);

        super.onPause();
    }

    @Subscribe
    public void handleLatestLoading(final CommunicationManager.LatestLoading event) {
        mSwipeRefreshLayout.setRefreshing(true);
        mLatestRefreshing = true;
    }

    @Subscribe
    public void handleStatsLoading(final CommunicationManager.StatsLoading event) {
        mSwipeRefreshLayout.setRefreshing(true);
        mStatsRefreshing = true;
    }

    @Subscribe(sticky = true)
    public void handleLatestResponse(CommunicationManager.LatestResponse event) {
        try {
            // Data part
            HashMap<String, Float> sensorsData = mActivity.getSensorsData();

            mWaterLevelText.setText(Integer.toString(Math.round(sensorsData.get("water_level"))));
            mTemperatureText.setText(Integer.toString(Math.round(sensorsData.get("temperature"))));
            mSoilMoistureText.setText(Integer.toString(Math.round(sensorsData.get("soil_moisture"))));
            mLuminosityText.setText(Integer.toString(Math.round(sensorsData.get("luminosity"))));

            // Date and Time
            HashMap<String, Calendar> datesData = mActivity.getDatesData();

            SimpleDateFormat outputDateFormat = new SimpleDateFormat(getString(R.string.latest_data_date_format), Locale.US);
            mLatestDataDateText.setText(outputDateFormat.format(datesData.get("latest").getTime()));;
        } catch (NullPointerException e) {
            // Reset to default values
            mSoilMoistureText.setText("");
            mTemperatureText.setText("");
            mWaterLevelText.setText("");
            mLuminosityText.setText("");
            mLatestDataDateText.setText(getString(R.string.latest_data_date_unknown));

            // Show an error message
            Snackbar.make(getView(), R.string.reception_latest_error, Snackbar.LENGTH_LONG).show();
        }

        try {
            // Date and Time
            HashMap<String, Calendar> datesData = mActivity.getDatesData();

            SimpleDateFormat outputDateFormat = new SimpleDateFormat(getString(R.string.last_watering_format), Locale.US);
            mLastWateringText.setText(outputDateFormat.format(datesData.get("watering").getTime()));
        } catch (NullPointerException e) {
            // Reset to default values
            mLastWateringText.setText(R.string.last_watering_unknown);

            // Show an error message
            Snackbar.make(getView(), R.string.reception_watering_error, Snackbar.LENGTH_LONG).show();
        }

        mLatestRefreshing = false;
        if(!mStatsRefreshing) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    @Subscribe(sticky = true)
    public void handleStatsResponse(CommunicationManager.StatsResponse event) {
        try {
            mStats = mActivity.getStats();
            updatePlantStatus();
        } catch (NullPointerException e) {
            mPlantStatusText.setText(R.string.plant_status_unknown);

            Snackbar.make(getView(), R.string.reception_stats_error, Snackbar.LENGTH_LONG).show();
        }

        mStatsRefreshing = false;
        if(!mLatestRefreshing) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    @Subscribe
    public void handleConfigurationResponse(CommunicationManager.ConfigurationResponse event) {
        if(event.response != null && event.hint.equals("plant")) {
            try {
                updatePlantStatus();
            } catch (NullPointerException e) {
                mPlantStatusText.setText(R.string.plant_status_unknown);

            }
        }
    }

    private void updatePlantStatus() {
        Plant plant = mActivity.getPlant();

        if(plant.waterLevelMin <= mStats.get("water_level") && mStats.get("water_level") <= plant.waterLevelMax
                && plant.temperatureMin <= mStats.get("temperature") && mStats.get("temperature") <= plant.temperatureMax
                && plant.soilMoistureMin <= mStats.get("soil_moisture") && mStats.get("soil_moisture") <= plant.soilMoistureMax
                && plant.luminosityMin <= mStats.get("luminosity") && mStats.get("luminosity") <= plant.luminosityMax) {
            mPlantStatusText.setText(R.string.plant_status_good);
        } else {
            mPlantStatusText.setText(R.string.plant_status_bad);
        }
    }
}