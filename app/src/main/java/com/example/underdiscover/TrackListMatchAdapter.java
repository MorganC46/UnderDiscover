package com.example.underdiscover;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

public class TrackListMatchAdapter extends ArrayAdapter {

    protected Activity context;
    private int layoutFile;
    private ArrayList<TrackDetails> trackDetails;

    protected TrackListMatchAdapter(Activity context, ArrayList<TrackDetails> trackDetails, int layoutFile) {
        super(context, layoutFile, trackDetails);

        this.context = context;
        this.trackDetails = trackDetails;
        this.layoutFile = layoutFile;
    }

    public View getView(int count, View view, ViewGroup viewGroup) {

        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(layoutFile, null, true);

        TrackDetails currentTrack = trackDetails.get(count);

        TextView trackText = rowView.findViewById(R.id.trackNameMatch);
        trackText.setText(currentTrack.getTrackName());

        TextView artistText = rowView.findViewById(R.id.artistNameMatch);
        artistText.setText(currentTrack.getArtistName());

        ImageView imageView = rowView.findViewById(R.id.trackImageMatch);
        imageView.setImageDrawable(currentTrack.getImage());

        Button playButton = rowView.findViewById(R.id.playTrackMatch);
        Button metaDataButton = rowView.findViewById(R.id.getMetaDataMatch);

        ProgressBar overallMatch = rowView.findViewById(R.id.overallMatchBar);
        TextView overallMatchText = rowView.findViewById(R.id.overallMatchText);

        overallMatch.setProgress((int)Math.round(100-currentTrack.getOverallMatch()));

        overallMatchText.setText((int)Math.round(100-currentTrack.getOverallMatch()) + "%");

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        metaDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent metaDataIntent = new Intent(context, MetadataActivity.class);

                String[] trackUri = currentTrack.getTrackUri().split(":");

                metaDataIntent.putExtra("TrackID", trackUri[2]);
                metaDataIntent.putExtra("Access", context.getIntent().getStringExtra("Access"));
                metaDataIntent.putExtra("TrackName", currentTrack.getTrackName() + " by " + currentTrack.getArtistName());

                context.startActivity(metaDataIntent);
            }
        });

        return rowView;
    }
}