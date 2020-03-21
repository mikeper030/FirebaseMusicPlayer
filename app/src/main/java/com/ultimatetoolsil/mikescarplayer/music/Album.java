package com.ultimatetoolsil.mikescarplayer.music;

import com.ultimatetoolsil.mikescarplayer.music.Song;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by Mike on 02/03/2019.
 */

public class Album implements Serializable {
    String cover;
    String title;
    String artist;
    boolean favorite;
    public long timestamp=0;
    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    String year;
    HashMap<String,Song> songs;
    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }


    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }



    public Album() {
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public HashMap<String, Song> getSongs() {
        return songs;
    }

    public void setSongs(HashMap<String, Song> songs) {
        this.songs = songs;
    }
}
