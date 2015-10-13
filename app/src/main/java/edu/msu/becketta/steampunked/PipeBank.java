package edu.msu.becketta.steampunked;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.io.Serializable;

/**
 * Created by Aaron Beckett on 10/12/2015.
 *
 * An object that manages the drawing and maintenance of a bank of randomly generated pipes
 * that players can select from on their turn
 */
public class PipeBank implements Serializable {

    /**
     * Paint object used to draw the pipe bank rectangle
     */
    private transient Paint bankPaint;

    public PipeBank() {
        init();
    }

    public void init() {
        bankPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bankPaint.setColor(Color.argb(90, 0, 100, 0));  // A semi-transparent green
    }

    public void draw(Canvas canvas, float width, float height) {
        // Green rectangle
        canvas.drawRect(0f, 0f, width, height, bankPaint);

        // New pipes: 5 new pipes evenly spaced with normalized coordinates
    }
}
