package edu.msu.becketta.steampunked;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.io.Serializable;
import java.util.Random;

/**
 * Created by Aaron Beckett on 10/12/2015.
 *
 * An object that manages the drawing and maintenance of a bank of randomly generated pipes
 * that players can select from on their turn
 */
public class PipeBank implements Serializable {

    /**
     * Number of pipes in the bank
     */
    public final int bankSize = 5;

    /**
     * Array that stores the relative probabilities of generating each pipe type
     */
    private final PipeProbability[] relativePipeProbs = {
            new PipeProbability(Pipe.pipeType.STRAIGHT, 1),
            new PipeProbability(Pipe.pipeType.RIGHT_ANGLE, 2),
            new PipeProbability(Pipe.pipeType.TEE, 2),
            new PipeProbability(Pipe.pipeType.CAP, 1)
    };

    /**
     * Array of pipes in the bank
     */
    private Pipe[] pipes = new Pipe[bankSize];

    /**
     * The pipe that is being dragged
     */
    private Pipe activePipe = null;

    /**
     * Random number generator used to select new pipes for the bank
     */
    private static Random random = new Random(System.nanoTime());

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
        // Calculate total relative probs in order to scale the random integer generation
        int probTotal = 0;
        for(PipeProbability pair : relativePipeProbs) {
            probTotal += pair.relativeProb;
        }

        // Get random integer and calculate the corresponding index
        int index = random.nextInt(probTotal);
        int sum = 0;
        int i=0;
        while(sum <= index ) {
            sum = sum + relativePipeProbs[i++].relativeProb;
        }

        return new Pipe(context, relativePipeProbs[i-1].type);
    }

    public Pipe getActivePipe() {
        return activePipe;
    }

    public void clearActivePipe() {
        activePipe = null;
    }

    public Pipe hitPipe(float xpos, float ypos) {
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

    /**
     * Nested class that gives a pipe type a weighted probability
     */
    private static class PipeProbability implements Serializable {
        public Pipe.pipeType type;
        public int relativeProb;

        public PipeProbability(Pipe.pipeType typ, int prob) {
            type = typ;
            relativeProb = prob;
        }
    }
}
