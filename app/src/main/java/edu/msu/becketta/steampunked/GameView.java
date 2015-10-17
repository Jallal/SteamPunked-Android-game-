package edu.msu.becketta.steampunked;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;

import java.io.Serializable;

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
        float bankXOffset = 0f;
        float bankYOffset = canvas.getHeight() * bankLocation;

        // Landscape layout
        if(canvas.getWidth() > canvas.getHeight()) {
            fieldWidth = (int)(canvas.getWidth() * bankLocation);
            fieldHeight = canvas.getHeight();
            bankWidth = (int)(canvas.getWidth() * (1 - bankLocation));
            bankHeight = canvas.getHeight();
            bankXOffset = canvas.getWidth() * bankLocation;
            bankYOffset = 0f;
        }

        // Determine the scale to draw things
        if(fieldWidth <= fieldHeight) {
            params.gameFieldScale = fieldWidth / params.gameFieldWidth;
        } else {
            params.gameFieldScale = fieldHeight / params.gameFieldHeight;
        }

        // Determine the margins for the playing field
        params.marginX = (int)((fieldWidth - params.gameFieldWidth * params.gameFieldScale) / 2);
        params.marginY = (int)((fieldHeight - params.gameFieldHeight * params.gameFieldScale) / 2);

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
        canvas.translate(bankXOffset, bankYOffset);
        bank.draw(canvas, bankWidth, bankHeight, params.blockSize);
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

        params.gameFieldWidth = gameField.getWidth() * params.blockSize;
        params.gameFieldHeight = gameField.getHeight() * params.blockSize;
    }

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

    private static class Parameters implements Serializable {
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
        public int marginX = 0;
        public int marginY = 0;

        /**
         * Current scale to draw the playing field
         */
        public float gameFieldScale = 1f;
    }
}
