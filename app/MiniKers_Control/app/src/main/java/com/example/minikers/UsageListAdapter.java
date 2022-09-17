package com.example.minikers;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

public class UsageListAdapter extends ListAdapter<Usage, UsageViewHolder> {

    ArrayList<LineChart> currentGraphs;
    ArrayList<LineChart> voltageGraphs;
    ArrayList<LocalTime> times;
    ArrayList<String> timesAsStrings; //to avoid calling .toString() during every onBindViewHolder call
    ArrayList<ActuationType>  uses;
    Context ctx;
    private static final String TAG = "GraphRecyclerAdapter";


    public UsageListAdapter(@NonNull DiffUtil.ItemCallback<Usage> diffCallback, Context ct, ArrayList<LineChart> currentGraphs, ArrayList<LineChart> voltageGraphs, ArrayList<LocalTime> times, ArrayList<ActuationType> uses) {
        super(diffCallback);

        ctx = ct;
        this.currentGraphs = currentGraphs;
        this.voltageGraphs = voltageGraphs;
        this.times = times;
        timesAsStrings = new ArrayList<String>();
        this.times.forEach((time) -> {
            //timesAsStrings.add(time.toString());

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("KK:mm:ss a", Locale.ENGLISH);
            String formattedTime = time.format(formatter);
            timesAsStrings.add(formattedTime);
        });
        this.uses = uses;

        Log.d(TAG, "RecyclerView Adapter sees " + currentGraphs.size() + " current graphs and " + voltageGraphs.size() + " voltage graphs");
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
        Log.d(TAG, "onBindViewHolder was called");

        holder.graphsHolder.removeView(holder.currentGraph);
        holder.graphsHolder.removeView(holder.voltageGraph);

        //Todo: Refactor and remove magic numbers
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
        LinearLayout.LayoutParams cparams = (LinearLayout.LayoutParams) cg.getLayoutParams();
        cparams.gravity = Gravity.CENTER;
        cg.setLayoutParams(cparams);


        if(voltageGraphs.get(position).getParent() != null)
            ((ViewGroup)voltageGraphs.get(position).getParent()).removeView(voltageGraphs.get(position));
        holder.graphsHolder.addView(voltageGraphs.get(position));
        LineChart vg = voltageGraphs.get(position);
        //Chart formatting
        YAxis voltageAxisRight = vg.getAxisRight();
        voltageAxisRight.setDrawLabels(false);
        vg.getDescription().setEnabled(false);
        vg.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        vg.getLayoutParams().height = getPxFromDp(cg, 150);
        vg.getLayoutParams().width = getPxFromDp(cg, 250);
        ViewGroup.MarginLayoutParams vlp = (ViewGroup.MarginLayoutParams) vg.getLayoutParams();
        vlp.topMargin = getPxFromDp(cg, 30);
        //vlp.bottomMargin = 50;
        LinearLayout.LayoutParams vparams = (LinearLayout.LayoutParams) vg.getLayoutParams();
        vparams.gravity = Gravity.CENTER;
        vg.setLayoutParams(vparams);

        //Display the usage time
        holder.timeText.setText(timesAsStrings.get(position));
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
        holder.setVoltageGraph(vg);
    }

}