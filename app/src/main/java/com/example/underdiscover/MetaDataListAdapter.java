package com.example.underdiscover;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MetaDataListAdapter extends ArrayAdapter {

    protected Activity context;
    private int layoutFile;
    private String[] metadata;

    protected MetaDataListAdapter(Activity context, String[] metadata, int layoutFile) {
        super(context, layoutFile, metadata);

        this.context = context;
        this.metadata = metadata;
        this.layoutFile = layoutFile;
    }

    public View getView(int count, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(layoutFile, null, true);

        TextView attributeField = rowView.findViewById(R.id.attributeText);
        attributeField.setText(metadata[count]);

        return rowView;
    }


}
