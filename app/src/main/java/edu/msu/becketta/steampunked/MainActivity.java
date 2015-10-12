package edu.msu.becketta.steampunked;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

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
    void onStartGame(View view) {
        Intent intent = new Intent(this, GameActivity.class);

        // TODO: get variable values from button and spinners views
        // Get player names from views and get board size from spinner
        String playerOne = "";
        String playerTwo = "";
        GameView.dimension boardSize = GameView.dimension.SMALL;

        intent.putExtra(GameActivity.PLAYER_ONE_NAME, playerOne);
        intent.putExtra(GameActivity.PLAYER_TWO_NAME, playerTwo);
        intent.putExtra(GameView.BOARD_SIZE, boardSize);

        startActivity(intent);
    }
}
