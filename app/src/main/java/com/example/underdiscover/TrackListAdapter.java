package com.example.underdiscover;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class TrackListAdapter extends ArrayAdapter {

    protected Activity context;
    private int layoutFile;
    private ArrayList<TrackDetails> trackDetails;

    protected TrackListAdapter(Activity context, ArrayList<TrackDetails> trackDetails, int layoutFile) {
        super(context, layoutFile, trackDetails);

        this.context = context;
        this.trackDetails = trackDetails;
        this.layoutFile = layoutFile;
    }

    public View getView(int count, View view, ViewGroup viewGroup) {

        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(layoutFile, null, true);

        TrackDetails currentTrack = trackDetails.get(count);

        if (currentTrack.getType().equals("track")) {
            TextView trackText = rowView.findViewById(R.id.trackName);
            trackText.setText(currentTrack.getTrackName());

            TextView artistText = rowView.findViewById(R.id.artistName);
            artistText.setText(currentTrack.getArtistName());

            ImageView imageView = rowView.findViewById(R.id.trackImage);
            imageView.setImageDrawable(currentTrack.getImage());

            Button playButton = rowView.findViewById(R.id.playTrack);
            Button metaDataButton = rowView.findViewById(R.id.getMetaData);

            playButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (currentTrack.getPreviewUrl() == null) {
                        //SNACKBAR
                    }
                    else {
                        if (AudioPreviewManager.isMediaPlayerActive()) {
                            AudioPreviewManager.killMediaPlayer();
                        }
                        else {
                            try {
                                AudioPreviewManager.playPreview(context, currentTrack.getPreviewUrl());
                            } catch (Exception e) {

                            }
                        }
                    }
                }
            });

            metaDataButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent metaDataIntent = new Intent(context, MetadataBasicActivity.class);

                    AudioPreviewManager.killMediaPlayer();

                    String[] trackUri = currentTrack.getTrackUri().split(":");

                    metaDataIntent.putExtra("TrackID", trackUri[2]);
                    metaDataIntent.putExtra("Access", context.getIntent().getStringExtra("Access"));
                    metaDataIntent.putExtra("TrackName", currentTrack.getTrackName() + " by " + currentTrack.getArtistName());

                    context.startActivity(metaDataIntent);
                    context.finish();
                }
            });
        }

        if (currentTrack.getType().equals("album")) {
            TextView artistText = rowView.findViewById(R.id.artistNameAlbum);
            artistText.setText(currentTrack.getArtistName());

            TextView albumText = rowView.findViewById(R.id.albumName);
            albumText.setText(currentTrack.getTrackName());

            ImageView imageView = rowView.findViewById(R.id.trackImageAlbum);
            imageView.setImageDrawable(currentTrack.getImage());

            Button albumTracksButton = rowView.findViewById(R.id.getAlbumTracks);

            albumTracksButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String[] trackUri = currentTrack.getTrackUri().split(":");

                    final String query = "https://api.spotify.com/v1/albums/" + trackUri[2]
                            + "/tracks";

                    try {
                        String result = new GenericHttpRequests.HttpRequestGet(query, context.getIntent().getStringExtra("Access")).execute().get();
                        ((SearchActivity)context).dealWithAlbumTracksResult(result, currentTrack.getImage());

                    } catch (ExecutionException e) {
                        //TODO: Handle Exception
                    } catch (InterruptedException e) {
                        //TODO: Handle Exception
                    }
                }
            });
        }

        if (currentTrack.getType().equals("artist")) {
            TextView artistText = rowView.findViewById(R.id.artistName);
            artistText.setText(currentTrack.getArtistName());

            ImageView imageView = rowView.findViewById(R.id.artistImage);
            imageView.setImageDrawable(currentTrack.getImage());

            Button artistTracksButton = rowView.findViewById(R.id.getArtistTracks);

            artistTracksButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String[] artistUri = currentTrack.getTrackUri().split(":");

                    final String query = "https://api.spotify.com/v1/artists/" + artistUri[2]
                            + "/albums";

                    try {
                        String result = new GenericHttpRequests.HttpRequestGet(query, context.getIntent().getStringExtra("Access")).execute().get();
                        ((SearchActivity)context).dealWithArtistAlbumsResult(result);

                    } catch (ExecutionException e) {
                        //TODO: Handle Exception
                    } catch (InterruptedException e) {
                        //TODO: Handle Exception
                    }
                }
            });
        }

        return rowView;
    }


}