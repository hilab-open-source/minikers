package com.example.minikers_receiver;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

public class UsageListAdapter extends RecyclerView.Adapter<UsageViewHolder> {

    ArrayList<LineChart> currentGraphs;
    ArrayList<Double> voltages;
    ArrayList<LocalTime> startTimes;
    ArrayList<LocalTime> endTimes;
    ArrayList<String> startTimesAsStrings; //to avoid calling .toString() during every onBindViewHolder call
    ArrayList<String> endTimesAsStrings;
    ArrayList<ActuationType>  uses;
    Context ctx;
    private static final String TAG = "GraphRecyclerAdapter";


    public UsageListAdapter(Context ct, ArrayList<LineChart> currentGraphs, ArrayList<Double> voltages, ArrayList<LocalTime> startTimes, ArrayList<LocalTime> endTimes, ArrayList<ActuationType> uses) {


        ctx = ct;
        this.currentGraphs = currentGraphs;
        this.voltages = voltages;
        this.startTimes = startTimes;
        this.endTimes = endTimes;
        this.startTimesAsStrings = new ArrayList<String>();
        this.startTimesAsStrings = LocalTimesToStrings(startTimes);

        this.endTimesAsStrings = new ArrayList<String>();
        this.endTimesAsStrings = LocalTimesToStrings(endTimes);

        this.uses = uses;

        Log.d(TAG, "RecyclerView Adapter sees " + currentGraphs.size() + " current graphs" );
    }

    private ArrayList<String> LocalTimesToStrings(ArrayList<LocalTime> times) {
        ArrayList<String> timesAsStrings = new ArrayList<String>();
        times.forEach((time) -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("KK:mm:ss a", Locale.ENGLISH);
            String formattedTime = time.format(formatter);
            timesAsStrings.add(formattedTime);
        });
        return timesAsStrings;
    }

    private int getPxFromDp(View v, int dp) {
        DisplayMetrics displayMetrics = v.getContext().getResources().getDisplayMetrics();
        return (int)((dp * displayMetrics.density) + 0.5);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public UsageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "Name of ViewHolder's parent is: " + parent.getId());
        return UsageViewHolder.create(parent);
    }

    @Override
    public int getItemCount() {
        return currentGraphs.size(); //not great?
    }

    @Override
    public void onBindViewHolder(UsageViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder");

        holder.graphsHolder.removeView(holder.currentGraph);


        if(currentGraphs.get(position).getParent() != null)
            ((ViewGroup)currentGraphs.get(position).getParent()).removeView(currentGraphs.get(position));
        holder.graphsHolder.addView(currentGraphs.get(position));
        //Chart formatting
        LineChart cg = currentGraphs.get(position);
        YAxis axisRight = cg.getAxisRight();
        axisRight.setDrawLabels(false); //MPAndroidChart draws y-axis number labels on both left and right sides by default, but we want the left side only
        cg.getDescription().setEnabled(false);
        cg.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        cg.getLayoutParams().height = getPxFromDp(cg, 150);
        cg.getLayoutParams().width = getPxFromDp(cg, 250);
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) cg.getLayoutParams();
        lp.topMargin = getPxFromDp(cg, 50);
        //lp.bottomMargin = 50;
        LinearLayout.LayoutParams cparams = (LinearLayout.LayoutParams) cg.getLayoutParams();
        cparams.gravity = Gravity.CENTER;
        cg.setLayoutParams(cparams);
        //cg.setVisibility(View.GONE);


        //Set voltage text
        holder.voltageText.setText("Voltage: " + voltages.get(position) + " V");

        //Display the usage time
        holder.timeText.setText("Start time: " + startTimesAsStrings.get(position));
        holder.endTimeText.setText("End time: " + endTimesAsStrings.get(position));
        holder.timeText.setBackgroundColor( (uses.get(position) == ActuationType.Automatic) ? ContextCompat.getColor(ctx, R.color.automatic) : ContextCompat.getColor(ctx, R.color.manual));

//        //Collapse the graphs initially
//        holder.graphsHolder.setVisibility(View.GONE);
//        holder.timeText.setClickable(true);
//
//        //To expand/collapse graphs when user clicks date
//        holder.timeText.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                holder.graphsHolder.setVisibility( (holder.graphsHolder.getVisibility() == View.GONE) ? View.VISIBLE : View.GONE);
//            }
//        });

        //To avoid adding duplicate graphs when scrolling through again
        holder.setCurrentGraph(cg);
    }


}