package edu.msu.becketta.steampunked;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;

/**
 * TODO: document your custom view class.
 */
public class GameView extends View {

    public final static String BOARD_SIZE = "edu.msu.becketta.steampunked.BOARD_SIZE";

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
     * Member object that represents the playing field
     */
    private PlayingArea gameField = null;


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
        // TODO???: add any default setup here, not sure if needed yet
    }

    public void initialize(Intent intent) {
        dimension size = (dimension)intent.getSerializableExtra(BOARD_SIZE);
        initializeGameArea(size);
    }

    public void saveState(Bundle bundle) {
        // TODO: add Serializable PlayingArea "gameField" to the bundle
    }

    public void loadState(Bundle bundle) {
        // TODO: load Serializable PlayingArea back into "gameField"

        // Need to sync the pipes restored in gameField so that they get the reference
        // to the PlayingArea they are in
        gameField.syncPipes();
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

        /*
         * Draw pipe bank
         */
        // Green rectangle

        // New pipes: 5 new pipes evenly spaced with normalized coordinates
    }

    /**
     * Create the PlayingArea object with the given dimension
     * @param board Dimension of the playing field
     */
    public void initializeGameArea(dimension board) {
        switch (board) {
            case SMALL:
                gameField = new PlayingArea(5, 5);
                break;
            case MEDIUM:
                gameField = new PlayingArea(10, 10);
                break;
            case LARGE:
                gameField = new PlayingArea(20, 20);
                break;
            default:
                gameField = new PlayingArea(10, 10);
                break;
        }
    }
}
