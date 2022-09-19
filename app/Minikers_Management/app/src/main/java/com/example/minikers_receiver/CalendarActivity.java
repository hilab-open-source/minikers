package com.example.minikers_receiver;



import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;


import java.time.LocalDate;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CalendarActivity extends AppCompatActivity {
    private final static String TAG = "CalendarActivity";

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";


    private String mDeviceName;
    private String mDeviceAddress;


    private TextView deviceNameText;
    MaterialCalendarView calendar;

    private int TILE_WIDTH;
    private int TILE_HEIGHT;




    CombinedChart batteryGraph;

    private TextView deviceInfoText;

    FirebaseFirestore db;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        db = FirebaseFirestore.getInstance();

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        if(mDeviceName == null)
            Log.w(TAG, "Error: Device name is null");
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        if(mDeviceAddress == null)
            Log.w(TAG, "Error: Device address is null");

        Log.d(TAG, "Device name is " + mDeviceName);



        setUpCalendarView();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        deviceInfoText = (TextView) findViewById(R.id.deviceInfoText);
        deviceInfoText.setText(mDeviceName + " (Location: workshop)");



        batteryGraph = (CombinedChart) findViewById(R.id.batteryChartForMonth);

        //Todo: replace this line with a call to setUpBatteryGraph using real data
        setUpSampleBatteryGraph();


    }


    private void setUpSampleBatteryGraph(){
        //Example battery/graph data
        Integer[] sampleBatteryLevels = new Integer[]{100, 90, 80, 83, 75, 91, 95, 94, 96, 81,
                76, 75, 74, 78, 82, 80, 88, 73, 64, 60,
                59, 61, 63, 65, 78, 62, 60, 70, 72, 77};
        Float[] sampleNetEnergyChange = new Float[]{0F, -10F, -10F, 3F, -8F, 16F, 4F, -1F, 2F, -15F,
                -5F, -1F, -1F, 4F, 4F, -2F, 8F, -15F, -9F, -4F,
                -1F, 2F, 2F, 2F, 13F, -16F, -2F, 10F, 2F, 5F};
        for(int i = 0; i <= 29; i++){
            sampleNetEnergyChange[i] = sampleNetEnergyChange[i] / 25;
        }



        setUpBatteryGraph(sampleBatteryLevels, sampleNetEnergyChange);
    }

    private void setUpBatteryGraph(Integer[] batteryLevels, Float[] netEnergyChange){
        //Get the number of days in the current month
        CalendarDay currentDate = calendar.getCurrentDate();
        LocalDate ld = LocalDate.of(currentDate.getYear(), currentDate.getMonth(), currentDate.getDay());
        int numDaysInMonth = ld.lengthOfMonth();

        Integer[] days = new Integer[numDaysInMonth];
        for(int i = 0; i <= numDaysInMonth - 1; i++){
            days[i] = i;
        }

        drawBatteryGraph(batteryGraph, new ArrayList<Integer>(Arrays.asList(batteryLevels)), new ArrayList<Float>(Arrays.asList(netEnergyChange)), new ArrayList<Integer>(Arrays.asList(days)), "Battery level (%)", ContextCompat.getColor(this, R.color.battery));
    }

    private void setUpCalendarView(){
        //Set up calendar and tester buttons for auto/manual use
        calendar = (MaterialCalendarView) findViewById(R.id.calendar);

        //MaterialCalendarView's getTileWidth() returns negative so this is a temp fix
        //Divide screen width by 7.5 because there are 7 days in a row, and we want some space between linespans of adjacent dates so they don't touch
        TILE_WIDTH = (int) (getResources().getDisplayMetrics().widthPixels / 7.5);
        TILE_HEIGHT = (int) (getResources().getDisplayMetrics().heightPixels / 12);
        Log.d(TAG, "Tile width is " + TILE_WIDTH + " and tile height is " + TILE_HEIGHT);


        calendar.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                String formattedDate =  date.getYear() + "-" + String.format("%02d", date.getMonth()) + "-" + String.format("%02d", date.getDay());
                Log.d(TAG, "Selected date: " + formattedDate);

                //Go to SingleDayData page
                Intent i = new Intent(getApplicationContext(), SingleDayData.class);
                i.putExtra("date", formattedDate);
                i.putExtra("year", date.getYear());
                i.putExtra("month", date.getMonth());
                i.putExtra("day", date.getDay());
                i.putExtra(EXTRAS_DEVICE_NAME, mDeviceName);
                i.putExtra(EXTRAS_DEVICE_ADDRESS,  mDeviceAddress);
                startActivity(i);

            }
        });



        showMonthsLineSpans(calendar.getCurrentDate());

        calendar.setOnMonthChangedListener(new OnMonthChangedListener() {
                                               @Override
                                               public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {
                                                   showMonthsLineSpans(date);

                                                   //Todo: also change the battery graph (for when the app takes real data)
                                               }
                                           }
        );
    }

    private void showMonthsLineSpans(CalendarDay focusedDate) {
        Log.d(TAG, "showMonthsLineSpans");
        LocalDate ld = LocalDate.of(focusedDate.getYear(), focusedDate.getMonth(), focusedDate.getDay());
        int currentMonth = focusedDate.getMonth();
        int i = currentMonth;



        Map<LocalDate, ArrayList<String>> datesAndUses = new HashMap<LocalDate, ArrayList<String>>();
        if(db == null)
            Log.w(TAG, "FirebaseFirestore database reference is null");
        db.collection(mDeviceAddress)
                .orderBy(FirestoreKey.START_TIME_KEY, Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                //updateLineSpan(currentday)
                                long startTimeEpoch = ((Timestamp) document.get(FirestoreKey.START_TIME_KEY)).getSeconds();
                                ZoneOffset offset = ZoneId.systemDefault().getRules().getOffset(LocalDateTime.now());
                                LocalDate startDate = LocalDateTime.ofEpochSecond(startTimeEpoch, 0, offset).toLocalDate();

                                if(datesAndUses.containsKey(startDate)) {
                                    datesAndUses.get(startDate).add(document.getString(FirestoreKey.MODE_KEY));
                                }
                                else {
                                    //create the arraylist
                                    ArrayList<String> uses = new ArrayList<String>();
                                    uses.add(document.getString(FirestoreKey.MODE_KEY));

                                    datesAndUses.put(startDate, uses);
                                }

                            }

                            for (LocalDate ld : datesAndUses.keySet()) {
                                updateLineSpan(ld, datesAndUses.get(ld));
                            }

                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }




    private boolean drawBatteryGraph(CombinedChart lc, List<Integer> batteryLevels, List<Float> netChangePerDay, List<Integer> days, String title, int color) {

        if(days.size() != batteryLevels.size()) {
            Log.e(TAG, "Error: Domain and range have different lengths");
            return false;
        }

        Collections.sort(days);

        ArrayList<BarEntry> barEntries = new ArrayList<>();

        List<Entry> points = new ArrayList<Entry>();
        int size = days.size();
        for(int k = 0; k < size; k++) {
            points.add(new Entry(days.get(k), batteryLevels.get(k)));
            barEntries.add(new BarEntry(days.get(k), netChangePerDay.get(k)));
        }

        LineDataSet dataSet = new LineDataSet(points, title);
        dataSet.setDrawValues(false);
        dataSet.setColor(color);
        dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        LineData lineData = new LineData(dataSet); //LineData holds all the datasets for the graph -- in this case we only have one dataset

        CombinedData cd = new CombinedData();
        cd.setData(lineData);
        cd.setData(generateBarData(barEntries));

        lc.setData(cd);

        lc.setTouchEnabled(true);


        //Format x-axis
        XAxis xAxis = batteryGraph.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        lc.getDescription().setEnabled(false);
        lc.invalidate(); //Refresh
        return true;
    }

    private BarData generateBarData(ArrayList<BarEntry> entries) {
        BarDataSet set1 = new BarDataSet(entries, "Change in energy (mJ)");
        set1.setColor(Color.rgb(60, 220, 78));
        set1.setValueTextColor(Color.rgb(60, 220, 78));
        set1.setValueTextSize(10f);
        set1.setAxisDependency(YAxis.AxisDependency.RIGHT);
        set1.setDrawValues(false);



        BarData d = new BarData(set1);


        return d;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    private void updateLineSpan(LocalDate date, ArrayList<String> uses) {
        if(uses == null){
            Log.w(TAG, "ActuationType list is null");
            return;
        }


        int automatic = ContextCompat.getColor(this, R.color.automatic);
        int manual = ContextCompat.getColor(this, R.color.manual);
        int colors[] = {automatic, manual};

        CalendarDay calendarDay = CalendarDay.from(date.getYear(), date.getMonthValue(), date.getDayOfMonth());
        calendar.addDecorator(new EventDecorator(colors, calendarDay, uses, TILE_WIDTH));
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onStop(){
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }



}
