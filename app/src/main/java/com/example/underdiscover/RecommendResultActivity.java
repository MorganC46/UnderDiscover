package com.example.underdiscover;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class RecommendResultActivity extends AppCompatActivity {

    private Activity context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommend_result);

        this.context = this;

        try {
            String result = new GenericHttpRequests.HttpRequest(getIntent().getStringExtra("Query"), getIntent().getStringExtra("Access")).execute().get();
            dealWithResult(result);
        }
        catch(ExecutionException e) {
            //TODO: Handle Exception
        }
        catch(InterruptedException e) {
            //TODO: Handle Exception
        }

    }

    protected void dealWithResult(String result) {

        try {
            JSONObject jsonResp = new JSONObject(result);
            JSONArray trackList = (JSONArray) jsonResp.get("tracks");

            ArrayList<String> trackNameList = new ArrayList<String>();
            ArrayList<String> artistNameList = new ArrayList<String>();
            ArrayList<Drawable> imageList = new ArrayList<Drawable>();
            ArrayList<String> trackUriList = new ArrayList<String>();

            for (int count = 0; count < trackList.length(); count++) {
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
                    TrackListAdapter searchAdapter = new TrackListAdapter(context, trackNameList.toArray(new String[0]), artistNameList.toArray(new String[0]),
                            imageList.toArray(new Drawable[0]), trackUriList.toArray(new String[0]), R.layout.listview_track);
                    ListView searchList = findViewById(R.id.resultList);
                    searchList.setAdapter(searchAdapter);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
            System.exit(3);
        }
          catch (NullPointerException e) {
            //TODO: HANDLE EXCEPTION
          }

    }

}
