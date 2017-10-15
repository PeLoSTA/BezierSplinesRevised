package de.peterloos.beziersplines.activities;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import de.peterloos.beziersplines.R;
import de.peterloos.beziersplines.adapters.ConsoleArrayAdapter;
import de.peterloos.beziersplines.utils.ControlPointsHolder;

/**
 * Project: Bézier Splines Simulation
 * Copyright (c) 2017 by PeLo on 23.01.2017. All rights reserved.
 * Contact info: peter.loos@gmx.de
 */

public class ConsoleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_console);

        // prefer action bar title with two lines
        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.title_activity_console);
            actionBar.setSubtitle(this.getString(R.string.app_main_title));
        }

        // retrieve control references
        ListView listView = (ListView) this.findViewById(R.id.listview);
        TextView textView = (TextView) this.findViewById(R.id.textview_header_console);

        // create adapter
        ControlPointsHolder holder = ControlPointsHolder.getInstance();
        ArrayList<String> list = holder.getAsListOfStrings();

        // setup header and fill listview, if list isn't empty
        Resources res = this.getResources();
        String s = res.getString(R.string.header_console);
        if (list.size() == 0) {

            String header = String.format(Locale.getDefault(), s, 0);
            textView.setText(header);
        } else {

            String header = String.format(Locale.getDefault(), s, list.size());
            textView.setText(header);

            Context context = this.getApplicationContext();
            ConsoleArrayAdapter adapter = new ConsoleArrayAdapter(context, list);
            listView.setAdapter(adapter);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // want 'up' button to have the same functionality as the 'back' button;
        // therefore I'm overriding onOptionsItemSelected and just 'finish' the current activity
        switch (item.getItemId()) {
            case android.R.id.home:
                // up arrow in action bar clicked - goto main activity
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
