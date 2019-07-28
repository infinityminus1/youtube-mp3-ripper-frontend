package com.example.kurtalang.youtube_mp3_ripper_frontend;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final String TAG = "MainActivity.onCreate";

        int whichIntent = handleContentDeliveredByIntent();
        switch (whichIntent) {
            case 1:
                Log.d(TAG, "onCreate() received 1 for WhichIntent. Finish and return");
                MainActivity.this.finish();
                return;
            default:
                setContentView(R.layout.activity_main);
                Toolbar toolbar = findViewById(R.id.toolbar);
                setSupportActionBar(toolbar);
        }
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
        super.onActivityResult(requestCode, resultCode, data);
    }

    public int handleContentDeliveredByIntent() {
        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleSendText(intent); // Handle text being sent
                return 1;
            }
        } else {
            // Handle other intents, such as being started from the home screen
            return 0;
        }

        return 0;
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
            Log.d(TAG, "handleSendText() about to call createIntentToSuppressUI.");
            createIntentToSuppressUI(sharedText);
        } else {
            Log.d(TAG, "handleSendText() received NO shared text");
        }
    }

    public void createIntentToSuppressUI(String url) {
        Intent intent = new Intent(getApplicationContext(), HandleDownloadService.class);
        intent.putExtra(HandleDownloadService.URL_EXTRA, url);
        startService(intent);
    }
}
