package com.example.underdiscover;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class RecommendResultActivity extends AppCompatActivity {

    private Activity context;
    private int numberOfResults;
    private int toDisplay;
    private TrackListMatchAdapter searchAdapter;
    private ArrayList<String> trackUriList;
    private Double currentTightness;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommend_result);

        this.context = this;
        this.numberOfResults = 0;
        this.trackUriList = new ArrayList<>();
        this.currentTightness = getIntent().getDoubleExtra("Tightness", 15.0);

        generateRecommendations(currentTightness);

    }

    protected void dealWithRecommendedResult(String result) {

        try {
            JSONObject jsonResp = new JSONObject(result);
            JSONArray trackList = (JSONArray) jsonResp.get("tracks");
            numberOfResults = trackList.length();

            TextView title = findViewById(R.id.resultTitle);

            if (numberOfResults > 100) { title.setText("We found over 100 tracks we think you will like..."); }
            else if (numberOfResults == 0) { title.setText("We found no tracks we think you will like..."); }
            else { title.setText("We found " + numberOfResults + " tracks we think you will like..."); }

            ArrayList<TrackDetails> trackDetailsList = new ArrayList<>(numberOfResults);

            for (int count = 0; count < numberOfResults; count++) {
                if (trackList.getJSONObject(count).getString("type").equals("track")) {

                    String trackName = trackList.getJSONObject(count).getString("name");
                    String trackUri = trackList.getJSONObject(count).getString("uri");
                    String artistName = trackList.getJSONObject(count).getJSONArray("artists").getJSONObject(0).getString("name");
                    String imageUrl = trackList.getJSONObject(count).getJSONObject("album").getJSONArray("images").getJSONObject(1).getString("url");

                    try {

                        String compResult = new GenericHttpRequests.HttpRequestGet("https://api.spotify.com/v1/audio-features/" + trackUri.split(":")[2], getIntent().getStringExtra("Access")).execute().get();

                        compResult = compResult.replace("{", "").replace("}", "");
                        String[] resultArray = compResult.split(",");

                        HashMap<String, Double> individualPercentages = new HashMap<>();

                        HashMap<String, Double> requestTrack = (HashMap<String, Double>) getIntent().getSerializableExtra("ComparisonValues");
                        for (HashMap.Entry<String, Double> requestPair : requestTrack.entrySet()) {

                            for (int varCount = 0; varCount < resultArray.length; varCount++) {
                                String[] variable = resultArray[varCount].split(":");
                                variable[0] = variable[0].replaceAll("\\s", "").replaceAll("\"", "");

                                if (variable[0].equals(requestPair.getKey())) {
                                    individualPercentages.put(variable[0], returnAttributePercentage(Double.parseDouble(variable[1]), requestPair.getValue()));
                                }
                            }
                        }

                        Double overallDifference = 0.0;
                        for (HashMap.Entry<String, Double> attributes : individualPercentages.entrySet()) {
                            overallDifference = overallDifference + attributes.getValue();
                        }
                        if (overallDifference != 0.0) {
                            overallDifference = overallDifference/(double)individualPercentages.size();
                        }
                        trackUriList.add(trackUri);
                        TrackDetails trackDetails = new TrackDetails(trackName, artistName, trackUri, new GenericHttpRequests.ImageRequest(imageUrl).execute().get(), overallDifference, individualPercentages);

                        trackDetailsList.add(trackDetails);
                    }

                    catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
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

                        Collections.sort(trackDetailsList, (TrackDetails track1, TrackDetails track2) ->
                            track1.getOverallMatch().compareTo(track2.getOverallMatch())
                        );

                        searchAdapter = new TrackListMatchAdapter(context, new ArrayList(trackDetailsList.subList(0, toDisplay)), R.layout.listview_track_match);
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

                                    searchAdapter = new TrackListMatchAdapter(context, new ArrayList(trackDetailsList.subList(0, toDisplay)), R.layout.listview_track_match);

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

                        if (numberOfResults > 50) {
                            Snackbar refineResults = Snackbar.make(findViewById(R.id.resultList), "Large result detected - would you like to refine?", Snackbar.LENGTH_LONG + 10000)
                                    .setAction("YES", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            currentTightness = currentTightness * 0.8;
                                            searchList.removeFooterView(loadMore);
                                            generateRecommendations(currentTightness);
                                        }
                                    });
                            refineResults.show();
                        }
                        else if (numberOfResults < 10) {
                            Snackbar refineResults = Snackbar.make(findViewById(R.id.resultList), "Small result detected - would you like to broaden?", Snackbar.LENGTH_LONG + 10000)
                                    .setAction("YES", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            currentTightness = currentTightness * 1.2;
                                            searchList.removeFooterView(loadMore);
                                            generateRecommendations(currentTightness);
                                        }
                                    });
                            refineResults.show();
                        }

                    }
                    else {
                        Snackbar refineResults = Snackbar.make(findViewById(R.id.resultList), "No results detected - would you like to broaden?", Snackbar.LENGTH_INDEFINITE)
                                .setAction("YES", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        currentTightness = currentTightness * 1.2;
                                        generateRecommendations(currentTightness);
                                    }
                                });
                        refineResults.show();
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

    private Double returnAttributePercentage(Double value, Double compare) {
        if (value > compare) {
            return (100.0 * ((value - compare) / compare));
        }
        if (compare > value) {
            return (100.0 * ((compare - value) / value));
        }
        if (compare == value) {
            return 100.0;
        }
        return 0.0;
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

    private void generateRecommendations(Double selectedTightness) {
        String query = getIntent().getStringExtra("Query");

        HashMap<String,Double> passValues = (HashMap<String,Double>)getIntent().getSerializableExtra("ComparisonValues");

        try {

            for (Map.Entry<String,Double> attributes : passValues.entrySet()) {

                if (attributes.getKey().equals("danceability") || attributes.getKey().equals("energy") || attributes.getKey().equals("speechiness") ||
                        attributes.getKey().equals("valence") || attributes.getKey().equals("acousticness") || attributes.getKey().equals("tempo")) {
                    query = query + "&min_" + attributes.getKey() + "=" + (attributes.getValue() - (attributes.getValue() * (selectedTightness / 100))) + "&max_" +
                            attributes.getKey() + "=" + (attributes.getValue() + (attributes.getValue() * (selectedTightness / 100))) + "&target_" +
                            attributes.getKey() + "=" + attributes.getValue();
                }
                if (attributes.getKey().equals("loudness")) {
                    query = query + "&min_" + attributes.getKey() + "=" + (attributes.getValue() + (attributes.getValue() * (selectedTightness / 100))) + "&max_" +
                            attributes.getKey() + "=" + (attributes.getValue() - (attributes.getValue() * (selectedTightness / 100))) + "&target_" +
                            attributes.getKey() + "=" + attributes.getValue();
                }
            }

            String result = new GenericHttpRequests.HttpRequestGet(query, getIntent().getStringExtra("Access")).execute().get();
            dealWithRecommendedResult(result);

        } catch (ExecutionException e) {
            //TODO: Handle Exception
        } catch (InterruptedException e) {
            //TODO: Handle Exception
        }
    }

}
