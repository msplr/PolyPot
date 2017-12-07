package ch.epfl.pdse.polypotapp;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.widget.SeekBar;
import android.widget.TextView;

public class PreferenceSeekBar extends Preference {
    private SeekBar mSeekBar;
    private TextView mText;

    private final int mMax;
    private int mValue;

    public PreferenceSeekBar(Context context) {
        // Delegate to other constructor
        this(context, null);
    }

    public PreferenceSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PreferenceSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayoutResource(R.layout.layout_preference);
        setWidgetLayoutResource(R.layout.preference_seekbar);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PreferenceSeekBar);
        mMax = a.getInt(R.styleable.PreferenceSeekBar_max, 100); //TODO: min,max,interval
        a.recycle();
    }

    @Override
    public void onBindViewHolder(final PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        holder.itemView.setClickable(false);

        mSeekBar = (SeekBar) holder.findViewById(R.id.seekBar);
        mText = (TextView) holder.findViewById(R.id.text);

        mSeekBar.setProgress(mValue);
        mSeekBar.setMax(mMax);

        mText.setText(Integer.toString(mValue));

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mText.setText(Integer.toString(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mSeekBar.requestFocus();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mValue = seekBar.getProgress();
                persistInt(mValue);
            }
        });
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInteger(index, 0);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            // Restore existing state
            mValue = getPersistedInt(0);
        } else {
            // Set default state from the XML attribute
            mValue = (Integer) defaultValue;
            persistInt(mValue);
        }
    }
}