package com.example.minikers;



import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import androidx.lifecycle.ViewModelProvider;
import androidx.sqlite.db.SimpleSQLiteQuery;


import com.dropbox.core.android.Auth;
import com.dropbox.core.json.JsonReadException;
import com.dropbox.core.oauth.DbxCredential;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import java.io.OutputStream;
import java.time.LocalDate;

import java.time.LocalTime;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.List;
import java.util.Locale;

import java.util.UUID;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;

import com.dropbox.core.v2.files.WriteMode;
import com.dropbox.core.v2.users.FullAccount;



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

    public static UsageViewModel mUsageViewModel;


    private DbxClientV2 mDbxClient;

    private static final String APP_KEY = "cok1ath2jf73h6u";
    private ArrayList<String> app_scope;


    private String location;

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

        mUsageViewModel = new ViewModelProvider(this).get(UsageViewModel.class);


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


        app_scope = new ArrayList<String>();
        app_scope.add("account_info.read");
        app_scope.add("files.metadata.read");
        app_scope.add("files.metadata.write");
        app_scope.add("files.content.read");
        app_scope.add("files.content.write");



    }

    private void uploadDatabaseToDropbox(DbxCredential credentials) {
        String databasePath = "/data/data/com.example.minikers/databases/word_database";
        File dbFile = new File(databasePath);
        if(!dbFile.exists())
            return;

        Thread networkThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try  {
                    DbxRequestConfig config = DbxRequestConfig.newBuilder("MiniKers/1.0").build();
                    mDbxClient = new DbxClientV2(config, credentials);
                    FullAccount account = mDbxClient.users().getCurrentAccount();
                    Log.d(TAG, "Account: " + account.getName().getDisplayName());

                    uploadFile(dbFile);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        networkThread.start();

    }

    protected FileMetadata uploadFile(File fileToUpload) {
        try {

            String remoteFolderPath = "";

            // Note - this is not ensuring the name is a valid dropbox file name
            String remoteFileName = fileToUpload.getName();
            try (InputStream inputStream = new FileInputStream(fileToUpload)) {
                return mDbxClient.files().uploadBuilder(remoteFolderPath + "/" + remoteFileName)
                        .withMode(WriteMode.OVERWRITE)
                        .uploadAndFinish(inputStream);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        catch(Exception e){
            e.printStackTrace();
        }

        return null;
    }

    public static boolean exportDB(Context context) {
        String DATABASE_NAME = "word-database";
        String databasePath = "/data/data/com.example.minikers/databases/word_database";
        Log.d(TAG, "Database path is " + databasePath);
        String inFileName = databasePath;
        try {
            File dbFile = new File(inFileName);
            if(!dbFile.exists())
                Log.d(TAG, "dbFile doesn't exist");
            else {
                Log.d(TAG, "dbFile exists");
            }
//            if(!dbFile.canWrite())
//                dbFile.setWritable(true);
            if(!dbFile.canRead())
                dbFile.setReadable(true);
            FileInputStream fis = new FileInputStream(dbFile);

            String outFileName = context.getFilesDir() + "/" + DATABASE_NAME;
            Log.d(TAG, "Attempting to write database file to " + outFileName);
            File outFile = new File(outFileName);
            outFile.createNewFile(); //does nothing if the file already exists
            if(!outFile.canWrite())
                outFile.setWritable(true);
            if(!outFile.canRead())
                outFile.setReadable(true);
            OutputStream output = new FileOutputStream(outFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }
            //Close the streams
            output.flush();
            output.close();
            fis.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
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
        Log.d(TAG, "Adding a " + actuationTypeString + " event with start time " + startTime.toString());



        Usage c = new Usage(mDeviceAddress, mDeviceName, dateString, startTime.toString(), endTime.toString(), actuationTypeString, Arrays.toString(xcurrent), Arrays.toString(ycurrent), voltage, energy);
        mUsageViewModel.insert(c);

        Log.d(TAG, "New" + actuationTypeString + " usage");

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


        mUsageViewModel.checkpoint(new SimpleSQLiteQuery("pragma wal_checkpoint(full)")); //to write journaled changes to main database file

        // Check Dropbox authorization
        SharedPreferences prefs = getSharedPreferences("dropbox-auth-info", MODE_PRIVATE);

        String serializedCredential = prefs.getString("credential", null);

        if (serializedCredential == null) {
            DbxCredential credential = Auth.getDbxCredential();

            if (credential != null && !credential.aboutToExpire()) {
                prefs.edit().putString("credential", credential.toString()).apply();
                uploadData(credential);
            }
            else {
                startOAuth2Authentication(this, APP_KEY, app_scope);
            }
        }
        else {
            try {
                DbxCredential credential = DbxCredential.Reader.readFully(serializedCredential);

                uploadData(credential);
            } catch (JsonReadException e) {
                throw new IllegalStateException("Credential data corrupted: " + e.getMessage());
            }
        }


        String uid = Auth.getUid();
        String storedUid = prefs.getString("user-id", null);
        if (uid != null && !uid.equals(storedUid)) {
            prefs.edit().putString("user-id", uid).apply();
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

        //Upload the database to dropbox if the app is still authorized to do so.
        //Can't open a new activity to re-authenticate dropbox here bc the app is in the process of being closed,
        //so the user has to re-open the Minikers app to re-authenticate Dropbox (if the app isn't authorized anymore)
        SharedPreferences prefs = getSharedPreferences("dropbox-auth-info", MODE_PRIVATE);

        String serializedCredential = prefs.getString("credential", null);

        if (serializedCredential != null) {

            try {
                DbxCredential credential = DbxCredential.Reader.readFully(serializedCredential);


                mUsageViewModel.checkpoint(new SimpleSQLiteQuery("pragma wal_checkpoint(full)"));
                uploadData(credential);


            } catch (JsonReadException e) {
                throw new IllegalStateException("Credential data corrupted: " + e.getMessage());
            }
        }
    }

    private void uploadData(DbxCredential dbxCredential) {
        DropboxClientFactory.init(dbxCredential);
        uploadDatabaseToDropbox(dbxCredential);
    }


    protected boolean hasToken() {
        SharedPreferences prefs = getSharedPreferences("dropbox-sample", MODE_PRIVATE);
        return prefs.getString("credential", null) != null;
    }

    public static void startOAuth2Authentication(Context context, String app_key, List<String> scope) {
        Auth.startOAuth2PKCE(context, app_key, DbxRequestConfigFactory.getRequestConfig(), scope);
    }
    //End example authorization code

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
