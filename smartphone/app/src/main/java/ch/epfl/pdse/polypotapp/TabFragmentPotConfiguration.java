package ch.epfl.pdse.polypotapp;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;

import static android.content.Context.MODE_PRIVATE;

public class TabFragmentPotConfiguration extends PreferenceFragmentCompat {
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

        PreferenceManager prefMgr = getPreferenceManager();
        prefMgr.setSharedPreferencesName(Pot.getPreferenceName(args.getString("server"), args.getString("uuid")));
        prefMgr.setSharedPreferencesMode(MODE_PRIVATE);

        // Load the preferences from an XML resource
        setPreferencesFromResource(R.xml.pot_configuration, rootKey);
    }
}
