package com.example.underdiscover;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class TrackListMatchAdapter extends ArrayAdapter {

    protected Activity context;
    private int layoutFile;
    private String[] trackName;
    private String[] artistName;
    private Drawable[] image;
    private String[] uri;

    protected TrackListMatchAdapter(Activity context, String[] trackName, String[] artistName, Drawable[] image, String[] uri, int layoutFile) {
        super(context, layoutFile, trackName);

        this.context = context;
        this.trackName = trackName;
        this.artistName = artistName;
        this.image = image;
        this.uri = uri;
        this.layoutFile = layoutFile;
    }

    public View getView(int count, View view, ViewGroup viewGroup) {

        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(layoutFile, null, true);

        TextView trackText = rowView.findViewById(R.id.trackNameMatch);
        trackText.setText(trackName[count]);

        TextView artistText = rowView.findViewById(R.id.artistNameMatch);
        artistText.setText(artistName[count]);

        ImageView imageView = rowView.findViewById(R.id.trackImageMatch);
        imageView.setImageDrawable(image[count]);

        Button playButton = rowView.findViewById(R.id.playTrackMatch);
        Button metaDataButton = rowView.findViewById(R.id.getMetaDataMatch);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        metaDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent metaDataIntent = new Intent(context, MetadataActivity.class);

                String[] trackUri = uri[count].split(":");

                metaDataIntent.putExtra("TrackID", trackUri[2]);
                metaDataIntent.putExtra("Access", context.getIntent().getStringExtra("Access"));
                metaDataIntent.putExtra("TrackName", trackName[count] + " by " + artistName[count]);

                context.startActivity(metaDataIntent);
            }
        });


        return rowView;
    }


}