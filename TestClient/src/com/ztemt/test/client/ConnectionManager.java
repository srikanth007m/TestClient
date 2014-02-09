package com.ztemt.test.client;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.ztemt.test.client.ConnectionService.LocalBinder;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;

public class ConnectionManager {

    private static final String TAG = "ConnectionManager";

    public static final String ACTION_STATE_CHANGED = "com.ztemt.test.client.action.STATE_CHANGED";
    public static final String ACTION_MSG_RECEIVED = "com.ztemt.test.client.action.MSG_RECEIVED";
    public static final String EXTRA_STATE = "state";
    public static final String EXTRA_TEXT = "text";

    public static final int STATE_CONNECTING     = 1;
    public static final int STATE_CONNECT_FAILED = 2;
    public static final int STATE_CONNECT_OPENED = 3;
    public static final int STATE_CONNECT_CLOSED = 4;

    private static ConnectionManager sInstance;
    private Context mContext;
    private WebSocketConnection mConnection;
    private boolean mBound = false;

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocalBinder binder = (LocalBinder) service;
            mConnection = binder.getService().getConnection();
            mBound = true;
            broadcastStateChanged();
        }
    };

    private WebSocketHandler mHandler = new WebSocketHandler() {

        @Override
        public void onOpen() {
            broadcastStateChanged(STATE_CONNECT_OPENED);
            sendMessage(new MessageEntity(MessageConstants.MSG_REQUEST_DEVICES));
        }

        @Override
        public void onTextMessage(String payload) {
            Log.d(TAG, "Recv " + payload);
            broadcastMessageReceived(payload);
        }

        @Override
        public void onClose(int code, String reason) {
            broadcastStateChanged(STATE_CONNECT_CLOSED);
        }
    };

    private ConnectionManager(Context context) {
        mContext = context;
    }

    private void setContext(Context context) {
        mContext = context;
    }

    public static ConnectionManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new ConnectionManager(context);
        } else {
            sInstance.setContext(context);
        }
        return sInstance;
    }

    public void bindService() {
        // Bind to ConnectionService
        Intent intent = new Intent(mContext, ConnectionService.class);
        mContext.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public void unbindService() {
        // Unbind from the service
        if (mBound) {
            mContext.unbindService(mServiceConnection);
            mBound = false;
        }
    }

    public boolean isConnected() {
        if (mBound) {
            return mConnection.isConnected();
        }
        return false;
    }

    public void connect() {
        if (mBound) {
            try {
                broadcastStateChanged(STATE_CONNECTING);
                mConnection.connect(getWebSocketUrl(), mHandler);
            } catch (WebSocketException e) {
                broadcastStateChanged(STATE_CONNECT_FAILED);
                Log.e(TAG, "Failed connect to ws", e);
            }
        }
    }

    public void disconnect() {
        if (mBound && isConnected()) {
            mConnection.disconnect();
        }
    }

    public void sendMessage(MessageEntity entity) {
        if (mBound && isConnected()) {
            mConnection.sendTextMessage(entity.toString());
        }
    }

    private void broadcastStateChanged(int state) {
        Intent i = new Intent(ACTION_STATE_CHANGED).putExtra(EXTRA_STATE, state);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(i);
    }

    private void broadcastStateChanged() {
        broadcastStateChanged(isConnected() ? STATE_CONNECT_OPENED
                : STATE_CONNECT_CLOSED);
    }

    private void broadcastMessageReceived(String text) {
        Intent i = new Intent(ACTION_MSG_RECEIVED).putExtra(EXTRA_TEXT, text);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(i);
    }

    private String getWebSocketUrl() {
        TelephonyManager tm = (TelephonyManager) mContext
                .getSystemService(Context.TELEPHONY_SERVICE);
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("deviceId", tm.getDeviceId()));
        String param = URLEncodedUtils.format(params, HTTP.UTF_8);
        try {
            URI uri = URIUtils.createURI("ws",
                    mContext.getString(R.string.ws_host), 9001, "push", param,
                    null);
            return uri.toString();
        } catch (URISyntaxException e) {
            Log.e(TAG, "getWebSocketUrl", e);
            return "";
        }
    }
}
