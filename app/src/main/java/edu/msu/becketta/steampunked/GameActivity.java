package edu.msu.becketta.steampunked;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public class GameActivity extends AppCompatActivity {

    public final static String PLAYER_ONE_NAME = "edu.msu.becketta.steampunked.PLAYER_ONE_NAME";
    public final static String PLAYER_TWO_NAME = "edu.msu.becketta.steampunked.PLAYER_TWO_NAME";

    private final static String P_ONE = "player1";
    private final static String P_TWO = "player2";

    private String playerOneName;
    private String playerTwoName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        if(savedInstanceState != null) {
            playerOneName = savedInstanceState.getString(P_ONE);
            playerTwoName = savedInstanceState.getString(P_TWO);

            getGameView().loadState(savedInstanceState);
        } else { // There is no saved state, use the intent for initialization
            Intent intent = getIntent();
            playerOneName = intent.getStringExtra(PLAYER_ONE_NAME);
            playerTwoName = intent.getStringExtra(PLAYER_TWO_NAME);

            getGameView().initialize(intent);
            getGameView().setPlayerName(playerOneName, Pipe.PipeGroup.PLAYER_ONE);
            getGameView().setPlayerName(playerTwoName, Pipe.PipeGroup.PLAYER_TWO);
        }

        updateUI();
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);

        bundle.putString(P_ONE, playerOneName);
        bundle.putString(P_TWO, playerTwoName);

        getGameView().saveState(bundle);
    }

    @Override
    public void onBackPressed() {
        /*
         * Ask if the user is sure they want to quit the current game.
         *
         * If they do then call super.onBackPressed().
         * If they don't then do nothing.
         */
        AlertDialog.Builder builder = new AlertDialog.Builder(getGameView().getContext());
        builder.setTitle(R.string.quit_game);
        builder.setMessage(R.string.quit_game_confirmation);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                quitGame();
            }
        });
        builder.setNegativeButton(R.string.no, null);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void quitGame() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void onSurrender(View view) {
        String winner = getGameView().getPlayerOneTurn() ? playerTwoName : playerOneName;
        onGameOver(winner);
    }
    public void onInstall(View view) {
        getGameView().installPipe();
        updateUI();
    }
    public void onDiscard(View view) {
        getGameView().discard();
        updateUI();
    }
    public void onOpenValve(View view) {
        if(getGameView().openValve()) {
            onGameOver(getGameView().getPlayerOneTurn() ? playerOneName : playerTwoName);
        } else {
            onGameOver(getGameView().getPlayerOneTurn() ? playerTwoName : playerOneName);
        }
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
        return (GameView)findViewById(R.id.gameView);
    }

    //set the current active player
    private void updateUI(){
        TextView currentPlayer = (TextView)findViewById(R.id.currentPlayer);
        String baseText = getString(R.string.your_turn) + '\n';
        if(getGameView().getPlayerOneTurn()) currentPlayer.setText(baseText + playerOneName);
        else currentPlayer.setText(baseText + playerTwoName);
    }


}