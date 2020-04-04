package com.example.underdiscover;

import android.graphics.drawable.Drawable;

import java.util.HashMap;

public class TrackDetails {

    String trackName;
    String artistName;
    String trackUri;
    Drawable image;
    Double overallMatch;
    HashMap<String, Double> individualMatches;

    public TrackDetails(String trackName, String artistName, String trackUri, Drawable image, Double overallMatch, HashMap<String, Double> individualMatches) {
        this.trackName = trackName;
        this.artistName = artistName;
        this.trackUri = trackUri;
        this.image = image;
        this.overallMatch = overallMatch;
        this.individualMatches = individualMatches;
    }

    public TrackDetails(String trackName, String artistName, String trackUri, Drawable image) {
        this.trackName = trackName;
        this.artistName = artistName;
        this.trackUri = trackUri;
        this.image = image;
        this.overallMatch = null;
        this.individualMatches = null;
    }

    public void setTrackName(String trackName) { this.trackName = trackName; }
    public void setArtistName(String artistName) { this.artistName = artistName; }
    public void setTrackUri(String trackUri) { this.trackUri = trackUri; }
    public void setImage(Drawable image) { this.image = image; }
    public void setOverallMatch(Double overallMatch) { this.overallMatch = overallMatch; }
    public void setIndividualMatches(HashMap<String, Double> individualMatches) { this.individualMatches = individualMatches; }

    public String getTrackName() { return trackName; }
    public String getArtistName() { return artistName; }
    public String getTrackUri() { return trackUri; }
    public Drawable getImage() { return image; }
    public Double getOverallMatch() { return overallMatch; }
    public HashMap<String, Double> getIndividualMatches() { return individualMatches; }
}
