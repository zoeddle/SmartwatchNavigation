package com.example.carola.smartwatchnavigation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Carola on 27.09.16.
 */
public class PathInformationAdapter extends ArrayAdapter<PathInforamtion> {
    public PathInformationAdapter(Context context, ArrayList<PathInforamtion> information) {
        super(context, 0, information);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        PathInforamtion information = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.listview, parent, false);
        }
        // Lookup view for data population
        TextView tvName = (TextView) convertView.findViewById(R.id.tv_listPathInformation);
        TextView tvHome = (TextView) convertView.findViewById(R.id.tv_listInformationInM);
        // Populate the data into the template view using the data object
        if(information.angle <180){
            tvName.setText("links");
        }
        else if(Double.isNaN(information.angle)){
            tvName.setText(" ");
        }
        else {
            tvName.setText("rechts");
        }
        tvHome.setText((int) information.lenght +" m geradeaus");
        // Return the completed view to render on screen
        return convertView;
    }
}
