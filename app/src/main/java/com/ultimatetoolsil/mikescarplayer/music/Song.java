package com.ultimatetoolsil.mikescarplayer.music;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Mike on 12/12/2018.
 */

public class Song  implements Serializable{
    String title;
    String artist;
    String album;

    String thumbnail;

    public String getLink() {
        return link;
    }


    public void setLink(String link) {
        this.link = link;
    }

    String link;

    long time;
    //  private HashMap<String,String> magnets;
//    public void addMagnet(String key,String value){
//        magnets.put(key,value);
//    }
//    public HashMap<String, String> getMagnets() {
//        return magnets;
//    }
//
//    public void setMagnets(HashMap<String, String> magnets) {
//        this.magnets = magnets;
//    }
    public void setTime(long time) {
        this.time = time;
    }

    public String getTimeFormatted() {

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"
                , Locale.getDefault());
        formatter.setTimeZone(TimeZone.getDefault());
        return formatter.format(new Date(time));


    }
    public String getTitle() {
        return title;
    }

    public void setTitle(String name) {
        this.title = name;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }



    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public Song(){
    }

    public Song(String name, String artist, String album, String year, String thumbnail,long time) {

        this.time=time;
        this.title = name;
        this.artist = artist;
        this.album = album;

        this.thumbnail = thumbnail;
    }

}
