package ch.epfl.pdse.polypotapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;

import static android.content.Context.MODE_PRIVATE;

public class TabFragmentPotConfiguration extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static TabFragmentPotConfiguration newInstance(String server, String uuid) {
        TabFragmentPotConfiguration f = new TabFragmentPotConfiguration();

        Bundle args = new Bundle();
        args.putString("server", server);
        args.putString("uuid", uuid);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Bundle args = getArguments();

        PreferenceManager preferenceManager = getPreferenceManager();
        preferenceManager.setSharedPreferencesName(Pot.getPreferenceName(args.getString("server"), args.getString("uuid")));
        preferenceManager.setSharedPreferencesMode(MODE_PRIVATE);

        // Load the preferences from an XML resource
        setPreferencesFromResource(R.xml.pot_configuration, rootKey);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    // Update preference UI
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        findPreference(key).callChangeListener(sharedPreferences.getAll().get(key));
    }
}
