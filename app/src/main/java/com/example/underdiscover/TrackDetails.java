package com.example.underdiscover;

import android.graphics.drawable.Drawable;

import java.util.HashMap;

public class TrackDetails {

    String trackName;
    String artistName;
    String trackUri;
    Drawable image;
    int popularity;
    Double overallMatch;
    HashMap<String, Double> individualMatches;
    String type;

    public TrackDetails(String trackName, String artistName, String trackUri, Drawable image, int popularity, Double overallMatch, HashMap<String, Double> individualMatches) {
        this.trackName = trackName;
        this.artistName = artistName;
        this.trackUri = trackUri;
        this.image = image;
        this.popularity = popularity;
        this.overallMatch = overallMatch;
        this.individualMatches = individualMatches;
        this.type = null;
    }

    public TrackDetails(String trackName, String artistName, String trackUri, Drawable image, int popularity, String type) {
        this.trackName = trackName;
        this.artistName = artistName;
        this.trackUri = trackUri;
        this.image = image;
        this.popularity = popularity;
        this.overallMatch = null;
        this.individualMatches = null;
        this.type = type;
    }

    public TrackDetails(String trackName, String artistName, String trackUri, Drawable image, String type) {
        this.trackName = trackName;
        this.artistName = artistName;
        this.trackUri = trackUri;
        this.image = image;
        this.popularity = -1;
        this.overallMatch = null;
        this.individualMatches = null;
        this.type = type;
    }

    public void setTrackName(String trackName) { this.trackName = trackName; }
    public void setArtistName(String artistName) { this.artistName = artistName; }
    public void setTrackUri(String trackUri) { this.trackUri = trackUri; }
    public void setImage(Drawable image) { this.image = image; }
    public void setPopularity(int popularity) { this.popularity = popularity; }
    public void setOverallMatch(Double overallMatch) { this.overallMatch = overallMatch; }
    public void setIndividualMatches(HashMap<String, Double> individualMatches) { this.individualMatches = individualMatches; }
    public void setType(String type) { this.type = type; }

    public String getTrackName() { return trackName; }
    public String getArtistName() { return artistName; }
    public String getTrackUri() { return trackUri; }
    public Drawable getImage() { return image; }
    public int getPopularity() { return popularity; }
    public Double getOverallMatch() { return overallMatch; }
    public HashMap<String, Double> getIndividualMatches() { return individualMatches; }
    public String getType() { return type; }
}
