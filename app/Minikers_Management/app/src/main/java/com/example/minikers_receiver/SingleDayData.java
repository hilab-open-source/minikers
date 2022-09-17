package com.example.minikers_receiver;


import static com.example.minikers_receiver.CalendarActivity.EXTRAS_DEVICE_ADDRESS;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.ObjectInputStream;
import java.lang.reflect.Array;
import java.nio.Buffer;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.common.primitives.Floats;
import com.google.common.primitives.Ints;

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


    private UsageViewModel mUsageViewModel;
    private String mDeviceAddress;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_day_data);

        final Intent intent = getIntent();
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        if(mDeviceAddress == null)
            mDeviceAddress = "AA:BB:CC:DD:EE:FF"; //For testing

        mUsageViewModel = MainActivity.mUsageViewModel;


        titleText = (TextView) findViewById(R.id.fillerText);
        Intent i = getIntent();
        date = i.getStringExtra("date");
        titleText.setText("Data for " + date);

        Log.d(TAG, "In SingleDayData, address is " + mUsageViewModel);

        ArrayList<String> startTimeStrings = new ArrayList<String>(mUsageViewModel.getUsageStartTimesFromDayForMacAddress(mDeviceAddress, date));
        Log.d(TAG, "timeStrings list from synchronous database call is " + startTimeStrings.toString());
        startTimes = stringsToLocalTimes(startTimeStrings);

        ArrayList<String> endTimeStrings = new ArrayList<String>(mUsageViewModel.getUsageEndTimesFromDayForMacAddress(mDeviceAddress, date));
        endTimes = stringsToLocalTimes(endTimeStrings);

        //Parse Strings to ActuationTypes
        ArrayList<String> actuationTypeStrings = new ArrayList<String>(mUsageViewModel.getUsageTypesFromDayForMacAddress(mDeviceAddress, date));
        uses = new ArrayList<ActuationType>();
        actuationTypeStrings.forEach((str) -> {
            uses.add(ActuationType.valueOf(str));
        });




        currentGraphs = new ArrayList<LineChart>();

        //Set up currentGraphs
        ArrayList<Usage> usages = new ArrayList<Usage>(mUsageViewModel.getUsagesOfMacAddressForDay(mDeviceAddress, date));
        Log.d(TAG, "Found " + usages.size() + " usages for date " + date + " for mac address " + mDeviceAddress);
        for (Usage usage : usages) {
            Log.d(TAG, "heeeres a usage");
            LineChart currentGraph = new LineChart(this);
            createGraph(currentGraph, stringToFloatArray(usage.getCurrentXValues()), stringToFloatArray(usage.getCurrentYValues()), "Current (mA vs ms)", ContextCompat.getColor(this, R.color.current));
            currentGraphs.add(currentGraph);

        }

        //Set up voltages
        voltages = new ArrayList<Double>(mUsageViewModel.getVoltagesFromDayForMacAddress(mDeviceAddress, date));


        setupRecyclerView();

    }

    private ArrayList<LocalTime> stringsToLocalTimes(ArrayList<String> strings){
        ArrayList<LocalTime> times = new ArrayList<LocalTime>();
        strings.forEach((str) ->{
            times.add(LocalTime.parse(str));
        });

        return times;
    }


    private void setupRecyclerView() {
        recycler = (RecyclerView) findViewById(R.id.recycler);

        final UsageListAdapter adapter = new UsageListAdapter(new UsageListAdapter.WordDiff(), this, currentGraphs, voltages, startTimes, endTimes, uses);

        recycler.setAdapter(adapter);

        recycler.setLayoutManager(new LinearLayoutManager(this));


    }



    float[] stringToFloatArray(String str) {
        //str is in the format [1, 2, 3, 4, 5] so need to separate using commas as delimiters, and remove the opening and closing brackets
        String[] stringArray = str.replaceAll("\\[", "")
                .replaceAll("]", "")
                .replaceAll(" ", "")
                .split(",");

        int length = stringArray.length;
        float[] floats = new float[length];


        //Parse string array as int array
        for (int i = 0; i < length; i++) {
            floats[i] = Float.valueOf(stringArray[i]);
        }

        Log.d(TAG, "stringToIntArray: " + Arrays.toString(floats));

        return floats;
    }

    private boolean createGraph(LineChart lc, float[] xvals, float[] yvals, String title, int color) {
        if(xvals.length != yvals.length) {
            Log.e(TAG, "Error: Domain and range have different lengths");
            return false;
        }

        List<Entry> points = getPointsFloat(yvals, xvals);
        LineDataSet dataSet = new LineDataSet(points, title);
        dataSet.setColor(color);
        dataSet.setDrawCircles(false); //Attempting to not display a giant bubble on each point
        LineData lineData = new LineData(dataSet); //LineData holds all the datasets for the graph -- in this case we only have one dataset
        lc.setData(lineData);

        lc.invalidate(); //Refresh

        lc.setTouchEnabled(true);
        return true;
    }

    private List<Entry> getPointsFloat(float[] yvals, float[] xvals) {
        return getPointsFloat(Floats.asList(yvals), Floats.asList(xvals));
    }

    private List<Entry> getPointsFloat(List<Float> yvals, List<Float> xvals) {
        if(xvals.size() != yvals.size()) {
            Log.e(TAG, "Error: Domain and range have different lengths");
            return null;
        }

        List<Entry> points = new ArrayList<Entry>();
        int size = xvals.size();
        for(int k = 0; k < size; k++) {
            points.add(new Entry(xvals.get(k), yvals.get(k)));
        }
        return points;
    }





    int[] stringToIntArray(String str) {
        //str is in the format [1, 2, 3, 4, 5] so need to separate using commas as delimiters, and remove the opening and closing brackets
        String[] stringArray = str.replaceAll("\\[", "")
                .replaceAll("]", "")
                .replaceAll(" ", "")
                .split(",");

        //Log.d(TAG, "string -> array is: " + Arrays.toString(stringArray));
        int length = stringArray.length;
        int[] ints = new int[length];


        //Parse string array as int array
        for (int i = 0; i < length; i++) {
            ints[i] = Integer.valueOf(stringArray[i]);
        }

        Log.d(TAG, "stringToIntArray: " + Arrays.toString(ints));

        return ints;
    }

    //Todo: make this a generic method
    private boolean createGraph(LineChart lc, int[] xvals, int[] yvals, String title, int color) {
        if(xvals.length != yvals.length) {
            Log.e(TAG, "Error: Domain and range have different lengths");
            return false;
        }

        List<Entry> points = getPoints(yvals, xvals);
        LineDataSet dataSet = new LineDataSet(points, title);
        dataSet.setColor(color);
        LineData lineData = new LineData(dataSet); //LineData holds all the datasets for the graph -- in this case we only have one dataset
        lc.setData(lineData);

        lc.invalidate(); //Refresh

        lc.setTouchEnabled(true);
        return true;
    }


    private List<Entry> getPoints(int[] yvals, int[] xvals) {
        return getPoints(Ints.asList(yvals), Ints.asList(xvals));
    }

    private List<Entry> getPoints(List<Integer> yvals, List<Integer> xvals) {
        if(xvals.size() != yvals.size()) {
            Log.e(TAG, "Error: Domain and range have different lengths");
            return null;
        }

        List<Entry> points = new ArrayList<Entry>();
        int size = xvals.size();
        for(int k = 0; k < size; k++) {
            points.add(new Entry(xvals.get(k), yvals.get(k)));
        }
        return points;
    }

}