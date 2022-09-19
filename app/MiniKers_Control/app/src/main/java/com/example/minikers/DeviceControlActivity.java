package com.example.minikers;



import android.Manifest;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;

import android.os.Bundle;

import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import android.widget.ImageButton;

import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.time.LocalDate;

import java.time.LocalDateTime;
import java.time.LocalTime;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import java.util.Map;
import java.util.UUID;



import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.apache.commons.lang3.ArrayUtils;


public class DeviceControlActivity extends AppCompatActivity {
    private final static String TAG = DeviceControlActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private TextView mConnectionState;
    private TextView mDataField;
    private String mDeviceName;
    private String mDeviceAddress;

    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    public static UUID UART_UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E"); //BLE UART Service
    public static UUID TX_UUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E"); //BLE TX Characteristic
    public static UUID RX_UUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E"); //BLE RX Characteristic
    // UUID for the UART BTLE client characteristic which is necessary for notifications.
    public static UUID CLIENT_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    public static UUID BAT_UUID = UUID.fromString("0000180F-0000-1000-8000-00805F9B34FB"); //BLE Battery Service



    private TextView deviceNameText;

    private Button disconnectButton;
    Button automaticUseButtonOn;
    Button automaticUseButtonOff;
    Button LoadDataButton;

    ImageButton voiceInputButton;
    private SpeechRecognizer sr;



    private String location;

    FirebaseFirestore db; //Holds singleton FirebaseFirestore instance

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState(R.string.connected);
                Log.d(TAG, "Connected to device");
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                Log.d(TAG, "Disconnected from device");
                invalidateOptionsMenu();
                clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                //displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };

    private void clearUI() {
        if(mDataField == null)
            return;
        //mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
        mDataField.setText(R.string.no_data);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_control);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        if(mDeviceName == null)
            mDeviceName = "Sample_device";
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        if(mDeviceAddress == null)
            mDeviceAddress = "AA:BB:CC:DD:EE:FF";

        Log.d(TAG, "Device name is " + mDeviceName);
        deviceNameText = (TextView) findViewById(R.id.deviceName);


        location = "Lab";

        deviceNameText.setText("Device name: " + mDeviceName + "\n Location: " + location);



//        getSupportActionBar().setTitle(mDeviceName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        voiceInputButton = (ImageButton) findViewById(R.id.voiceInputButton);
        voiceInputButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startVoiceInput();
            }
        });

        sr = null;

        setUpButtons();





        db = FirebaseFirestore.getInstance(); //Uses same instance throughout the app's lifecycle, so doesn't create a new one if one already exists


    }



    private void setUpButtons(){
        //Set up calendar and tester buttons for auto/manual use

        //Sample data for testing
        double samplex[] = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        double sampleycurrent[] = {0x01, 0x10, 0x20, 0x6F, 0xFF, 0xEF, 0x7C, 0x69, 0x25, 0x00};
        double sampleycurrent2[] = {0x02, 0x15, 0x24, 0x5C, 0xEF, 0xEF, 0x7F, 0x69, 0x25, 0x00};
        double samplevoltage = 4.02;


        automaticUseButtonOn = (Button) findViewById(R.id.automaticUseButtonOn);
        automaticUseButtonOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onClickOn(); // Writes to bluetooth characteristic

                Log.d(TAG, "Automatic button on");

                //For testing
                addEvent(LocalDate.now(), LocalTime.now(), LocalTime.now().plusSeconds(8), ActuationType.Automatic, samplex, sampleycurrent, samplevoltage, 1.26);
            }
        });

        automaticUseButtonOff = (Button) findViewById(R.id.automaticUseButtonOff);
        automaticUseButtonOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onClickOff(); // Writes to bluetooth characteristic

                Log.d(TAG, "Automatic button off");

                //For testing
                addEvent(LocalDate.now(), LocalTime.now(), LocalTime.now().plusSeconds(8), ActuationType.Automatic, samplex, sampleycurrent, samplevoltage, 1.26);
            }
        });

        //Load data from the board
        LoadDataButton = (Button) findViewById(R.id.LoadDataButton);
        LoadDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Todo: code for reading FRAM data into app
                //Use addEvent() to add usages
                //Get a LocalDate using LocalDate.of(int year, int month, int dayOfMonth)
                //Get a LocalTime using LocalTime.of(int hour, int minute, int second)
                    //or LocalTime.of(int hour, int minute, int second, int nanoOfSecond)
                //Actuation type is either ActuationType.Manual or ActuationType.Automatic
                //xcurrent[] holds the times, ycurrent[] holds the values of the currents


                //LoadData(mDataField);
            }
        });

        disconnectButton = (Button) findViewById(R.id.disconnectButton);
        disconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBluetoothLeService.disconnect();
            }
        });


    }


    private void addEvent(LocalDate date, LocalTime startTime, LocalTime endTime, ActuationType actuationType, double[] xcurrent, double[] ycurrent, double voltage, double energy) {
        //Format the date into a user-friendly string
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        String dateString = date.format(formatter); //Formats into YYYY-MM-DD

        String actuationTypeString = (actuationType == ActuationType.Automatic) ? "Automatic" : "Manual";
        //Todo: error handling for actuationtype
        Log.d(TAG, "Adding a " + actuationTypeString + " event with start time " + startTime.toString());
        Log.d(TAG, "New" + actuationTypeString + " usage");


        Map<String, Object> newUsage = new HashMap<String, Object>();
        newUsage.put(FirestoreKey.DEVICE_ADDRESS_KEY, mDeviceAddress);
        newUsage.put(FirestoreKey.DEVICE_NAME_KEY, mDeviceName);

        LocalDateTime st = date.atTime(startTime);
        ZoneOffset offset = ZoneId.systemDefault().getRules().getOffset(st);
        long stEpoch = st.toEpochSecond(offset) * 1000; //multiply by 1000 to get milliseconds
        newUsage.put(FirestoreKey.START_TIME_KEY, new Date(stEpoch));

        LocalDateTime et = date.atTime(endTime);
        ZoneOffset etoffset = ZoneId.systemDefault().getRules().getOffset(et);
        long etEpoch = et.toEpochSecond(offset) * 1000; //multiply by 1000 to get milliseconds
        newUsage.put(FirestoreKey.END_TIME_KEY, new Date(etEpoch));

        newUsage.put(FirestoreKey.MODE_KEY, actuationTypeString);


        newUsage.put(FirestoreKey.CURRENT_X_KEY, Arrays.asList(ArrayUtils.toObject(xcurrent)));
        newUsage.put(FirestoreKey.CURRENT_Y_KEY, Arrays.asList(ArrayUtils.toObject(ycurrent)));
        newUsage.put(FirestoreKey.VOLTAGE_KEY, voltage);
        newUsage.put(FirestoreKey.ENERGY_KEY, energy);

        db.collection(mDeviceAddress)
                .add(newUsage)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }



    private void setUpSpeechRecognizer() {
        if(sr != null)
            return;


        sr = SpeechRecognizer.createSpeechRecognizer(this);
        Log.d(TAG, "Created new SpeechRecognizer");


        sr.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {
                Log.d(TAG, "Ready for speech");
                Toast t = Toast.makeText(getApplicationContext(), "Start speaking", Toast.LENGTH_SHORT);
                t.show();
            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {
                Toast t = Toast.makeText(getApplicationContext(), "Finished listening", Toast.LENGTH_SHORT);
                t.show();
            }

            @Override
            public void onError(int i) {

            }

            @Override
            public void onResults(Bundle bundle) {
                ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                String command = data.get(0);


                if(command.equalsIgnoreCase("open")) {
                    Log.d(TAG, "Creating automatic event -- ON -- based on voice input");

                    automaticUseButtonOn.performClick();
                }
                else if(command.equalsIgnoreCase("close")) {
                    Log.d(TAG, "Creating automatic event -- OFF -- based on voice input");

                    automaticUseButtonOff.performClick();
                }

            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });

    }


    public void startVoiceInput(){
        //Request microphone permissions
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, R.integer.RECORD_AUDIO_REQUESTCODE);
            return;
        }

        if(sr == null)
            setUpSpeechRecognizer();

        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());


        sr.startListening(i);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == R.integer.RECORD_AUDIO_REQUESTCODE) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startVoiceInput();
            }
            else{
                Toast.makeText(getApplicationContext(), "Microphone access required for voice commands", Toast.LENGTH_SHORT).show();
            }
        }
    }



    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }


    }





    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);

    }

    @Override
    protected void onStop(){
        super.onStop();

        //Destroy the SpeechRecognizer
        if(sr != null) {
            sr.destroy();
            sr = null;
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //mConnectionState.setText(resourceId);
            }
        });
    }

    private void displayData(String data) {
        if (data != null) {
            mDataField.setText(data);
        }
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }


    public void onClickOn(){
        if(mBluetoothLeService != null) {
            mBluetoothLeService.writeCharacteristic(UART_UUID, TX_UUID, 'o'); // On
        }
    }

    public void onClickOff(){
        if(mBluetoothLeService != null) {
            mBluetoothLeService.writeCharacteristic(UART_UUID, TX_UUID,'f'); // Off
        }
    }

    public void LoadData(View v){
        if(mBluetoothLeService != null) {
            mBluetoothLeService.readCharacteristic(UART_UUID, RX_UUID, CLIENT_UUID);
        }
    }


}
