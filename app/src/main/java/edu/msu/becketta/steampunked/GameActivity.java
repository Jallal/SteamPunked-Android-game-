package edu.msu.becketta.steampunked;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class GameActivity extends AppCompatActivity {

    public final String WINNER = "edu.msu.becketta.steampunked.WINNER";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
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

        intent.putExtra(WINNER, winner);

        startActivity(intent);
    }
}
