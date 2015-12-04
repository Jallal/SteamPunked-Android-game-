package edu.msu.becketta.steampunked;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
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
     * Dimension and spacing of pipes used to calculate hits
     */
    private float pipeDim;
    private float spacing;
    private boolean horizontal;

    /**
     * Constructor for the PipeBank
     * @param context Context passed to Pipes when generating new pipes
     */
    public PipeBank(Context context) {
        this.context = context;

        bankPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bankPaint.setColor(Color.argb(90, 0, 100, 0));  // A semi-transparent green

        for(int i = 0; i < bankSize; i++) {
            if (pipes[i] == null) {
                pipes[i] = getRandomPipe();
            }
        }
    }

    public void loadFromSavedState(XmlPullParser xml, GameView view) throws IOException, XmlPullParserException {

        final Pipe[] newPipes = new Pipe[bankSize];

        int count = 0;
        while (count < bankSize && xml.nextTag() == XmlPullParser.START_TAG) {
            if (xml.getName().equals("pipe")) {
                newPipes[count] = Pipe.bankPipeFromXml(xml, view.getContext());
            }
            Server.skipToEndTag(xml);
        }

        view.post(new Runnable() {

            @Override
            public void run() {
                pipes = newPipes;
            }
        });
    }

    public void saveToXML(XmlSerializer xml) throws IOException {
        xml.startTag(null, "bank");

        for (Pipe p : pipes) {
            p.bankPipeToXml(xml);
        }

        xml.endTag(null, "bank");
    }

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

    public void setActivePipe(Pipe active) {
        if(active == null) {
            for(int i = 0; i < pipes.length; i++) {
                if(pipes[i] == activePipe) {
                    pipes[i] = null;
                    pipes[i] = getRandomPipe();
                }
            }
        }
        activePipe = active;
    }


    /**
     * Check if the location hits a pipe in the pipe bank
     * @param xpos X position to test relative to the upper left corner of the pipe bank
     * @param ypos Y position to test relative to the upper left corner of the pipe bank
     * @return The pipe in the pipe bank at the location (xpos, ypos) or null if there is no pipe
     */
    public Pipe hitPipe(float xpos, float ypos) {


       // Log.i("HIT PIPE", "HIT PIPE" + xpos + "," +  ypos);

        activePipe = null;

        // Set the primary dimension that we care about
        //      (x dimension for horizontal, y dimension for vertical)
        float pos = xpos;
        if(!horizontal) {
            pos = ypos;
        }

        // Calculate which section the touch occurred in and if it hit a pipe
        int section = (int)(pos / (spacing + pipeDim));
        if((section < bankSize) && (pos % (spacing + pipeDim) > spacing)) {
            Log.i("Hit Pipe", "You hit pipe " + section + " in the pipe bank.");
            return pipes[section];
        }

        return null;
    }

    /**
     * Draw the pipe bank
     * @param canvas Canvas to draw the pipe bank on
     * @param width Width of the pipe bank
     * @param height Height of the pipe bank
     */
    public void draw(Canvas canvas, float width, float height, float blockSize) {
        /*
         * Draw the green rectangle as background
         */
        canvas.drawRect(0f, 0f, width, height, bankPaint);

        /*
         * Draw the pipes
         */
        // Create local variables that we need to calculate
        float spacingX, spacingY, scale;

        // Decide how to draw the pipes
        if(width >= height) {   // Draw pipes horizontally
            // Set variables
            horizontal = true;
            pipeDim = width / (bankSize + 2);
            spacing = spacingX = 2 * pipeDim / (bankSize + 1);
            spacingX += (pipeDim / 2);
            scale = pipeDim < height ? pipeDim / blockSize : height / blockSize;
            spacingY = height / 2;

            // Loop through the pipes to draw them, creating a new random pipe if necessary
            for(int i = 0; i < bankSize; i++) {

                if((pipes[i] == null)) {
                    pipes[i] = getRandomPipe();
                }

                canvas.save();
                canvas.translate((pipeDim/2)*i + spacingX*(i+1) , spacingY);
                canvas.scale(scale, scale);
                // Draw the pipe
                if(pipes[i] != activePipe) {
                    pipes[i].resetPipe();
                    pipes[i].draw(canvas);
                }

                canvas.restore();
            }
        } else {   // Draw pipes vertically
            // Set variables
            horizontal = false;
            pipeDim = height / (bankSize + 2);
            spacing = spacingY = 2 * pipeDim / (bankSize + 1);
            spacingY += (pipeDim / 2);
            scale = pipeDim < width ? pipeDim / blockSize : height / blockSize;
            spacingX = width / 2;

            // Loop through the pipes to draw them, creating a new random pipe if necessary
            for(int i = 0; i < bankSize; i++) {
                if(pipes[i] == null) {
                    pipes[i] = getRandomPipe();
                }

                canvas.save();
                canvas.translate(spacingX, (pipeDim/2)*i + spacingY*(i+1));
                canvas.scale(scale, scale);
                // Draw the pipe
                if(pipes[i] != activePipe) {
                    pipes[i].setLocation(0, 0);
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
