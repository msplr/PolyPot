package ch.epfl.pdse.polypotapp;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.support.v7.widget.AppCompatRadioButton;
import android.util.AttributeSet;
import android.widget.RadioGroup;

public class PreferenceColor extends Preference implements Preference.OnPreferenceChangeListener {
    private RadioGroup mRadioGroup1;
    private RadioGroup mRadioGroup2;

    private String mValue;

    public PreferenceColor(Context context) {
        // Delegate to other constructor
        this(context, null);
    }

    public PreferenceColor(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PreferenceColor(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayoutResource(R.layout.layout_preference);
        setWidgetLayoutResource(R.layout.preference_color);
    }

    @Override
    public void onBindViewHolder(final PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        holder.itemView.setClickable(false);

        mRadioGroup1 = (RadioGroup) holder.findViewById(R.id.color1);
        mRadioGroup2 = (RadioGroup) holder.findViewById(R.id.color2);

        AppCompatRadioButton radioButton1 =  mRadioGroup1.findViewWithTag(mValue);
        AppCompatRadioButton radioButton2 =  mRadioGroup1.findViewWithTag(mValue);

        if(radioButton1 != null) {
            radioButton1.setChecked(true);
        } else if(radioButton2 != null) {
            radioButton2.setChecked(true);
        }

        mRadioGroup1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {
                AppCompatRadioButton current = (AppCompatRadioButton) holder.findViewById(id);

                if(current != null && current.isChecked()) {
                    if(mRadioGroup2.getCheckedRadioButtonId() != -1) {
                        mRadioGroup2.clearCheck();
                    }

                    mValue = current.getTag().toString();
                    persistString(mValue);
                }
            }
        });

        mRadioGroup2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {
                AppCompatRadioButton current = (AppCompatRadioButton) holder.findViewById(id);

                if(current != null && current.isChecked()) {
                    if(mRadioGroup1.getCheckedRadioButtonId() != -1) {
                        mRadioGroup1.clearCheck();
                    }

                    mValue = current.getTag().toString();
                    persistString(mValue);
                }
            }
        });
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
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        mValue = (String) newValue;

        AppCompatRadioButton radioButton1 =  mRadioGroup1.findViewWithTag(mValue);
        AppCompatRadioButton radioButton2 =  mRadioGroup1.findViewWithTag(mValue);

        if(radioButton1 != null) {
            radioButton1.setChecked(true);
        } else if(radioButton2 != null) {
            radioButton2.setChecked(true);
        }

        return true;
    }
}