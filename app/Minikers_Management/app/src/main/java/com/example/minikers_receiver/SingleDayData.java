
package com.example.minikers_receiver;


import static com.example.minikers_receiver.CalendarActivity.EXTRAS_DEVICE_ADDRESS;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;


import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;


import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Calendar;

public class SingleDayData extends AppCompatActivity {

    private static final String TAG = "SingleDayData";
    TextView titleText;


    RecyclerView recycler;
    ArrayList<LineChart> currentGraphs;
    ArrayList<Double> voltages;
    ArrayList<LocalTime> startTimes;
    ArrayList<LocalTime> endTimes;
    ArrayList<ActuationType> uses;

    String date;


    private String mDeviceAddress;

    //For firebase
    FirebaseFirestore db;


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_day_data);

        final Intent intent = getIntent();
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        if(mDeviceAddress == null) {
            mDeviceAddress = "AA:BB:CC:DD:EE:FF";
            Log.w(TAG, "Device address is null!");
        }




        titleText = (TextView) findViewById(R.id.fillerText);
        Intent i = getIntent();
        date = i.getStringExtra("date");
        int year = i.getIntExtra("year", 0);
        int month = i.getIntExtra("month", 0);
        int day = i.getIntExtra("day", 0);
        if(year == 0 || month == 0 || day == 0) {
            Log.w(TAG, "Invalid year, month, or day passed from CalendarActivity to SingleDayData");
        }
        titleText.setText("Data for " + date);




        startTimes = new ArrayList<LocalTime>();

        endTimes = new ArrayList<LocalTime>();

        voltages = new ArrayList<Double>();

        uses = new ArrayList<ActuationType>();


        db = FirebaseFirestore.getInstance();

        Calendar startDay = Calendar.getInstance();
        startDay.set(year, month - 1, day, 0, 0, 0); //Month indexing starts at 0 instead of 1
        Date d = startDay.getTime();


        Calendar endBeforeDay = (Calendar) startDay.clone();
        endBeforeDay.add(Calendar.DAY_OF_MONTH, 1);
        Date e = endBeforeDay.getTime();

        currentGraphs = new ArrayList<LineChart>();


        db.collection(mDeviceAddress)
                .whereGreaterThan(FirestoreKey.START_TIME_KEY, d)
                .whereLessThan(FirestoreKey.START_TIME_KEY, e)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());

                                uses.add(ActuationType.valueOf(document.getString(FirestoreKey.MODE_KEY)));

                                voltages.add(document.getDouble(FirestoreKey.VOLTAGE_KEY));


                                LineChart currentGraph = new LineChart(getApplicationContext());
                                createGraph(currentGraph, (List<Double>) document.get(FirestoreKey.CURRENT_X_KEY), (List<Double>) document.get(FirestoreKey.CURRENT_Y_KEY), "Current (mA vs ms)", ContextCompat.getColor(getApplicationContext(), R.color.current));
                                currentGraphs.add(currentGraph);
                                Log.d(TAG, "currentGraphs size: " + currentGraphs.size());

                                //Start and end times:
                                long startTimeEpoch = ((Timestamp) document.get(FirestoreKey.START_TIME_KEY)).getSeconds();
                                ZoneOffset offset = ZoneId.systemDefault().getRules().getOffset(LocalDateTime.now());
                                LocalTime startTime = LocalDateTime.ofEpochSecond(startTimeEpoch, 0, offset).toLocalTime();
                                startTimes.add(startTime);

                                long endTimeEpoch = ((Timestamp) document.get(FirestoreKey.END_TIME_KEY)).getSeconds();
                                LocalTime endTime = LocalDateTime.ofEpochSecond(endTimeEpoch, 0, offset).toLocalTime();
                                endTimes.add(endTime);

                            }


                            setupRecyclerView();
                        }
                        else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });





    }


    private void setupRecyclerView() {
        recycler = (RecyclerView) findViewById(R.id.recycler);

        final UsageListAdapter adapter = new UsageListAdapter(this, currentGraphs, voltages, startTimes, endTimes, uses);

        recycler.setAdapter(adapter);

        recycler.setLayoutManager(new LinearLayoutManager(this));


    }


    private boolean createGraph(LineChart lc, List<Double> xvals, List<Double> yvals, String title, int color) {
        if(xvals.size() != yvals.size()) {
            Log.e(TAG, "Error: Domain and range have different lengths");
            return false;
        }

        List<Entry> points = getPointsFloat(yvals, xvals);
        LineDataSet dataSet = new LineDataSet(points, title);
        dataSet.setColor(color);
        dataSet.setDrawCircles(false); //Don't draw a bubble on each point
        LineData lineData = new LineData(dataSet); //LineData holds all the datasets for the graph -- in this case we only have one dataset
        lc.setData(lineData);

        lc.invalidate(); //Refresh

        lc.setTouchEnabled(true);
        return true;
    }


    private List<Entry> getPointsFloat(List<Double> yvals, List<Double> xvals) {
        if(xvals.size() != yvals.size()) {
            Log.e(TAG, "Error: Domain and range have different lengths");
            return null;
        }

        List<Entry> points = new ArrayList<Entry>();
        int size = xvals.size();
        for(int k = 0; k < size; k++) {
            points.add(new Entry(xvals.get(k).floatValue(), yvals.get(k).floatValue()));
        }
        return points;
    }





}
