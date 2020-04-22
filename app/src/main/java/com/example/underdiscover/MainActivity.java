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
import android.view.View;
import android.widget.Button;

import com.google.android.material.snackbar.Snackbar;
import com.spotify.sdk.android.authentication.*;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private static final String CLIENT_ID = "5abdc4ff7e8347bebd727e823afa7df7";
    private static final String REDIRECT_ID = "com.example.underdiscover://callback";
    private static final int REQUEST_CODE = 1337;
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

    public void onClickSearchSpotify(View view) {

        Intent searchIntent = new Intent(context, SearchActivity.class);
        searchIntent.putExtra("Access", ACCESS_TOKEN);

        MainActivity.this.startActivity(searchIntent);
    }
}
