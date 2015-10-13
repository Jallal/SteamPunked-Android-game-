package edu.msu.becketta.steampunked;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;

/**
 * TODO: document your custom view class.
 */
public class GameView extends View {

    // Intent identifiers
    public final static String BOARD_SIZE = "edu.msu.becketta.steampunked.BOARD_SIZE";

    // Bundle identifiers
    public final static String PLAYING_AREA = "playingArea";
    public final static String PIPE_BANK = "pipeBank";

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
     * Bitmaps for each type of pipe
     */
    private Bitmap rightAngle = null;
    private Bitmap straight = null;
    private Bitmap tee = null;
    private Bitmap cap = null;

    /**
     * Bitmap for a gauge
     */
    private Bitmap gauge = null;

    /**
     * Bitmap for a pipe handle drawn on initial pipe
     */
    private Bitmap handle = null;

    /**
     * Bitmap for a leak in the pipe system
     */
    private Bitmap leak = null;


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
        bank = new PipeBank();
        /*
         * Load bitmap images
         */
        loadBitmaps();
    }

    private void loadBitmaps() {
        rightAngle = BitmapFactory.decodeResource(getResources(), R.drawable.a90);
        straight = BitmapFactory.decodeResource(getResources(), R.drawable.straight);
        tee = BitmapFactory.decodeResource(getResources(), R.drawable.tee);
        cap = BitmapFactory.decodeResource(getResources(), R.drawable.cap);
        gauge = BitmapFactory.decodeResource(getResources(), R.drawable.gauge);
        handle = BitmapFactory.decodeResource(getResources(), R.drawable.handle);
        leak = BitmapFactory.decodeResource(getResources(), R.drawable.leak);
    }

    public void initialize(Intent intent) {
        dimension size = (dimension)intent.getSerializableExtra(BOARD_SIZE);
        initializeGameArea(size);
    }

    public void saveState(Bundle bundle) {
        bundle.putSerializable(PLAYING_AREA, gameField);
        bundle.putSerializable(PIPE_BANK, bank);
    }

    public void loadState(Bundle bundle) {
        gameField = (PlayingArea)bundle.getSerializable(PLAYING_AREA);
        // Need to sync the pipes restored in gameField so that they get the reference
        // to the PlayingArea they are in
        if(gameField != null) {
            gameField.syncPipes();
        }

        bank = (PipeBank)bundle.getSerializable(PIPE_BANK);
        // Need to call init on the PipeBank to recreate the Paint Objects
        if(bank != null) {
            bank.init();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // TODO: draw the playing field and bank of pipes to add
        // Use normalized coordinates:
        //      0 <= y < .8   draw the playing field
        //     .8 <= y <= 1   draw the pipe bank
        /*
         * Draw playing field
         */
        gameField.draw(canvas);

        /*
         * Draw pipe bank
         */
        canvas.save();
        canvas.translate(0f, canvas.getHeight() * bankLocation);
        bank.draw(canvas, (float)canvas.getWidth(), canvas.getHeight() * (1 - bankLocation));
        canvas.restore();
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
    }

    private void setBoardStartsEnds(int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4) {
        Pipe start1 = new Pipe(false, true, false, false);
        Pipe start2 = new Pipe(false, true, false, false);
        Pipe end1 = new Pipe(false, false, false, true);
        Pipe end2 = new Pipe(false, false, false, true);

        gameField.add(start1, x1, y1);
        gameField.add(start2, x2, y2);
        gameField.add(end1, x3, y3);
        gameField.add(end2, x4, y4);
    }
}
