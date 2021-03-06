package de.peterloos.beziersplines.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import java.util.Locale;

import de.peterloos.beziersplines.BezierGlobals;
import de.peterloos.beziersplines.R;
import de.peterloos.beziersplines.utils.BezierMode;
import de.peterloos.beziersplines.utils.BezierUtils;
import de.peterloos.beziersplines.utils.ControlPointsCalculator;
import de.peterloos.beziersplines.utils.ControlPointsHolder;
import de.peterloos.beziersplines.utils.SharedPreferencesUtils;
import de.peterloos.beziersplines.views.BezierGridView;
import de.peterloos.beziersplines.views.BezierListener;
import de.peterloos.beziersplines.views.BezierView;

/**
 * Project: Bézier Splines Simulation
 * Copyright (c) 2017 by PeLo on 23.01.2017. All rights reserved.
 * Contact info: peter.loos@gmx.de
 */

public class MainActivity
        extends AppCompatActivity
        implements OnClickListener, OnSeekBarChangeListener, OnItemSelectedListener {

    private static final int REQUESTCODE_SETTINGS = 1;

    // controls
    private ViewSwitcher viewSwitcher;
    private BezierView bezierViewWithoutGrid;
    private BezierGridView bezierViewWithGrid;
    private CheckBox checkboxConstruction;
    private CheckBox checkboxSnaptogrid;
    private TextView textViewInfo;
    private SeekBar seekBarResolution;
    private SeekBar seekBarT;
    private TextView textViewResolution;
    private TextView textViewT;
    private Spinner spinnerMode;
    private TableRow tableRowConstruction;

    // miscellaneous
    private String resultGridlines;
    private String resultStrokewidth;

    private int resolution;
    private boolean gridIsVisible;
    private boolean constructionIsVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);

        Log.v(BezierGlobals.TAG, "MainActivity::onCreate");

        // both portrait and landscape mode make this app more complicated
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // prefer action bar title with two lines
        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.app_main_title);
            actionBar.setSubtitle(this.getString(R.string.app_sub_title));
        }

        // miscellaneous
        this.resolution = 50;
        this.gridIsVisible = false;
        this.constructionIsVisible = false;

        // retrieve control references
        this.viewSwitcher = (ViewSwitcher) findViewById(R.id.viewswitcher);
        this.bezierViewWithoutGrid = (BezierView) this.findViewById(R.id.bezier_view_withoutgrid);
        this.bezierViewWithGrid = (BezierGridView) this.findViewById(R.id.bezier_view_withgrid);
        this.checkboxConstruction = (CheckBox) this.findViewById(R.id.checkbox_show_construction);
        this.checkboxSnaptogrid = (CheckBox) this.findViewById(R.id.checkbox_show_snaptogrid);
        this.textViewInfo = (TextView) this.findViewById(R.id.textview_info);
        this.seekBarResolution = (SeekBar) this.findViewById(R.id.seekbar_resolution);
        this.seekBarT = (SeekBar) this.findViewById(R.id.seekbar_t);
        this.textViewResolution = (TextView) this.findViewById(R.id.textview_resolution);
        this.textViewT = (TextView) this.findViewById(R.id.textview_t);
        this.spinnerMode = (Spinner) this.findViewById(R.id.spinner_editormode);
        this.tableRowConstruction = (TableRow) this.findViewById(R.id.t_seekbar);

        // connect with event handlers
        this.checkboxConstruction.setOnClickListener(this);
        this.checkboxSnaptogrid.setOnClickListener(this);
        this.seekBarResolution.setOnSeekBarChangeListener(this);
        this.seekBarT.setOnSeekBarChangeListener(this);
        this.spinnerMode.setOnItemSelectedListener(this);

        // reset controls into initial state
        this.seekBarResolution.setProgress(this.resolution);
        this.seekBarT.setProgress(50);
        this.bezierViewWithoutGrid.setShowConstruction(false);
        this.bezierViewWithGrid.setShowConstruction(false);

        this.checkboxConstruction.setChecked(this.constructionIsVisible);
        this.checkboxSnaptogrid.setChecked(this.gridIsVisible);
        this.tableRowConstruction.setVisibility(View.GONE);

        // read language independent strings for settings activity result handshake
        Resources res = this.getResources();
        this.resultGridlines = res.getString(R.string.result_gridlines);
        this.resultStrokewidth = res.getString(R.string.result_strokewidth);

        // sync shared preferences settings with bezier view
        Context context = this.getApplicationContext();
        int strokeWidthFactor = SharedPreferencesUtils.getPersistedStrokewidthFactor(context);
        this.bezierViewWithoutGrid.setStrokewidthFactor(strokeWidthFactor);
        this.bezierViewWithGrid.setStrokewidthFactor(strokeWidthFactor);

        // read shared preferences (gridlines factor)
        final int gridlinesFactor = SharedPreferencesUtils.getPersistedGridlinesFactor(context);
        this.bezierViewWithGrid.setDensityOfGridlines(gridlinesFactor);

        // connect event sinks with client
        this.bezierViewWithGrid.registerListener(new BezierListener() {
            @Override
            public void setInfo(String info) {
                MainActivity.this.textViewInfo.setText(info);
            }

            @Override
            public void setSize(int width, int height) {

                // set size of underlying bezier view
                BezierUtils.setViewWidth(width);
                BezierUtils.setViewHeight(height);

                // retrieve display metrics that describe the size and density of this display
                DisplayMetrics dm = new DisplayMetrics();
                MainActivity.this.getWindowManager().getDefaultDisplay().getMetrics(dm);

                // calculate cell lengths (according to unit 'cm')
                BezierUtils.calculateDeviceIndependentMetrics(dm, width, height);
                MainActivity.this.bezierViewWithGrid.setDensityOfGridlines(gridlinesFactor);

                float dipDistanceMaximum = BezierUtils.getDipDistanceMaximum();
                MainActivity.this.bezierViewWithGrid.setDipDistanceMaximum(dipDistanceMaximum);
                MainActivity.this.bezierViewWithoutGrid.setDipDistanceMaximum(dipDistanceMaximum);
            }

            @Override
            public void changeMode(BezierMode mode) {
                // should be only called with requested mode 'BezierMode.Create'
                MainActivity.this.spinnerMode.setSelection(0);
            }
        });

        this.bezierViewWithoutGrid.registerListener(new BezierListener() {
            @Override
            public void setInfo(String info) {
                MainActivity.this.textViewInfo.setText(info);
            }

            @Override
            public void setSize(int width, int height) {
                // nothing to do
            }

            @Override
            public void changeMode(BezierMode mode) {
                // should be only called with requested mode 'BezierMode.Create'
                MainActivity.this.spinnerMode.setSelection(0);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = this.getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        Context currentContext = this.getApplicationContext();

        if (id == R.id.menu_action_settings) {
            Intent settingsIntent = new Intent(currentContext, SettingsActivity.class);
            this.startActivityForResult(settingsIntent, REQUESTCODE_SETTINGS);
        } else if (id == R.id.menu_action_demo) {
            Intent demoIntent = new Intent(currentContext, DemonstrationActivity.class);
            this.startActivity(demoIntent);
        } else if (id == R.id.menu_action_about) {
            Intent demoIntent = new Intent(currentContext, AboutActivity.class);
            this.startActivity(demoIntent);
        } else if (id == R.id.menu_action_docs) {
            Intent demoIntent = new Intent(currentContext, DocumentationActivity.class);
            this.startActivity(demoIntent);
        } else if (id == R.id.menu_action_store) {
            this.saveSpline();
        } else if (id == R.id.menu_action_load) {
            this.loadSpline();
        } else if (id == R.id.menu_action_console) {
            Intent demoIntent = new Intent(currentContext, ConsoleActivity.class);
            this.startActivity(demoIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUESTCODE_SETTINGS) {
            if (resultCode == RESULT_OK) {

                int gridlinesFactor = data.getIntExtra(this.resultGridlines, -1);
                int strokewidthFactor = data.getIntExtra(this.resultStrokewidth, -1);

                if (gridlinesFactor != -1) {
                    this.bezierViewWithGrid.setDensityOfGridlines(gridlinesFactor);
                }

                if (strokewidthFactor != -1) {
                    this.bezierViewWithoutGrid.setStrokewidthFactor(strokewidthFactor);
                    this.bezierViewWithGrid.setStrokewidthFactor(strokewidthFactor);
                }
            } else if (resultCode == RESULT_CANCELED) {
                Log.v(BezierGlobals.TAG, "onActivityResult -> RESULT_CANCELED");
            }
        }
    }

    /*
     * implementing interface 'View.OnClickListener'
     */

    @Override
    public void onClick(View view) {

        if (view == this.checkboxConstruction) {
            if (this.checkboxConstruction.isChecked()) {
                this.constructionIsVisible = true;
                this.bezierViewWithoutGrid.setShowConstruction(true);
                this.bezierViewWithGrid.setShowConstruction(true);
                this.tableRowConstruction.setVisibility(View.VISIBLE);
            } else {
                this.constructionIsVisible = false;
                this.bezierViewWithoutGrid.setShowConstruction(false);
                this.bezierViewWithGrid.setShowConstruction(false);
                this.tableRowConstruction.setVisibility(View.GONE);
            }
        } else if (view == this.checkboxSnaptogrid) {
            if (this.checkboxSnaptogrid.isChecked()) {
                this.gridIsVisible = true;
                this.bezierViewWithGrid.snapAllPoints();
                this.viewSwitcher.showNext();

            } else {
                this.gridIsVisible = false;
                this.viewSwitcher.showPrevious();
            }
        }
    }

    /*
     * implementing interface 'SeekBar.OnSeekBarChangeListener'
     */

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

        if (seekBar == this.seekBarResolution) {

            // don't allow value of zero for resolution seekbar
            if (i == 0)
                i = 1;

            this.resolution = i;
            String s = Integer.toString(this.resolution);
            this.textViewResolution.setText(s);
            this.bezierViewWithoutGrid.setResolution(this.resolution);
            this.bezierViewWithGrid.setResolution(this.resolution);
        } else if (seekBar == this.seekBarT) {

            float constructionPosition = (float) 0.01 * i;
            String pos = String.format(Locale.getDefault(), "%.2f", constructionPosition);
            this.textViewT.setText(pos);
            this.bezierViewWithoutGrid.setT(constructionPosition);
            this.bezierViewWithGrid.setT(constructionPosition);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    /*
     * implementing interface 'AdapterView.OnItemSelectedListener'
     */

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        Log.v(BezierGlobals.TAG, "onItemSelected: " + Integer.toString(i) + ", " + Long.toString(l));

        ControlPointsHolder holder = ControlPointsHolder.getInstance();
        holder.setNormalMode();

        switch (i) {
            case 0:  // create mode
                this.bezierViewWithoutGrid.setMode(BezierMode.Create);
                this.bezierViewWithGrid.setMode(BezierMode.Create);
                break;

            case 1:  // edit mode
                this.bezierViewWithoutGrid.setMode(BezierMode.Edit);
                this.bezierViewWithGrid.setMode(BezierMode.Edit);
                break;

            case 2:  // delete single mode
                this.bezierViewWithoutGrid.setMode(BezierMode.Delete);
                this.bezierViewWithGrid.setMode(BezierMode.Delete);
                break;

            case 3:  // delete all mode
                this.bezierViewWithoutGrid.clear();
                this.bezierViewWithGrid.clear();
                this.bezierViewWithoutGrid.setMode(BezierMode.Create);
                this.bezierViewWithGrid.setMode(BezierMode.Create);
                this.seekBarResolution.setProgress(50);
                this.seekBarT.setProgress(50);
                this.spinnerMode.setSelection(0);
                break;

            case 4:  // demo mode
                this.bezierViewWithoutGrid.setMode(BezierMode.Demo);
                this.bezierViewWithGrid.setMode(BezierMode.Demo);
                holder.setScreenshotSpline(
                        ControlPointsCalculator.SCREENSHOT_NICE_FIGURE_02,
                        BezierUtils.getViewWidth(),
                        BezierUtils.getViewHeight());
                this.bezierViewWithoutGrid.invalidate();
                this.bezierViewWithGrid.invalidate();
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        Log.v(BezierGlobals.TAG, "onNothingSelected");
    }

    /*
     * persistence handling of current spline
     */

    private void saveSpline() {

        ControlPointsHolder holder = ControlPointsHolder.getInstance();
        String json = holder.getAsJSON();
        Log.v(BezierGlobals.TAG, "Saving JSON:");
        Log.v(BezierGlobals.TAG, json);
        Context context = this.getApplicationContext();
        SharedPreferencesUtils.persistSpline(context, json);

        Resources res = this.getResources();
        String msg = res.getString(R.string.saving_bezier_sline);
        String info = String.format(msg, holder.size());
        Toast.makeText(context, info, Toast.LENGTH_SHORT).show();
    }

    private void loadSpline() {

        Context context = this.getApplicationContext();
        String jsonStr = SharedPreferencesUtils.getPersistedSpline(context);
        Log.v(BezierGlobals.TAG, "Loading JSON:");
        Log.v(BezierGlobals.TAG, jsonStr);
        ControlPointsHolder holder = ControlPointsHolder.getInstance();
        holder.setAsJSON(jsonStr);

        // snap points, if necessary, and force a redraw on both views
        if (this.gridIsVisible) {
            this.bezierViewWithGrid.snapAllPoints();
        }

        this.bezierViewWithGrid.invalidate();
        this.bezierViewWithoutGrid.invalidate();

        Resources res = this.getResources();
        String msg = res.getString(R.string.loading_bezier_sline);
        String info = String.format(msg, holder.size());
        Toast.makeText(context, info, Toast.LENGTH_SHORT).show();
    }

     /*
     * lifecycle methods
     */

    @Override
    public void onBackPressed() {

        Log.v(BezierGlobals.TAG, "MainActivity::onBackPressed");
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // being called especially, when a second activity (e.g. settings) is finished
        Log.v(BezierGlobals.TAG, "MainActivity::onResume");

        // switch control points container back to users mode
        ControlPointsHolder holder = ControlPointsHolder.getInstance();
        holder.setNormalMode();

        this.bezierViewWithoutGrid.invalidate();
        this.bezierViewWithGrid.invalidate();
    }

    @Override
    protected void onPause() {

        Log.v(BezierGlobals.TAG, "MainActivity::onPause");
        super.onPause();
    }

    @Override
    protected void onDestroy() {

        // main activity is going to be destroyed
        Log.v(BezierGlobals.TAG, "MainActivity::onDestroy");

        // need to clear current list of controls points - on restart the main canvas should be empty
        ControlPointsHolder holder = ControlPointsHolder.getInstance();
        holder.clear();

        super.onDestroy();
    }

    @Override
    protected void onStart() {

        Log.v(BezierGlobals.TAG, "MainActivity::onStart");
        super.onStart();
    }

    @Override
    protected void onStop() {

        Log.v(BezierGlobals.TAG, "MainActivity::onStop");
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        Log.v(BezierGlobals.TAG, "MainActivity::onSaveInstanceState");
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {

        Log.v(BezierGlobals.TAG, "MainActivity::onRestoreInstanceState");
        super.onRestoreInstanceState(savedInstanceState);
    }
}
