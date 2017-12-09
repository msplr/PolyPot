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
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.YAxis;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;

import java.text.ParseException;

public class TabFragmentSoilMoisture extends Fragment {
    private ActivityMain mActivity;

    private String mServer;
    private String mUUID;

    private LineChart mChart;
    private int mColor;
    private TextView mDescription;

    public static TabFragmentSoilMoisture newInstance(String server, String uuid) {
        TabFragmentSoilMoisture f = new TabFragmentSoilMoisture();

        Bundle args = new Bundle();
        args.putString("server", server);
        args.putString("uuid", uuid);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

       mActivity = (ActivityMain) context;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle args = getArguments();
        mServer = args.getString("server");
        mUUID = args.getString("uuid");

        return inflater.inflate(R.layout.fragment_soil_moisture, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        final SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                CommunicationManager.getDefault(getActivity()).clearCache();
                EventBus.getDefault().post(new CommunicationManager.DataRequest(mServer, mUUID, mActivity.getFromDate(), mActivity.getToDate()));
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        mChart = view.findViewById(R.id.graph_soil_moisture);
        mColor = getResources().getColor(R.color.lightBlue);
        mDescription = view.findViewById(R.id.description_soil_moisture);

        updateLimits();

        GraphHelper.configureChart(mChart, mColor, 0, 100);
        EventBus.getDefault().post(new CommunicationManager.DataRequest(mServer, mUUID, mActivity.getFromDate(), mActivity.getToDate()));

        updateDescription();
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
            GraphHelper.updateChartWithData(mChart, mColor, "soil_moisture", getString(R.string.label_soil_moisture), event.response, mActivity);
        } catch (NullPointerException|JSONException|ParseException e) {
            // Display error on chart
            mChart.clear();
            mChart.setNoDataText(getString(R.string.reception_data_error));

            // Show an error message
            Snackbar.make(getView(), R.string.reception_data_error, Snackbar.LENGTH_LONG).show();
        }
    }

    @Subscribe
    public void handlePreferenceChange(ActivityMain.PreferenceChanged event) {
        if(!event.failed && event.key.equals("plant")) {
            updateDescription();
        } else if(!event.failed && event.key.equals("target_soil_moisture")) {
            updateLimits();
        }
    }

    private void updateDescription() {
        mDescription.setText(mActivity.getPlant().soilMoistureDescription);
    }

    private void updateLimits() {
        int target = mActivity.getSharedPreferences().getInt("target_soil_moisture", 0);

        YAxis leftAxis = mChart.getAxisLeft();
        LimitLine limitLine = new LimitLine(target, getString(R.string.target_soil_moisture_limit));
        limitLine.setLineWidth(1f);
        limitLine.enableDashedLine(2, 2, 0);
        limitLine.setTextSize(4f);
        leftAxis.addLimitLine(limitLine);
    }
}
