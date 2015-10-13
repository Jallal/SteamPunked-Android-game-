package edu.msu.becketta.steampunked;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class EndGameActivity extends AppCompatActivity {

    public final static String WINNER = "edu.msu.becketta.steampunked.WINNER";

    /**
     * String that saves the winner's name
     */
    private String winner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_game);

        if(savedInstanceState != null) {
            // TODO: load winner string saved in bundle
        } else {
            Intent intent = getIntent();
            winner = intent.getStringExtra(WINNER);
        }
    }
}
