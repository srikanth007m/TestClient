package com.ztemt.test.client;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import de.tavendo.autobahn.WebSocketConnection;

public class ConnectionService extends Service {

    //private static final String TAG = "ConnectionService";

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    // Web socket connection
    private WebSocketConnection mConnection = new WebSocketConnection();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public WebSocketConnection getConnection() {
        return mConnection;
    }

    public class LocalBinder extends Binder {
        public ConnectionService getService() {
            return ConnectionService.this;
        }
    }
}
