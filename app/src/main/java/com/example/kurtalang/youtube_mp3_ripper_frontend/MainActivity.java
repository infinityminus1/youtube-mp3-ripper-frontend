package com.example.kurtalang.youtube_mp3_ripper_frontend;

import android.app.DownloadManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class MainActivity extends AppCompatActivity {

    public static final int MP3_DOWNLOAD_REQUEST_CODE = 1000;
    private final static String BACKEND_URL_BASE = "http://54.215.234.18/?url=";

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        final String TAG = "MainActivity.onActivityResult";
        if (requestCode == MP3_DOWNLOAD_REQUEST_CODE) {
            switch (resultCode) {
                case DownloadIntentService.INVALID_URL_CODE:
                    Log.d(TAG, "onActivityResult() received INVALID_URL_CODE");
                    break;
                case DownloadIntentService.ERROR_CODE:
                    Log.d(TAG, "onActivityResult() received ERROR_CODE");
                    break;
                case DownloadIntentService.RESULT_CODE:
                    Log.d(TAG, "onActivityResult() received RESULT_CODE");
                    break;
            }
            Log.d(TAG, String.format("onActivityResult(): Data from intent is: %s", data));
        }
        super.onActivityResult(requestCode, resultCode, data);
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
            get_youtube_title_with_volley(sharedText);
//            downloadData(sharedText);
//            createIntentForApiCall(sharedText);
        } else {
            Log.d(TAG, "handleSendText() received NO shared text");
        }
    }

    public void createIntentForApiCall(String url) {
        PendingIntent pendingResult = createPendingResult(MP3_DOWNLOAD_REQUEST_CODE, new Intent(), 0);
        Intent intent = new Intent(getApplicationContext(), DownloadIntentService.class);

        intent.putExtra(DownloadIntentService.URL_EXTRA, url);
        intent.putExtra(DownloadIntentService.PENDING_RESULT_EXTRA, pendingResult);

        startService(intent);
    }

    private long downloadData(String youtube_url, String youtube_title) {

        String full_url = BACKEND_URL_BASE + youtube_url;
        Uri uri = Uri.parse(full_url);

        // Create request for android download manager
        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(uri);

//        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED); //Notify client once download is completed!

//        downloadManager.enqueue(request);
        Toast.makeText(getApplicationContext(), "Downloading File", //To notify the Client that the file is being downloaded
                Toast.LENGTH_LONG).show();

        //Setting title of request
        request.setTitle(youtube_title);

        //Setting description of request
//        request.setDescription("Android Data download using DownloadManager.");

        //Set the local destination for the downloaded file to a path
        //within the application's external files directory
        request.setDestinationInExternalFilesDir(MainActivity.this, Environment.DIRECTORY_DOWNLOADS, youtube_title);

        //Enqueue download and save into referenceId
        return downloadManager.enqueue(request);
    }

    public void get_youtube_title_with_volley(final String youtube_url) {

        final String TAG = "get_youtube_title_with_volley.handleSendText";

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = BACKEND_URL_BASE + youtube_url;

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.HEAD, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // TODO: find "youtube_title" header value and return here.
                        Log.d(TAG,"Response is: " + response.substring(0, 500));

                        downloadData(youtube_url,"PLACE_TITLE_HERE");
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG,String.format("That didn't work! %s", error));
                    }
                }
        );
        stringRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 5000000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
}
