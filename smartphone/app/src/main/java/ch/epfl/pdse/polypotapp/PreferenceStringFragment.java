package ch.epfl.pdse.polypotapp;

import android.os.Bundle;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView.BufferType;

public class PreferenceStringFragment extends PreferenceDialogFragmentCompat {

    private EditText mStringEditText;

    /**
     * Creates a new Instance of the PreferenceEditTextDialogFragment and stores the key of the
     * related Preference
     *
     * @param key The key of the related Preference
     * @return A new Instance of the PreferenceEditTextDialogFragment
     */
    public static PreferenceStringFragment newInstance(String key) {
        final PreferenceStringFragment fragment = new PreferenceStringFragment();
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

        mStringEditText = view.findViewById(R.id.string);

        // Get the value from the related Preference
        String value = ((PreferenceString) getPreference()).getValue();

        mStringEditText.setText(value, BufferType.EDITABLE);
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
            String value = mStringEditText.getText().toString();

            // Save the value
            PreferenceString preference = (PreferenceString) getPreference();
            preference.setValue(value);

            // Update the summary
            preference.setSummary(preference.getOriginalSummary());

        }
    }
}