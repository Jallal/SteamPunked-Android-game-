package edu.msu.becketta.steampunked;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public class EndGameActivity extends AppCompatActivity {

    public final static String WINNER = "edu.msu.becketta.steampunked.WINNER";

    /**
     * String that saves the winner's name
     */
    private String winner;
    private TextView winnerElement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_game);

        if(savedInstanceState != null) {
            // TODO: load winner string saved in bundle
        } else {
            Intent intent = getIntent();
            winner = intent.getStringExtra(WINNER);
            winnerElement = (TextView)findViewById(R.id.winnerTag);
            winnerElement.setText(winner);
        }
    }

    public void onNewGame(View view){
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public void onBackPressed(){}
}
