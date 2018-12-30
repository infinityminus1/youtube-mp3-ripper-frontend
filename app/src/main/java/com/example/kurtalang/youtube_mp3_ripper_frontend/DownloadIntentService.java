package com.example.kurtalang.youtube_mp3_ripper_frontend;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class DownloadIntentService extends IntentService {

    private static final String TAG = DownloadIntentService.class.getSimpleName();
    private static final String URL_BASE = "http://54.215.234.18/?url=";

    public static final String PENDING_RESULT_EXTRA = "pending_result";
    public static final String URL_EXTRA = "url";

    public static final int RESULT_CODE = 0;
    public static final int INVALID_URL_CODE = 1;
    public static final int CONNECTION_ERROR_CODE = 2;
    public static final int INPUT_STREAM_ERROR_CODE = 3;
    public static final int ERROR_CODE = 4;

    public DownloadIntentService() {
        super("Anonymous");
    }

    public DownloadIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        URL url;
        HttpURLConnection urlConnection;

        PendingIntent reply = intent.getParcelableExtra(PENDING_RESULT_EXTRA);
        String url_string = URL_BASE + intent.getStringExtra(URL_EXTRA);

        try {

            // Create the url object
            try {
                url = new URL(url_string);
            } catch (MalformedURLException exc) {
                reply.send(INVALID_URL_CODE);
                return;
            } catch (Exception exc) {
                // could do better by treating the different sax/xml exceptions individually
                reply.send(ERROR_CODE);
                return;
            }

            // Open the connection to the url
            try {
                Log.d(TAG, String.format("About to open connection to %s", url_string));
                urlConnection = (HttpURLConnection) url.openConnection();
            }
            catch (java.io.IOException e) {
                String msg = String.format("DownloadIntentService() openConnection() IOException ", e);
                Log.d(TAG, msg);
                reply.send(CONNECTION_ERROR_CODE);
                return;
            } catch (Exception exc) {
                // could do better by treating the different sax/xml exceptions individually
                reply.send(ERROR_CODE);
                return;
            }

            // Get the result of the input stream if available and close the url connection
            try {
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            }
            catch (java.io.IOException e) {
                String msg = String.format("makeYoutubeRipApiCall() getInputStream() IOException ", e);
                Log.d(TAG, msg);
                reply.send(INPUT_STREAM_ERROR_CODE);
            } catch (Exception exc) {
                // could do better by treating the different sax/xml exceptions individually
                reply.send(ERROR_CODE);
            }
            finally {
                urlConnection.disconnect();
                reply.send(RESULT_CODE);
            }

        } catch (PendingIntent.CanceledException exc) {
            Log.i(TAG, "reply cancelled", exc);
        }
    }
}
