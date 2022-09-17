package com.example.minikers;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;

class UsageViewHolder extends RecyclerView.ViewHolder {
    TextView timeText;
    LineChart currentGraph;
    LineChart voltageGraph;
    LinearLayout rootLayout;
    LinearLayout graphsHolder;

    private UsageViewHolder(View itemView) {
        super(itemView);

        timeText = (TextView) itemView.findViewById(R.id.timeText);
        currentGraph = (LineChart) itemView.findViewById(R.id.currentChart1);
        voltageGraph = (LineChart) itemView.findViewById(R.id.voltageChart1);
        rootLayout = (LinearLayout) itemView.findViewById(R.id.recyclerLayout);
        graphsHolder = (LinearLayout) itemView.findViewById(R.id.graphsHolder);
    }

    public void setCurrentGraph(LineChart c){
        currentGraph = c;
    }
    public void setVoltageGraph(LineChart v){
        voltageGraph = v;
    }

    static UsageViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.graphs_list_recycler, parent, false);
        return new UsageViewHolder(view);

    }


}