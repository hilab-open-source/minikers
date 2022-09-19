package com.example.minikers_receiver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;



import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";

    private RecyclerView recycler;

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ArrayList<String> addresses = new ArrayList<String>();
        ArrayList<String> names = new ArrayList<String>();

        db = FirebaseFirestore.getInstance();
        db.collection(FirestoreKey.DEVICES_COLLECTION_KEY).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                addresses.add(document.getString("address"));
                                names.add(document.getString("name"));

                            }

                            setupRecyclerView(addresses, names);
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

    }


    @Override
    protected void onResume() {
        super.onResume();


    }





    private void setupRecyclerView(ArrayList<String> deviceAddresses, ArrayList<String> deviceNames) {
        recycler = (RecyclerView) findViewById(R.id.deviceItemRecycler);



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