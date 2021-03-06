package de.peterloos.beziersplines.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import de.peterloos.beziersplines.R;

/**
 * Project: Bézier Splines Simulation
 * Copyright (c) 2017 by PeLo on 23.01.2017. All rights reserved.
 * Contact info: peter.loos@gmx.de
 */

public class ConsoleArrayAdapter extends ArrayAdapter<String> {

    private Context context;
    private ArrayList<String> list;

    public ConsoleArrayAdapter(Context context, ArrayList<String> list) {
        super(context, -1, list);
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View rowView = convertView;

        // reuse views
        if (rowView == null) {

            // inflate this row
            LayoutInflater inflater =
                    (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            rowView = inflater.inflate(R.layout.console_list_row, parent, false);

            // configure view holder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.textview_counter = (TextView) rowView.findViewById(R.id.textview_counter);
            viewHolder.textview_x = (TextView) rowView.findViewById(R.id.textview_x);
            viewHolder.textview_y = (TextView) rowView.findViewById(R.id.textview_y);
            rowView.setTag(viewHolder);
        }

        // fill data
        ViewHolder holder = (ViewHolder) rowView.getTag();

        String spos = String.format(Locale.getDefault(), "%4d:", (position + 1));
        holder.textview_counter.setText(spos);

        String s = this.list.get(position);
        String[] parts = s.split("-");
        String sx = parts[0];
        String sy = parts[1];

        String s1 = String.format(Locale.getDefault(), "X = %s", sx);
        holder.textview_x.setText(s1);

        String s2 = String.format(Locale.getDefault(), "Y = %s", sy);
        holder.textview_y.setText(s2);

        return rowView;
    }

    private static class ViewHolder {
        TextView textview_counter;
        TextView textview_x;
        TextView textview_y;
    }
}
