package edu.msu.becketta.steampunked;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * My support for Google Cloud Messaging
 */
public class GCM {
    public static final String REGISTER_URL = "http://cse.msu.edu/~elhazzat/cse476/proj2/register.php";

    public void register(final Context context) {

        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    InstanceID instanceID = InstanceID.getInstance(context);
                    String token = instanceID.getToken(context.getString(R.string.gcm_defaultSenderId),
                            GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                    // [END get_token]
                    Log.i("gcm", "GCM Registration Token: " + token);

                    sendToServer(token);

                } catch(IOException ex) {

                }



            }
        }).start();


    }

    private boolean sendToServer(String token) {
        /*
         * Convert to post data
         */
        String postDataStr;
        try {
            postDataStr = "token=" + URLEncoder.encode(token, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return false;
        }

        /*
         * Send the data to the server
         */
        byte[] postData = postDataStr.getBytes();

        InputStream stream = null;
        try {
            URL url = new URL(REGISTER_URL);

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

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                Log.i("response", line);
            }

        } catch (MalformedURLException e) {
            return false;
        } catch (IOException ex) {
            return false;
        } finally {
            if(stream != null) {
                try {
                    stream.close();
                } catch(IOException ex) {
                    // Fail silently
                }
            }
        }

        return true;
    }
}
