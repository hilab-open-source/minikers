package com.example.minikers_receiver;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.style.LineBackgroundSpan;
import android.util.Log;


public class LineSpan implements LineBackgroundSpan {

    /**
     * Default radius used
     */
    public static final float DEFAULT_HEIGHT = 20;
    public static final float DEFAULT_WIDTH = 120;

    private final float width;

    //This ends up being set to default_height in every constructor to avoid ambiguity with float/float/int so x-offset is possible
    private final float height;
    private final int color;

    float xoffset = 0;


    public LineSpan() {
        this.height = DEFAULT_HEIGHT;
        this.width = DEFAULT_WIDTH;
        this.color = 0;
    }


    public LineSpan(int color) {
        this.height = DEFAULT_HEIGHT;
        this.width = DEFAULT_WIDTH;
        this.color = color;
    }


    public LineSpan(float width) {
        this.width = width;
        this.height = DEFAULT_HEIGHT;
        this.color = 0;
    }



    public LineSpan(float width, int color) {
        this.width = width;
        this.height = DEFAULT_HEIGHT;
        this.color = color;
    }

    public LineSpan(float width, float xoffset, int color) {
        this.width = width;
        this.height = DEFAULT_HEIGHT;
        this.xoffset = xoffset;
        this.color = color;
    }

    @Override
    public void drawBackground(
            Canvas canvas, Paint paint,
            int left, int right, int top, int baseline, int bottom,
            CharSequence charSequence,
            int start, int end, int lineNum
    ) {
        int oldColor = paint.getColor();
        if (color != 0) {
            paint.setColor(color);
        }


        float bottomcoord = bottom + height;
        float topcoord = bottomcoord - height;

        float leftcoord = left + xoffset;
        if(leftcoord < left)
            leftcoord = left;
        float rightcoord = leftcoord + width;
        if(rightcoord > right)
            rightcoord = right;

        canvas.drawRect(leftcoord, topcoord, rightcoord, bottomcoord, paint);
        paint.setColor(oldColor);
    }
}