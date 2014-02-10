package com.ztemt.test.client;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.CompoundButton;
import de.ankri.views.Switch;

public class ConnectionEnabler implements CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "ConnectionEnabler";

    private Context mContext;
    private Switch mSwitch;
    private boolean mValidListener;

    private ConnectionManager mCM;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ConnectionManager.ACTION_STATE_CHANGED.equals(action)) {
                int state = intent.getIntExtra(ConnectionManager.EXTRA_STATE, 0);
                switch (state) {
                case ConnectionManager.STATE_CONNECT_CLOSED:
                    setChecked(false);
                    break;
                case ConnectionManager.STATE_CONNECT_OPENED:
                    setChecked(true);
                    break;
                case ConnectionManager.STATE_CONNECT_FAILED:
                    setChecked(false);
                    break;
                case ConnectionManager.STATE_CONNECTING:
                    Log.d(TAG, "CONNECTION_STATE_CONNECTING");
                    break;
                }
            }
        }
    };

    public ConnectionEnabler(Context context, Switch switch_, ConnectionManager cm) {
        mContext = context;
        mSwitch = switch_;
        mValidListener = false;
        mCM = cm;
        context.startService(new Intent(context, ConnectionService.class));
    }

    public void resume() {
        mSwitch.setEnabled(!ActivityManager.isUserAMonkey());
        mSwitch.setOnCheckedChangeListener(this);
        mValidListener = true;
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mReceiver,
                new IntentFilter(ConnectionManager.ACTION_STATE_CHANGED));
    }

    public void pause() {
        mSwitch.setOnCheckedChangeListener(null);
        mValidListener = false;
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mReceiver);
    }

    public void setSwitch(Switch switch_) {
        if (mSwitch == switch_) return;
        mSwitch.setOnCheckedChangeListener(null);
        mSwitch = switch_;
        mSwitch.setOnCheckedChangeListener(mValidListener ? this : null);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            mCM.connect();
        } else {
            mCM.disconnect();
        }
    }

    private void setChecked(boolean isChecked) {
        if (isChecked != mSwitch.isChecked()) {
            // set listener to null, so onCheckedChanged won't be called
            // if the checked status on Switch isn't changed by user click
            if (mValidListener) {
                mSwitch.setOnCheckedChangeListener(null);
            }
            mSwitch.setChecked(isChecked);
            if (mValidListener) {
                mSwitch.setOnCheckedChangeListener(this);
            }
        }
    }
}
