package com.example.underdiscover;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import androidx.appcompat.app.AppCompatActivity;

public class SearchActivity extends AppCompatActivity {

    private Activity context;
    //TODO: Create list for result data AKA attributeList

    private final String SEARCH_URL = "https://api.spotify.com/v1/search";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        this.context = this;

    }

    protected void onClickReturnToMain(View view) {
        finish();
    }

    public void onClickSearchExecute(View view) {

        EditText textBox = findViewById(R.id.searchTextBox);
        String searchRequest = textBox.getText().toString();
        String query = ""; //TODO: Manipulate searchRequest to create query

        final String apiUrl = SEARCH_URL + query;

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

    protected void dealWithResult(String result) {
        //TODO: Handle result for search
    }
}
