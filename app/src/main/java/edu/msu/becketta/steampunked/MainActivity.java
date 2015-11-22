package edu.msu.becketta.steampunked;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private final static String PREFERENCES = "preferences";
    private final static String USERNAME = "username";
    private final static String PASSWORD = "password";

    /**
     * Is the player logged in to the system
     */
    private boolean isLoggedIn = false;

    /**
     * The user's saved or entered username
     */
    private String username;

    /**
     * The user's saved or entered password
     */
    private String password;

    /**
     * Store the selected board size
      */
    GameView.dimension boardSize;


    /**
     * Create the activity
     *
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
                switch (pos) {
                    case 0:
                        boardSize = GameView.dimension.SMALL;
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

        readPreferences();

    }

    private void readPreferences() {
        SharedPreferences settings = getSharedPreferences(PREFERENCES, MODE_PRIVATE);

        username = settings.getString(USERNAME, "");
        password = settings.getString(PASSWORD, "");

        setLoginStatus();
    }

    private void writePreferneces() {
        SharedPreferences settings = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.putString(USERNAME, username);
        editor.putString(PASSWORD, password);

        editor.commit();
    }


    /**
     * The board size choice spinner
     */
    private Spinner getSpinner() {
        return (Spinner) findViewById(R.id.spinnerFields);
    }


    /**
     * Start the game activity
     *
     * @param view The view calling this function
     */
    public void onLoginStartGame(View view) {
        if (isLoggedIn) {
            // TODO: If we're logged in then we need to start a new game. Hold off on this until after the checkpoint
        } else {

            TextView user = (TextView) findViewById(R.id.username);
            username = user.getText().toString();
            TextView pass = (TextView) findViewById(R.id.password);
            password = pass.getText().toString();

            if (setLoginStatus()) {
                // TODO: Enable/disable certain views and change text of "Login" button
                TextView startButton = (TextView) findViewById(R.id.startButton);
                startButton.setText(R.string.gameNew);
            } else {
                // TODO: popup Toast that says we were unable to login
                //display in short period of time
                Toast.makeText(getApplicationContext(), "Unable to login",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Display the game instructions
     */
    public void onHowToPlay(View view) {
        new AlertDialog.Builder(this)
                .setTitle("Game Instructions")
                .setMessage(("Connect your pipes from start to finish before your friends do!  " +
                        "But be careful, if there is a leak in your connection you lose!"))
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
               
            }
        }).show();
    }

    private boolean setLoginStatus() {
        if (username != "" && password != "" && Server.checkUser(username, password)) {
            isLoggedIn = true;
        } else {
            isLoggedIn = false;
        }

        return isLoggedIn;
    }
}
