package com.example.underdiscover;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MetadataAdvancedActivity extends AppCompatActivity {

    private Activity context;
    private Double tightPercent;
    private Map<String,Boolean> selectedAttributes;
    private ArrayList<String> attributeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_metadata);

        final String apiUrl = "https://api.spotify.com/v1/audio-features/" + getIntent().getStringExtra("TrackID");

        TextView title = findViewById(R.id.currentTrack);
        title.setText("Meta Data for: " + getIntent().getStringExtra("TrackName"));

        TextView infoBox = findViewById(R.id.infoBox);
        infoBox.setText("Click attributes to select them for inclusion in recommendations!");

        Button recommendButton = findViewById(R.id.footerButton1);
        recommendButton.setText("RECOMMEND");

        Button returnButton = findViewById(R.id.footerButton2);
        returnButton.setText("RETURN TO BASIC");

        this.context = this;
        this.attributeList = (ArrayList<String>) getIntent().getSerializableExtra("AttributeList");

        this.selectedAttributes = new HashMap<>();
        selectedAttributes.put("danceability", false);
        selectedAttributes.put("energy", false);
        selectedAttributes.put("loudness", false);
        selectedAttributes.put("speechiness", false);
        selectedAttributes.put("acousticness", false);
        selectedAttributes.put("valence", false);
        selectedAttributes.put("tempo", false);

        SeekBar tightnessBar = findViewById(R.id.tightnessBar);

        int tightness = tightnessBar.getProgress();
        TextView tightnessLabel = findViewById(R.id.tightnessTitle);
        tightnessLabel.setText("Tightness Percentage:" + tightness + "%");

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

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MetaDataAdvancedListAdapter metadataAdapter = new MetaDataAdvancedListAdapter(context, attributeList.toArray(new String[0])
                        , R.layout.listview_metadata_advanced);

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
                                        String attributeName = ((TextView)view.findViewById(R.id.attributeTextAdvanced)).getText().toString().split(":")[0].toLowerCase();
                                        selectedAttributes.put(attributeName, false);
                                    }
                                    else {
                                        metadataList.getChildAt(i).setBackgroundColor(Color.GREEN);
                                        String attributeName = ((TextView)view.findViewById(R.id.attributeTextAdvanced)).getText().toString().split(":")[0].toLowerCase();
                                        selectedAttributes.put(attributeName, true);
                                    }
                                }
                                else {
                                    metadataList.getChildAt(i).setBackgroundColor(Color.GREEN);
                                    String attributeName = ((TextView)view.findViewById(R.id.attributeTextAdvanced)).getText().toString().split(":")[0].toLowerCase();
                                    selectedAttributes.put(attributeName, true);
                                }
                            }
                        }
                    }
                });
            }
        });

    }

    public void onClickFooterButton1(View view) {

        HashMap passValues = new HashMap<>();
        HashMap<String, Double> attributeValues = (HashMap<String,Double>)getIntent().getSerializableExtra("AttributeValues");

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
        recommendResultIntent.putExtra("AlgorithmType", "advanced");
        context.startActivity(recommendResultIntent);
    }

    public void onClickFooterButton2(View view) {
        finish();
    }

}