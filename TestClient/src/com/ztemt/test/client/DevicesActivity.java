package com.ztemt.test.client;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListActivity;

import de.ankri.views.Switch;

public class DevicesActivity extends SherlockListActivity implements
        ActionBar.OnNavigationListener, ConnectionListener {

    /**
     * The serialization (saved instance state) Bundle key representing the
     * current dropdown position.
     */
    private static final String SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

    private ConnectionManager mConnectionManager;
    private ConnectionEnabler mConnectionEnabler;
    private Switch mSwitch;

    private static List<MessageEntity> sEntities = new ArrayList<MessageEntity>();
    private DevicesAdapter mAdapter;
    private int mPosition = 0;

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

        mConnectionManager = new ConnectionManager(this);
        mConnectionManager.setMessageListener(this);
        mConnectionEnabler = new ConnectionEnabler(this, mSwitch, mConnectionManager);

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
        mConnectionManager.bindService();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mConnectionEnabler.pause();
        mConnectionManager.unbindService();
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
        mPosition = position;
        filterDevices();
        return true;
    }

    @Override
    public void onConnected() {
        requestDevices();
    }

    @Override
    public void onMessage(MessageEntity message) {
        if (message.getType() == MessageConstants.MSG_REQUEST_DEVICES) {
            sEntities.clear();
            sEntities.addAll(message.getArray("devices"));
            filterDevices();
        }
    }

    @Override
    public void onDisconnected() {
        sEntities.clear();
        filterDevices();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        List<MessageEntity> entities = new ArrayList<MessageEntity>();
        MessageEntity msg1 = new MessageEntity(7001);
        msg1.putInt("id", 1);
        msg1.putString("params", "20000");
        MessageEntity msg2 = new MessageEntity(7002);
        msg2.putInt("id", 2);
        msg2.putString("params", "10000 10");
        MessageEntity msg3 = new MessageEntity(7003);
        msg3.putInt("id", 3);
        msg3.putString("params", "10000 10");
        MessageEntity msg4 = new MessageEntity(7004);
        msg4.putInt("id", 4);
        msg4.putString("params", "10000 10");
        MessageEntity msg5 = new MessageEntity(7005);
        msg5.putInt("id", 5);
        msg5.putString("params", "10000 10");
        MessageEntity msg6 = new MessageEntity(7006);
        msg6.putInt("id", 6);
        msg6.putString("params", "10000 10");
        MessageEntity msg7 = new MessageEntity(7007);
        msg7.putInt("id", 7);
        msg7.putString("params", "10000 10");
        MessageEntity msg8 = new MessageEntity(7008);
        msg8.putInt("id", 8);
        msg8.putString("params", "--ei times 10");

        entities.add(msg1);
        entities.add(msg2);
        entities.add(msg3);
        entities.add(msg4);
        entities.add(msg5);
        entities.add(msg6);
        entities.add(msg7);
        entities.add(msg8);

        MessageEntity entity = new MessageEntity(MessageConstants.MSG_PUBLISH_TASK);
        entity.putString("deviceId", mAdapter.getItem(position).deviceId);
        entity.putBoolean("update", true);
        entity.putArray("tests", entities);
        mConnectionManager.sendMessage(entity);
    }

    private void filterDevices() {
        List<Device> devices = new ArrayList<Device>();
        for (MessageEntity entity : sEntities) {
            Device device = new Device();
            device.online = entity.getInt("online", 0) == 1;
            if (mPosition == 0 && device.online || mPosition == 1
                    && !device.online || mPosition == 2) {
                device.deviceId = entity.getString("deviceId", "0");
                device.platform = entity.getString("platform", "Unknown");
                device.version = entity.getString("version", "");
                device.model = entity.getString("model", "");
                device.baseband = entity.getString("baseband", "");
                device.build = entity.getString("build", "");
                device.address = entity.getString("address", "");
                device.user = entity.getString("user", "");
                device.buildDate = entity.getString("buildDate", "");
                device.ip = entity.getString("ip", "");
                devices.add(device);
            }
        }
        mAdapter.update(devices);
    }

    private void requestDevices() {
        MessageEntity message = new MessageEntity(MessageConstants.MSG_REQUEST_DEVICES);
        mConnectionManager.sendMessage(message);
    }
}
