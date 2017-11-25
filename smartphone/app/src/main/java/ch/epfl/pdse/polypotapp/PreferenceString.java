package ch.epfl.pdse.polypotapp;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.preference.DialogPreference;
import android.util.AttributeSet;

public class PreferenceString extends DialogPreference {
    private String mValue;
    private String mOriginalSummary;

    public PreferenceString(Context context) {
        // Delegate to other constructor
        this(context, null);
    }

    public PreferenceString(Context context, AttributeSet attrs) {
        // Delegate to other constructor
        // Use the preferenceStyle as the default style
        this(context, attrs, R.attr.preferenceStyle);
    }

    public PreferenceString(Context context, AttributeSet attrs, int defStyleAttr) {
        // Delegate to other constructor
        this(context, attrs, defStyleAttr, defStyleAttr);
    }

    public PreferenceString(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        mOriginalSummary = super.getSummary().toString();
    }

    @Override
    public int getDialogLayoutResource() {
        return R.layout.preference_string;
    }

    public String getmValue() {
        return mValue;
    }

    public void setmValue(String v) {
        mValue = v;

        // Save to SharedPreference
        persistString(v);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            // Restore existing state
            mValue = this.getPersistedString("");
        } else {
            // Set default state from the XML attribute
            mValue = (String) defaultValue;
            persistString(mValue);
        }
    }

    @Override
    public CharSequence getSummary() {
        // Add ability to replace %s by current value
        return String.format(super.getSummary().toString(), mValue);
    }

    public CharSequence getmOriginalSummary() {
        // Add ability to replace %s by current value from original summary
        return String.format(mOriginalSummary, mValue);
    }
}