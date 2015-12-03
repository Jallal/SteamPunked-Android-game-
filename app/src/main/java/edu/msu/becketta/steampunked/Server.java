package edu.msu.becketta.steampunked;

import android.util.Xml;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Created by Aaron Beckett on 11/22/2015.
 */
public class Server {

    private static final String LOGIN_URL = "http://cse.msu.edu/~elhazzat/cse476/proj2/login.php";
    private static final String CREATE_USER_URL = "http://cse.msu.edu/~elhazzat/cse476/proj2/newuser.php";
    private static final String CREATE_NEW_GAME = "http://cse.msu.edu/~elhazzat/cse476/proj2/newgame.php";
    private static final String JOIN_GAME = "http://cse.msu.edu/~elhazzat/cse476/proj2/joingame.php";
    private static final String UPDATE_GAME = "http://cse.msu.edu/~elhazzat/cse476/proj2/updategame.php";
    private static final String GET_GAME_STATUS = "http://cse.msu.edu/~elhazzat/cse476/proj2/getgamestatus.php";
    private static final String UTF8 = "UTF-8";

    public enum GamePostMode {
        CREATE,
        UPDATE
    }

    /**
     * Have we been told to cancel?
     */
    private boolean cancel = false;


    public InputStream getGameState(String usr) {
        // Create a get query
        String query = GET_GAME_STATUS + "?username=" + usr;

        try {
            URL url = new URL(query);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            int responseCode = conn.getResponseCode();
            if(responseCode != HttpURLConnection.HTTP_OK) {
                return null;
            }

            InputStream stream = conn.getInputStream();
            return stream;

        } catch (MalformedURLException e) {
            // Should never happen
            return null;
        } catch (IOException ex) {
            return null;
        }
    }

    public boolean sendGameState(String usr, GameActivity game, GamePostMode mode, String token) {
        // Create an XML packet with the information about the current image
        XmlSerializer xml = Xml.newSerializer();
        StringWriter writer = new StringWriter();

        try {
            xml.setOutput(writer);

            xml.startDocument(UTF8, true);

            game.saveToXML(xml);

            xml.endDocument();

        } catch (IOException e) {
            // This won't occur when writing to a string
            return false;
        }

        final String xmlStr = writer.toString();

        /*
         * Convert the XML into HTTP POST data
         */
        String postDataStr;
        try {
            postDataStr = "game=" + URLEncoder.encode(xmlStr, UTF8);
        } catch (UnsupportedEncodingException e) {
            return false;
        }

        /*
         * Send the data to the server
         */
        byte[] postData = postDataStr.getBytes();
        InputStream stream = null;
        try {
            String query;
            switch(mode) {
                case CREATE:
                    query = CREATE_NEW_GAME + "?username=" + usr + "&token=" + token;
                    break;
                case UPDATE:
                    query = UPDATE_GAME + "?username=" + usr;
                    break;
                default:
                    query = UPDATE_GAME + "?username=" + usr;
                    break;
            }
            URL url = new URL(query);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Length", Integer.toString(postData.length));
            conn.setUseCaches(false);

            OutputStream out = conn.getOutputStream();
            out.write(postData);
            out.close();

            int responseCode = conn.getResponseCode();
            if(responseCode != HttpURLConnection.HTTP_OK) {
                return false;
            }

            stream = conn.getInputStream();

            /**
             * Create an XML parser for the result
             */
            if(serverFailed(stream)) {
                return false;
            }

        } catch (MalformedURLException e) {
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

    public boolean joinGame(String usr, String token) {

        // Create the get query
        String query = JOIN_GAME + "?username=" + usr + "&token=" + token;

        InputStream stream = null;
        try {
            URL url = new URL(query);

            if (cancel) { return false; }
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            String postDataStr = "bullshit";
            byte[] postData = postDataStr.getBytes();

            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Length", Integer.toString(postData.length));
            conn.setUseCaches(false);

            OutputStream out = conn.getOutputStream();
            out.write(postData);
            out.close();

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
     * Send the server a username and password to login
     * @param usr The username
     * @param password The user's password
     * @return true if the login was successful
     */
    public boolean login(String usr, String password) {
        // Create the get query
        String query = LOGIN_URL + "?username=" + usr + "&password=" + password;

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
        // Create the get query
        String query = CREATE_USER_URL + "?username=" + usr + "&password=" + password;

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

    private boolean serverFailed(InputStream stream) {
        boolean fail = true;

        try {
            Scanner scanner = new Scanner(stream);

            String code = scanner.next();

            if (code.equals("success")) {
                fail = false;
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

    /**
     * Skip the XML parser to the end tag for whatever
     * tag we are currently within.
     * @param xml the parser
     * @throws IOException
     * @throws XmlPullParserException
     */
    public static void skipToEndTag(XmlPullParser xml)
            throws IOException, XmlPullParserException {
        int tag;
        do
        {
            tag = xml.next();
            if(tag == XmlPullParser.START_TAG) {
                // Recurse over any start tag
                skipToEndTag(xml);
            }
        } while(tag != XmlPullParser.END_TAG &&
                tag != XmlPullParser.END_DOCUMENT);
    }
}
