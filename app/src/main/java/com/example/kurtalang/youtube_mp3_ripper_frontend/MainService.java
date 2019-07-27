package com.example.kurtalang.youtube_mp3_ripper_frontend;

import android.app.DownloadManager;
import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;

public class MainService extends IntentService {
    private final static String BACKEND_URL_BASE = "http://52.52.45.237/?url=";  // AWS elastic IP
    public static final String URL_EXTRA = "url";


    private static final String TAG = DownloadIntentService.class.getSimpleName();
    private static final String URL_BASE = "http://54.193.90.124/?url=";


    public MainService() {
        super("Anonymous");
    }

    public MainService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String url_string = intent.getStringExtra(URL_EXTRA);
        get_youtube_title_with_volley(url_string);
    }

    public void get_youtube_title_with_volley(final String youtube_url) {

        final String TAG = "MainService.get_youtube_title_with_volley.handleSendText";

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = BACKEND_URL_BASE + youtube_url;

        Log.d(TAG, String.format("Passed in youtube_url is %s", youtube_url));
        Log.d(TAG, String.format("Concated url is %s", url));
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
        final String TAG = "MainService.getYoutubeTitleFromResponseHeaders";

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

    private long downloadData(String youtube_url, String youtube_title) {

        final String TAG = "MainService.downloadData";

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

        //Set the local destination for the downloaded file to a path within the application's external files directory
        request.setDestinationInExternalFilesDir(MainService.this, Environment.DIRECTORY_DOWNLOADS, youtube_title);

        //Enqueue download and save into referenceId
        Log.d(TAG, String.format("Enqueuing download request for %s with the url %s", youtube_title, uri));
        long download_id = downloadManager.enqueue(request);
        Log.d(TAG, String.format("Download ID from enqueue request is %d", download_id));
        return download_id;
    }

}
