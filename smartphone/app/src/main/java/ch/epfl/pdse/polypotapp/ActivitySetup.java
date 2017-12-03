package ch.epfl.pdse.polypotapp;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ActivitySetup extends AppCompatActivity {

    private CommunicationManager mCommunicationManager;
    private BroadcastReceiver mReceiver;

    private String mUUID;
    private EditText mSSID;
    private EditText mPassword;
    private EditText mServer;

    private int mUserId;
    private int mPotId;
    private WifiManager mWifiManager;
    private WifiState mState;

    private ProgressBar mWifiPresenceProgressBar;
    private ImageView   mWifiPresenceCross;
    private ImageView   mWifiPresenceTick;
    private ProgressBar mWifiDisconnectUserProgressBar;
    private ImageView   mWifiDisconnectUserCross;
    private ImageView   mWifiDisconnectUserTick;
    private ProgressBar mWifiConnectPotProgressBar;
    private ImageView   mWifiConnectPotCross;
    private ImageView   mWifiConnectPotTick;
    private ProgressBar mUUIDGenerateProgressBar;
    private ImageView   mUUIDGenerateCross;
    private ImageView   mUUIDGenerateTick;
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
        WIFI_PRESENCE, DISCONNECT_USER, CONNECT_POT, GENERATE_UUID, SEND_CONFIGURATION,
        DISCONNECT_POT, RECONNECT_USER, NONE
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        mSSID = findViewById(R.id.ssid_edit);
        mPassword = findViewById(R.id.password_edit);
        mServer = findViewById(R.id.server_edit);

        mServer.setText(preferences.getString("server", ""), TextView.BufferType.EDITABLE);

        String SSID = mWifiManager.getConnectionInfo().getSSID();
        mSSID.setText(SSID.substring(1, SSID.length()-1));

        mWifiPresenceProgressBar       = findViewById(R.id.wifi_presence_progressBar);
        mWifiPresenceCross             = findViewById(R.id.wifi_presence_cross);
        mWifiPresenceTick              = findViewById(R.id.wifi_presence_tick);
        mWifiDisconnectUserProgressBar = findViewById(R.id.wifi_disconnect_user_progressBar);
        mWifiDisconnectUserCross       = findViewById(R.id.wifi_disconnect_user_cross);
        mWifiDisconnectUserTick        = findViewById(R.id.wifi_disconnect_user_tick);
        mWifiConnectPotProgressBar     = findViewById(R.id.wifi_connect_pot_progressBar);
        mWifiConnectPotCross           = findViewById(R.id.wifi_connect_pot_cross);
        mWifiConnectPotTick            = findViewById(R.id.wifi_connect_pot_tick);
        mUUIDGenerateProgressBar       = findViewById(R.id.uuid_generate_progressBar);
        mUUIDGenerateCross             = findViewById(R.id.uuid_generate_cross);
        mUUIDGenerateTick              = findViewById(R.id.uuid_generate_tick);
        mConfigurationSendProgressBar  = findViewById(R.id.configuration_send_progressBar);
        mConfigurationSendCross        = findViewById(R.id.configuration_send_cross);
        mConfigurationSendTick         = findViewById(R.id.configuration_send_tick);
        mWifiDisconnectPotProgressBar  = findViewById(R.id.wifi_disconnect_pot_progressBar);
        mWifiDisconnectPotCross        = findViewById(R.id.wifi_disconnect_pot_cross);
        mWifiDisconnectPotTick         = findViewById(R.id.wifi_disconnect_pot_tick);
        mWifiConnectUserProgressBar    = findViewById(R.id.wifi_reconnect_user_progressBar);
        mWifiConnectUserCross          = findViewById(R.id.wifi_reconnect_user_cross);
        mWifiConnectUserTick           = findViewById(R.id.wifi_reconnect_user_tick);

        mState = WifiState.NONE;

        String[] PERMS_INITIAL={
                Manifest.permission.ACCESS_COARSE_LOCATION,
        };
        ActivityCompat.requestPermissions(this, PERMS_INITIAL, 0); //TODO: handle negative response

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();

                if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) && mState == WifiState.WIFI_PRESENCE) {
                    scanResultsAvailable();
                } else if (action.equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)) {
                    int status = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, 0);

                    if(status == WifiManager.ERROR_AUTHENTICATING) {
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
                } else if(action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                    NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

                    if(networkInfo.getType() == ConnectivityManager.TYPE_WIFI && networkInfo.isConnected()) {
                        // Wifi is connected
                        switch(mState) {
                            case CONNECT_POT:
                                potWifiConnected(false);
                                break;

                            case RECONNECT_USER:
                                userWifiReconnected(false);
                                break;
                        }
                    }
                } else if(action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                    NetworkInfo networkInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);

                    if(networkInfo.getType() == ConnectivityManager.TYPE_WIFI && !networkInfo.isConnected()) {
                        // Wifi is disconnected
                        switch(mState) {
                            case DISCONNECT_USER:
                                userWifiDisconnected(false);
                                break;

                            case DISCONNECT_POT:
                                potWifiDisconnected(false);
                                break;
                        }
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
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mReceiver, filter);

        EventBus.getDefault().register(this);

        mCommunicationManager = new CommunicationManager(this, null);

        resetView();

        if(mWifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLED) {
            Snackbar.make(findViewById(R.id.scrollView), "Please, enable your WiFi (or setup won't work).", Snackbar.LENGTH_INDEFINITE).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        mCommunicationManager.stop();

        EventBus.getDefault().unregister(this);

        unregisterReceiver(mReceiver);
    }

    private void resetView() {
        mWifiPresenceProgressBar.setVisibility(View.INVISIBLE);
        mWifiPresenceCross.setVisibility(View.GONE);
        mWifiPresenceTick.setVisibility(View.GONE);
        mWifiDisconnectUserProgressBar.setVisibility(View.INVISIBLE);
        mWifiDisconnectUserCross.setVisibility(View.GONE);
        mWifiDisconnectUserTick.setVisibility(View.GONE);
        mWifiConnectPotProgressBar.setVisibility(View.INVISIBLE);
        mWifiConnectPotCross.setVisibility(View.GONE);
        mWifiConnectPotTick.setVisibility(View.GONE);
        mUUIDGenerateProgressBar.setVisibility(View.INVISIBLE);
        mUUIDGenerateCross.setVisibility(View.GONE);
        mUUIDGenerateTick.setVisibility(View.GONE);
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
        resetView();

        checkWifiPresence();
    }

    /***** Check WiFi presence *****/

    private void checkWifiPresence() {
        mState = WifiState.WIFI_PRESENCE;
        mWifiPresenceProgressBar.setVisibility(View.VISIBLE);

        mWifiManager.startScan();
    }

    private void scanResultsAvailable() {
        List<ScanResult> wifiList = mWifiManager.getScanResults();
        boolean found = false;
        for (ScanResult wifi : wifiList) {
            if (wifi.SSID.equals("PolyPot")) {
                found = true;
            }
        }

        mWifiPresenceProgressBar.setVisibility(View.GONE);
        if (!found) {
            mWifiPresenceCross.setVisibility(View.VISIBLE);

            mWifiDisconnectUserProgressBar.setVisibility(View.GONE);
            mWifiDisconnectUserCross.setVisibility(View.VISIBLE);
            mWifiConnectPotProgressBar.setVisibility(View.GONE);
            mWifiConnectPotCross.setVisibility(View.VISIBLE);
            mUUIDGenerateProgressBar.setVisibility(View.GONE);
            mUUIDGenerateCross.setVisibility(View.VISIBLE);
            mConfigurationSendProgressBar.setVisibility(View.GONE);
            mConfigurationSendCross.setVisibility(View.VISIBLE);
            mWifiDisconnectPotProgressBar.setVisibility(View.GONE);
            mWifiDisconnectPotCross.setVisibility(View.VISIBLE);
            mWifiConnectUserProgressBar.setVisibility(View.GONE);
            mWifiConnectUserCross.setVisibility(View.VISIBLE);

            mState = WifiState.NONE;
        } else {
            mWifiPresenceTick.setVisibility(View.VISIBLE);
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
            mUUIDGenerateProgressBar.setVisibility(View.GONE);
            mUUIDGenerateCross.setVisibility(View.VISIBLE);
            mConfigurationSendProgressBar.setVisibility(View.GONE);
            mConfigurationSendCross.setVisibility(View.VISIBLE);
            mWifiDisconnectPotProgressBar.setVisibility(View.GONE);
            mWifiDisconnectPotCross.setVisibility(View.VISIBLE);
            mWifiConnectUserProgressBar.setVisibility(View.GONE);
            mWifiConnectUserCross.setVisibility(View.VISIBLE);

            mState = WifiState.NONE;
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
        wifiConf.priority = 1000;

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

            mUUIDGenerateProgressBar.setVisibility(View.GONE);
            mUUIDGenerateCross.setVisibility(View.VISIBLE);
            mConfigurationSendProgressBar.setVisibility(View.GONE);
            mConfigurationSendCross.setVisibility(View.VISIBLE);
            mWifiDisconnectPotProgressBar.setVisibility(View.GONE);
            mWifiDisconnectPotCross.setVisibility(View.VISIBLE);

            reconnectUserWifi();
        } else {
            mWifiConnectPotTick.setVisibility(View.VISIBLE);
            generateUUID();
        }
    }

    /***** Generate UUID *****/

    private void generateUUID() {
        mState = WifiState.GENERATE_UUID;
        mUUIDGenerateProgressBar.setVisibility(View.VISIBLE);

        mUUID = UUID.randomUUID().toString();

        mUUIDGenerateProgressBar.setVisibility(View.GONE);
        // Nothing can go wrong
        mUUIDGenerateTick.setVisibility(View.VISIBLE);

        sendConfiguration();
    }

    /***** Send configuration *****/

    private void sendConfiguration() {
        mState = WifiState.SEND_CONFIGURATION;
        mConfigurationSendProgressBar.setVisibility(View.VISIBLE);

        mSSID = findViewById(R.id.ssid_edit);
        mPassword = findViewById(R.id.password_edit);
        mServer = findViewById(R.id.server_edit);

        HashMap<String, Object> configuration = new HashMap<>();
        configuration.put("ssid", mSSID.getText().toString());
        configuration.put("password", mPassword.getText().toString());
        configuration.put("server", mServer.getText().toString());
        configuration.put("uuid", mUUID);

        JSONObject jsonRequest = new JSONObject(configuration);
        EventBus.getDefault().post(new CommunicationManager.Request(CommunicationManager.RequestType.POST_SETUP, jsonRequest));
    }

    @Subscribe
    public void handleSetupData(CommunicationManager.SetupDataReady event) {
        mConfigurationSendProgressBar.setVisibility(View.GONE);
        if(event.response == null) {
            mConfigurationSendCross.setVisibility(View.VISIBLE);
        } else {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("server", mServer.getText().toString());
            editor.putString("uuid", mUUID);
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
    }
}
