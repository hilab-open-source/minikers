package com.example.minikers_receiver;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;


//Defines the fields for a row of the database table, as well as getter and setter functions
@Entity(tableName = "usage_table")
public class Usage {

    @PrimaryKey (autoGenerate = true)
    private int ID;

    @NonNull
    @ColumnInfo(name="deviceName")
    private String mDeviceName;

    @NonNull
    @ColumnInfo(name="macAddress")
    private String mMacAddress;

    @NonNull
    @ColumnInfo(name = "day")
    private String mDay;

    @NonNull
    @ColumnInfo(name = "startTime")
    private String mStartTime;

    @NonNull
    @ColumnInfo(name = "endTime")
    private String mEndTime;

    //The app gets timeseries data about a current,
    //so mCurrentXValues contains the x-values (time), with commas as separators
    @NonNull
    @ColumnInfo(name = "currentXValues")
    private String mCurrentXValues;

    //Contains the y-values for the timeseries graph with commas as separators
    @NonNull
    @ColumnInfo(name = "currentYValues")
    private String mCurrentYValues;

    @NonNull
    @ColumnInfo(name = "voltage")
    private double mVoltage;

    //Manual or Automatic
    @NonNull
    @ColumnInfo(name = "mode")
    private String mMode;

    @NonNull
    @ColumnInfo(name = "energy")
    private double mEnergy;


    public Usage(@NonNull String macAddress, @NonNull String deviceName, @NonNull String day, @NonNull String startTime, @NonNull String endTime, @NonNull String mode, @NonNull String currentXValues, @NonNull String currentYValues, @NonNull double voltage, @NonNull double energy) {
        this.mDay = day;
        this.mStartTime = startTime;
        this.mEndTime = endTime;
        this.mCurrentXValues = currentXValues;
        this.mCurrentYValues = currentYValues;
        this.mVoltage = voltage;
        this.mMode = mode;
        this.mDeviceName = deviceName;
        this.mMacAddress = macAddress;
        this.mEnergy = energy;
    }


    public String getStartTime(){
        return this.mStartTime;
    }

    public String getEndTime() {
        return this.mEndTime;
    }

    public double getVoltage(){
        return this.mVoltage;
    }


    public String getMode(){
        return this.mMode;
    }

    public String getCurrentXValues() { //Every field needs to either be public or have a getter method -- since mWord is private we made a getter for it.
        return this.mCurrentXValues;
    }

    public String getCurrentYValues() { //Every field needs to either be public or have a getter method -- since mWord is private we made a getter for it.
        return this.mCurrentYValues;
    }

    public String getDay() {
        return mDay;
    }

    public void setID(int id) {
        this.ID = id;
    }
    public int getID() {
        return ID;
    }

    public String getDeviceName(){
        return mDeviceName;
    }

    public String getMacAddress(){
        return mMacAddress;
    }

    public double getEnergy(){
        return mEnergy;
    }

    //Used only for debugging in Android Studio
    public String toString(){
        return "Device mac address: " + mMacAddress + ", device name: " + mDeviceName + ", Day: " + mDay + ", Start time: " + mStartTime + ", End time: " + mEndTime + ", X-values for current: " + mCurrentXValues + ", Y-values for current: "
                + mCurrentYValues + ", Voltage: " + mVoltage + ", mode: " + mMode;
    }


}
