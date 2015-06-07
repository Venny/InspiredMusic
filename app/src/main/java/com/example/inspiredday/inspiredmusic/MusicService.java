package com.example.inspiredday.inspiredmusic;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Inspired Day on 5/30/2015.
 * Bound Service
 */
public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
MediaPlayer.OnCompletionListener {
    //media player
    private MediaPlayer player;
    //song list
    private ArrayList<Song> songs;
    //current position
    private int songPosn;

    private final IBinder musicBind = new MusicBinder();

    private String songTitle = "";
    private static final int NOTIFY_ID = 1;
    private boolean shuffle = false;
    private Random rand;

    private Intent widgetIntent;

    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent){
        player.stop();
        player.release(); // Player goes to End state
        return false;
    }

    /* Media player */
    @Override
    public void onCompletion(MediaPlayer mp) {
        mp.reset(); // Idle state
        playNext();
    }


    @Override
    public void onPrepared(MediaPlayer mp) {
        //start playback
        player.start();
        System.out.println("Started. onPrepared");

        // Broadcast intent to activity to let it know the media player has been prepared
        Intent onPreparedIntent = new Intent("MEDIA_PLAYER_PREPARED");
        LocalBroadcastManager.getInstance(this).sendBroadcast(onPreparedIntent);

        Intent notIntent = new Intent(this, MainActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0, notIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this); // NotificationCompat for devices before API 16

        builder.setContentIntent(pendInt)
                .setSmallIcon(R.drawable.play)
                .setTicker(songTitle)
                .setOngoing(true)
                .setContentTitle("Playing")
                .setContentText((songTitle));
        Notification not = builder.build();
        startForeground(NOTIFY_ID, not);

    }

    @Override
    public void onCreate(){
        //create the service
        super.onCreate();
        //initialize position
        songPosn = 0;
        //create player
        player = new MediaPlayer();
        rand = new Random();
        widgetIntent = new Intent(this, InspiredWidget.class);
        initMusicPlayer();
        System.out.println("onCreate: " + widgetIntent);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        System.out.println("onError");
        return false;
    }

    @Override
    public void  onDestroy(){
        System.out.println("onDestroy");
        stopForeground(true);
    }



    public void initMusicPlayer(){
        //set player properties
        // The wakeMode will help the player to stay alive when the phone becomes idle
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    public void setList(ArrayList<Song> theSongs){
        songs = theSongs;
    }

    /* Music Binder */
    public class MusicBinder extends Binder {
        MusicService getService(){
            return MusicService.this;
        }
    }

    public void  playSong() {
        //reset the player
        player.reset();
        //get song
        Song playSong = songs.get(songPosn);
        //set song title
        songTitle = playSong.getTitle();
        //get id
        long currentSong = playSong.getID();
        //set uri
        Uri trackUri = ContentUris.withAppendedId(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currentSong);

        try{
            player.setDataSource(getApplicationContext(), trackUri); // Set the player from Idle state to Initialized
        }
        catch (Exception e){
            Log.e("MUSIC SERVICE", "Error setting data source");
        }
        System.out.println("Before preparing --> playSong()");
        // When the player is prepared the onPrepared() will be executed
        player.prepareAsync();

    }

    public void setSong(int songIndex){
        songPosn = songIndex;
    }


    /* MediaPlayerController methods */
    public int getPosn(){
        return player.getCurrentPosition();
    }
    public int getDur(){
        return player.getDuration();
    }
    public boolean isPng(){
        return player.isPlaying();
    }
    public void pausePlayer(){
        player.pause();
    }
    public void seek(int posn){
        player.seekTo(posn);
    }
    public void go(){
        player.start();
    }
    // Return to previous
    public void playPrev(){
        songPosn--;
        if(songPosn < 0){
            songPosn = songs.size() - 1;
        }
        playSong();
    }
    // Skip to next
    public void playNext(){
        if(shuffle){
            int newSong = songPosn;
            while(newSong == songPosn){
                newSong = rand.nextInt(songs.size());
            }
            songPosn = newSong;
        }
        else {
            songPosn++;
            if(songPosn >= songs.size()){
                songPosn = 0;
            }
        }
        playSong();
    }

    //Shuffle method
    public void setShuffle(MenuItem item){
        if(shuffle){
            shuffle = false;
            item.setIcon(R.drawable.rand);
        }
        else {
            shuffle = true;
            item.setIcon(R.drawable.rand_active);
        }
    }

}
