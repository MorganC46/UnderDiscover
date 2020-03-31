package com.example.underdiscover;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class RecommendResultActivity extends AppCompatActivity {

    private Activity context;
    private int numberOfResults;
    private int toDisplay;
    private TrackListAdapter searchAdapter;
    private ArrayList<String> trackUriList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommend_result);

        this.context = this;
        this.numberOfResults = 0;
        this.trackUriList = new ArrayList<>();

        try {
            String result = new GenericHttpRequests.HttpRequestGet(getIntent().getStringExtra("Query"), getIntent().getStringExtra("Access")).execute().get();
            dealWithRecommendedResult(result);
        } catch (ExecutionException e) {
            //TODO: Handle Exception
        } catch (InterruptedException e) {
            //TODO: Handle Exception
        }

    }

    protected void dealWithRecommendedResult(String result) {

        try {
            JSONObject jsonResp = new JSONObject(result);
            JSONArray trackList = (JSONArray) jsonResp.get("tracks");
            numberOfResults = trackList.length();

            TextView title = findViewById(R.id.resultTitle);
            if (numberOfResults == 100) {
                title.setText("We found over 100 tracks we think you will like...");
            } else {
                title.setText("We found " + numberOfResults + " tracks we think you will like...");
            }

            ArrayList<String> trackNameList = new ArrayList<String>();
            ArrayList<String> artistNameList = new ArrayList<String>();
            ArrayList<Drawable> imageList = new ArrayList<Drawable>();

            for (int count = 0; count < numberOfResults; count++) {
                if (trackList.getJSONObject(count).getString("type").equals("track")) {

                    String trackOutput = trackList.getJSONObject(count).getString("name");

                    trackNameList.add(trackOutput);

                    String trackUri = trackList.getJSONObject(count).getString("uri");
                    trackUriList.add(trackUri);

                    String artistOutput = trackList.getJSONObject(count).getJSONArray("artists").getJSONObject(0).getString("name");
                    artistNameList.add(artistOutput);

                    String imageUrl = trackList.getJSONObject(count).getJSONObject("album").getJSONArray("images").getJSONObject(1).getString("url");
                    try {
                        imageList.add(new GenericHttpRequests.ImageRequest(imageUrl).execute().get());
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

                    if (numberOfResults != 0) {

                        if (numberOfResults <= 9) {
                            toDisplay = numberOfResults;
                        } else {
                            toDisplay = 10;
                        }

                        searchAdapter = new TrackListAdapter(context, trackNameList.subList(0, toDisplay).toArray(new String[0]), artistNameList.subList(0, toDisplay).toArray(new String[0]),
                                imageList.subList(0, toDisplay).toArray(new Drawable[0]), trackUriList.subList(0, toDisplay).toArray(new String[0]), R.layout.listview_track);
                        ListView searchList = findViewById(R.id.resultList);

                        Button loadMore = new Button(context);
                        loadMore.setText("Load more recommendations...");

                        loadMore.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                if (toDisplay == numberOfResults) {
                                    Snackbar allResultsShown = Snackbar.make(findViewById(R.id.resultList), "No further recommendations to show!", Snackbar.LENGTH_LONG);
                                    allResultsShown.show();
                                } else {

                                    if (numberOfResults < toDisplay + 10) {
                                        toDisplay = numberOfResults;
                                    } else {
                                        toDisplay = toDisplay + 10;
                                    }

                                    searchAdapter = new TrackListAdapter(context, trackNameList.subList(0, toDisplay).toArray(new String[0]), artistNameList.subList(0, toDisplay).toArray(new String[0]),
                                            imageList.subList(0, toDisplay).toArray(new Drawable[0]), trackUriList.subList(0, toDisplay).toArray(new String[0]), R.layout.listview_track);

                                    searchList.setAdapter(searchAdapter);
                                    if ((toDisplay % 10) != 0) {
                                        searchList.setSelection(toDisplay - (16 - (10 - (toDisplay % 10))));
                                    } else {
                                        searchList.setSelection(toDisplay - 16);
                                    }
                                    searchAdapter.notifyDataSetChanged();

                                }
                            }
                        });

                        searchList.addFooterView(loadMore);
                        searchList.setAdapter(searchAdapter);
                    }
                    else {
                        context.finish();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
            System.exit(3);
        } catch (NullPointerException e) {
            //TODO: HANDLE EXCEPTION
        }

    }

    public void onClickAddToPlaylist(View v) {

        String playlistQuery = "https://api.spotify.com/v1/users/" + returnUserId() + "/playlists";

        try {
            String playlistOutput = new GenericHttpRequests.HttpRequestGet(playlistQuery, getIntent().getStringExtra("Access")).execute().get();
            JSONObject jsonResp = new JSONObject(playlistOutput);
            JSONArray plList = (JSONArray) jsonResp.get("items");

            ArrayList<String> playlistNames = new ArrayList<>();
            ArrayList<String> playlistIDs = new ArrayList<>();

            for (int count = 0; count < plList.length(); count++) {
                if (plList.getJSONObject(count).getJSONObject("owner").getString("id").equals(returnUserId())) {
                    playlistNames.add(plList.getJSONObject(count).getString("name"));
                    playlistIDs.add(plList.getJSONObject(count).getString("id"));
                }
            }

            AlertDialog.Builder selectPlaylist = new AlertDialog.Builder(this);
            selectPlaylist.setTitle("Select a playlist to add to!");
            selectPlaylist.setItems(playlistNames.toArray(new String[0]), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int chosen) {
                    System.out.println(playlistIDs.get(chosen));
                    addSongsToPlaylist("https://api.spotify.com/v1/playlists/" + playlistIDs.get(chosen));
                }
            });
            selectPlaylist.show();

        } catch (ExecutionException e) {
        //TODO: Handle Exception
        } catch (InterruptedException e) {
        //TODO: Handle Exception
        } catch (JSONException e) {

        }
    }

    public void onClickCreateNewPlaylist(View v) {

        String newPlaylistQuery = "https://api.spotify.com/v1/users/" + returnUserId() + "/playlists";

        try {

            JSONObject newPlaylistObject = new JSONObject();
            newPlaylistObject.put("name", "UnderDiscover Recommends");
            newPlaylistObject.put("description", "Here is the playlist you requested from our recommendations! Enjoy!");

            String playlistId = new GenericHttpRequests.HttpRequestPost(newPlaylistQuery, getIntent().getStringExtra("Access"),
                    newPlaylistObject.toString(), null).execute().get();

            addSongsToPlaylist(playlistId);

        } catch (ExecutionException e) {
            //TODO: Handle Exception
        } catch (InterruptedException e) {
            //TODO: Handle Exception
        } catch (JSONException e) {
            //TODO: Handle
        }
    }

    private void addSongsToPlaylist(String playlistId) {
        String appendPlaylistQuery = playlistId + "/tracks";
        JSONArray jsUriArray = new JSONArray(trackUriList);

        try {
            String snapshotId = new GenericHttpRequests.HttpRequestPost(appendPlaylistQuery, getIntent().getStringExtra("Access"),
                    null, jsUriArray).execute().get();

            if (snapshotId.equals("") || snapshotId.equals(null)) {
                Snackbar newPlaylistNotCreated = Snackbar.make(findViewById(R.id.resultList), "Something didn't work - please try again!", Snackbar.LENGTH_LONG);
                newPlaylistNotCreated.show();
            }
            else {
                Snackbar newPlaylistCreated = Snackbar.make(findViewById(R.id.resultList), "We've created your recommendation list! Check it out!", Snackbar.LENGTH_LONG);
                newPlaylistCreated.show();
            }

        } catch (ExecutionException e) {
            //TODO: Handle Exception
        } catch (InterruptedException e) {
            //TODO: Handle Exception
        }
    }

    private String returnUserId() {

        String queryResult = "";
        String userId = "";

        try {

            queryResult = new GenericHttpRequests.HttpRequestGet("https://api.spotify.com/v1/me",
                    getIntent().getStringExtra("Access")).execute().get();

            JSONObject jsonResp = new JSONObject(queryResult);
            userId = jsonResp.getString("id");

        } catch (ExecutionException e) {
            //TODO: Handle Exception

        } catch (InterruptedException e) {
            //TODO: Handle Exception

        } catch (JSONException e) {
            //TODO: Handle Exception
        }

        return userId;
    }

}
