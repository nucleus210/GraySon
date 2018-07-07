package com.example.root.grayson.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.root.grayson.R;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;


public class DeviceListAdapter extends ArrayAdapter<BluetoothDevice> {

    private LayoutInflater mLayoutInflater;
    private ArrayList<BluetoothDevice> mDevices;
    private int  mViewResourceId;
    private Intent mIntent;


    DeviceListAdapter(Context context, Intent intent, int tvResourceId, ArrayList<BluetoothDevice> devices){
        super(context, tvResourceId,devices);
        this.mDevices = devices;
        this.mIntent = intent;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mViewResourceId = tvResourceId;
    }

    @SuppressLint({"ViewHolder", "SetTextI18n"})
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        convertView = mLayoutInflater.inflate(mViewResourceId, null);

        BluetoothDevice device = mDevices.get(position);

        if (device != null) {
            TextView deviceName = convertView.findViewById(R.id.tvDeviceName);
            TextView deviceAddress = convertView.findViewById(R.id.tvDeviceAddress);
            TextView rssi_msg = convertView.findViewById(R.id.signal_rssi_text);

            if (deviceName != null) {
                deviceName.setText(device.getName());
            }
            if (deviceAddress != null) {
                deviceAddress.setText(device.getAddress());
            }

            if (rssi_msg != null) {
                try {
                    String action = mIntent.getAction();
                    if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                        int rssi = mIntent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                        rssi_msg.setText(rssi_msg.getText() + " " + rssi + "dBm\n");
                    }
                }catch (NullPointerException w) {
                    Log.e(TAG,"Null signal", w);
                }
            }
        }

        return convertView;
    }
}
