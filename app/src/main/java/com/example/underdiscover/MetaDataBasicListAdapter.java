package com.example.underdiscover;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public class MetaDataBasicListAdapter extends ArrayAdapter {

    protected Activity context;
    private int layoutFile;
    private String[] metadata;
    private Map<String, Double> attributeValues;
    private String access;
    private String trackID;

    protected MetaDataBasicListAdapter(Activity context, String[] metadata, Map<String,Double> attributeValues, String access, String trackID, int layoutFile) {
        super(context, layoutFile, metadata);

        this.context = context;
        this.metadata = metadata;
        this.layoutFile = layoutFile;
        this.attributeValues = attributeValues;
        this.access = access;
        this.trackID = trackID;
    }

    public View getView(int count, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(layoutFile, null, true);

        TextView attributeField = rowView.findViewById(R.id.attributeTextBasic);
        attributeField.setText(metadata[count]);

        Button recommendHigher = rowView.findViewById(R.id.basicMore);
        recommendHigher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                HashMap passValues = new HashMap<>();
                String attributeName = metadata[count].split(":")[0].toLowerCase();

                for (HashMap.Entry<String,Double> valuePair : attributeValues.entrySet()) {
                    if (valuePair.getKey().equals(attributeName)) {

                        passValues.put(valuePair.getKey(), valuePair.getValue());

                    }
                }

                Intent recommendResultIntent = new Intent(context, RecommendResultActivity.class);
                recommendResultIntent.putExtra("Access", access);
                recommendResultIntent.putExtra("Query", "https://api.spotify.com/v1/recommendations?limit=100&market=US&seed_tracks=" + trackID);
                recommendResultIntent.putExtra("ComparisonValues", passValues);
                recommendResultIntent.putExtra("Tightness", 15.0);
                recommendResultIntent.putExtra("AlgorithmType", "higher");
                context.startActivity(recommendResultIntent);
            }
        });

        Button recommendLower = rowView.findViewById(R.id.basicLess);
        recommendLower.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                HashMap passValues = new HashMap<>();
                String attributeName = metadata[count].split(":")[0].toLowerCase();

                for (HashMap.Entry<String,Double> valuePair : attributeValues.entrySet()) {
                    if (valuePair.getKey().equals(attributeName)) {

                        passValues.put(valuePair.getKey(), valuePair.getValue());

                    }
                }

                Intent recommendResultIntent = new Intent(context, RecommendResultActivity.class);
                recommendResultIntent.putExtra("Access", access);
                recommendResultIntent.putExtra("Query", "https://api.spotify.com/v1/recommendations?limit=100&market=US&seed_tracks=" + trackID);
                recommendResultIntent.putExtra("ComparisonValues", passValues);
                recommendResultIntent.putExtra("Tightness", 15.0);
                recommendResultIntent.putExtra("AlgorithmType", "lower");
                context.startActivity(recommendResultIntent);
            }
        });

        return rowView;
    }


}
