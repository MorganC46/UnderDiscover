package com.example.underdiscover;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.spotify.protocol.client.CallResult;
import com.spotify.protocol.types.Track;
import com.spotify.sdk.android.authentication.*;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private static final String CLIENT_ID = "5abdc4ff7e8347bebd727e823afa7df7";
    private static final String REDIRECT_ID = "com.example.underdiscover://callback";
    private static final int REQUEST_CODE = 1337;
    private SpotifyAppRemote mSpotifyAppRemote;
    private int playerState = 0;
    private String ACCESS_TOKEN;
    private Activity context;

    protected void onCreate(Bundle savedInstanceState) {

        if (isNetworkAvailable() == false) {
            Snackbar noInternet = Snackbar.make(findViewById(R.id.text), "No Network Detected", Snackbar.LENGTH_INDEFINITE);
            noInternet.show();
        }
        else {
            authenticateSpotify(); //TODO: Do this before the main activity is created - possibly in separate activity?
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("UnderDiscover");
        this.context = this;
    }

    @Override
    protected void onStart() {
        super.onStart();

        ConnectionParams params = new ConnectionParams.Builder(CLIENT_ID)
                .setRedirectUri(REDIRECT_ID).showAuthView(true).build();
        SpotifyAppRemote.connect(this, params, new Connector.ConnectionListener() {
            @Override
            public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                mSpotifyAppRemote = spotifyAppRemote;
                mSpotifyAppRemote.getPlayerApi().pause();
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.e("MyActivity", throwable.getMessage(), throwable);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSpotifyAppRemote.getPlayerApi().pause();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }

    private void firstPlay(View view) {
        getCurrentTrack(view);
        mSpotifyAppRemote.getPlayerApi().play("spotify:playlist:0KJZaU4cVWKF68kyAoR1TA");
    }

    private void pause(View view) {
        mSpotifyAppRemote.getPlayerApi().pause();
    }

    private void play(View view) {
        getCurrentTrack(view);
        mSpotifyAppRemote.getPlayerApi().resume();
    }

    private void getCurrentTrack(View view) {
        mSpotifyAppRemote.getPlayerApi().subscribeToPlayerState().setEventCallback(playerState -> {
            Track track = playerState.track;
            if (track != null) {
                TextView title = findViewById(R.id.currentlyPlaying);
                title.setText("Currently Playing: " + track.name + " by " + track.artist.name);
                ImageView image = findViewById(R.id.currentlyPlayingImage);
                mSpotifyAppRemote.getImagesApi().getImage(track.imageUri).setResultCallback(new CallResult.ResultCallback<Bitmap>() {
                    @Override
                    public void onResult(Bitmap bitmap) {
                        image.setImageBitmap(bitmap);
                    }
                });
            }
        });
    }

    private boolean isNetworkAvailable() {
        //Boolean method to check for a network connection - used to make pathway decisions
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connManager != null) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                NetworkCapabilities capabilities = connManager.getNetworkCapabilities(connManager.getActiveNetwork());
                if (capabilities != null) {
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        return true;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        return true;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                        return true;
                    } else {
                        return false;
                    }
                }
            } else {
                NetworkInfo activeNetworkInfo = connManager.getActiveNetworkInfo();
                return activeNetworkInfo != null && activeNetworkInfo.isConnected();
            }
        }
        return false;
    }

    private void authenticateSpotify() {
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_ID);
        builder.setScopes(new String[]{"user-read-playback-state,streaming,playlist-read-collaborative,user-modify-playback-state,playlist-modify-public,user-library-modify,user-top-read,user-read-currently-playing,playlist-read-private,app-remote-control,playlist-modify-private,user-library-read"});
        AuthenticationRequest request = builder.build();
        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        ACCESS_TOKEN = AuthenticationClient.getResponse(resultCode, intent).getAccessToken();

        try {
            String accountResult = new GenericHttpRequests.HttpRequestGet("https://api.spotify.com/v1/me", ACCESS_TOKEN).execute().get();

            JSONObject jsonResp = new JSONObject(accountResult);
            String accountType = jsonResp.getString("product");

            if (accountType.equals("premium")) {
                Snackbar notifyType = Snackbar.make(findViewById(R.id.searchSpotify), "This is a premium account!", Snackbar.LENGTH_INDEFINITE);
                notifyType.show();
            }
            else {
                Snackbar notifyType = Snackbar.make(findViewById(R.id.searchSpotify), "This is a non-premium account!", Snackbar.LENGTH_INDEFINITE);
                notifyType.show();
            }
        }
        catch (JSONException e) { //TODO:
        }
        catch (ExecutionException e) { //TODO:
        }
        catch (InterruptedException e){ //TODO:
        }
    }

    public void onClickChangePlayerState(View view) {

        Button changePlayerState = findViewById(R.id.changePlayerState);

        switch (playerState) {
            case 0:
                firstPlay(view); playerState = 1;
                changePlayerState.setText("Pause");
                break;
            case 1:
                pause(view); playerState = 2;
                changePlayerState.setText("Resume");
                break;
            case 2:
                play(view); playerState = 1;
                changePlayerState.setText("Pause");
                break;
        }
    }

    public void onClickSkipSong(View view) {
        mSpotifyAppRemote.getPlayerApi().skipNext();
    }

    public void onClickShowMetaData(View view) {

        if (playerState != 0) {

            mSpotifyAppRemote.getPlayerApi().subscribeToPlayerState().setEventCallback(playerState -> {
                Intent metaDataIntent = new Intent(MainActivity.this, MetadataBasicActivity.class);

                String[] trackUri = playerState.track.uri.split(":");

                metaDataIntent.putExtra("TrackID", trackUri[2]);
                metaDataIntent.putExtra("Access", ACCESS_TOKEN);
                metaDataIntent.putExtra("TrackName", playerState.track.name + " by " + playerState.track.artist.name);

                MainActivity.this.startActivity(metaDataIntent);

            });

            Button changePlayerState = findViewById(R.id.changePlayerState);
            playerState = 2;
            changePlayerState.setText("Resume");
        }

        else {
            Snackbar noTrack = Snackbar.make(view, "No track to gather data on playing!", Snackbar.LENGTH_LONG);
            noTrack.show();
        }
    }

    public void onClickSearchSpotify(View view) {

        if (playerState == 1) {
            pause(view); playerState = 2;
        }

        Intent searchIntent = new Intent(MainActivity.this, SearchActivity.class);
        searchIntent.putExtra("Access", ACCESS_TOKEN);

        MainActivity.this.startActivity(searchIntent);
    }
}
