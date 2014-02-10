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
    public static final String EXTRA_STATE = "state";

    public static final int STATE_CONNECTING     = 1;
    public static final int STATE_CONNECT_FAILED = 2;
    public static final int STATE_CONNECT_OPENED = 3;
    public static final int STATE_CONNECT_CLOSED = 4;

    private static WebSocketConnection sConnection;
    private boolean mBound = false;

    private Context mContext;
    private ConnectionListener mConnectionListener;

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocalBinder binder = (LocalBinder) service;
            sConnection = binder.getService().getConnection();
            mBound = true;
            broadcastStateChanged();
        }
    };

    private WebSocketHandler mHandler = new WebSocketHandler() {

        @Override
        public void onOpen() {
            broadcastStateChanged(STATE_CONNECT_OPENED);
            if (mConnectionListener != null) {
                mConnectionListener.onConnected();
            }
        }

        @Override
        public void onTextMessage(String payload) {
            Log.d(TAG, "Recv " + payload);
            if (mConnectionListener != null) {
                mConnectionListener.onMessage(new MessageEntity(payload));
            }
        }

        @Override
        public void onClose(int code, String reason) {
            broadcastStateChanged(STATE_CONNECT_CLOSED);
            if (mConnectionListener != null) {
                mConnectionListener.onDisconnected();
            }
        }
    };

    public ConnectionManager(Context context) {
        mContext = context;
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
        if (sConnection != null) {
            return sConnection.isConnected();
        }
        return false;
    }

    public void connect() {
        if (sConnection != null) {
            try {
                broadcastStateChanged(STATE_CONNECTING);
                sConnection.connect(getWebSocketUrl(), mHandler);
            } catch (WebSocketException e) {
                broadcastStateChanged(STATE_CONNECT_FAILED);
                Log.e(TAG, "Failed connect to ws", e);
            }
        }
    }

    public void disconnect() {
        if (isConnected()) {
            sConnection.disconnect();
        }
    }

    public void sendMessage(MessageEntity entity) {
        if (isConnected()) {
            sConnection.sendTextMessage(entity.toString());
        }
    }

    public void setMessageListener(ConnectionListener listener) {
        mConnectionListener = listener;
    }

    private void broadcastStateChanged(int state) {
        Intent i = new Intent(ACTION_STATE_CHANGED).putExtra(EXTRA_STATE, state);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(i);
    }

    private void broadcastStateChanged() {
        broadcastStateChanged(isConnected() ? STATE_CONNECT_OPENED
                : STATE_CONNECT_CLOSED);
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
