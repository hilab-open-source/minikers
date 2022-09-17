package com.example.minikers_receiver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.sqlite.db.SimpleSQLiteQuery;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxPKCEWebAuth;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxWebAuth;
import com.dropbox.core.TokenAccessType;
import com.dropbox.core.android.Auth;
import com.dropbox.core.json.JsonReadException;
import com.dropbox.core.oauth.DbxCredential;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.WriteMode;
import com.dropbox.core.v2.users.FullAccount;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import android.net.Uri;
import android.view.View;
import android.widget.Button;

import org.checkerframework.checker.units.qual.A;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";

    private Button goToCalendarButton;


    public static UsageViewModel mUsageViewModel;

    private final String databaseFilenameInDropbox = "word_database";
    public static File databaseFile;

    private DbxClientV2 mDbxClient;
    private RecyclerView recycler;

    private static final String APP_KEY = "cok1ath2jf73h6u";
    private ArrayList<String> app_scope;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        app_scope = new ArrayList<String>();
        app_scope.add("account_info.read");
        app_scope.add("files.metadata.read");
        app_scope.add("files.metadata.write");
        app_scope.add("files.content.read");
        app_scope.add("files.content.write");


    }


    // From Dropbox example authorization code
    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences prefs = getSharedPreferences("dropbox-auth-info", MODE_PRIVATE);

        String serializedCredential = prefs.getString("credential", null);

        if (serializedCredential == null) {
            DbxCredential credential = Auth.getDbxCredential();

            if (credential != null && !credential.aboutToExpire()) {
                prefs.edit().putString("credential", credential.toString()).apply();
                initAndLoadData(credential);
            }
            else {
                startOAuth2Authentication(this, APP_KEY, app_scope);
            }
        }
        else {
            try {
                DbxCredential credential = DbxCredential.Reader.readFully(serializedCredential);

                initAndLoadData(credential);
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


    private void initAndLoadData(DbxCredential dbxCredential) {
        DropboxClientFactory.init(dbxCredential);

        loadData(dbxCredential);
    }

    protected void loadData(DbxCredential credentials) {
        setUpDatabase(credentials);
    }

    protected boolean hasToken() {
        SharedPreferences prefs = getSharedPreferences("dropbox-sample", MODE_PRIVATE);
        return prefs.getString("credential", null) != null;
    }

    public static void startOAuth2Authentication(Context context, String app_key, List<String> scope) {
        Auth.startOAuth2PKCE(context, app_key, DbxRequestConfigFactory.getRequestConfig(), scope);
    }
    //End example authorization code



    private void setUpDatabase(DbxCredential credentials){
        Context ctx = this;
        Thread networkThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try  {

                    DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox/java-tutorial").build();
                    mDbxClient = new DbxClientV2(config, credentials);
                    FullAccount account = mDbxClient.users().getCurrentAccount();
                    Log.d(TAG, "Account: " + account.getName().getDisplayName());

                    ListFolderResult result = mDbxClient.files().listFolder("");
                    while (true) {
                        //Find word database
                        for (Metadata metadata : result.getEntries()) {
                            Log.d(TAG, "metadata: " + metadata.getName());

                            if(metadata.getName().equals(databaseFilenameInDropbox)){
                                databaseFile = new File(getFilesDir(), databaseFilenameInDropbox);
                                downloadDatabaseFile((FileMetadata) metadata, databaseFile);
                            }
                        }

                        if (!result.getHasMore()) {
                            break;
                        }

                        result = mDbxClient.files().listFolderContinue(result.getCursor());
                    }



                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        networkThread.start();

        try {
            networkThread.join();

            //Delete the database if it already exists
            if (!this.deleteDatabase("word_database"))
                Log.d(TAG, "Deleting database was unsuccessful");
            else
                Log.d(TAG, "Successfully deleted database");


            //Build the database
            mUsageViewModel = new ViewModelProvider(this).get(UsageViewModel.class);
            for(Usage u: mUsageViewModel.getRecordsFromDayAsOrdinaryList("2022-05-08")) {
                Log.d(TAG, "Usage: " + u.toString());
            }

            Log.d(TAG, "About to begin checkpoint");
            mUsageViewModel.checkpoint(new SimpleSQLiteQuery("pragma wal_checkpoint(full)")); //to write journaled changes to main database file
            Log.d(TAG, "Finished checkpointing");



            setupRecyclerView();
        }
        catch(Exception e){
            e.printStackTrace();
        }


    }

    private File downloadDatabaseFile(FileMetadata remoteMetadata, File localDatabaseFile) {
        FileMetadata metadata = remoteMetadata; //the metadata of the database in the Dropbox that we want to download
        try {
            Log.d(TAG, "Want to download file to " + localDatabaseFile.getAbsolutePath());
            localDatabaseFile.createNewFile();

            // Download the file.
            try (OutputStream outputStream = new FileOutputStream(localDatabaseFile)) {
                mDbxClient.files().download(metadata.getPathLower(), metadata.getRev())
                        .download(outputStream);
            }

            // Tell android about the file
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(Uri.fromFile(localDatabaseFile));
            this.sendBroadcast(intent);

            return localDatabaseFile;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }



    private void setupRecyclerView() {
        recycler = (RecyclerView) findViewById(R.id.deviceItemRecycler);

        ArrayList<String> deviceAddresses = new ArrayList<String>(mUsageViewModel.getAllMacAddresses());


        ArrayList<String> deviceNames = new ArrayList<String>();
        for(String address : deviceAddresses) {
            ArrayList<Usage> usages = new ArrayList<Usage>(mUsageViewModel.getDeviceUsagesUsingMacAddress(address));
            deviceNames.add(usages.get(0).getDeviceName());
        }
        final DeviceItemAdapter adapter = new DeviceItemAdapter(this, deviceAddresses, deviceNames);

        recycler.setAdapter(adapter);

        recycler.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart(){
        super.onStart();

    }

    @Override
    protected void onStop(){
        super.onStop();


    }

}