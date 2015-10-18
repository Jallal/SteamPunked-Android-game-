package edu.msu.becketta.steampunked;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {


    //member variable for the boardsize
    GameView.dimension boardSize;


    //set the playerOne and playerTwo names


    /**
     * Create the activity
     * @param savedInstanceState Stored activity bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
         * Set up the spinner
         */

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.field_sizes, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        getSpinner().setAdapter(adapter);
        getSpinner().setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View view,
                                       int pos, long id) {
                switch(pos){
                    case 0:
                        boardSize  = GameView.dimension.SMALL;
                        break;
                    case 1:
                        boardSize = GameView.dimension.MEDIUM;
                        break;
                    case 2:
                        boardSize = GameView.dimension.LARGE;
                        break;
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }

        });

    }


    /**
     * The board size choice spinner
     */
    private Spinner getSpinner() {
        return (Spinner) findViewById(R.id.spinnerFields);
    }


    /**
     * Start the game activity
     * @param view The view calling this function
     */
    public void onStartGame(View view) {
        Intent intent = new Intent(this, GameActivity.class);

        //set member variables for playerone and playertwo
        String playerOne;
        String playerTwo;
        TextView textview1 = (TextView) findViewById(R.id.player1);
        playerOne = textview1.getText().toString();
        TextView textview2 = (TextView) findViewById(R.id.player2);
        playerTwo = textview2.getText().toString();
        if (playerOne.trim().equals(""))  playerOne = "Player 1";
        if (playerTwo.trim().equals("")) playerTwo = "Player 2";



        intent.putExtra(GameActivity.PLAYER_ONE_NAME, playerOne);
        intent.putExtra(GameActivity.PLAYER_TWO_NAME, playerTwo);
        intent.putExtra(GameView.BOARD_SIZE, boardSize);

        startActivity(intent);
    }
}
