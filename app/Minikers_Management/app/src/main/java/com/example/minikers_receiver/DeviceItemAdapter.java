package com.example.minikers_receiver;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class DeviceItemAdapter extends RecyclerView.Adapter<DeviceItemAdapter.DeviceHolder> {

    Context ctx;
    ArrayList<String> deviceAddresses;
    ArrayList<String> deviceNames;
    private static final String TAG = "DeviceItemAdapter";

    public DeviceItemAdapter(Context ct, ArrayList<String> addressesList, ArrayList<String> deviceNamesList) {
        ctx = ct;
        this.deviceAddresses = new ArrayList<String>();
        this.deviceNames = new ArrayList<String>();
        deviceAddresses = addressesList;
        deviceNames = deviceNamesList;
    }

    //Called by other activities to add new items
    public void addItem(String deviceName, String deviceAddress){
        deviceAddresses.add(deviceAddress);
        deviceNames.add(deviceAddress);

        this.notifyDataSetChanged();
    }

    public boolean removeItem(int position) {
        if(position >= deviceAddresses.size() || position < 0)
            return false;
        deviceAddresses.remove(position);
        deviceNames.remove(position);

        this.notifyDataSetChanged();

        return true;
    }

    //create the holder for an item (row) in a layout
    @Override
    public DeviceHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(ctx); //inflates xml file to java file
        View v = inflater.inflate(R.layout.device_item_recycler, parent, false);
        return new DeviceHolder(v);
    }


    //sets the values for each item in the layout
    //This also runs each time you notify the data set has changed (with notifyDataSetChanged())
    @Override
    public void onBindViewHolder(DeviceHolder holder, int position) {
        String address = deviceAddresses.get(position);
        String name = deviceNames.get(position);
        holder.deviceAddress.setText(address);
        holder.deviceName.setText(name);

        holder.recyclerItemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Clicked on recycler item");
                Intent i = new Intent(ctx, CalendarActivity.class);
                i.putExtra(CalendarActivity.EXTRAS_DEVICE_ADDRESS, address);
                i.putExtra(CalendarActivity.EXTRAS_DEVICE_NAME, name);
                ctx.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        if(deviceAddresses.size() != deviceNames.size())
            return -1;

        return deviceAddresses.size();
    }


    public class DeviceHolder extends RecyclerView.ViewHolder{
        TextView deviceName;
        TextView deviceAddress;
        ImageView icon; //Todo
        RelativeLayout recyclerItemLayout;

        public DeviceHolder(View itemView) {
            super(itemView);
            recyclerItemLayout = (RelativeLayout) itemView.findViewById(R.id.deviceItemLayout);
            deviceName = (TextView) itemView.findViewById(R.id.recyclerItemDeviceName);
            deviceAddress = (TextView) itemView.findViewById(R.id.recyclerItemDeviceAddress);
        }

    }
}