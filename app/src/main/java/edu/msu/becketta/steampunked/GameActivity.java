package edu.msu.becketta.steampunked;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
    }
    public void onDiscard(View view) {
        getGameView().discard();
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
}