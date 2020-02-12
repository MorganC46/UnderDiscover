package com.example.underdiscover;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class SearchListAdapter extends ArrayAdapter {

    protected Activity context;
    private int layoutFile;
    private String[] searchResult;

    protected SearchListAdapter(Activity context, String[] searchResult, int layoutFile) {
        super(context, layoutFile, searchResult);

        this.context = context;
        this.searchResult = searchResult;
        this.layoutFile = layoutFile;
    }

    public View getView(int count, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(layoutFile, null, true);

        TextView attributeField = rowView.findViewById(R.id.trackName);
        attributeField.setText(searchResult[count]);

        return rowView;
    }


}