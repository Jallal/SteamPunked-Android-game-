package edu.msu.becketta.steampunked;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;

/**
 * TODO: document your custom view class.
 */
public class GameView extends View {

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

    public void saveState(Bundle bundle) {
        // TODO: add any variables to the bundle, may use a serializable nested object
    }

    public void loadState(Bundle bundle) {
        // TODO: load the saved variables from the bundle
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // TODO: draw the playing field and bank of pipes to add
    }

    public void initializeGameArea(int x, int y) {
        gameField = new PlayingArea(x, y);
    }
}
