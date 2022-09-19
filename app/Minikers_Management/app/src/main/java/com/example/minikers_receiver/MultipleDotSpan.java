package com.example.minikers_receiver;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.style.LineBackgroundSpan;
import android.util.Log;

//Draws a single dot at a specified position
//Differs from MaterialCalendarView's default dot in that it assumes there are multiple dots
//under a single day, so must specify the dot's position
public class MultipleDotSpan implements LineBackgroundSpan {

    //Default radius and height of a single dot
    public static final float DEFAULT_RADIUS = 8;
    private final float DEFAULT_TOTAL_HEIGHT = 112;

    private float radius;
    private int color;
    private int position; //As in an array. E.g. a dot can be the seventh dot on a device where only 5 dots fit in a row, so it ends up on the next row.
    private float totalWidth; //How many pixels/dp wide a row is
    private float totalHeight;

    private int numDotsInRow;
    private float widthOfColumn;
    private int numDotsInColumn;
    private float heightOfRow;

    private final String TAG = "MultipleDotSpan";

    public MultipleDotSpan(float radius, int position, int color, float totalWidth) {
        this.radius = radius;
        this.color = color;
        this.position = position;
        this.totalWidth = totalWidth;
        this.totalHeight = DEFAULT_TOTAL_HEIGHT;

        setUpConstants();
    }

    public MultipleDotSpan(int position, float totalWidth, int color) {
        this.radius = DEFAULT_RADIUS;
        this.color = color;
        this.position = position;
        this.totalWidth = totalWidth;
        this.totalHeight = DEFAULT_TOTAL_HEIGHT;

        setUpConstants();
    }


    private void setUpConstants(){
        this.numDotsInRow = (int) (totalWidth / (radius * 2));
        this.widthOfColumn = totalWidth / numDotsInRow;
        this.numDotsInColumn = (int) (totalHeight / (radius * 2));
        this.heightOfRow = totalHeight / numDotsInColumn;
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


        int columnNumber = position % (numDotsInRow);
        float centerX = columnNumber * widthOfColumn + radius;

        int rowNumber = position / (numDotsInRow);
        float centerY = rowNumber *  heightOfRow + radius;


        canvas.drawCircle(left + centerX, bottom + centerY, radius, paint);
        paint.setColor(oldColor);
    }
}