package com.example.underdiscover;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
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
    //TODO: Create list for result data AKA attributeList

    private final String SEARCH_URL = "https://api.spotify.com/v1/search?";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        this.context = this;

    }

    public void onClickReturnToMain(View view) {
        finish();
    }

    public void onClickSearchExecute(View view) {

        EditText textBox = findViewById(R.id.searchTextBox);
        if (textBox.getText().toString().equals("")) {
            Snackbar noText = Snackbar.make(view, "Please enter a search query!", Snackbar.LENGTH_LONG);
            noText.show();
        }

        else {
            InputMethodManager inputManager = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);

            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);

            String searchRequest = textBox.getText().toString().replaceAll(" ", "+");
            String query = "q=" + searchRequest + "&type=track"; //TODO: Manipulate searchRequest to create query

            final String apiUrl = SEARCH_URL + query;

            try {
                String result = new GenericHttpRequests.HttpRequestGet(apiUrl, getIntent().getStringExtra("Access")).execute().get();
                dealWithResult(result, textBox.getText().toString());
            } catch (ExecutionException e) {
                //TODO: Handle Exception
            } catch (InterruptedException e) {
                //TODO: Handle Exception
            }
        }
    }

        protected void dealWithResult(String result, String check) {

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
                                                .execute().get()
                                );

                                trackDetailsList.add(currentTrack);
                                Log.d("TEST", currentTrack.getTrackName());

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
                        TrackListAdapter searchAdapter = new TrackListAdapter(context, trackDetailsList, R.layout.listview_track);
                        ListView searchList = findViewById(R.id.searchList);
                        searchList.setAdapter(searchAdapter);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
                System.exit(3);
            }

        }
    }
