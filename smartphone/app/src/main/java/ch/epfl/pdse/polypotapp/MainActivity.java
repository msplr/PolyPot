package ch.epfl.pdse.polypotapp;

import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.icu.util.GregorianCalendar;
import android.icu.util.TimeZone;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParsePosition;

public class MainActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private MenuItem mPreviousDay;
    private MenuItem mCurrentDay;
    private MenuItem mNextDay;

    private Calendar mDate;
    private SimpleDateFormat mDateFormat = new SimpleDateFormat("YYYY-MM-dd");

    private CommunicationManager mCommunicationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Use today by default
        mDate = GregorianCalendar.getInstance();

        // Set everything more specific than day to zero
        mDate.set(Calendar.HOUR_OF_DAY, 0);
        mDate.set(Calendar.MINUTE, 0);
        mDate.set(Calendar.SECOND, 0);
        mDate.set(Calendar.MILLISECOND, 0);

        mCommunicationManager = CommunicationManager.getInstance(this);
        mCommunicationManager.updateContext(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager){
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                super.onTabSelected(tab);

                // In case onPrepareOptionsMenu was not currently called
                if(mCurrentDay != null) {
                    if(tab.getPosition() == Tabs.SUMMARY || tab.getPosition() == Tabs.CONFIGURATION) {
                        // Hide date on Summary and Configuration tabs
                        mPreviousDay.setVisible(false);
                        mCurrentDay.setVisible(false);
                        mNextDay.setVisible(false);
                    } else {
                        // Show date on others
                        mPreviousDay.setVisible(true);
                        mCurrentDay.setVisible(true);
                        mNextDay.setVisible(true);
                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        final CommunicationManager.SummaryDataReadyListener listener = new CommunicationManager.SummaryDataReadyListener() {
            public void onDataReady(JSONObject summaryData) {
                try {
                    // Switch to latest data date
                    SimpleDateFormat inputDateFormat = new SimpleDateFormat("YYYY-MM-dd'T'HH:mm:ssXXXXX");
                    inputDateFormat.parse(summaryData.getString("datetime"), mDate, new ParsePosition(0));
                    mDate.setTimeZone(TimeZone.getDefault());

                    // Set everything more specific than day to zero
                    mDate.set(Calendar.HOUR_OF_DAY, 0);
                    mDate.set(Calendar.MINUTE, 0);
                    mDate.set(Calendar.SECOND, 0);
                    mDate.set(Calendar.MILLISECOND, 0);

                    // Update date in toolbar
                    mCurrentDay.setTitle(mDateFormat.format(mDate));

                    // Only switch the first time
                    mCommunicationManager.removeSummaryDataReadyListener("mainActivityListener");

                    // Update graphs
                    mCommunicationManager.getData();
                } catch (final JSONException e) {
                    Snackbar.make(getView(), getString(R.string.error_reception_summary), Snackbar.LENGTH_LONG).show();
                }
            }
        };
        mCommunicationManager.addSummaryDataReadyListener("mainActivityListener", listener);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        // Save reference to date in toolbar
        mPreviousDay = menu.findItem(R.id.previous_day);
        mCurrentDay = menu.findItem(R.id.current_day);
        mNextDay = menu.findItem(R.id.next_day);

        // Update date in toolbar
        mCurrentDay.setTitle(mDateFormat.format(mDate));

        // Hide date if on Summary and Configuration tabs
        if(mViewPager.getCurrentItem() == Tabs.SUMMARY || mViewPager.getCurrentItem() == Tabs.CONFIGURATION) {
            mPreviousDay.setVisible(false);
            mCurrentDay.setVisible(false);
            mNextDay.setVisible(false);
        }

        return true;
    }

    // For CommunicationManager
    public Calendar getDate() {
        return mDate;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.previous_day:
                // Remove one day
                mDate.add(Calendar.DAY_OF_MONTH, -1);

                // Update date in toolbar
                mCurrentDay.setTitle(mDateFormat.format(mDate));

                // Update data and graphs
                mCommunicationManager.getData();

                return true;

            case R.id.current_day:
                // Let the user pick the date
                DialogFragment datePicker = new DatePickerFragment(mCurrentDay, mDate, mDateFormat);
                datePicker.show(getSupportFragmentManager(), "datePicker");

                return true;

            case R.id.next_day:
                // Add one day
                mDate.add(Calendar.DAY_OF_MONTH, 1);

                // Update date in toolbar
                mCurrentDay.setTitle(mDateFormat.format(mDate));

                // Update data and graphs
                mCommunicationManager.getData();

                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    // Fet tabs from Fragment or elsewhere
    public View getView() {
        return mViewPager;
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            switch (position) {
                case Tabs.SUMMARY:
                    return new TabFragmentSummary();
                case Tabs.WATER_LEVEL:
                    return new TabFragmentWaterLevel();
                case Tabs.TEMPERATURE:
                    return new TabFragmentTemperature();
                case Tabs.HUMIDITY:
                    return new TabFragmentSoilMoisture();
                case Tabs.LUMINOSITY:
                    return new TabFragmentLuminosity();
                case Tabs.CONFIGURATION:
                    return new TabFragmentConfiguration();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            // Show 5 total pages.
            return 6;
        }
    }

    // Switch to the good tab when the user press on a CardView
    public void cardClick(View v) {
        switch(v.getId()) {
            case R.id.water_level_card:
                mViewPager.setCurrentItem(Tabs.WATER_LEVEL);
                break;
            case R.id.temperature_card:
                mViewPager.setCurrentItem(Tabs.TEMPERATURE);
                break;
            case R.id.soil_moisture_card:
                mViewPager.setCurrentItem(Tabs.HUMIDITY);
                break;
            case R.id.luminosity_card:
                mViewPager.setCurrentItem(Tabs.LUMINOSITY);
                break;
        }
    }
}
