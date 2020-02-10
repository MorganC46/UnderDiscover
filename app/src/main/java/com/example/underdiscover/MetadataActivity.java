package com.example.underdiscover;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MetadataActivity extends AppCompatActivity {

    private AsyncTask fetchInstance;
    private final String METADATA_URL = "https://api.spotify.com/v1/audio-features/";
    private ArrayList<String> attributeList;
    private Activity context;
    ListView metadataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_metadata);

        final String apiUrl = METADATA_URL + getIntent().getStringExtra("TrackID");

        TextView title = findViewById(R.id.currentTrack);
        title.setText("Meta Data for: " + getIntent().getStringExtra("TrackName"));

        this.attributeList = new ArrayList<>();
        this.context = this;

        fetchInstance = new HttpStuff().execute(apiUrl);

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void onClickReturnToMain(View view) {
        finish();
    }

    private class HttpStuff extends AsyncTask<String, Void, String> {
        //Connection method
        protected String doInBackground(String... urlString) {

            String result = "";
            HttpURLConnection urlConn;

            try {
                URL url = new URL(urlString[0]);
                urlConn = (HttpURLConnection) url.openConnection();
                urlConn.setRequestMethod("GET");
                urlConn.setRequestProperty("Accept", "application/json");
                urlConn.setRequestProperty("Content-Type", "application/json");
                urlConn.setRequestProperty("Authorization", "Bearer " + getIntent().getStringExtra("Access"));

                if (urlConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    result = streamIntoString(urlConn.getInputStream());
                    dealWithResult(result);
                }
            }

            catch(MalformedURLException eURL) {
                eURL.printStackTrace();
                System.exit(1);
            }

            catch(IOException eIO) {
                eIO.printStackTrace();
                System.exit(2);
            }

            return null;
        }
    }

    protected String streamIntoString(InputStream stream) {
        //Method to process and correctly separate input streams
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            String data;
            String result = "";

            while ((data = reader.readLine()) != null) {
                result += data;
            }
            if (null != stream) {
                stream.close();
            }
            return result;
        }
        catch (IOException eIO) {
            eIO.printStackTrace();
            System.exit(3);
        }
        return null;
    }

    protected void dealWithResult(String result) {
        result = result.replace("{", "").replace("}", "");
        String[] resultArray = result.split(",");

        for (int count = 0; count < resultArray.length; count++) {
            String[] variable = resultArray[count].split(":");
            variable[0] = variable[0].replace(" ", "").replace("\"", "");

            attributeList.add(variable[0].substring(0,1).toUpperCase() + variable[0].substring(1)
                + ": " + variable[1]);

        }

        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                MetaDataListAdapter metadataAdapter = new MetaDataListAdapter(context, attributeList.toArray(new String[0]), R.layout.listview_metadata);
                metadataList = findViewById(R.id.attributeListView);
                metadataList.setAdapter(metadataAdapter);
            }
        });
    }

//        try {
//            JSONObject jsonResp = new JSONObject(result);
//        }
//        catch(JSONException eJSON) {
//            eJSON.printStackTrace();
//            System.exit(3);
//        }
}
