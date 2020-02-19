package com.example.underdiscover;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class MetadataActivity extends AppCompatActivity {

    private ArrayList<String> attributeList;
    private Activity context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_metadata);

        final String apiUrl = "https://api.spotify.com/v1/audio-features/" + getIntent().getStringExtra("TrackID");

        TextView title = findViewById(R.id.currentTrack);
        title.setText("Meta Data for: " + getIntent().getStringExtra("TrackName"));

        this.attributeList = new ArrayList<>();
        this.context = this;

        try {
            String result = new GenericHttpRequests.HttpRequest(apiUrl, getIntent().getStringExtra("Access")).execute().get();
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

    protected void dealWithResult(String result) {
        result = result.replace("{", "").replace("}", "");
        String[] resultArray = result.split(",");

        for (int count = 0; count < resultArray.length; count++) {
            String[] variable = resultArray[count].split(":");
            variable[0] = variable[0].replaceAll("\\s", "").replaceAll("\"", "");

            if (variable[0].equals("danceability") || variable[0].equals("energy") || variable[0].equals("speechiness") ||
                    variable[0].equals("valence") || variable[0].equals("acousticness")) {
                double oneToHundred = (Double.parseDouble(variable[1]))*100;
                double oneToHundredRounded = Math.round(oneToHundred*10) / 10.0;
                attributeList.add(variable[0].substring(0,1).toUpperCase() + variable[0].substring(1)
                        + ": " + oneToHundredRounded);
            }
            else if (variable[0].equals("loudness")) {
                double loudness = Math.round(Double.parseDouble(variable[1])*10) / 10.0;
                attributeList.add(variable[0].substring(0,1).toUpperCase() + variable[0].substring(1)
                        + ": " + loudness + " dB");
            }
            else if (variable[0].equals("tempo")) {
                int tempo = (int) Math.round(Double.parseDouble(variable[1]));
                attributeList.add(variable[0].substring(0,1).toUpperCase() + variable[0].substring(1)
                        + ": " + tempo + " BPM");
            }
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MetaDataListAdapter metadataAdapter = new MetaDataListAdapter(context, attributeList.toArray(new String[0]), R.layout.listview_metadata);
                ListView metadataList = findViewById(R.id.attributeList);
                metadataList.setAdapter(metadataAdapter);
            }
        });
    }

}
