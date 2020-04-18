package com.example.underdiscover;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class MetadataBasicActivity extends AppCompatActivity {

    private ArrayList<String> attributeList;
    private Activity context;
    private Map<String,Double> attributeValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_metadata);

        final String apiUrl = "https://api.spotify.com/v1/audio-features/" + getIntent().getStringExtra("TrackID");

        TextView title = findViewById(R.id.currentTrack);
        title.setText("Meta Data for: " + getIntent().getStringExtra("TrackName"));

        Button recommendButton = findViewById(R.id.footerButton1);
        recommendButton.setText("ADVANCED");

        Button returnButton = findViewById(R.id.footerButton2);
        returnButton.setText("RETURN HOME");

        this.attributeList = new ArrayList<>();
        this.context = this;

        this.attributeValues = new HashMap<>();
        attributeValues.put("danceability", 0.0);
        attributeValues.put("energy", 0.0);
        attributeValues.put("loudness", 0.0);
        attributeValues.put("speechiness", 0.0);
        attributeValues.put("acousticness", 0.0);
        attributeValues.put("valence", 0.0);
        attributeValues.put("tempo", 0.0);

        SeekBar tightnessBar = findViewById(R.id.tightnessBar);
        TextView tightnessLabel = findViewById(R.id.tightnessTitle);

        tightnessBar.setVisibility(View.GONE);
        tightnessLabel.setVisibility(View.GONE);

        try {
            String result = new GenericHttpRequests.HttpRequestGet(apiUrl, getIntent().getStringExtra("Access")).execute().get();
            dealWithResult(result);
        } catch (ExecutionException e) {
            //TODO: Handle Exception
        } catch (InterruptedException e) {
            //TODO: Handle Exception
        }
    }

    public void onClickFooterButton1(View view) {
        Intent metaDataAdvancedIntent = new Intent(context, MetadataAdvancedActivity.class);

        metaDataAdvancedIntent.putExtra("TrackID", getIntent().getStringExtra("TrackID"));
        metaDataAdvancedIntent.putExtra("Access", getIntent().getStringExtra("Access"));
        metaDataAdvancedIntent.putExtra("TrackName", getIntent().getStringExtra("TrackName"));
        metaDataAdvancedIntent.putExtra("AttributeValues", (Serializable)attributeValues);
        metaDataAdvancedIntent.putExtra("AttributeList", attributeList);

        context.startActivity(metaDataAdvancedIntent);
    }

    public void onClickFooterButton2(View view) {
        finish();
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
                MetaDataBasicListAdapter metadataAdapter = new MetaDataBasicListAdapter(context, attributeList.toArray(new String[0]),
                        attributeValues, getIntent().getStringExtra("Access"), getIntent().getStringExtra("TrackID"), R.layout.listview_metadata_basic);
                ListView metadataList = findViewById(R.id.attributeList);
                metadataList.setAdapter(metadataAdapter);
            }

        });
    }

}
