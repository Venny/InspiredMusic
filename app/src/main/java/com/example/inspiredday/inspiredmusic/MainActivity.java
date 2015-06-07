package com.example.inspiredday.inspiredmusic;

import com.example.inspiredday.inspiredmusic.MusicService.MusicBinder;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.MediaController.MediaPlayerControl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class MainActivity extends ActionBarActivity implements MediaPlayerControl {

    private ArrayList<Song> songList;
    private ListView songView;

    private MusicService musicSrv;
    private Intent playIntent;
    private MusicController controller;

    private boolean musicBound = false;
    private boolean paused = false, playbackPaused = false;


    /* Activity methods */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        songView = (ListView)findViewById(R.id.song_list);
        songList = new ArrayList<Song>();
        getSongList();
        // Sort the songs alphabetically
        Collections.sort(songList, new Comparator<Song>(){
            public int compare(Song a, Song b){
                return a.getTitle().compareTo(b.getTitle());
            }
        });

        SongAdapter songAdt = new SongAdapter(this, songList);
        songView.setAdapter(songAdt);

        setController();
    }

    @Override
    protected void onStart(){
        super.onStart();
        if(playIntent == null){
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        paused = true;
    }

    @Override
    protected void onResume(){
        // Set up receiver for media player onPrepared broadcast
        LocalBroadcastManager.getInstance(this).registerReceiver(onPreparedReceiver,
                new IntentFilter("MEDIA_PLAYER_PREPARED"));
        super.onResume();
        if(paused){
            //System.out.println("onResume() paused");
            //setController(); // Doesn't need to be used here. We have already called it in onCreate()
            paused = false;
        }
    }

    @Override
    protected void onStop(){
        controller.hide();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch(item.getItemId()){
            case R.id.action_shuffle:
                //shuffle
                musicSrv.setShuffle(item);
                break;
            case R.id.action_end:
                stopService(playIntent);
                musicSrv = null;
                System.exit(0);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy(){
        stopService(playIntent);
        musicSrv = null;
        super.onDestroy();
    }


    public void getSongList(){
        //retrieve the song info
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

        if (musicCursor != null && musicCursor.moveToFirst()){
            //get columns
            int titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);

            //add songs to list
            do{
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                songList.add(new Song(thisId,thisTitle, thisArtist));
            }
            while(musicCursor.moveToNext());
        }
    }

    //connect to the service class
    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicBinder binder = (MusicBinder)service;
            //get service
            musicSrv = binder.getService();
            //pass list
            musicSrv.setList(songList);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    // OnClick function implemented in the xml file
    public void songPicked(View view){
        musicSrv.setSong(Integer.parseInt(view.getTag().toString()));
        musicSrv.playSong();
        if(playbackPaused){
            playbackPaused = false;
        }
    }

    /* MediaPlayerControl methods */
    @Override
    public void start() {
        System.out.println("Music start");
        musicSrv.go();
    }

    @Override
    public void pause() {
        System.out.println("Music paused");
        playbackPaused = true;
        musicSrv.pausePlayer();
    }

    @Override
    public void seekTo(int pos) {
        musicSrv.seek(pos);
    }

    @Override
    public int getDuration() {  // It is important for the slider in the controller
        if(musicSrv != null && musicBound && musicSrv.isPng()){
            return musicSrv.getDur();
        }
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        if(musicSrv != null && musicBound && musicSrv.isPng()){
            return musicSrv.getPosn();
        }
        return 0;

    }

    @Override
    public boolean isPlaying() {
        if(musicSrv != null && musicBound){
            return musicSrv.isPng();
        }
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }  // Setting it to true

    @Override
    public boolean canSeekBackward() {
        return true;
    } // True

    @Override
    public boolean canSeekForward() {
        return true;
    } // True

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    private void setController(){
        //set the controller up
        if(controller == null){
            System.out.println(1111);
            controller = new MusicController(this);
        }
        controller.setPrevNextListeners(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playNext();
                    }

                }, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playPrev();
                    }
                }
        );
        //controller.show(0); // the int is 0 --> to make the controller always visible
        controller.setMediaPlayer(this);
        controller.setEnabled(true);
        controller.setAnchorView(findViewById(R.id.song_list));
    }

    //play next
    private void playNext(){
        musicSrv.playNext();
        if(playbackPaused){
            playbackPaused = false;
        }

    }
    //play previous
    private void playPrev(){
        musicSrv.playPrev();
        if(playbackPaused){
            playbackPaused = false;
        }
        Log.d("Prev", "Song");
    }

    // Broadcast receiver to determine when music player has been prepared
    private BroadcastReceiver onPreparedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // When music player has been prepared, show controller
            controller.show(0);
        }
    };
}
