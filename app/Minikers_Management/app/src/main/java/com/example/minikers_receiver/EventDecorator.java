package com.example.minikers_receiver;


import android.util.Log;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

public class EventDecorator implements DayViewDecorator {

    private static final String TAG = "EventDecorator";
    private final int colors[];

    private final CalendarDay date;
    ArrayList<String> uses;
    private final int maxWidth;
    private  int maxHeight;

    public EventDecorator(int colors[], CalendarDay date, ArrayList<String> uses, int maxWidth) {
        this.colors = colors;
        this.date = date;
        this.uses = uses;
        this.maxWidth = maxWidth;
    }

    public EventDecorator(int colors[], CalendarDay date, ArrayList<String> uses, int maxWidth, int maxHeight) {
        this.colors = colors;
        //this.dates = new HashSet<>(dates);
        this.date = date;
        this.uses = uses;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return !date.isAfter(day) && !date.isBefore(day);
    }


    @Override
    public void decorate(DayViewFacade view) {

        int numDots = uses.size();
        for(int i = 0 ; i < numDots; i++) {
            int color = colors[0];
            if(uses.get(i).equalsIgnoreCase("Manual"))
                color = colors[1];

            view.addSpan(new MultipleDotSpan(i, maxWidth, color));

        }

    }
}