package edu.msu.becketta.steampunked;

import android.content.Context;
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

    public final int bankSize = 5;

    /**
     * Array of pipes in the bank
     */
    private Pipe[] pipes = new Pipe[bankSize];

    /**
     * The pipe that is being dragged
     */
    private Pipe activePipe = null;

    /**
     * The context that has the pipe drawables
     */
    private transient Context context;

    /**
     * Paint object used to draw the pipe bank rectangle
     */
    private transient Paint bankPaint;

    /**
     * Constructor for the PipeBank
     * @param context Context passed to Pipes when generating new pipes
     */
    public PipeBank(Context context) {
        this.context = context;

        bankPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bankPaint.setColor(Color.argb(90, 0, 100, 0));  // A semi-transparent green
    }

    // TODO: may need to add setter for the context if we need to re-set the context after PipeBank is serialized

    /**
     * Generate a random pipe
     * @return A random pipe
     */
    private Pipe getRandomPipe() {
        // TODO: randomize pipe type selection
        return new Pipe(context, Pipe.pipeType.STRAIGHT);
    }

    public Pipe getActivePipe() {
        return activePipe;
    }

    public void clearActivePipe() {
        activePipe = null;
    }

    public Pipe hitPipe(float xpod, float ypos) {
        activePipe = null;
        return null;
    }

    /**
     * Draw the pipe bank
     * @param canvas Canvas to draw the pipe bank on
     * @param width Width of the pipe bank
     * @param height Height of the pipe bank
     */
    public void draw(Canvas canvas, float width, float height, float blockSize) {
        // Draw the green rectangle as background
        canvas.drawRect(0f, 0f, width, height, bankPaint);

        // Draw the pipes
        float pipeDim, spacingX, spacingY, scale;
        if(width >= height) {   // Draw pipes horizontally
            pipeDim = width / (bankSize + 2);
            spacingX = 2 * pipeDim / (bankSize + 1);
            scale = pipeDim < height ? pipeDim / blockSize : height / blockSize;
            spacingY = (height - blockSize*scale) / 2;
            for(int i = 0; i < bankSize; i++) {
                if(pipes[i] == null) {
                    pipes[i] = getRandomPipe();
                }

                canvas.save();
                canvas.translate(pipeDim*i + spacingX*(i+1) , spacingY);
                canvas.scale(scale, scale);
                // Draw the pipe
                if(pipes[i] != activePipe) {
                    pipes[i].draw(canvas);
                }

                canvas.restore();
            }
        } else {   // Draw pipes vertically
            pipeDim = height / (bankSize + 2);
            spacingY = 2 * pipeDim / (bankSize + 1);
            scale = pipeDim < width ? pipeDim / blockSize : height / blockSize;
            spacingX = (width - blockSize*scale) / 2;
            for(int i = 0; i < bankSize; i++) {
                if(pipes[i] == null) {
                    pipes[i] = getRandomPipe();
                }

                canvas.save();
                canvas.translate(spacingX, pipeDim*i + spacingY*(i+1));
                canvas.scale(scale, scale);
                // Draw the pipe
                if(pipes[i] != activePipe) {
                    pipes[i].draw(canvas);
                }

                canvas.restore();
            }
        }
    }
}
