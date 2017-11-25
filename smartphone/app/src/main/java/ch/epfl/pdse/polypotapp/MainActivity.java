package ch.epfl.pdse.polypotapp;

import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.icu.util.GregorianCalendar;
import android.os.Bundle;
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
    
    private GregorianCalendar mDate;
    private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

                if(tab.getPosition() == 0 || tab.getPosition() == 5) {
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
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Save date for internal use
        mDate = new GregorianCalendar();

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

        // Hide date on by default (Summary tab)
        mPreviousDay.setVisible(false);
        mCurrentDay.setVisible(false);
        mNextDay.setVisible(false);

        return true;
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

                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
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
                case 0:
                    TabFragmentSummary tab0 = new TabFragmentSummary();
                    return tab0;
                case 1:
                    TabFragmentWaterLevel tab1 = new TabFragmentWaterLevel();
                    return tab1;
                case 2:
                    TabFragmentTemperature tab2 = new TabFragmentTemperature();
                    return tab2;
                case 3:
                    TabFragmentHumidity tab3 = new TabFragmentHumidity();
                    return tab3;
                case 4:
                    TabFragmentLuminosity tab4 = new TabFragmentLuminosity();
                    return tab4;
                case 5:
                    TabFragmentConfiguration tab5 = new TabFragmentConfiguration();
                    return tab5;
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
                mViewPager.setCurrentItem(1);
                break;
            case R.id.temperature_card:
                mViewPager.setCurrentItem(2);
                break;
            case R.id.humidity_card:
                mViewPager.setCurrentItem(3);
                break;
            case R.id.luminosity_card:
                mViewPager.setCurrentItem(4);
                break;
        }
    }
}
