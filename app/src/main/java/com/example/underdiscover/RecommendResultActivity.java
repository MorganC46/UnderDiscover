package com.example.underdiscover;

import android.app.Activity;
import android.graphics.drawable.Drawable;
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
import java.util.concurrent.ExecutionException;

public class RecommendResultActivity extends AppCompatActivity {

    private Activity context;
    private int numberOfResults;
    private int toDisplay;
    private TrackListAdapter searchAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommend_result);

        this.context = this;
        this.numberOfResults = 0;

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
            numberOfResults = trackList.length();

            TextView title = findViewById(R.id.resultTitle);
            if (numberOfResults == 100) { title.setText("We found over 100 tracks we think you will like..."); }
            else { title.setText("We found " + numberOfResults + " tracks we think you will like..."); }

            ArrayList<String> trackNameList = new ArrayList<String>();
            ArrayList<String> artistNameList = new ArrayList<String>();
            ArrayList<Drawable> imageList = new ArrayList<Drawable>();
            ArrayList<String> trackUriList = new ArrayList<String>();

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

                    if (numberOfResults <= 9) { toDisplay = numberOfResults-1; }
                    else { toDisplay = 10; }

                    searchAdapter = new TrackListAdapter(context, trackNameList.subList(0,toDisplay).toArray(new String[0]), artistNameList.subList(0,toDisplay).toArray(new String[0]),
                            imageList.subList(0,toDisplay).toArray(new Drawable[0]), trackUriList.subList(0,toDisplay).toArray(new String[0]), R.layout.listview_track);
                    ListView searchList = findViewById(R.id.resultList);

                    Button loadMore = new Button(context);
                    loadMore.setText("Load more recommendations...");

                    loadMore.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            if (toDisplay == numberOfResults) {
                                Snackbar allResultsShown = Snackbar.make(findViewById(R.id.resultList), "No further recommendations to show!", Snackbar.LENGTH_LONG);
                                allResultsShown.show();
                            }

                            else {

                                if (numberOfResults < toDisplay+10) { toDisplay = numberOfResults; }
                                else {toDisplay = toDisplay+10; }

                                searchAdapter = new TrackListAdapter(context, trackNameList.subList(0,toDisplay).toArray(new String[0]), artistNameList.subList(0,toDisplay).toArray(new String[0]),
                                        imageList.subList(0,toDisplay).toArray(new Drawable[0]), trackUriList.subList(0,toDisplay).toArray(new String[0]), R.layout.listview_track);

                                searchList.setAdapter(searchAdapter);
                                if ((toDisplay % 10) != 0) { searchList.setSelection(toDisplay-(16-(10-(toDisplay % 10)))); }
                                else { searchList.setSelection(toDisplay-16); }
                                searchAdapter.notifyDataSetChanged();

                            }
                        }
                    });

                    searchList.addFooterView(loadMore);
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
