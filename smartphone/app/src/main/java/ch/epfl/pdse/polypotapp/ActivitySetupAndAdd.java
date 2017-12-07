package ch.epfl.pdse.polypotapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.UUID;

public class ActivitySetupAndAdd extends AppCompatActivity {
    private SharedPreferences mSharedPreferencesSetup;

    private ArrayList<Pot> mPots;
    private Pot potToAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_and_add);
        getSupportFragmentManager().beginTransaction().replace(R.id.container, new FragmentSetupAndAddConfiguration()).commit();

        mSharedPreferencesSetup = getSharedPreferences("setup_and_add", Context.MODE_PRIVATE);

        // If a pot was added, immediately go back to home screen
        if(mSharedPreferencesSetup.getBoolean("pot_added", false)) {
            SharedPreferences.Editor editor = mSharedPreferencesSetup.edit();
            editor.putBoolean("pot_added", false);
            editor.apply();
            finish();
        }

        mPots = Pot.getPots(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Avoid keyboard appearing when settings show up
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    protected void onResume() {
        super.onResume();

        EventBus.getDefault().register(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_setup_and_add, menu);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();

        EventBus.getDefault().unregister(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.setup_and_add:
                setupAndAdd(false);
                return true;

            case R.id.add_only:
                setupAndAdd(true);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    private void setupAndAdd(boolean addOnly) {
        String name = mSharedPreferencesSetup.getString("name", "").trim();
        String server = mSharedPreferencesSetup.getString("server", "").trim();
        String uuid = mSharedPreferencesSetup.getString("uuid", "").trim();

        String ssid = mSharedPreferencesSetup.getString("ssid", "").trim();
        String password = mSharedPreferencesSetup.getString("password", "").trim();

        if(name.isEmpty()) {
            Snackbar.make(findViewById(android.R.id.content), R.string.empty_name, Snackbar.LENGTH_LONG).show();
            return;
        } else if(server.isEmpty() || (!server.startsWith("http://") && !server.startsWith("https://"))) {
            Snackbar.make(findViewById(android.R.id.content), R.string.empty_or_invalid_server, Snackbar.LENGTH_LONG).show();
            return;
        } else if(!uuid.isEmpty()) {
            try {
                UUID.fromString(uuid);
            } catch (IllegalArgumentException e) {
                Snackbar.make(findViewById(android.R.id.content), R.string.invalid_uuid, Snackbar.LENGTH_LONG).show();
                return;
            }

            if(Pot.exists(mPots, server, uuid)) {
                Snackbar.make(findViewById(android.R.id.content), R.string.pot_already_exists, Snackbar.LENGTH_LONG).show();
                return;
            }
        }

        if(addOnly) {
            if(uuid.isEmpty()) {
                Snackbar.make(findViewById(android.R.id.content), R.string.empty_uuid, Snackbar.LENGTH_LONG).show();
                return;
            }

            potToAdd = new Pot(name, server, uuid);

            // Check server and UUID
            EventBus.getDefault().post(new CommunicationManager.LatestRequest(server, uuid));
        } else {
            if(ssid.isEmpty()) {
                Snackbar.make(findViewById(android.R.id.content), R.string.empty_ssid, Snackbar.LENGTH_LONG).show();
                return;
            } else if(password.isEmpty()) {
                Snackbar.make(findViewById(android.R.id.content), R.string.empty_password, Snackbar.LENGTH_LONG).show();
                return;
            }

            // Show setup progress screen
            Intent intent = new Intent(this, ActivitySetupProgress.class);
            startActivity(intent);
        }
    }

    @Subscribe
    public void handleLatestData(CommunicationManager.LatestDataReady event) {
        if(event.response == null) {
            // The pot doesn't exist, or the server didn't respond.
            Snackbar.make(findViewById(android.R.id.content), R.string.uuid_or_server_not_found, Snackbar.LENGTH_LONG).show();
        } else {
            // Add pot to list and save list
            mPots.add(potToAdd);
            Pot.savePots(this, mPots);

            // Create config of pot in corresponding preference
            //TODO: which configuration to use ? Default, smartphone or server ?
            PreferenceManager.setDefaultValues(this, Pot.getPreferenceName(potToAdd.server, potToAdd.uuid), MODE_PRIVATE, R.xml.pot_configuration, true);

            // Go back to home screen
            finish();
        }
    }
}
