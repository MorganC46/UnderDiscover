package com.example.underdiscover;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.concurrent.ExecutionException;

public class MetaDataListAdapter extends ArrayAdapter {

    private static final String RECOMMEND_URL = "https://api.spotify.com/v1/recommendations?";

    protected Activity context;
    private int layoutFile;
    private String[] metadata;
    private String trackId;
    private String accessToken;

    protected MetaDataListAdapter(Activity context, String[] metadata, int layoutFile,
                                  String trackId, String accessToken) {
        super(context, layoutFile, metadata);

        this.context = context;
        this.metadata = metadata;
        this.layoutFile = layoutFile;
        this.trackId = trackId;
        this.accessToken = accessToken;
    }

    public View getView(int count, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(layoutFile, null, true);

        TextView attributeField = rowView.findViewById(R.id.attributeText);
        attributeField.setText(metadata[count]);

        Button infoButton = rowView.findViewById(R.id.showInfo);
        Button recommendationButton = rowView.findViewById(R.id.getRecommendation);

        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater popupInflater = (LayoutInflater)
                        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View popupView = popupInflater.inflate(R.layout.popup_window_metadata, null);

                int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                final PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);

                TextView info = popupView.findViewById(R.id.metaDataPopUpInfo);
                info.setText(returnMetaDataInfo(metadata[count].split(":")[0]));

                popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
            }
        });

        recommendationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String attributeName = metadata[count].split(":")[0].toLowerCase();
                String query = "";
                double attributeValue = 1;

                String attributeValueString = metadata[count].split(":")[1].replaceAll("\"", "");

                if (attributeName.equals("danceability") || attributeName.equals("energy") || attributeName.equals("speechiness") ||
                        attributeName.equals("valence") || attributeName.equals("acousticness")) {
                    attributeValue = Double.parseDouble(attributeValueString) / 100;
                }
                if (attributeName.equals("loudness") || attributeName.equals("tempo")) {
                    attributeValueString = attributeValueString.split(" ")[1];
                    attributeValue = Double.parseDouble(attributeValueString.split(" ")[0]);
                }

                if (attributeName.equals("loudness")) {
                    query = RECOMMEND_URL + "limit=100&market=US&seed_tracks=" + trackId + "&min_" +
                            attributeName + "=" + (attributeValue+(attributeValue*0.1)) + "&max_" +
                            attributeName + "=" + (attributeValue-(attributeValue*0.1)) + "&target_" +
                            attributeName + "=" + attributeValue;
                }
                else {
                    query = RECOMMEND_URL + "limit=100&market=US&seed_tracks=" + trackId + "&min_" +
                            attributeName + "=" + (attributeValue-(attributeValue*0.1)) + "&max_" +
                            attributeName + "=" + (attributeValue+(attributeValue*0.1)) + "&target_" +
                            attributeName + "=" + attributeValue;
                }

                Intent recommendResultIntent = new Intent(context, RecommendResultActivity.class);
                recommendResultIntent.putExtra("Query", query);
                recommendResultIntent.putExtra("Access", accessToken);
                context.startActivity(recommendResultIntent);

            }
        });

        return rowView;
    }

    protected String returnMetaDataInfo(String type) {
        String ret = "";
        switch(type) {
            case "Danceability":
                ret = "Danceability describes how suitable a track is for dancing based on a combination of musical elements including tempo, rhythm stability, beat strength, and overall regularity. A value of 0 is least danceable and 100 is most danceable.";
                break;
            case "Energy":
                ret = "Energy represents a perceptual measure of intensity and activity. Typically, energetic tracks feel fast, loud, and noisy. For example, death metal has high energy, while a Bach prelude scores low on the scale. A value of 0 is least energetic and 100 is most energetic.";
                break;
            case "Speechiness":
                ret = "Speechiness detects the presence of spoken words in a track. The more exclusively speech-like the recording, the closer to 100 the attribute value. Values close to 0 likely represent instrumental tracks.";
                break;
            case "Acousticness":
                ret = "A confidence measure from 0 to 100 of whether the track is acoustic. 100 represents high confidence the track is acoustic.";
                break;
            case "Valence":
                ret = "A measure from 0 to 100 describing the musical positiveness conveyed by a track. Tracks with high valence sound more positive (e.g. happy, cheerful, euphoric), while tracks with low valence sound more negative (e.g. sad, depressed, angry).";
                break;
            case "Tempo":
                ret = "The overall estimated tempo of a track in beats per minute (BPM). In musical terminology, tempo is the speed or pace of a given piece and derives directly from the average beat duration.";
                break;
        }
        return ret;
    }


}
