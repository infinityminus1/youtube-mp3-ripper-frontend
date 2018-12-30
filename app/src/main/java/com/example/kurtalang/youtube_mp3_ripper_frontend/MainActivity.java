package com.example.kurtalang.youtube_mp3_ripper_frontend;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handleContentDeliveredByIntent();

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void handleContentDeliveredByIntent() {
        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleSendText(intent); // Handle text being sent
            }
        } else {
            // Handle other intents, such as being started from the home screen
            return;
        }
    }

    public void handleSendText(Intent intent) {
        final String TAG = "MainActivity.handleSendText";
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);

        // Update UI to reflect text being shared
        if (sharedText != null) {
            String msg = String.format("handleSendText() received shared text %s", sharedText);
            Log.d(TAG, msg);
            // Assert URL from youtube is passed
            if (!sharedText.contains("youtu")) {
                msg = String.format("handleSendText() received shared text %s does not contain a youtube url", sharedText);
                Log.d(TAG, msg);
                return;
            }

            // TODO: Make api GET call to "54.215.234.18&uri="
            makeYoutubeRipApiCall(sharedText);
        }

        Log.d(TAG, "handleSendText() received NO shared text");
    }

    public void makeYoutubeRipApiCall(String url) {
        final String TAG = "MainActivity.makeYoutubeRipApiCall";
        final String URL_BASE = "http://54.215.234.18/?url=";
        URL url_obj;
        HttpURLConnection urlConnection;

        try {
            String url_string = URL_BASE + url;
            url_obj = new URL(url_string);
        }
        catch (java.net.MalformedURLException e) {
            String msg = String.format("makeYoutubeRipApiCall() MalformedURLException ", e);
            Log.d(TAG, msg);
            return;
        }

        try {
            urlConnection = (HttpURLConnection) url_obj.openConnection();
        }
        catch (java.io.IOException e) {
            String msg = String.format("makeYoutubeRipApiCall() openConnection() IOException ", e);
            Log.d(TAG, msg);
            return;
        }

        try {
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
        }
        catch (java.io.IOException e) {
            String msg = String.format("makeYoutubeRipApiCall() getInputStream() IOException ", e);
            Log.d(TAG, msg);
            return;
        }
        finally {
            urlConnection.disconnect();
        }
    }
}
