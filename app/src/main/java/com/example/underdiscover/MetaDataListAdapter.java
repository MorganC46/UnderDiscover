package com.example.underdiscover;

import android.app.Activity;
import android.content.Context;
import android.text.Layout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
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

        attributeField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater popupInflater = (LayoutInflater)
                        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View popupView = popupInflater.inflate(R.layout.popup_window_metadata, null);

                int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                final PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);

                popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

                //TODO: Decide whether to implement onTouch to dismiss if touched on box, not just outside of it
            }
        });

        return rowView;
    }


}
