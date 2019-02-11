package com.example.kurtalang.youtube_mp3_ripper_frontend;

import android.app.DownloadManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    public static final int MP3_DOWNLOAD_REQUEST_CODE = 1000;
    private final static String BACKEND_URL_BASE = "http://52.52.45.237/?url=";  // AWS elastic IP

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handleContentDeliveredByIntent();

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
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

        final String TAG = "downloadData";

        String full_url = BACKEND_URL_BASE + youtube_url;
        Uri uri = Uri.parse(full_url);

        // Create request for android download manager
        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(uri);

        request.allowScanningByMediaScanner();

        // Notify client once download is completed!
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        // To notify the Client that the file is being downloaded
        Toast.makeText(getApplicationContext(), "Downloading File", Toast.LENGTH_LONG).show();

        //Setting title of request
        request.setTitle(youtube_title);

        //Setting description of request
//        request.setDescription("Android Data download using DownloadManager.");

        //Set the local destination for the downloaded file to a path within the application's external files directory
        request.setDestinationInExternalFilesDir(MainActivity.this, Environment.DIRECTORY_DOWNLOADS, youtube_title);

        //Enqueue download and save into referenceId
        Log.d(TAG, String.format("Enqueuing download request for %s", youtube_title));
        return downloadManager.enqueue(request);
    }

    public void get_youtube_title_with_volley(final String youtube_url) {

        final String TAG = "get_youtube_title_with_volley.handleSendText";

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = BACKEND_URL_BASE + youtube_url;

        // Create new volley request for a http HEAD call
        MetaRequest jsonObjectRequest = new MetaRequest(Request.Method.HEAD, url,null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        String youtube_title = getYoutubeTitleFromResponseHeaders(response);

                        // Call download manager with given url and title
                        downloadData(youtube_url, youtube_title);

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, String.format("That didn't work! %s", error));
                    }
                });

        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);
    }

    public String getYoutubeTitleFromResponseHeaders(JSONObject response) {
        final String TAG = "getYoutubeTitleFromResponseHeaders";

        Log.d(TAG,"Response: " + response.toString());

        try {
            JSONObject headers = response.getJSONObject("headers");
            Log.d(TAG, "Response.headers: " + headers);

            String youtube_title = headers.getString("youtube_title");
            Log.d(TAG, "Response.headers.youtube_title: " + youtube_title);

            return youtube_title;

        } catch (JSONException je) {
            return "unknown";
        }
    }
}
