package edu.msu.becketta.steampunked;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class GameActivity extends AppCompatActivity {

    public final static String PLAYER_ONE_NAME = "edu.msu.becketta.steampunked.PLAYER_ONE_NAME";
    public final static String PLAYER_TWO_NAME = "edu.msu.becketta.steampunked.PLAYER_TWO_NAME";
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

    private String playerOneName;
    private String playerTwoName;
    private dimension boardSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        if(savedInstanceState != null) {
            // TODO: load the bundle info for GameActivity
        } else { // There is no saved state, use the intent for initialization
            Intent intent = getIntent();
            playerOneName = intent.getStringExtra(PLAYER_ONE_NAME);
            playerTwoName = intent.getStringExtra(PLAYER_TWO_NAME);
            boardSize = (dimension)intent.getSerializableExtra(BOARD_SIZE);
        }

        initializeGameView(boardSize, savedInstanceState);
    }

    private void initializeGameView(dimension board, Bundle savedState) {
        switch (board) {
            case SMALL:
                getGameView().initializeGameArea(5, 5);
                break;
            case MEDIUM:
                getGameView().initializeGameArea(10, 10);
                break;
            case LARGE:
                getGameView().initializeGameArea(20, 20);
                break;
        }

        if(savedState != null) {
            getGameView().loadState(savedState);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);

        getGameView().saveState(bundle);
    }

    void onForfeit(View view) {
        String winner = "Player One";
        onGameOver(winner);
    }

    /**
     * Once someone wins or there is a forfeit
     */
    void onGameOver(String winner) {
        Intent intent = new Intent(this, EndGameActivity.class);

        intent.putExtra(EndGameActivity.WINNER, winner);

        startActivity(intent);
    }

    GameView getGameView() {
        // TODO: once GameView has been added to the layout, findViewByID and return
        return new GameView(this);
    }
}
