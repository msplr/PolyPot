package ch.epfl.pdse.polypotapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class ActivitySetupProgress extends AppCompatActivity {

    private SharedPreferences mSharedPreferencesSetup;
    private BroadcastReceiver mReceiver;

    private String mName;
    private String mServer;
    private String mUUID;

    private String mSSID;
    private String mPassword;

    private int mTargetSoilMoisture;
    private String mWaterVolumePumped;
    private String mLoggingInterval;
    private String mSendingInterval;
    private HashMap<String, Object> mConfiguration;

    private int mUserId;
    private int mPotId;
    private WifiManager mWifiManager;
    private WifiState mState;
    private boolean mSetupSuccessful;

    private ProgressBar mUUIDCheckOrGenerateProgressBar;
    private ImageView   mUUIDCheckOrGenerateCross;
    private ImageView   mUUIDCheckOrGenerateTick;
    private ProgressBar mWifiDisconnectUserProgressBar;
    private ImageView   mWifiDisconnectUserCross;
    private ImageView   mWifiDisconnectUserTick;
    private ProgressBar mWifiConnectPotProgressBar;
    private ImageView   mWifiConnectPotCross;
    private ImageView   mWifiConnectPotTick;
    private ProgressBar mConfigurationSendProgressBar;
    private ImageView   mConfigurationSendCross;
    private ImageView   mConfigurationSendTick;
    private ProgressBar mWifiDisconnectPotProgressBar;
    private ImageView   mWifiDisconnectPotCross;
    private ImageView   mWifiDisconnectPotTick;
    private ProgressBar mWifiConnectUserProgressBar;
    private ImageView   mWifiConnectUserCross;
    private ImageView   mWifiConnectUserTick;

    private enum WifiState {
        DISCONNECT_USER, CONNECT_POT, GENERATE_UUID, SEND_CONFIGURATION,
        DISCONNECT_POT, RECONNECT_USER, NONE
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_progress);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSharedPreferencesSetup = getSharedPreferences("setup_and_add", Context.MODE_PRIVATE);

        mName = mSharedPreferencesSetup.getString("name", "").trim();
        mServer = mSharedPreferencesSetup.getString("server", "").trim();
        mUUID = mSharedPreferencesSetup.getString("uuid", "").trim();

        mSSID = mSharedPreferencesSetup.getString("ssid", "").trim();
        mPassword = mSharedPreferencesSetup.getString("password", "").trim();

        mTargetSoilMoisture = mSharedPreferencesSetup.getInt("target_soil_moisture", 0);
        mWaterVolumePumped = mSharedPreferencesSetup.getString("water_volume_pumped", null);
        mLoggingInterval = mSharedPreferencesSetup.getString("logging_interval", null);
        mSendingInterval = mSharedPreferencesSetup.getString("sending_interval", null);

        mConfiguration = new HashMap<>();
        mConfiguration.put("target_soil_moisture", mTargetSoilMoisture);
        mConfiguration.put("water_volume_pumped", mWaterVolumePumped);
        mConfiguration.put("logging_interval", mLoggingInterval);
        mConfiguration.put("sending_interval", mSendingInterval);

        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        mUUIDCheckOrGenerateProgressBar = findViewById(R.id.uuid_generate_progressBar);
        mUUIDCheckOrGenerateCross       = findViewById(R.id.uuid_generate_cross);
        mUUIDCheckOrGenerateTick        = findViewById(R.id.uuid_generate_tick);
        mWifiDisconnectUserProgressBar  = findViewById(R.id.wifi_disconnect_user_progressBar);
        mWifiDisconnectUserCross        = findViewById(R.id.wifi_disconnect_user_cross);
        mWifiDisconnectUserTick         = findViewById(R.id.wifi_disconnect_user_tick);
        mWifiConnectPotProgressBar      = findViewById(R.id.wifi_connect_pot_progressBar);
        mWifiConnectPotCross            = findViewById(R.id.wifi_connect_pot_cross);
        mWifiConnectPotTick             = findViewById(R.id.wifi_connect_pot_tick);
        mConfigurationSendProgressBar   = findViewById(R.id.configuration_send_progressBar);
        mConfigurationSendCross         = findViewById(R.id.configuration_send_cross);
        mConfigurationSendTick          = findViewById(R.id.configuration_send_tick);
        mWifiDisconnectPotProgressBar   = findViewById(R.id.wifi_disconnect_pot_progressBar);
        mWifiDisconnectPotCross         = findViewById(R.id.wifi_disconnect_pot_cross);
        mWifiDisconnectPotTick          = findViewById(R.id.wifi_disconnect_pot_tick);
        mWifiConnectUserProgressBar     = findViewById(R.id.wifi_reconnect_user_progressBar);
        mWifiConnectUserCross           = findViewById(R.id.wifi_reconnect_user_cross);
        mWifiConnectUserTick            = findViewById(R.id.wifi_reconnect_user_tick);

        mState = WifiState.NONE;

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();

                switch (action) {
                    case WifiManager.SUPPLICANT_STATE_CHANGED_ACTION:
                        int status = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, 0);

                        if (status == WifiManager.ERROR_AUTHENTICATING) {
                            switch (mState) {
                                case DISCONNECT_USER:
                                    userWifiDisconnected(true);
                                    break;

                                case CONNECT_POT:
                                    potWifiConnected(true);
                                    break;

                                case DISCONNECT_POT:
                                    potWifiDisconnected(true);
                                    break;

                                case RECONNECT_USER:
                                    userWifiReconnected(true);
                                    break;
                            }
                        }
                        break;

                    case WifiManager.NETWORK_STATE_CHANGED_ACTION: {
                        NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

                        if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI && networkInfo.isConnected()) {
                            // Wifi is connected
                            switch (mState) {
                                case CONNECT_POT:
                                    potWifiConnected(!mWifiManager.getConnectionInfo().getSSID().equals("PolyPot"));
                                    break;

                                case RECONNECT_USER:
                                    userWifiReconnected(false);
                                    break;
                            }
                        }
                        break;
                    }
                    case ConnectivityManager.CONNECTIVITY_ACTION: {
                        NetworkInfo networkInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);

                        if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI && !networkInfo.isConnected()) {
                            // Wifi is disconnected
                            switch (mState) {
                                case DISCONNECT_USER:
                                    userWifiDisconnected(false);
                                    break;

                                case DISCONNECT_POT:
                                    potWifiDisconnected(false);
                                    break;
                            }
                        }
                        break;
                    }
                }
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first

        mState = WifiState.NONE;

        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mReceiver, filter);

        EventBus.getDefault().register(this);

        resetView();
    }

    @Override
    protected void onPause() {
        super.onPause();

        EventBus.getDefault().unregister(this);

        unregisterReceiver(mReceiver);
    }

    private void resetView() {
        mUUIDCheckOrGenerateProgressBar.setVisibility(View.INVISIBLE);
        mUUIDCheckOrGenerateCross.setVisibility(View.GONE);
        mUUIDCheckOrGenerateTick.setVisibility(View.GONE);
        mWifiDisconnectUserProgressBar.setVisibility(View.INVISIBLE);
        mWifiDisconnectUserCross.setVisibility(View.GONE);
        mWifiDisconnectUserTick.setVisibility(View.GONE);
        mWifiConnectPotProgressBar.setVisibility(View.INVISIBLE);
        mWifiConnectPotCross.setVisibility(View.GONE);
        mWifiConnectPotTick.setVisibility(View.GONE);
        mConfigurationSendProgressBar.setVisibility(View.INVISIBLE);
        mConfigurationSendCross.setVisibility(View.GONE);
        mConfigurationSendTick.setVisibility(View.GONE);
        mWifiDisconnectPotProgressBar.setVisibility(View.INVISIBLE);
        mWifiDisconnectPotCross.setVisibility(View.GONE);
        mWifiDisconnectPotTick.setVisibility(View.GONE);
        mWifiConnectUserProgressBar.setVisibility(View.INVISIBLE);
        mWifiConnectUserCross.setVisibility(View.GONE);
        mWifiConnectUserTick.setVisibility(View.GONE);
    }

    public void setupClick(View v) {
        mState = WifiState.NONE;
        mSetupSuccessful = true;

        resetView();

        checkOrGenerateUUID();
    }


    private void checkOrGenerateUUID() {
        mState = WifiState.GENERATE_UUID;
        mUUIDCheckOrGenerateProgressBar.setVisibility(View.VISIBLE);

        if(mUUID.isEmpty()) {
            HashMap<String, Object> setup = new HashMap<>();
            setup.put("configuration", mConfiguration);

            JSONObject jsonRequest = new JSONObject(setup);
            EventBus.getDefault().post(new CommunicationManager.SetupRequest(mServer, mUUID, jsonRequest));
        } else {
            EventBus.getDefault().post(new CommunicationManager.LatestRequest(mServer, mUUID));
        }
    }

    @Subscribe
    public void handleSetupData(CommunicationManager.SetupDataReady event) {
        mUUIDCheckOrGenerateProgressBar.setVisibility(View.GONE);
        if(event.response == null) {
            mUUIDCheckOrGenerateCross.setVisibility(View.VISIBLE);

            mWifiDisconnectUserProgressBar.setVisibility(View.GONE);
            mWifiDisconnectUserCross.setVisibility(View.VISIBLE);
            mWifiConnectPotProgressBar.setVisibility(View.GONE);
            mWifiConnectPotCross.setVisibility(View.VISIBLE);
            mConfigurationSendProgressBar.setVisibility(View.GONE);
            mConfigurationSendCross.setVisibility(View.VISIBLE);
            mWifiDisconnectPotProgressBar.setVisibility(View.GONE);
            mWifiDisconnectPotCross.setVisibility(View.VISIBLE);

            mSetupSuccessful = false;

            userWifiReconnected(true);
        } else {
            try {
                mUUID = event.response.getString("uuid");
            } catch (JSONException e) {
                mUUIDCheckOrGenerateCross.setVisibility(View.VISIBLE);

                mWifiDisconnectUserProgressBar.setVisibility(View.GONE);
                mWifiDisconnectUserCross.setVisibility(View.VISIBLE);
                mWifiConnectPotProgressBar.setVisibility(View.GONE);
                mWifiConnectPotCross.setVisibility(View.VISIBLE);
                mConfigurationSendProgressBar.setVisibility(View.GONE);
                mConfigurationSendCross.setVisibility(View.VISIBLE);
                mWifiDisconnectPotProgressBar.setVisibility(View.GONE);
                mWifiDisconnectPotCross.setVisibility(View.VISIBLE);

                mSetupSuccessful = false;

                userWifiReconnected(true);
            }

            mUUIDCheckOrGenerateTick.setVisibility(View.VISIBLE);

            disconnectUserWifi();
        }
    }

    @Subscribe
    public void handleLatestData(CommunicationManager.LatestDataReady event) {
        mUUIDCheckOrGenerateProgressBar.setVisibility(View.GONE);
        if(event.response == null) {
            mUUIDCheckOrGenerateCross.setVisibility(View.VISIBLE);

            mWifiDisconnectUserProgressBar.setVisibility(View.GONE);
            mWifiDisconnectUserCross.setVisibility(View.VISIBLE);
            mWifiConnectPotProgressBar.setVisibility(View.GONE);
            mWifiConnectPotCross.setVisibility(View.VISIBLE);
            mConfigurationSendProgressBar.setVisibility(View.GONE);
            mConfigurationSendCross.setVisibility(View.VISIBLE);
            mWifiDisconnectPotProgressBar.setVisibility(View.GONE);
            mWifiDisconnectPotCross.setVisibility(View.VISIBLE);

            mSetupSuccessful = false;

            userWifiReconnected(true);
        } else {
            //TODO: which configuration to use ? Smartphone or server ?
            mConfigurationSendTick.setVisibility(View.VISIBLE);

            disconnectUserWifi();
        }
    }

    /***** Disconnect from user WiFi *****/

    private void disconnectUserWifi() {
        mState = WifiState.DISCONNECT_USER;
        mWifiDisconnectUserProgressBar.setVisibility(View.VISIBLE);

        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        mUserId = wifiInfo.getNetworkId();

        if (mUserId != -1) {
            boolean ret = mWifiManager.disconnect();

            if(!ret) {
                userWifiDisconnected(true);
            }
        } else {
            userWifiDisconnected(false);
        }
    }

    private void userWifiDisconnected(boolean error) {
        mWifiDisconnectUserProgressBar.setVisibility(View.GONE);
        if (error) {
            mWifiDisconnectUserCross.setVisibility(View.VISIBLE);

            mWifiConnectPotProgressBar.setVisibility(View.GONE);
            mWifiConnectPotCross.setVisibility(View.VISIBLE);
            mConfigurationSendProgressBar.setVisibility(View.GONE);
            mConfigurationSendCross.setVisibility(View.VISIBLE);
            mWifiDisconnectPotProgressBar.setVisibility(View.GONE);
            mWifiDisconnectPotCross.setVisibility(View.VISIBLE);

            mSetupSuccessful = false;

            userWifiReconnected(true);
        } else {
            mWifiDisconnectUserTick.setVisibility(View.VISIBLE);
            connectPotWifi();
        }
    }

    /***** Connect to pot WiFi *****/

    private void connectPotWifi() {
        mState = WifiState.CONNECT_POT;
        mWifiConnectPotProgressBar.setVisibility(View.VISIBLE);

        WifiConfiguration wifiConf = new WifiConfiguration();
        wifiConf.SSID = "\"PolyPot\"";
        wifiConf.preSharedKey = "\"setupPolyPot\"";

        mPotId = mWifiManager.addNetwork(wifiConf);

        if(mPotId == -1) {
            potWifiConnected(true);
        }

        boolean ret = mWifiManager.enableNetwork(mPotId, true);

        if(!ret) {
            potWifiConnected(true);
        }
    }

    private void potWifiConnected(boolean error) {
        mWifiConnectPotProgressBar.setVisibility(View.GONE);
        if (error) {
            mWifiConnectPotCross.setVisibility(View.VISIBLE);

            mConfigurationSendProgressBar.setVisibility(View.GONE);
            mConfigurationSendCross.setVisibility(View.VISIBLE);
            mWifiDisconnectPotProgressBar.setVisibility(View.GONE);
            mWifiDisconnectPotCross.setVisibility(View.VISIBLE);

            mSetupSuccessful = false;

            reconnectUserWifi();
        } else {
            mWifiConnectPotTick.setVisibility(View.VISIBLE);
            sendConfiguration();
        }
    }

    /***** Send configuration *****/

    private void sendConfiguration() {
        mState = WifiState.SEND_CONFIGURATION;
        mConfigurationSendProgressBar.setVisibility(View.VISIBLE);

        HashMap<String, Object> setup = new HashMap<>();
        setup.put("server", mServer);
        setup.put("uuid", mUUID);
        setup.put("ssid", mSSID);
        setup.put("password", mPassword);
        setup.put("configuration", mConfiguration);

        JSONObject jsonRequest = new JSONObject(setup);
        EventBus.getDefault().post(new CommunicationManager.SetupPotRequest(mServer, mUUID, jsonRequest));
    }

    @Subscribe
    public void handleSetupPotData(CommunicationManager.SetupPotDataReady event) {
        mConfigurationSendProgressBar.setVisibility(View.GONE);
        if(event.response == null) {
            mConfigurationSendCross.setVisibility(View.VISIBLE);

            mSetupSuccessful = false;
        } else {
            // Add pot to pots' list
            ArrayList<Pot> pots = Pot.getPots(this);
            pots.add(new Pot(mName, mServer, mUUID));
            Pot.savePots(this, pots);

            // Save config of pot in corresponding preference
            SharedPreferences sharedPreferences = getSharedPreferences(Pot.getPreferenceName(mServer, mUUID), MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("target_soil_moisture", mTargetSoilMoisture);
            editor.putString("water_volume_pumped", mWaterVolumePumped);
            editor.putString("logging_interval", mLoggingInterval);
            editor.putString("sending_interval", mSendingInterval);
            editor.apply();

            editor = mSharedPreferencesSetup.edit();
            editor.putBoolean("pot_added", true);
            editor.apply();

            mConfigurationSendTick.setVisibility(View.VISIBLE);
        }

        disconnectPotWifi();
    }

    /***** Disconnect Pot WiFi *****/

    private void disconnectPotWifi() {
        mState = WifiState.DISCONNECT_POT;
        mWifiDisconnectPotProgressBar.setVisibility(View.VISIBLE);

        boolean ret = mWifiManager.disconnect();

        if(!ret) {
            potWifiDisconnected(true);
        }
    }

    private void potWifiDisconnected(boolean error) {
        mWifiDisconnectPotProgressBar.setVisibility(View.GONE);
        if (error) {
            mWifiDisconnectPotCross.setVisibility(View.VISIBLE);
        } else {
            mWifiDisconnectPotTick.setVisibility(View.VISIBLE);
        }

        reconnectUserWifi();
    }

    /***** Connect back to WiFi *****/

    private void reconnectUserWifi() {
        mState = WifiState.RECONNECT_USER;
        mWifiConnectUserProgressBar.setVisibility(View.VISIBLE);

        mWifiManager.removeNetwork(mPotId);

        if (mUserId != -1) {
            boolean ret = mWifiManager.disconnect();

            if(!ret) {
                userWifiReconnected(true);
            }
        } else {
            userWifiReconnected(false);
        }
    }

    private void userWifiReconnected(boolean error) {
        mWifiConnectUserProgressBar.setVisibility(View.GONE);
        if(error) {
            mWifiConnectUserCross.setVisibility(View.VISIBLE);
        } else {
            mWifiConnectUserTick.setVisibility(View.VISIBLE);
        }

        mState = WifiState.NONE;

        if(mSetupSuccessful) {
            Snackbar.make(findViewById(android.R.id.content), R.string.setup_success, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.finish, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    }).show();
        } else {
            Snackbar.make(findViewById(android.R.id.content), R.string.setup_error, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.edit, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    }).show();
        }
    }
}
