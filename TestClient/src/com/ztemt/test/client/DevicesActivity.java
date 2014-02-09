package com.ztemt.test.client;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.ArrayAdapter;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListActivity;

import de.ankri.views.Switch;

public class DevicesActivity extends SherlockListActivity implements
        ActionBar.OnNavigationListener {

    /**
     * The serialization (saved instance state) Bundle key representing the
     * current dropdown position.
     */
    private static final String SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

    private ConnectionEnabler mConnectionEnabler;
    private Switch mSwitch;

    private DevicesAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.devices);

        // Set up the action bar to show a dropdown list.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        // Set up the dropdown list navigation in the action bar.
        actionBar.setListNavigationCallbacks(
        // Specify a SpinnerAdapter to populate the dropdown list.
                new ArrayAdapter<String>(this,
                        android.R.layout.simple_list_item_1,
                        android.R.id.text1, new String[] {
                                getString(R.string.title_online),
                                getString(R.string.title_offline),
                                getString(R.string.title_all) }), this);

        // Set up the switch
        final int padding = getResources().getDimensionPixelSize(
                R.dimen.action_bar_switch_padding);
        mSwitch = new Switch(this);
        mSwitch.setPadding(0, 0, padding, 0);

        mConnectionEnabler = new ConnectionEnabler(this, mSwitch);

        mAdapter = new DevicesAdapter(this);
        setListAdapter(mAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(mSwitch, new ActionBar.LayoutParams(
                ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER_VERTICAL | Gravity.RIGHT));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mConnectionEnabler.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mConnectionEnabler.pause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        getSupportActionBar().setDisplayOptions(0, ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(null);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Restore the previously serialized current dropdown position.
        if (savedInstanceState.containsKey(SELECTED_NAVIGATION_ITEM)) {
            getSupportActionBar().setSelectedNavigationItem(
                    savedInstanceState.getInt(SELECTED_NAVIGATION_ITEM));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Serialize the current dropdown position.
        outState.putInt(SELECTED_NAVIGATION_ITEM, getSupportActionBar()
                .getSelectedNavigationIndex());
    }

    @Override
    public boolean onNavigationItemSelected(int position, long id) {
        // When the given dropdown item is selected, show its contents in the
        // container view.
        /*DevicesFragment fragment = new DevicesFragment();
        Bundle args = new Bundle();
        args.putInt(DevicesFragment.ARG_SECTION_NUMBER, position);
        fragment.setArguments(args);
        getSupportFragmentManager().beginTransaction().replace(R.id.container,
                fragment).commit();*/
        Log.d("ConnectionManager", "position=" + position);
        return true;
    }
}
