package com.ztemt.test.client;

public interface ConnectionListener {

    public void onConnected();

    public void onMessage(MessageEntity message);

    public void onDisconnected();
}
