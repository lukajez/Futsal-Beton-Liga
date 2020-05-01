package com.example.mosis;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class DevicesListAdapter extends ArrayAdapter<BluetoothDevice> {

    TextView devUser, devAddress;
    private LayoutInflater layoutInflater;
    private ArrayList<BluetoothDevice> devices;
    private int resId;

    BluetoothAdapter mBlueAdapter;

    public DevicesListAdapter(Context context, int resId, ArrayList<BluetoothDevice> devices) {

        super(context, resId, devices);
        this.devices = devices;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.resId = resId;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = layoutInflater.inflate(resId, null);

        final BluetoothDevice device = devices.get(position);
        mBlueAdapter = BluetoothAdapter.getDefaultAdapter();

        if (device != null)
        {
            devUser = convertView.findViewById(R.id.txt_device_user);
            devAddress = convertView.findViewById(R.id.txt_device_address);

            Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/adventproregular.ttf");
            devUser.setTypeface(typeface);
            devAddress.setTypeface(typeface);


            if (devUser != null)
                devUser.setText(device.getName());
            if (devAddress != null)
                devAddress.setText(device.getAddress());
        }

        return convertView;
    }




}
