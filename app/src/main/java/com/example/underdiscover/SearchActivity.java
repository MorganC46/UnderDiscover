package com.example.underdiscover;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SearchActivity extends AppCompatActivity {

    private Activity context;
    protected TrackListAdapter mainList;
    //TODO: Create list for result data AKA attributeList

    private final String SEARCH_URL = "https://api.spotify.com/v1/search?";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        this.context = this;
        this.mainList = null;

    }

    public void onClickReturnToMain(View view) {
        finish();
    }

    public void onClickSearchTrack(View view) { generateSearchResult("track"); }
    public void onClickSearchArtist(View view) { generateSearchResult("artist"); }
    public void onClickSearchAlbum(View view) { generateSearchResult("album"); }

    protected void generateSearchResult(String type) {
        EditText textBox = findViewById(R.id.searchTextBox);
        if (textBox.getText().toString().equals("")) {
            Snackbar noText = Snackbar.make(findViewById(R.id.returnToMain), "Please enter a search query!", Snackbar.LENGTH_LONG);
            noText.show();
        }

        else {
            InputMethodManager inputManager = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);

            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);

            String searchRequest = textBox.getText().toString().replaceAll(" ", "+");
            String query = "q=" + searchRequest + "&type=" + type + "&limit=50"; //TODO: Manipulate searchRequest to create query

            final String apiUrl = SEARCH_URL + query;

            try {
                String result = new GenericHttpRequests.HttpRequestGet(apiUrl, getIntent().getStringExtra("Access")).execute().get();

                if (type.equals("track")) {
                    dealWithTrackResult(result, textBox.getText().toString());
                }
                else if (type.equals("artist")) {
                    dealWithArtistResult(result, textBox.getText().toString());
                }
                else if (type.equals("album")) {
                    dealWithAlbumResult(result, textBox.getText().toString());
                }

            } catch (ExecutionException e) {
                //TODO: Handle Exception
            } catch (InterruptedException e) {
                //TODO: Handle Exception
            }
        }
    }

    protected void dealWithTrackResult(String result, String check) {

        try {
            JSONObject jsonResp = new JSONObject(result);
            JSONObject list = jsonResp.optJSONObject("tracks");
            JSONArray trackList = (JSONArray) list.get("items");

            ArrayList<TrackDetails> trackDetailsList = new ArrayList<>();

            for (int count = 0; count < trackList.length(); count++) {

                if (trackList.getJSONObject(count).getString("type").equals("track")) {
                    if (trackList.getJSONObject(count).getString("name").toLowerCase().contains(check.toLowerCase())) {
                        try {
                            TrackDetails currentTrack = new TrackDetails(
                                    trackList.getJSONObject(count).getString("name"),
                                    trackList.getJSONObject(count).getJSONArray("artists").getJSONObject(0).getString("name"),
                                    trackList.getJSONObject(count).getString("uri"),
                                    new GenericHttpRequests.ImageRequest(
                                            trackList.getJSONObject(count).getJSONObject("album").getJSONArray("images").getJSONObject(1).getString("url"))
                                            .execute().get(),
                                    Integer.parseInt(trackList.getJSONObject(count).getString("popularity")),
                                    "track"
                            );

                            trackDetailsList.add(currentTrack);

                        } catch (ExecutionException e) {
                            //TODO: Handle Exception
                        } catch (InterruptedException e) {
                            //TODO: Handle Exception
                        }
                    }
                }
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mainList != null) {
                        mainList.clear();
                    }
                    mainList = new TrackListAdapter(context, trackDetailsList, R.layout.listview_track);
                    ListView searchList = findViewById(R.id.searchList);
                    searchList.setAdapter(mainList);
                    mainList.notifyDataSetChanged();
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
            System.exit(3);
        }
    }

    protected void dealWithAlbumTracksResult(String result, Drawable albumImage) {

        try {
            JSONObject jsonResp = new JSONObject(result);
            JSONArray trackList = (JSONArray) jsonResp.get("items");

            ArrayList<TrackDetails> trackDetailsList = new ArrayList<>();

            for (int count = 0; count < trackList.length(); count++) {

                if (trackList.getJSONObject(count).getString("type").equals("track")) {
                    TrackDetails currentTrack = new TrackDetails(
                            trackList.getJSONObject(count).getString("name"),
                            trackList.getJSONObject(count).getJSONArray("artists").getJSONObject(0).getString("name"),
                            trackList.getJSONObject(count).getString("uri"),
                            albumImage,
                            Integer.parseInt(trackList.getJSONObject(count).getString("popularity")),
                            "track"
                    );

                    trackDetailsList.add(currentTrack);

                }
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mainList != null) {
                        mainList.clear();
                    }
                    mainList = new TrackListAdapter(context, trackDetailsList, R.layout.listview_track);
                    ListView searchList = findViewById(R.id.searchList);
                    searchList.setAdapter(mainList);
                    mainList.notifyDataSetChanged();
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
            System.exit(3);
        }
    }

    protected void dealWithAlbumResult(String result, String check) {

        try {
            JSONObject jsonResp = new JSONObject(result);
            JSONObject list = jsonResp.optJSONObject("albums");
            JSONArray albumList = (JSONArray) list.get("items");

            ArrayList<TrackDetails> albumDetailsList = new ArrayList<>();

            for (int count = 0; count < albumList.length(); count++) {

                if (albumList.getJSONObject(count).getString("name").toLowerCase().contains(check.toLowerCase())) {
                    try {
                        TrackDetails currentAlbum = new TrackDetails(
                                albumList.getJSONObject(count).getString("name"),
                                albumList.getJSONObject(count).getJSONArray("artists").getJSONObject(0).getString("name"),
                                albumList.getJSONObject(count).getString("uri"),
                                new GenericHttpRequests.ImageRequest(
                                        albumList.getJSONObject(count).getJSONArray("images").getJSONObject(1).getString("url"))
                                        .execute().get(),
                                "album"
                        );
                        albumDetailsList.add(currentAlbum);
                    } catch (ExecutionException e) {
                        //TODO: Handle Exception
                    } catch (InterruptedException e) {
                        //TODO: Handle Exception
                    }
                }
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mainList != null) {
                        mainList.clear();
                    }
                    mainList = new TrackListAdapter(context, albumDetailsList, R.layout.listview_album);
                    ListView searchList = findViewById(R.id.searchList);
                    searchList.setAdapter(mainList);
                    mainList.notifyDataSetChanged();
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
            System.exit(3);
        }
    }

    protected void dealWithArtistResult(String result, String check) {

        try {
            JSONObject jsonResp = new JSONObject(result);
            JSONObject list = jsonResp.optJSONObject("artists");
            JSONArray artistList = (JSONArray) list.get("items");

            ArrayList<TrackDetails> artistDetailsList = new ArrayList<>();

            for (int count = 0; count < artistList.length(); count++) {

                if (artistList.getJSONObject(count).getString("name").toLowerCase().contains(check.toLowerCase())) {
                    if (artistList.getJSONObject(count).getJSONArray("images").length() >= 1) {
                        try {
                            TrackDetails currentArtist = new TrackDetails(
                                    null,
                                    artistList.getJSONObject(count).getString("name"),
                                    artistList.getJSONObject(count).getString("uri"),
                                    new GenericHttpRequests.ImageRequest(
                                            artistList.getJSONObject(count).getJSONArray("images").getJSONObject(1).getString("url"))
                                            .execute().get(),
                                    "artist"
                            );
                            artistDetailsList.add(currentArtist);

                        } catch (ExecutionException e) {
                            //TODO: Handle Exception
                        } catch (InterruptedException e) {
                            //TODO: Handle Exception
                        }
                    }
                }
            }

            for (int count = 0; count < artistList.length(); count++) {

                if (artistList.getJSONObject(count).getString("name").toLowerCase().contains(check.toLowerCase())) {
                    if (artistList.getJSONObject(count).getJSONArray("images").length() < 1) {
                        TrackDetails currentArtist = new TrackDetails(
                                null,
                                artistList.getJSONObject(count).getString("name"),
                                artistList.getJSONObject(count).getString("uri"),
                                new ColorDrawable(Color.TRANSPARENT),
                                "artist"
                        );
                           artistDetailsList.add(currentArtist);
                    }
                }
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mainList != null) {
                        mainList.clear();
                    }
                    mainList = new TrackListAdapter(context, artistDetailsList, R.layout.listview_artist);
                    ListView searchList = findViewById(R.id.searchList);
                    searchList.setAdapter(mainList);
                    mainList.notifyDataSetChanged();
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
            System.exit(3);
        }
    }

    protected void dealWithArtistAlbumsResult(String result) {

        try {
            JSONObject jsonResp = new JSONObject(result);
            JSONArray albumList = (JSONArray) jsonResp.get("items");

            ArrayList<TrackDetails> albumDetailsList = new ArrayList<>();

            for (int count = 0; count < albumList.length(); count++) {

                try {
                    TrackDetails currentAlbum = new TrackDetails(
                            albumList.getJSONObject(count).getString("name"),
                            albumList.getJSONObject(count).getJSONArray("artists").getJSONObject(0).getString("name"),
                            albumList.getJSONObject(count).getString("uri"),
                            new GenericHttpRequests.ImageRequest(
                                    albumList.getJSONObject(count).getJSONArray("images").getJSONObject(1).getString("url"))
                                    .execute().get(),
                            "album"
                    );
                    albumDetailsList.add(currentAlbum);
                } catch (ExecutionException e) {
                    //TODO: Handle Exception
                } catch (InterruptedException e) {
                    //TODO: Handle Exception
                }
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mainList != null) {
                        mainList.clear();
                    }
                    mainList = new TrackListAdapter(context, albumDetailsList, R.layout.listview_album);
                    ListView searchList = findViewById(R.id.searchList);
                    searchList.setAdapter(mainList);
                    mainList.notifyDataSetChanged();
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
            System.exit(3);
        }
    }
}
