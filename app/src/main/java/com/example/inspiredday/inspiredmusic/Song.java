package com.example.inspiredday.inspiredmusic;

/**
 * Created by Inspired Day on 5/30/2015.
 */
public class Song {
    private long id;
    private String title;
    private String artist;

    public Song(long songID, String songTitle, String songArtist){
        id = songID;
        title = songTitle;
        artist = songArtist;
    }

    // Get methods
    public long getID(){
        return id;
    }
    public  String getTitle(){
        return title;
    }
    public String getArtist(){
        return artist;
    }
}
