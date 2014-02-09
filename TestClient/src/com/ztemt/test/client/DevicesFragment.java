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

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;

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
        int state = DevicesAdapter.STATE_UNKNOWN;
        Bundle args = getArguments();
        if (args != null && args.containsKey(ARG_SECTION_NUMBER)) {
            state = args.getInt(ARG_SECTION_NUMBER);
        }
        mAdapter = new DevicesAdapter(getActivity());
        setListAdapter(mAdapter);
        //connect();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.devices, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        //requestDevices();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //disconnect();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
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
        sendMessage(entity);
    }

    private void handleMessage(MessageEntity entity) {
        switch (entity.getType()) {
        case MessageConstants.MSG_REQUEST_DEVICES:
            mAdapter.update(entity.getArray("devices"));
            break;
        }
    }

    private boolean isConnected() {
        return mConnection.isConnected();
    }

    private void sendMessage(String text) {
        if (isConnected()) {
            try {
                mConnection.sendTextMessage(text);
                Log.d(TAG, "Send " + text);
            } catch (NullPointerException e) {
                Log.e(TAG, "WebSocketConnection internal error");
            }
        }
    }

    private void sendMessage(MessageEntity entity) {
        sendMessage(entity.toString());
    }

    private void requestDevices() {
        sendMessage(new MessageEntity(MessageConstants.MSG_REQUEST_DEVICES));
    }
}
