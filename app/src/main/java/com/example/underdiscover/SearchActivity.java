package com.example.underdiscover;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
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
                String result = new GenericHttpRequests.HttpRequest(apiUrl, getIntent().getStringExtra("Access")).execute().get();
                dealWithResult(result, textBox.getText().toString());
            } catch (ExecutionException e) {
                //TODO: Handle Exception
            } catch (InterruptedException e) {
                //TODO: Handle Exception
            }
        }
    }

    protected class ImageRequest extends AsyncTask<Void, Void, Drawable> {

        String imageUrl;

        protected ImageRequest(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        protected Drawable doInBackground(Void... params) {
            try {
                InputStream imageInput = (InputStream) new URL(imageUrl).getContent();
                Drawable imageDrawable = Drawable.createFromStream(imageInput, "artwork");
                return imageDrawable;
            } catch (MalformedURLException eURL) {
                eURL.printStackTrace();
                System.exit(1);
            } catch (IOException eIO) {
                eIO.printStackTrace();
                System.exit(2);
            }
            return null;
        }
    }

        protected void dealWithResult(String result, String check) {

            try {
                JSONObject jsonResp = new JSONObject(result);
                JSONObject list = jsonResp.optJSONObject("tracks");
                JSONArray trackList = (JSONArray) list.get("items");

                ArrayList<String> trackNameList = new ArrayList<String>();
                ArrayList<String> artistNameList = new ArrayList<String>();
                ArrayList<Drawable> imageList = new ArrayList<Drawable>();
                ArrayList<String> trackUriList = new ArrayList<String>();

                for (int count = 0; count < trackList.length(); count++) {
                    if (trackList.getJSONObject(count).getString("type").equals("track")) {

                        String trackOutput = trackList.getJSONObject(count).getString("name");

                        if (trackOutput.toLowerCase().contains(check.toLowerCase()) == true) {
                            trackNameList.add(trackOutput);

                            String trackUri = trackList.getJSONObject(count).getString("uri");
                            trackUriList.add(trackUri);

                            String artistOutput = trackList.getJSONObject(count).getJSONArray("artists").getJSONObject(0).getString("name");
                            artistNameList.add(artistOutput);

                            String imageUrl = trackList.getJSONObject(count).getJSONObject("album").getJSONArray("images").getJSONObject(1).getString("url");
                            try {
                                imageList.add(new ImageRequest(imageUrl).execute().get());
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
                        SearchListAdapter searchAdapter = new SearchListAdapter(context, trackNameList.toArray(new String[0]), artistNameList.toArray(new String[0]),
                                imageList.toArray(new Drawable[0]), trackUriList.toArray(new String[0]), R.layout.listview_search);
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
