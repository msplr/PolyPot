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

public class TabFragmentWaterLevel extends Fragment {
    private ActivityMain mActivity;

    private String mServer;
    private String mUUID;

    private LineChart mChart;
    private int mColor;
    private TextView mDescription;

    public static TabFragmentWaterLevel newInstance(String server, String uuid) {
        TabFragmentWaterLevel f = new TabFragmentWaterLevel();

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

        return inflater.inflate(R.layout.fragment_water_level, container, false);
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

        mChart = view.findViewById(R.id.graph_water_level);
        mColor = getResources().getColor(R.color.lightBlue);
        mDescription = view.findViewById(R.id.description_water_level);

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
            GraphHelper.updateChartWithData(mChart, mColor, "water_level", getString(R.string.label_water_level), event.response, mActivity);
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
        }
    }

    private void updateDescription() {
        mDescription.setText(mActivity.getPlant().waterLevelDescription);
    }
}
