package edu.msu.becketta.steampunked;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class EndGameActivity extends AppCompatActivity {

    public final static String WINNER = "edu.msu.becketta.steampunked.WINNER";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_game);
    }
}
