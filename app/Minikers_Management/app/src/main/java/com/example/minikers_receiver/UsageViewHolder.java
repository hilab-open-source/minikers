package com.example.minikers_receiver;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;

public class UsageViewHolder extends RecyclerView.ViewHolder {
    TextView timeText;
    LineChart currentGraph;
    TextView voltageText;
    TextView endTimeText;
    LinearLayout rootLayout;
    LinearLayout graphsHolder;

    public UsageViewHolder(View itemView) {
        super(itemView);

        timeText = (TextView) itemView.findViewById(R.id.timeText);
        currentGraph = (LineChart) itemView.findViewById(R.id.currentChart1);
        voltageText = (TextView) itemView.findViewById(R.id.voltageText);
        endTimeText = (TextView) itemView.findViewById(R.id.endTimeText);
        rootLayout = (LinearLayout) itemView.findViewById(R.id.recyclerLayout);
        graphsHolder = (LinearLayout) itemView.findViewById(R.id.graphsHolder);
    }

    public void setCurrentGraph(LineChart c){
        currentGraph = c;
    }

    static UsageViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.usage_recycler_item, parent, false);
        return new UsageViewHolder(view);
    }


}