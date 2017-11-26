package ch.epfl.pdse.polypotapp;

import android.os.Bundle;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

public class PreferenceSeekBarFragment extends PreferenceDialogFragmentCompat {

    private SeekBar seekBar;
    private TextView text;

    /**
     * Creates a new Instance of the PreferenceSeekBarDialogFragment and stores the key of the
     * related Preference
     *
     * @param key The key of the related Preference
     * @return A new Instance of the PreferenceSeekBarDialogFragment
     */
    public static PreferenceSeekBarFragment newInstance(String key) {
        final PreferenceSeekBarFragment fragment = new PreferenceSeekBarFragment();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);

        return fragment;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        seekBar = (SeekBar) view.findViewById(R.id.seekBar);
        text = (TextView) view.findViewById(R.id.text);

        // Get the value from the related Preference
        Integer value = ((PreferenceSeekBar) getPreference()).getmValue();

        seekBar.setProgress(value);
        text.setText(Integer.toString(value));

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                text.setText(Integer.toString(progress));
            }

            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        //TODO: min/max
    }

    /**
     * Called when the Dialog is closed.
     *
     * @param positiveResult Whether the Dialog was accepted or canceled.
     */
    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            // Generate value to save
            int value = seekBar.getProgress();

            // Save the value
            PreferenceSeekBar preference = (PreferenceSeekBar) getPreference();
            preference.setmValue(value);

            // Update the summary
            preference.setSummary(preference.getmOriginalSummary());

        }
    }
}