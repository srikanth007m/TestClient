package com.ztemt.test.client;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.client.utils.URIUtils;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class DevicesFragment extends ListFragment {

    // The fragment argument representing the section number for this fragment
    public static final String ARG_SECTION_NUMBER = "section_number";
    private static final String TAG = "DevicesFragment";

    private DevicesAdapter mAdapter;

    private WebSocketConnection mConnection = new WebSocketConnection();

    private WebSocketHandler mHandler = new WebSocketHandler() {

        @Override
        public void onOpen() {
            Log.d(TAG, "onOpen");
            requestDevices();
        }

        @Override
        public void onTextMessage(String payload) {
            Log.d(TAG, "Recv " + payload);
            handleMessage(new MessageEntity(payload));
        }

        @Override
        public void onClose(int code, String reason) {
            Log.d(TAG, "onClose(" + code + ", " + reason + ")");
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new DevicesAdapter(getActivity());
        setListAdapter(mAdapter);
        connect();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.devices, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        requestDevices();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disconnect();
    }

    private void handleMessage(MessageEntity entity) {
        switch (entity.getType()) {
        case MessageConstants.MSG_REQUEST_DEVICES:
            mAdapter.update(entity.getArray("online"));
            break;
        }
    }

    private String getWebSocketUrl() {
        try {
            URI uri = URIUtils.createURI("ws", getString(R.string.ws_host), 9001, "pull", null, null);
            return uri.toString();
        } catch (URISyntaxException e) {
            Log.e(TAG, "getWebSocketUrl", e);
            return "";
        }
    }

    private void connect() {
        try {
            mConnection.connect(getWebSocketUrl(), mHandler);
        } catch (WebSocketException e) {
            Log.e(TAG, "Error connect to web socket", e);
        }
    }

    private boolean isConnected() {
        return mConnection.isConnected();
    }

    private void disconnect() {
        if (isConnected()) {
            mConnection.disconnect();
        }
    }

    private void sendMessage(String text) {
        if (isConnected()) {
            mConnection.sendTextMessage(text);
            Log.d(TAG, "Send " + text);
        }
    }

    private void sendMessage(MessageEntity entity) {
        sendMessage(entity.toString());
    }

    private void requestDevices() {
        sendMessage(new MessageEntity(MessageConstants.MSG_REQUEST_DEVICES));
    }
}
