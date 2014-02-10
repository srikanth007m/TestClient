package com.ztemt.test.client;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class DevicesAdapter extends BaseAdapter {

    private Context mContext;
    private List<Device> mDevices = new ArrayList<Device>();

    public DevicesAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return mDevices.size();
    }

    @Override
    public Device getItem(int position) {
        return mDevices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = new ViewHolder();
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(
                    android.R.layout.simple_list_item_2, null);
            holder.text1 = (TextView) convertView.findViewById(android.R.id.text1);
            holder.text2 = (TextView) convertView.findViewById(android.R.id.text2);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Device device = mDevices.get(position);
        holder.text1.setText(device.deviceId);
        holder.text2.setText(device.ip);
        return convertView;
    }

    public void update(List<Device> devices) {
        mDevices.clear();
        mDevices.addAll(devices);
        notifyDataSetChanged();
    }

    private class ViewHolder {
        TextView text1;
        TextView text2;
    }
}

class Device {
    String deviceId;
    String platform;
    String version;
    String model;
    String baseband;
    String build;
    String address;
    String user;
    String buildDate;
    String ip;
    boolean online;
}
