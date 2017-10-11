package de.peterloos.beziersplines.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import de.peterloos.beziersplines.R;

/**
 * Created by loospete on 11.10.2017.
 */

public class ConsoleArrayAdapter extends ArrayAdapter<String> {

    private final Context context;
    private final String[] values;

    public ConsoleArrayAdapter(Context context, String[] values) {
        super(context, -1, values);
        this.context = context;
        this.values = values;
    }

    // TODO: DAS ist alles noch anzupassen

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.console_list_row, parent, false);

        String sx = String.format("X = %d", position);
        TextView textView1 = (TextView) rowView.findViewById(R.id.textview_x);
        textView1.setText(sx);

        String sy = String.format("X = %d", position);
        TextView textView2 = (TextView) rowView.findViewById(R.id.textview_y);
        textView2.setText(sy);

        return rowView;
    }
}
