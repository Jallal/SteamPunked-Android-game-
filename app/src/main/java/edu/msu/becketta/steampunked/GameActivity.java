package edu.msu.becketta.steampunked;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class GameActivity extends AppCompatActivity {

    public final static String PLAYER_ONE_NAME = "edu.msu.becketta.steampunked.PLAYER_ONE_NAME";
    public final static String PLAYER_TWO_NAME = "edu.msu.becketta.steampunked.PLAYER_TWO_NAME";

    private String playerOneName;
    private String playerTwoName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        if(savedInstanceState != null) {
            // TODO: load playerOneName and playerTwoName from bundle

            getGameView().loadState(savedInstanceState);
        } else { // There is no saved state, use the intent for initialization
            Intent intent = getIntent();
            playerOneName = intent.getStringExtra(PLAYER_ONE_NAME);
            playerTwoName = intent.getStringExtra(PLAYER_TWO_NAME);

            getGameView().initialize(intent);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);

        getGameView().saveState(bundle);
    }

    public void onForfeit(View view) {
        String winner = "Player One";
        onGameOver(winner);
    }

    /**
     * Once someone wins or there is a forfeit
     */
    public void onGameOver(String winner) {
        Intent intent = new Intent(this, EndGameActivity.class);

        intent.putExtra(EndGameActivity.WINNER, winner);

        startActivity(intent);
    }

    GameView getGameView() {
        // TODO: once GameView has been added to the layout, findViewByID and return
        return new GameView(this);
    }
}
