package edu.msu.becketta.steampunked;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.text.method.Touch;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * TODO: document your custom view class.
 */
public class GameView extends View {

    // Intent identifiers
    public final static String BOARD_SIZE = "edu.msu.becketta.steampunked.BOARD_SIZE";

    // Bundle identifiers
    public final static String PLAYING_AREA = "playingArea";
    public final static String PIPE_BANK = "pipeBank";
    public final static String PARAMETERS = "parameters";

    /**
     * Valid board sizes:
     *      SMALL: 5x5
     *      MEDIUM: 10x10
     *      LARGE: 20x20
     */
    public enum dimension {
        SMALL,
        MEDIUM,
        LARGE
    }

    /**
     * Normalized y location of the top of the pipe bank
     */
    private final static float bankLocation = 0.8f;

    /**
     * Member object that represents the playing field
     */
    private PlayingArea gameField = null;

    /**
     * Member object that represents the pipe bank
     */
    private PipeBank bank = null;

    /**
     * Object to store some view parameters
     */
    private Parameters params = null;
    /**
     * First touch status
     */
    private Touch touch1 = new Touch();

    /**
     * Second touch status
     */
    private Touch touch2 = new Touch();





    public GameView(Context context) {
        super(context);
        init(null, 0);
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public GameView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        bank = new PipeBank(getContext());
        params = new Parameters();
    }

    public void initialize(Intent intent) {
        dimension size = (dimension)intent.getSerializableExtra(BOARD_SIZE);
        initializeGameArea(size);
    }

    public void saveState(Bundle bundle) {
        bundle.putSerializable(PLAYING_AREA, gameField);
        bundle.putSerializable(PIPE_BANK, bank);
        bundle.putSerializable(PARAMETERS, params);
    }

    public void loadState(Bundle bundle) {
        params = (Parameters)bundle.getSerializable(PARAMETERS);

        gameField = (PlayingArea)bundle.getSerializable(PLAYING_AREA);
        // Need to sync the pipes restored in gameField so that they get the reference
        // to the PlayingArea they are in
        if(gameField != null) {
            gameField.syncPipes();
        }

        bank = (PipeBank)bundle.getSerializable(PIPE_BANK);
    }

    /**
     * Draw the Game View
     * @param canvas Canvas to draw on
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // TODO: draw the playing field and bank of pipes to add
        // Use normalized coordinates:
        //      0 <= y < .8   draw the playing field
        //     .8 <= y <= 1   draw the pipe bank

        // Determine which orientation to draw the view in

        // Portrait layout
        int fieldWidth = canvas.getWidth();
        int fieldHeight = (int)(canvas.getHeight() * bankLocation);
        int bankWidth = canvas.getWidth();
        int bankHeight = (int)(canvas.getHeight() * (1 - bankLocation));
        params.bankXOffset = 0f;
        params.bankYOffset = canvas.getHeight() * bankLocation;

        // Landscape layout
        if(canvas.getWidth() > canvas.getHeight()) {
            fieldWidth = (int)(canvas.getWidth() * bankLocation);
            fieldHeight = canvas.getHeight();
            bankWidth = (int)(canvas.getWidth() * (1 - bankLocation));
            bankHeight = canvas.getHeight();
            params.bankXOffset = canvas.getWidth() * bankLocation;
            params.bankYOffset = 0f;
        }

        // Determine the scale to draw things if the gameFieldScale has not been initialized
        if(params.gameFieldScale == -1f) {
            if(fieldWidth <= fieldHeight) {
                params.gameFieldScale = fieldWidth / params.gameFieldWidth;
            } else {
                params.gameFieldScale = fieldHeight / params.gameFieldHeight;
            }
        }

        // Determine the margins for the playing field if they haven't been initialized yet
        if(params.marginX == 10000000 || params.marginY == 10000000) {
            params.marginX = (int)((fieldWidth - params.gameFieldWidth * params.gameFieldScale) / 2);
            params.marginY = (int)((fieldHeight - params.gameFieldHeight * params.gameFieldScale) / 2);
        }

        /*
         * Draw playing field
         */
        canvas.save();
        canvas.translate(params.marginX, params.marginY);
        canvas.scale(params.gameFieldScale, params.gameFieldScale);
        gameField.draw(canvas, params.blockSize);
        canvas.restore();


        /*
         * Draw pipe bank
         */
        canvas.save();
        canvas.translate(params.bankXOffset, params.bankYOffset);
        bank.draw(canvas, bankWidth, bankHeight, params.blockSize);
        canvas.restore();

        if(params.currentPipe != null){
            canvas.save();
            canvas.translate(params.marginX, params.marginY);
            canvas.scale(params.gameFieldScale, params.gameFieldScale);
            params.currentPipe.draw(canvas);
            canvas.restore();
        }
    }


    /**
     * Handle a touch event
     * @param event The touch event
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int id = event.getPointerId(event.getActionIndex());

        switch(event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                // Set touch ids
                touch1.id = id;
                touch2.id = -1;

                // Set x and y locations in touch1 to locations in the playing field
                getPositions(event);
                touch1.copyToLast();

                // Convert the raw touch data to location in the pipe bank
                // to check if it's a valid location
                float bankx = (event.getX() - params.bankXOffset);
                float banky = (event.getY() - params.bankYOffset);

                // Handle the case where the first touch is over the Pipe Bank
                if(bankx >= 0 && banky >= 0) {
                    params.currentPipe = bank.hitPipe(bankx, banky);
                    bank.setActivePipe(params.currentPipe);
                    if(params.currentPipe != null) {
                        // This location is relative to the playing area
                        params.currentPipe.setLocation(touch1.x,touch1.y);
                    }
                }

                // If the current pipe is not null check if we're selecting it again or selecting
                // the playing area
                params.draggingPipe = false;
                if (params.currentPipe != null) {
                    if(params.currentPipe.hit(touch1.x, touch1.y)) {
                        params.draggingPipe = true;
                    }
                }

                // If we're not dragging the pipe, then touch1 should store
                // raw locations (not relative to the playing area)
                if (!params.draggingPipe) {
                    getPositions(event, false);
                    touch1.copyToLast();
                }

                invalidate();
                return true;

            case MotionEvent.ACTION_POINTER_DOWN:
                if (touch1.id >= 0 && touch2.id < 0) {
                    touch2.id = id;
                    touch2.copyToLast();
                    return true;
                }
                break;

            case MotionEvent.ACTION_UP:

            case MotionEvent.ACTION_CANCEL:
                touch1.id = -1;
                touch2.id = -1;
                invalidate();
                return true;

            case MotionEvent.ACTION_POINTER_UP:
                if (id == touch2.id) {
                    touch2.id = -1;
                } else if (id == touch1.id) {
                    // Make what was touch2 now be touch1 by
                    // swapping the objects.
                    Touch t = touch1;
                    touch1 = touch2;
                    touch2 = t;
                    touch2.id = -1;
                }
                invalidate();
                return true;

            case MotionEvent.ACTION_MOVE:
                if(params.currentPipe != null && params.draggingPipe) {
                    // We are moving the active pipe, use locations relative to the playing area
                    getPositions(event);
                    moveCurrentPipe();
                } else {
                    // We are moving the playing area, use raw touch locations
                    getPositions(event, false);
                    movePlayingArea();
                    scalePlayingArea();
                }
                return true;
        }

        return super.onTouchEvent(event);
    }

    /**
     * Overload getPositions function to allow the default call to convert raw locations
     * to playing area locations
     * @param event The motion event with the touch information
     */
    private void getPositions(MotionEvent event) {
        getPositions(event, true);
    }

    /**
     * Store touch locations in touch objects, translate raw points to playing area points if desired
     * @param event The touch event to handle
     * @param makeRelativeToPlayArea Should the raw locations be converted to playing area locations?
     */
    private void getPositions(MotionEvent event, boolean makeRelativeToPlayArea) {
        for(int i=0;  i<event.getPointerCount();  i++) {
            // Get the pointer id
            int id = event.getPointerId(i);

            // Convert to image coordinates only if we don't want the raw positions
            float x = event.getX(i);
            float y = event.getY(i);
            if(makeRelativeToPlayArea) {
                x = (x - params.marginX) / params.gameFieldScale;
                y = (y - params.marginY) / params.gameFieldScale;
            }

            if(id == touch1.id) {
                touch1.copyToLast();
                touch1.x = x;
                touch1.y = y;
            } else if(id == touch2.id) {
                touch2.copyToLast();
                touch2.x = x;
                touch2.y = y;
            }
        }

        invalidate();
    }

    /**
     * Move the playing area based on the computed deltas in touch1
     */
    private void movePlayingArea() {
        // If no touch1, we have nothing to do
        // This should not happen, but it never hurts
        // to check.
        if(touch1.id < 0) {
            return;
        }

        if(touch1.id >= 0) {
            // At least one touch
            // We are moving
            touch1.computeDeltas();

            params.marginX += touch1.dX;
            params.marginY += touch1.dY;
        }
    }

    /**
     * Scale the playing area based on touch1 and touch2 distances
     */
    private void scalePlayingArea() {

    }

    /**
     * Move the currentPipe based on the computed deltas in touch1
     */
    private void moveCurrentPipe() {
        // If no touch1, we have nothing to do
        // This should not happen, but it never hurts
        // to check.
        if(touch1.id < 0) {
            return;
        }

        if(touch1.id >= 0) {
            // At least one touch
            // We are moving
            touch1.computeDeltas();

            params.currentPipe.setLocation(params.currentPipe.getX() + touch1.dX, params.currentPipe.getY() + touch1.dY);
        }
        if(touch2.id >= 0) {
            // Two touches

            /*
             * Rotation
             */
            float angle1 = angle(touch1.lastX, touch1.lastY, touch2.lastX, touch2.lastY);
            float angle2 = angle(touch1.x, touch1.y, touch2.x, touch2.y);
            float da = angle2 - angle1;
            rotate(da, touch1.x, touch1.y);
        }
    }

    /**
     * Create the PlayingArea object with the given dimension
     * @param board Dimension of the playing field
     */
    private void initializeGameArea(dimension board) {
        switch (board) {
            case SMALL:
                gameField = new PlayingArea(5, 5);
                setBoardStartsEnds(0, 1, 0, 3, 4, 2, 4, 4);
                break;
            case MEDIUM:
                gameField = new PlayingArea(10, 10);
                setBoardStartsEnds(0, 2, 0, 6, 9, 3, 9, 7);
                break;
            case LARGE:
                gameField = new PlayingArea(20, 20);
                setBoardStartsEnds(0, 6, 0, 13, 19, 8, 19, 15);
                break;
        }

        params.gameFieldWidth = gameField.getWidth() * params.blockSize;
        params.gameFieldHeight = gameField.getHeight() * params.blockSize;
    }

    /**
     * Initialize the start and end pipes in the playing area
     * @param x1 X coordinate for player1 start pipe
     * @param y1 Y coordinate for player1 start pipe
     * @param x2 X coordinate for player2 start pipe
     * @param y2 Y coordinate for player2 start pipe
     * @param x3 X coordinate for player1 end pipe
     * @param y3 Y coordinate for player1 end pipe
     * @param x4 X coordinate for player2 end pipe
     * @param y4 Y coordinate for player2 end pipe
     */
    private void setBoardStartsEnds(int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4) {
        Pipe start1 = new Pipe(getContext(), Pipe.pipeType.START);
        Pipe start2 = new Pipe(getContext(), Pipe.pipeType.START);
        Pipe end1 = new Pipe(getContext(), Pipe.pipeType.END);
        Pipe end2 = new Pipe(getContext(), Pipe.pipeType.END);

        params.blockSize = start1.getBitmapHeight();

        gameField.add(start1, x1, y1);
        gameField.add(start2, x2, y2);
        gameField.add(end1, x3, y3);
        gameField.add(end2, x4, y4);
    }
    
    public void installPipe(){
         float x = (params.currentPipe.getX() - params.marginX) / params.gameFieldScale;
        float y = (params.currentPipe.getY() - params.marginY) / params.gameFieldScale;
        gameField.add(params.currentPipe,(int)x ,(int)y);

    }


    /**
     * Rotate the image around the point x1, y1
     * @param dAngle Angle to rotate in degrees
     * @param x1 rotation point x
     * @param y1 rotation point y
     */
    public void rotate(float dAngle, float x1, float y1) {
        params.pipeAngle += dAngle;

        // Compute the radians angle
        double rAngle = Math.toRadians(dAngle);
        float ca = (float) Math.cos(rAngle);
        float sa = (float) Math.sin(rAngle);
        float xp = (params.currentPipe.getX() - x1) * ca - (params.currentPipe.getX() - y1) * sa + x1;
        float yp = (params.currentPipe.getY() - x1) * sa + (params.currentPipe.getY() - y1) * ca + y1;

        params.currentPipe.setLocation(xp,yp);
    }
    /**
     * Determine the angle for two touches
     * @param x1 Touch 1 x
     * @param y1 Touch 1 y
     * @param x2 Touch 2 x
     * @param y2 Touch 2 y
     * @return computed angle in degrees
     */
    private float angle(float x1, float y1, float x2, float y2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        return (float) Math.toDegrees(Math.atan2(dy, dx));
    }


    /********************** NESTED CLASSES *******************************/

    /**
     * Private nested class that acts as a serializable container for important parameters
     */
    private static class Parameters implements Serializable {

        /**
         * Reference to the currently selected pipe
         */
        public Pipe currentPipe = null;

        /**
         * Are we dragging a pipe?
         */
        public boolean draggingPipe = false;

        /**
         * Standard block size in the playing field, used to determine the scale to draw components
         */
        public float blockSize = 0f;

        /**
         * Width and height of the game field
         */
        public float gameFieldWidth = 0f;
        public float gameFieldHeight = 0f;

        /**
         * Current X and Y margins of the playing field
         */
        // Initialize to a ridiculously large value so we know to
        // initialize it in onDraw the first time
        public int marginX = 10000000;
        public int marginY = 10000000;

        /**
         * X and Y offset of the pipe bank
         */
        public float bankXOffset = 0;
        public float bankYOffset = 0;

        /**
         * Current scale to draw the playing field
         */
        public float gameFieldScale = -1f;
        
         /**
         * Pipe rotation angle
         */
        public float pipeAngle = 0;
    }


    /**
     * Local class to handle the touch status for one touch.
     * We will have one object of this type for each of the
     * two possible touches.
     */
    private class Touch {
        /**
         * Change in x value from previous
         */
        public float dX = 0;

        /**
         * Change in y value from previous
         */
        public float dY = 0;
        /**
         * Touch id
         */
        public int id = -1;

        /**
         * Current x location
         */
        public float x = 0;

        /**
         * Current y location
         */
        public float y = 0;

        /**
         * Previous x location
         */
        public float lastX = 0;

        /**
         * Previous y location
         */
        public float lastY = 0;
        /**
         * Copy the current values to the previous values
         */
        public void copyToLast() {
            lastX = x;
            lastY = y;
        }
        /**
         * Compute the values of dX and dY
         */
        public void computeDeltas() {
            dX = x - lastX;
            dY = y - lastY;
        }

    }

}