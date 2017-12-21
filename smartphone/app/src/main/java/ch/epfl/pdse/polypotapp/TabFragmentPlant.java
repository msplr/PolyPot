package ch.epfl.pdse.polypotapp;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.HashMap;

public class TabFragmentPlant extends Fragment {
    private ActivityMain mActivity;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private TextView mPlantConfigured;
    private TextView mPlantDescription;

    private TextView mMinWaterLevel;
    private TextView mCurrentWaterLevel;
    private TextView mMaxWaterLevel;
    private TextView mMinTemperature;
    private TextView mCurrentTemperature;
    private TextView mMaxTemperature;
    private TextView mMinSoilMoisture;
    private TextView mCurrentSoilMoisture;
    private TextView mMaxSoilMoisture;
    private TextView mMinLuminosity;
    private TextView mCurrentLuminosity;
    private TextView mMaxLuminosity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mActivity = (ActivityMain) context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        return inflater.inflate(R.layout.fragment_plant, container, false);
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

        mPlantConfigured = view.findViewById(R.id.configured_plant);
        mPlantDescription = view.findViewById(R.id.description_plant);

        mMinWaterLevel = view.findViewById(R.id.min_water_level);
        mCurrentWaterLevel = view.findViewById(R.id.current_water_level);
        mMaxWaterLevel = view.findViewById(R.id.max_water_level);
        mMinTemperature = view.findViewById(R.id.min_temperature);
        mCurrentTemperature = view.findViewById(R.id.current_temperature);
        mMaxTemperature = view.findViewById(R.id.max_temperature);
        mMinSoilMoisture = view.findViewById(R.id.min_soil_moisture);
        mCurrentSoilMoisture = view.findViewById(R.id.current_soil_moisture);
        mMaxSoilMoisture = view.findViewById(R.id.max_soil_moisture);
        mMinLuminosity = view.findViewById(R.id.min_luminosity);
        mCurrentLuminosity = view.findViewById(R.id.current_luminosity);
        mMaxLuminosity = view.findViewById(R.id.max_luminosity);
    }

    @Override
    public void onResume() {
        super.onResume();

        EventBus.getDefault().register(this);

        updateDescription();
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);

        super.onPause();
    }

    @Subscribe(sticky = true)
    public void handleStatsLoading(final CommunicationManager.StatsLoading event) {
        mSwipeRefreshLayout.setRefreshing(true);
    }

    @Subscribe(sticky = true)
    public void handleStatsResponse(CommunicationManager.StatsResponse event) {
        try {
            HashMap<String, Float> stats = mActivity.getStats();

            mCurrentWaterLevel.setText(String.format("%.1f", stats.get("water_level")));
            mCurrentTemperature.setText(String.format("%.1f", stats.get("temperature")));
            mCurrentSoilMoisture.setText(String.format("%.1f", stats.get("soil_moisture")));
            mCurrentLuminosity.setText(String.format("%.1f", stats.get("luminosity")));
        } catch (NullPointerException e) {
            mCurrentWaterLevel.setText("");
            mCurrentTemperature.setText("");
            mCurrentSoilMoisture.setText("");
            mCurrentLuminosity.setText("");
        }

        EventBus.getDefault().removeStickyEvent(CommunicationManager.StatsLoading.class);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Subscribe
    public void handleConfigurationResponse(CommunicationManager.ConfigurationResponse event) {
        if(event.response != null && event.key.equals("plant")) {
            updateDescription();
        }
    }

    private void updateDescription() {
        Plant plant = mActivity.getPlant();

        mPlantConfigured.setText(String.format(getString(R.string.plant_configured), plant.name));
        mPlantDescription.setText(plant.description);


        mMinWaterLevel.setText(valueToText(plant.waterLevelMin));
        mMaxWaterLevel.setText(valueToText(plant.waterLevelMax));

        mMinTemperature.setText(valueToText(plant.temperatureMin));
        mMaxTemperature.setText(valueToText(plant.temperatureMax));

        mMinSoilMoisture.setText(valueToText(plant.soilMoistureMin));
        mMaxSoilMoisture.setText(valueToText(plant.soilMoistureMax));

        mMinLuminosity.setText(valueToText(plant.luminosityMin));
        mMaxLuminosity.setText(valueToText(plant.luminosityMax));
    }

    private String valueToText(float value) {
        if(Float.isInfinite(value)) {
            return "N/A";
        } else {
            return Float.toString(value);
        }
    }
}