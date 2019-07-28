package com.example.kurtalang.youtube_mp3_ripper_frontend;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import com.example.kurtalang.youtube_mp3_ripper_frontend.SongList.Song;
import com.example.kurtalang.youtube_mp3_ripper_frontend.SongList.SongAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity {

    private ArrayList<Song> songList;
    private ListView songView;

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
                createSongUI();
//                Toolbar toolbar = findViewById(R.id.toolbar);
//                setSupportActionBar(toolbar);
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

    public void createSongUI() {
        songView = (ListView)findViewById(R.id.song_list);
        songList = new ArrayList<Song>();

        populateSongList();

        Collections.sort(songList, new Comparator<Song>(){
            public int compare(Song a, Song b){
                return a.getTitle().compareTo(b.getTitle());
            }
        });

        SongAdapter songAdt = new SongAdapter(this, songList);
        songView.setAdapter(songAdt);

    }

    public void populateSongList() {
        //retrieve song info
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

        if(musicCursor!=null && musicCursor.moveToFirst()){
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                songList.add(new Song(thisId, thisTitle, thisArtist));
            }
            while (musicCursor.moveToNext());
        }
    }
}
