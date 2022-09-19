package com.example.minikers_receiver;

//Constants for reading/writing from Firestore database
//Each key is the name of a Firestore field, e.g. deviceName: drawer
public class FirestoreKey {
    public static final String DEVICE_NAME_KEY = "deviceName";
    public static final String DEVICE_ADDRESS_KEY = "deviceAddress";
    public static final String START_TIME_KEY = "startTime";
    public static final String END_TIME_KEY = "endTime";
    public static final String MODE_KEY = "mode";
    public static final String VOLTAGE_KEY = "voltage";
    public static final String CURRENT_X_KEY = "currentXValues";
    public static final String CURRENT_Y_KEY = "currentYValues";
    public static final String ENERGY_KEY = "energy";
    public static final String DEVICES_COLLECTION_KEY = "devices";
}
