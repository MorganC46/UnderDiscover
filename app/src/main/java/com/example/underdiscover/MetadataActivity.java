package com.example.underdiscover;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class MetadataActivity extends AppCompatActivity {

    private ArrayList<String> attributeList;
    private Activity context;
    private Map<String,Boolean> selectedAttributes;
    private Map<String,Double> attributeValues;
    private Double tightPercent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_metadata);

        final String apiUrl = "https://api.spotify.com/v1/audio-features/" + getIntent().getStringExtra("TrackID");

        TextView title = findViewById(R.id.currentTrack);
        title.setText("Meta Data for: " + getIntent().getStringExtra("TrackName"));

        this.attributeList = new ArrayList<>();
        this.context = this;

        this.selectedAttributes = new HashMap<>();
        selectedAttributes.put("danceability", false);
        selectedAttributes.put("energy", false);
        selectedAttributes.put("loudness", false);
        selectedAttributes.put("speechiness", false);
        selectedAttributes.put("acousticness", false);
        selectedAttributes.put("valence", false);
        selectedAttributes.put("tempo", false);

        this.attributeValues = new HashMap<>();
        attributeValues.put("danceability", 0.0);
        attributeValues.put("energy", 0.0);
        attributeValues.put("loudness", 0.0);
        attributeValues.put("speechiness", 0.0);
        attributeValues.put("acousticness", 0.0);
        attributeValues.put("valence", 0.0);
        attributeValues.put("tempo", 0.0);

        SeekBar tightnessBar = findViewById(R.id.tightnessBar);

        int tightness = tightnessBar.getProgress();
        TextView tightnessLabel = findViewById(R.id.tightnessTitle);
        tightnessLabel.setText("Tightness Percentage:" + tightness);

        this.tightPercent = 15.0;

        tightnessBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar tightnessBar, int tightness, boolean fromUser) {
                tightnessLabel.setText("Tightness Percentage: " + tightness + "%");
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                tightPercent = (double)tightnessBar.getProgress();
            }

        });

        try {
            String result = new GenericHttpRequests.HttpRequestGet(apiUrl, getIntent().getStringExtra("Access")).execute().get();
            dealWithResult(result);
        }
        catch(ExecutionException e) {
            //TODO: Handle Exception
        }
        catch(InterruptedException e) {
            //TODO: Handle Exception
        }

    }

    public void onClickReturnToMain(View view) {
        finish();
    }

    public void onClickRecommendTracks(View view) {

        String query = "https://api.spotify.com/v1/recommendations?limit=100&market=US&seed_tracks="
                + getIntent().getStringExtra("TrackID");

        HashMap passValues = new HashMap<>();

        for (Map.Entry<String,Boolean> selectedPair : selectedAttributes.entrySet()) {
            if (selectedPair.getValue()) {

                String attributeName = selectedPair.getKey();

                for (HashMap.Entry<String,Double> valuePair : attributeValues.entrySet()) {
                    if (valuePair.getKey().equals(attributeName)) {

                        passValues.put(valuePair.getKey(), valuePair.getValue());

                        double attributeValue;
                        attributeValue = valuePair.getValue();

                        if (attributeName.equals("danceability") || attributeName.equals("energy") || attributeName.equals("speechiness") ||
                                attributeName.equals("valence") || attributeName.equals("acousticness") || attributeName.equals("tempo")) {
                            query = query + "&min_" + attributeName + "=" + (attributeValue-(attributeValue*(tightPercent/100))) + "&max_" +
                                    attributeName + "=" + (attributeValue+(attributeValue*(tightPercent/100))) + "&target_" +
                                    attributeName + "=" + attributeValue;
                        }
                        if (attributeName.equals("loudness")) {
                            query = query + "&min_" + attributeName + "=" + (attributeValue+(attributeValue*(tightPercent/100))) + "&max_" +
                                    attributeName + "=" + (attributeValue-(attributeValue*(tightPercent/100))) + "&target_" +
                                    attributeName + "=" + attributeValue;
                        }
                    }
                }
            }
        }

        Intent recommendResultIntent = new Intent(context, RecommendResultActivity.class);
        recommendResultIntent.putExtra("Query", query);
        recommendResultIntent.putExtra("Access", getIntent().getStringExtra("Access"));
        recommendResultIntent.putExtra("ComparisonValues", passValues);
        context.startActivity(recommendResultIntent);
    }

    protected void dealWithResult(String result) {

        result = result.replace("{", "").replace("}", "");
        String[] resultArray = result.split(",");

        for (int count = 0; count < resultArray.length; count++) {
            String[] variable = resultArray[count].split(":");
            variable[0] = variable[0].replaceAll("\\s", "").replaceAll("\"", "");

            if (variable[0].equals("danceability") || variable[0].equals("energy") || variable[0].equals("speechiness") ||
                    variable[0].equals("valence") || variable[0].equals("acousticness")) {
                attributeValues.put(variable[0], Double.parseDouble(variable[1]));
                double oneToHundred = (Double.parseDouble(variable[1]))*100;
                double oneToHundredRounded = Math.round(oneToHundred*10) / 10.0;
                attributeList.add(variable[0].substring(0,1).toUpperCase() + variable[0].substring(1)
                        + ": " + oneToHundredRounded);
            }
            else if (variable[0].equals("loudness")) {
                attributeValues.put(variable[0], Double.parseDouble(variable[1]));
                double loudness = Math.round(Double.parseDouble(variable[1])*10) / 10.0;
                attributeList.add(variable[0].substring(0,1).toUpperCase() + variable[0].substring(1)
                        + ": " + loudness + " dB");
            }
            else if (variable[0].equals("tempo")) {
                attributeValues.put(variable[0], Double.parseDouble(variable[1]));
                int tempo = (int) Math.round(Double.parseDouble(variable[1]));
                attributeList.add(variable[0].substring(0,1).toUpperCase() + variable[0].substring(1)
                        + ": " + tempo + " BPM");
            }
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MetaDataListAdapter metadataAdapter = new MetaDataListAdapter(context, attributeList.toArray(new String[0])
                        , R.layout.listview_metadata);

                ListView metadataList = findViewById(R.id.attributeList);
                metadataList.setAdapter(metadataAdapter);
                metadataList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                        for (int i = 0; i < metadataList.getChildCount(); i++) {
                            if(pos == i ){
                                if (metadataList.getChildAt(i).getBackground() instanceof ColorDrawable) {
                                    int code = ((ColorDrawable) metadataList.getChildAt(i).getBackground()).getColor();
                                    if (code == Color.GREEN) {
                                        metadataList.getChildAt(i).setBackgroundColor(Color.TRANSPARENT);
                                        String attributeName = ((TextView)view.findViewById(R.id.attributeText)).getText().toString().split(":")[0].toLowerCase();
                                        selectedAttributes.put(attributeName, false);
                                    }
                                    else {
                                        metadataList.getChildAt(i).setBackgroundColor(Color.GREEN);
                                        String attributeName = ((TextView)view.findViewById(R.id.attributeText)).getText().toString().split(":")[0].toLowerCase();
                                        selectedAttributes.put(attributeName, true);
                                    }
                                }
                                else {
                                    metadataList.getChildAt(i).setBackgroundColor(Color.GREEN);
                                    String attributeName = ((TextView)view.findViewById(R.id.attributeText)).getText().toString().split(":")[0].toLowerCase();
                                    selectedAttributes.put(attributeName, true);
                                }
                            }
                        }
                    }
                });
            }
        });
    }

}
