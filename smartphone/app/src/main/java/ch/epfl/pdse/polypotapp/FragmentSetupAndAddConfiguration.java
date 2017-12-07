package ch.epfl.pdse.polypotapp;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;

import static android.content.Context.MODE_PRIVATE;

public class FragmentSetupAndAddConfiguration extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        PreferenceManager prefMgr = getPreferenceManager();
        prefMgr.setSharedPreferencesName("setup_and_add");
        prefMgr.setSharedPreferencesMode(MODE_PRIVATE);

        // Load the preferences from an XML resource
        setPreferencesFromResource(R.xml.setup_and_add_configuration, rootKey);
        addPreferencesFromResource(R.xml.pot_configuration);
    }
}
