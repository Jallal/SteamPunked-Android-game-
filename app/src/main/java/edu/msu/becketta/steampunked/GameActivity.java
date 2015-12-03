package edu.msu.becketta.steampunked;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;

public class GameActivity extends AppCompatActivity {

    public final static String MY_NAME = "edu.msu.becketta.steampunked.MY_NAME";
    public final static String GCM_TOKEN = "edu.msu.becketta.steampunked.GCM_TOKEN";
    public final static String AM_PLAYER_ONE = "edu.msu.becketta.steampunked.AM_PLAYER_ONE";

    private final static String P_NAME = "my_name";
    private final static String TOKEN = "token";
    private final static String O_NAME = "opponent_name";
    private final static String AM_P1 = "am_player_one";

    private String myName = "";
    private String token;
    private String opponentName = "";
    private boolean amPlayerOne;

    private ProgressDialog progressDialog;
    private boolean startGame = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        if(savedInstanceState != null) {
            myName = savedInstanceState.getString(P_NAME);
            token = savedInstanceState.getString(TOKEN);
            opponentName = savedInstanceState.getString(O_NAME);
            amPlayerOne = savedInstanceState.getBoolean(AM_P1);

            getGameView().loadState(savedInstanceState);
        } else { // There is no saved state, use the intent for initialization
            Intent intent = getIntent();
            myName = intent.getStringExtra(MY_NAME);
            token = intent.getStringExtra(GCM_TOKEN);
            amPlayerOne = intent.getBooleanExtra(AM_PLAYER_ONE, false);

            if (amPlayerOne) {
                getGameView().initialize(intent);
                uploadGameState(Server.GamePostMode.CREATE);
                waitForPlayerTwo();
            } else {
                getInitialGame();
            }
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MyGcmListenerService.MESSAGE);
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                handleServerMessage(intent);
            }
        }, intentFilter);

        updateUI();
    }

    private void handleServerMessage(Intent intent) {
        String message = intent.getStringExtra(MyGcmListenerService.DATA);
        Log.i("Push Message", message);
        if (message.equals("turn")) {
            getGameView().startTurn();
        } else if (!message.equals("") && amPlayerOne) {
            opponentName = message;
            getGameView().setPlayerNames(myName, opponentName, Pipe.PipeGroup.PLAYER_ONE);
            startGame();
        }
        updateUI();
    }

    private void waitForPlayerTwo() {
        if (!startGame) {
            progressDialog = ProgressDialog.show(this, "Hold your horses!", "Waiting for second player...", true, true, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    onBackPressed();
                }
            });
        }
    }

    private void startGame() {
        progressDialog.dismiss();
        startGame = true;
        getGameView().startTurn();
        getGameView().invalidate();
    }

    private void getInitialGame() {

    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);

        bundle.putString(P_NAME, myName);
        bundle.putString(TOKEN, token);
        bundle.putString(O_NAME, opponentName);
        bundle.putSerializable(AM_P1, amPlayerOne);

        getGameView().saveState(bundle);
    }

    private void loadGameState() {
        // TODO: get the game state from the server
        if (!getGameView().isInitialized()) {
            GameView.dimension size = GameView.dimension.SMALL;
            getGameView().initialize(size);
        }

        new AsyncTask<String, Void, Boolean>() {

            private ProgressDialog progressDialog;
            private Server server = new Server();
            private volatile boolean cancel = false;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                // TODO: change strings
                progressDialog = ProgressDialog.show(GameActivity.this,
                        getString(R.string.please_wait),
                        getString(R.string.logging_in),
                        true, true, new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                cancel = true;
                            }
                        });
            }

            @Override
            protected Boolean doInBackground(String... params) {
                InputStream stream = server.getGameState(params[0]);
                boolean success = stream != null;
                if(success) {/*
                    try {
                        if (cancel) {
                            return false;
                        }
                    }*/
                }
                return success;
            }

            @Override
            protected void onPostExecute(Boolean success) {
                progressDialog.dismiss();
            }
        }.execute(myName);
    }

    private void uploadGameState(Server.GamePostMode mode) {
        // TODO: push the game state to the server
        UploadTask update = new UploadTask();
        update.setGameView(getGameView());
        update.setUploadMode(mode);
        switch (mode) {
            case UPDATE:
                update.execute(myName, null);
                break;
            case CREATE:
                update.execute(myName, token);
                break;
        }
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
        // TODO: alert the server that I've quit the game

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void onSurrender(View view) {
        onGameOver(opponentName);
    }
    public void onInstall(View view) {
        getGameView().installPipe();
        updateUI();

        // TODO: alert the server that my turn is over by updating the game state
    }
    public void onDiscard(View view) {
        getGameView().discard();
        updateUI();

        // TODO: alert the server that my turn is over by updating the game state
    }
    public void onOpenValve(View view) {
        if(getGameView().openValve()) {
            onGameOver(myName);
        } else {
            onGameOver(opponentName);
        }
    }

    /**
     * Once someone wins or there is a forfeit
     */
    public void onGameOver(String winner) {
        // TODO: Send the final game state to the server

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
        Button discard = (Button) findViewById(R.id.discardButton);
        Button install = (Button) findViewById(R.id.installButton);
        Button surrender = (Button) findViewById(R.id.surrender);
        Button openValve = (Button) findViewById(R.id.openValveButton);

        if (getGameView().isMyTurn()) {
            currentPlayer.setText(baseText + myName);
            discard.setEnabled(true);
            install.setEnabled(true);
            surrender.setEnabled(true);
            openValve.setEnabled(true);
        } else {
            currentPlayer.setText(baseText + opponentName);
            discard.setEnabled(false);
            install.setEnabled(false);
            surrender.setEnabled(false);
            openValve.setEnabled(false);
        }
    }


    private class UploadTask extends AsyncTask<String, Void, Boolean> {

        private ProgressDialog progressDialog;
        private Server server = new Server();
        private GameView view;
        private Server.GamePostMode uploadMode;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // TODO: change strings
            progressDialog = ProgressDialog.show(GameActivity.this,
                    getString(R.string.please_wait),
                    getString(R.string.logging_in),
                    true, true, new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            server.cancel();
                        }
                    });
        }

        public void setGameView(GameView view) {
            this.view = view;
        }
        public void setUploadMode(Server.GamePostMode mode) {
            uploadMode = mode;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            boolean success = server.sendGameState(params[0], view, uploadMode, params[1]);
            return success;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            progressDialog.dismiss();
            // TODO: change strings
            if (success) {
                Toast.makeText(GameActivity.this,
                        R.string.login_success,
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(GameActivity.this,
                        R.string.login_fail,
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}