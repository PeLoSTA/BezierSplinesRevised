package de.peterloos.beziersplines.activities;

import android.content.Context;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.ArrayList;

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

        // create adapter
        Context context = this.getApplicationContext();

//        String[] values = new String[] { "Android", "iPhone", "WindowsMobile",
//                "Blackberry", "WebOS", "Ubuntu", "Windows7", "Max OS X",
//                "Linux", "OS/2" };

        ControlPointsHolder holder = ControlPointsHolder.getInstance();

        ArrayList<String> resultt = holder.getAsListOfStrings();

        ConsoleArrayAdapter adapter = new ConsoleArrayAdapter (context, resultt);

        // TODO: Sonderfälle in der Ergebnisliste erstellen: Liste der Länge 0 und 1
        // Achtung: In der Liste der Länge 1 kommt das = nicht vor ?!?!?!

        listView.setAdapter(adapter);
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
