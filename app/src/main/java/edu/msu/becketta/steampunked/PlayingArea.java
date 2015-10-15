package edu.msu.becketta.steampunked;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.io.Serializable;

/**
 * A representation of the playing area
 */
public class PlayingArea implements Serializable {
    /**
     * Width of the playing area (integer number of cells)
     */
    private int width;

    /**
     * Height of the playing area (integer number of cells)
     */
    private int height;

    /**
     * Storage for the pipes
     * First level: X, second level Y
     */
    private Pipe [][] pipes;

    /**
     * TEMPORARY PAINT OBJECT FOR TESTING
     */
    private transient Paint debugPaint;

    /**
     * Construct a playing area
     * @param width Width (integer number of cells)
     * @param height Height (integer number of cells)
     */
    public PlayingArea(int width, int height) {
        this.width = width;
        this.height = height;

        // Allocate the playing area
        // Java automatically initializes all of the locations to null
        pipes = new Pipe[width][height];

        debugPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        debugPaint.setColor(Color.argb(25, 0, 100, 82));  // A transparent blue
    }

    /**
     * Get the playing area height
     * @return Height
     */
    public int getHeight() {
        return height;
    }

    /**
     * Get the playing area width
     * @return Width
     */
    public int getWidth() {
        return width;
    }

    /**
     * Get the pipe at a given location.
     * This will return null if outside the playing area.
     * @param x X location
     * @param y Y location
     * @return Reference to Pipe object or null if none exists
     */
    public Pipe getPipe(int x, int y) {
        if(x < 0 || x >= width || y < 0 || y >= height) {
            return null;
        }

        return pipes[x][y];
    }

    /**
     * Add a pipe to the playing area
     * @param pipe Pipe to add
     * @param x X location
     * @param y Y location
     */
    public void add(Pipe pipe, int x, int y) {
        pipes[x][y] = pipe;
        pipe.set(this, x, y);
    }

    /**
     * Search to determine if this pipe has no leaks
     * @param start Starting pipe to search from
     * @return true if no leaks
     */
    public boolean search(Pipe start) {
        /*
         * Set the visited flags to false
         */
        for(Pipe[] row: pipes) {
            for(Pipe pipe : row) {
                if (pipe != null) {
                    pipe.setVisited(false);
                }
            }
        }

        /*
         * The pipe itself does the actual search
         */
        return start.search();
    }

    /**
     * Iterate through each pipe in the playing area to set its loaction and
     * ensure it has the reference to this PlayingArea
     */
    public void syncPipes() {
        /*
         * Set the position of each pipe and give it a reference to this PlayingArea
         */
        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                if (pipes[x][y] != null) {
                    pipes[x][y].set(this, x, y);
                }
            }
        }
    }

    public void draw(Canvas canvas, float blockSize) {
        canvas.save();

        int playingAreaSize = (int)(width * blockSize);

        /*
         * Draw the outline of the playing field for now
         */
        canvas.drawRect(0, 0, playingAreaSize, playingAreaSize, debugPaint);

        /*
         * Draw each pipe
         */
        for(Pipe[] row : pipes) {
            for(Pipe pipe : row) {
                if(pipe != null) {
                    pipe.drawInPlayingArea(canvas);
                }
            }
        }

        canvas.restore();
    }
}
