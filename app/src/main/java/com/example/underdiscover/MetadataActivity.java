package com.example.underdiscover;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class MetadataActivity extends AppCompatActivity {

    private ArrayList<String> attributeList;
    private Activity context;
    private Map<String,Boolean> selectedAttributes;
    private Map<String,Double> attributeValues;
    private Double tightPercent;
    private boolean advancedMode;
    private SeekBar tightnessBar;
    private ListView metadataListBasic;
    private ListView metadataListAdvanced;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_metadata);

        final String apiUrl = "https://api.spotify.com/v1/audio-features/" + getIntent().getStringExtra("TrackID");

        TextView title = findViewById(R.id.currentTrack);
        title.setText("Meta Data for: " + getIntent().getStringExtra("TrackName"));

        this.attributeList = new ArrayList<>();
        this.context = this;
        this.advancedMode = false;
        this.tightnessBar = findViewById(R.id.tightnessBar);
        this.metadataListBasic = findViewById(R.id.attributeListBasic);
        this.metadataListAdvanced = findViewById(R.id.attributeListAdvanced);

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

        try {
            String result = new GenericHttpRequests.HttpRequestGet(apiUrl, getIntent().getStringExtra("Access")).execute().get();
            dealWithResult(result);
            createBasicView();
            createAdvancedView();
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

    public void onClickSwitchMode(View view) {
        if (advancedMode == false) {

            TextView desc = findViewById(R.id.infoBox);
            desc.setText("Click attributes to select them for inclusion in recommendations!");

            tightnessBar.setVisibility(View.VISIBLE);
            metadataListBasic.setVisibility(View.GONE);
            metadataListAdvanced.setVisibility(View.VISIBLE);

            advancedMode = true;
        }

        if (advancedMode == true) {

            TextView desc = findViewById(R.id.infoBox);
            desc.setText("Select 'More' or 'Less' to generate songs with a higher or lower value of selected attribute!");

            tightnessBar.setVisibility(View.GONE);
            metadataListBasic.setVisibility(View.VISIBLE);
            metadataListAdvanced.setVisibility(View.GONE);

            advancedMode = false;
        }
    }

    public void onClickRecommendTracks(View view) {

        HashMap passValues = new HashMap<>();

        for (Map.Entry<String,Boolean> selectedPair : selectedAttributes.entrySet()) {
            if (selectedPair.getValue()) {

                String attributeName = selectedPair.getKey();

                for (HashMap.Entry<String,Double> valuePair : attributeValues.entrySet()) {
                    if (valuePair.getKey().equals(attributeName)) {

                        passValues.put(valuePair.getKey(), valuePair.getValue());

                    }
                }
            }
        }

        Intent recommendResultIntent = new Intent(context, RecommendResultActivity.class);
        recommendResultIntent.putExtra("Access", getIntent().getStringExtra("Access"));
        recommendResultIntent.putExtra("Query", "https://api.spotify.com/v1/recommendations?limit=100&market=US&seed_tracks=" + getIntent().getStringExtra("TrackID"));
        recommendResultIntent.putExtra("ComparisonValues", passValues);
        recommendResultIntent.putExtra("Tightness", tightPercent);
        context.startActivity(recommendResultIntent);
    }

    protected void createBasicView() {
        MetaDataBasicListAdapter metadataAdapter = new MetaDataBasicListAdapter(context, attributeList.toArray(new String[0])
                , R.layout.listview_metadata_basic);
        metadataListBasic.setAdapter(metadataAdapter);
    }

    protected void createAdvancedView() {

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

        tightnessBar.setVisibility(View.GONE);

        MetaDataAdvancedListAdapter metadataAdapter = new MetaDataAdvancedListAdapter(context, attributeList.toArray(new String[0])
                , R.layout.listview_metadata_advanced);

        metadataListAdvanced.setAdapter(metadataAdapter);
        metadataListAdvanced.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                for (int i = 0; i < metadataListAdvanced.getChildCount(); i++) {
                    if(pos == i ){
                        if (metadataListAdvanced.getChildAt(i).getBackground() instanceof ColorDrawable) {
                            int code = ((ColorDrawable) metadataListAdvanced.getChildAt(i).getBackground()).getColor();
                            if (code == Color.GREEN) {
                                metadataListAdvanced.getChildAt(i).setBackgroundColor(Color.TRANSPARENT);
                                String attributeName = ((TextView)view.findViewById(R.id.attributeTextAdvanced)).getText().toString().split(":")[0].toLowerCase();
                                selectedAttributes.put(attributeName, false);
                            }
                            else {
                                metadataListAdvanced.getChildAt(i).setBackgroundColor(Color.GREEN);
                                String attributeName = ((TextView)view.findViewById(R.id.attributeTextAdvanced)).getText().toString().split(":")[0].toLowerCase();
                                selectedAttributes.put(attributeName, true);
                            }
                        }
                        else {
                            metadataListAdvanced.getChildAt(i).setBackgroundColor(Color.GREEN);
                            String attributeName = ((TextView)view.findViewById(R.id.attributeTextAdvanced)).getText().toString().split(":")[0].toLowerCase();
                            selectedAttributes.put(attributeName, true);
                        }
                    }
                }
            }
        });
        metadataListAdvanced.setVisibility(View.GONE);
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
    }

}
