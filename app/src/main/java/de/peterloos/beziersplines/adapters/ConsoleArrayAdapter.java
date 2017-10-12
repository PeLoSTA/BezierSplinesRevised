package de.peterloos.beziersplines.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import de.peterloos.beziersplines.R;

/**
 * Created by loospete on 11.10.2017.
 */

public class ConsoleArrayAdapter extends ArrayAdapter<String> {

    private final Context context;
    private final ArrayList<String> values;

    // TODO: Liste values in list umbenennen ....

    public ConsoleArrayAdapter(Context context, ArrayList<String> values) {
        super(context, -1, values);
        this.context = context;
        this.values = values;
    }

    // TODO: DAS ist alles noch anzupassennnnn

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.console_list_row, parent, false);

        String s = this.values.get(position);
        String[] parts = s.split("-");
        String sx = parts[0]; // 004
        String sy = parts[1];

        String s1 = String.format("X = %s", sx);
        TextView textView1 = (TextView) rowView.findViewById(R.id.textview_x);
        textView1.setText(s1);

        String s2 = String.format("Y = %s", sy);
        TextView textView2 = (TextView) rowView.findViewById(R.id.textview_y);
        textView2.setText(s2);

        return rowView;
    }
}
