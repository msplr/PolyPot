package ch.epfl.pdse.polypotapp;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class TabFragmentPlant extends Fragment {
    private ActivityMain mActivity;

    private String mServer;
    private String mUUID;

    private TextView mPlantConfigured;
    private TextView mPlantDescription;

    public static TabFragmentPlant newInstance(String server, String uuid) {
        TabFragmentPlant f = new TabFragmentPlant();

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

        return inflater.inflate(R.layout.fragment_plant, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        mPlantConfigured = view.findViewById(R.id.configured_plant);
        mPlantDescription = view.findViewById(R.id.description_plant);

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
    public void handlePreferenceChange(ActivityMain.PreferenceChanged event) {
        if(!event.failed && event.key.equals("plant")) {
            updateDescription();
        }
    }

    private void updateDescription() {
        Plant plant = mActivity.getPlant();
        mPlantConfigured.setText(String.format(getString(R.string.plant_configured), plant.name));
        mPlantDescription.setText(plant.description);
    }
}