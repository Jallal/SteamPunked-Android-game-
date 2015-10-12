package edu.msu.becketta.steampunked;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

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

    public final String PLAYER_ONE_NAME = "edu.msu.becketta.steampunked.PLAYER_ONE_NAME";
    public final String PLAYER_TWO_NAME = "edu.msu.becketta.steampunked.PLAYER_TWO_NAME";
    public final String BOARD_SIZE = "edu.msu.becketta.steampunked.BOARD_SIZE";

    /**
     * Create the activity
     * @param savedInstanceState Stored activity bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     * Start the game activity
     * @param view The view calling this function
     */
    public void onStartGame(View view) {
        Intent intent = new Intent(this, GameActivity.class);

        // Get player names from views and get board size from spinner
        String playerOne = "";
        String playerTwo = "";
        int boardSize = 0;

        intent.putExtra(PLAYER_ONE_NAME, playerOne);
        intent.putExtra(PLAYER_TWO_NAME, playerTwo);
        intent.putExtra(BOARD_SIZE, boardSize);

        startActivity(intent);
    }
}
