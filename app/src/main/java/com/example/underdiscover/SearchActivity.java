package com.example.underdiscover;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import androidx.appcompat.app.AppCompatActivity;

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
        String searchRequest = textBox.getText().toString().replaceAll(" ", "+");
        String query = "q=" + searchRequest + "&type=track"; //TODO: Manipulate searchRequest to create query

        final String apiUrl = SEARCH_URL + query;

        try {
            String result = new GenericHttpRequests.HttpRequest(apiUrl, getIntent().getStringExtra("Access")).execute().get();
            dealWithResult(result, textBox.getText().toString());
        }
        catch(ExecutionException e) {
            //TODO: Handle Exception
        }
        catch(InterruptedException e) {
            //TODO: Handle Exception
        }
    }

    protected void dealWithResult(String result, String check) {

        try{
            JSONObject jsonResp = new JSONObject(result);
            JSONObject list = jsonResp.optJSONObject("tracks");
            JSONArray trackList = (JSONArray) list.get("items");

            ArrayList<String> outputList = new ArrayList<String>();

            for (int count=0; count < trackList.length(); count++) {
                if (trackList.getJSONObject(count).getString("type").equals("track")) {
                    String trackOutput = trackList.getJSONObject(count).getString("name");
                    String artistOutput = trackList.getJSONObject(count).getJSONArray("artists").getJSONObject(0).getString("name");
                    if (trackOutput.toLowerCase().contains(check.toLowerCase()) == true) {
                        outputList.add(trackOutput + " by " + artistOutput);
                    }
                }
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    SearchListAdapter searchAdapter = new SearchListAdapter(context, outputList.toArray(new String[0]), R.layout.listview_search);
                    ListView searchList = findViewById(R.id.searchList);
                    searchList.setAdapter(searchAdapter);
                }
            });
        }
        catch(JSONException e){
            e.printStackTrace();
            System.exit(3);
        }

    }
}
