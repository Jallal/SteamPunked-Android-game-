package edu.msu.becketta.steampunked;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Created by Aaron Beckett on 11/22/2015.
 */
public class Server {

    private static final String LOGIN_URL = "http://webdev.cse.msu.edu/~elhazzat/cse476/proj2/login.php";
    private static final String CREATE_USER_URL = "";
    private static final String UTF8 = "UTF-8";

    /**
     * Have we been told to cancel?
     */
    private boolean cancel = false;


    /**
     * Send the server a username and password to login
     * @param usr The username
     * @param password The user's password
     * @return true if the login was successful
     */
    public boolean login(String usr, String password) {
        // Create the get query
        String query = LOGIN_URL + "?user=" + usr + "&password=" + password;

        InputStream stream = null;
        try {
            URL url = new URL(query);

            if (cancel) { return false; }
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return false;
            }

            stream = conn.getInputStream();
            if(serverFailed(stream)) {
                return false;
            }

        } catch (MalformedURLException e) {
            // Should never happen
            return false;
        } catch (IOException ex) {
            return false;
        } finally {
            if(stream != null) {
                try {
                    stream.close();
                } catch (IOException ex) {
                    // Fail silently
                }
            }
        }

        return true;
    }

    /**
     * Send the server a new username and password to create a new user
     * @param usr The new username
     * @param password The new user's password
     * @return true if the new user was successfully created
     */
    public boolean createNewUser(String usr, String password) {
        return false;
    }

    private boolean serverFailed(InputStream stream) {
        boolean fail = false;

        try {
            Scanner scanner = new Scanner(stream);

            String code = scanner.next();

            if (code != "success") {
                fail = true;
            }

            scanner.close();

        } catch (NoSuchElementException ex) {
            fail = true;
        }

        return fail;
    }

    public void cancel() {
        this.cancel = true;
    }
}
