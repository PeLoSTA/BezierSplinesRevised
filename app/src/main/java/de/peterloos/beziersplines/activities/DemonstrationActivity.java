package de.peterloos.beziersplines.activities;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import de.peterloos.beziersplines.BezierGlobals;
import de.peterloos.beziersplines.utils.BezierMode;
import de.peterloos.beziersplines.utils.BezierPoint;
import de.peterloos.beziersplines.R;
import de.peterloos.beziersplines.utils.BezierUtils;
import de.peterloos.beziersplines.utils.ControlPointsCalculator;
import de.peterloos.beziersplines.utils.ControlPointsHolder;
import de.peterloos.beziersplines.utils.SharedPreferencesUtils;
import de.peterloos.beziersplines.views.BezierGridView;
import de.peterloos.beziersplines.views.BezierListener;

/**
 * Project: Bézier Splines Simulation
 * Copyright (c) 2017 by PeLo on 23.01.2017. All rights reserved.
 * Contact info: peter.loos@gmx.de
 */

public class DemonstrationActivity
        extends AppCompatActivity
        implements OnClickListener {

    private static final int DemoViewResolution = 200; // resolution of demonstration view
    private static final int TaskDelay = 30; // animation velocity
    private static final float OffsetFromBorderDp = 15F; // density-independent pixels: offset from border

    private DemoOperation task;

    // size of underlying bezier view(s) (data type Size only at API level 21 supported)
    private int viewWidth;
    private int viewHeight;

    private BezierGridView bezierViewWithGrid;
    private Button buttonStop;
    private Button buttonRestart;
    private TextView textViewResolution;
    private TextView textViewT;

    private ControlPointsHolder holder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Log.v(BezierGlobals.TAG, "DemonstrationActivity::onCreate");

        this.viewWidth = -1;
        this.viewHeight = -1;

        // prefer action bar title with two lines
        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.title_activity_demo);
            actionBar.setSubtitle(this.getString(R.string.app_main_title));
        }

        // retrieve control references
        this.bezierViewWithGrid = (BezierGridView) this.findViewById(R.id.bezier_view_demo);
        this.textViewResolution = (TextView) this.findViewById(R.id.textview_resolution);
        this.textViewT = (TextView) this.findViewById(R.id.textview_t);
        this.buttonStop = (Button) this.findViewById(R.id.button_stop);
        this.buttonRestart = (Button) this.findViewById(R.id.button_restart);

        // connect with event handlers
        this.buttonStop.setOnClickListener(this);
        this.buttonRestart.setOnClickListener(this);

        // initialize bezier view
        this.bezierViewWithGrid.setMode(BezierMode.Demo);
        this.bezierViewWithGrid.setResolution(DemoViewResolution);
        this.bezierViewWithGrid.setT(0);
        this.bezierViewWithGrid.setShowConstruction(true);

        // retrieve shared preferences
        Context context = this.getApplicationContext();
        int strokewidthFactor = SharedPreferencesUtils.getPersistedStrokewidthFactor(context);
        this.bezierViewWithGrid.setStrokewidthFactor(strokewidthFactor);

        // initialize controls
        String resolution = String.format(Locale.getDefault(), "%d", DemoViewResolution);
        this.textViewResolution.setText(resolution);
        String t = String.format(Locale.getDefault(), "%1.2f", 0.0);
        this.textViewT.setText(t);

        this.task = null;

        // switch control points container to demo mode
        this.holder = ControlPointsHolder.getInstance();
        this.holder.setDemoMode();
        this.holder.clear();

        // connect event sink with client
        this.bezierViewWithGrid.registerListener(new BezierListener() {
            @Override
            public void setInfo(String info) {
            }

            @Override
            public void setSize(int width, int height) {

                DemonstrationActivity.this.viewWidth = width;
                DemonstrationActivity.this.viewHeight = height;

                // retrieve display metrics that describe the size and density of this display
                DisplayMetrics dm = new DisplayMetrics();
                DemonstrationActivity.this.getWindowManager().getDefaultDisplay().getMetrics(dm);

                // calculate some cell lengths (according to unit 'cm')
                BezierUtils.calculateDeviceIndependentMetrics(dm, width, height);
                DemonstrationActivity.this.bezierViewWithGrid.setDensityOfGridlines(BezierGlobals.GridlineIndexHigh);

                String info =
                        String.format(Locale.getDefault(),
                                "DemonstrationActivity: Size in Pixel: -------------> %d, %d",
                                DemonstrationActivity.this.viewWidth, DemonstrationActivity.this.viewHeight);
                Log.v(BezierGlobals.TAG, info);

                DemonstrationActivity.this.holder.setDemoSpline(width, height);

                DemonstrationActivity.this.task = new DemoOperation();
                DemonstrationActivity.this.task.setRunning(true);
                DemonstrationActivity.this.task.execute();
            }

            @Override
            public void changeMode(BezierMode mode) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // want 'up' button to have the same functionality as the 'back' button;
        // therefore I'm overriding onOptionsItemSelected and just 'finish' the current activity
        switch (item.getItemId()) {
            case android.R.id.home:

                // cancel current task, if any
                if (this.task != null) {
                    this.task.setRunning(false);
                }

                // up arrow in action bar clicked - goto main activity
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * called when user press the back button
     */
    @Override
    public void onBackPressed() {
        if (this.task != null) {
            this.task.setRunning(false);
            this.task = null;
        }

        super.onBackPressed();
    }

    @Override
    public void onClick(View view) {

        if (view == this.buttonStop) {

            if (this.task != null) {

                this.task.setRunning(false);
                this.task = null;
            }
        } else if (view == this.buttonRestart) {

            if (this.task == null) {

                this.bezierViewWithGrid.clear();
                this.bezierViewWithGrid.setT(0);
                String t = String.format(Locale.getDefault(), "%1.2f", 0.0);
                this.textViewT.setText(t);
                this.task = new DemoOperation();
                this.task.setRunning(true);
                this.task.execute();
            }
        }
    }

    private class DemoOperation extends AsyncTask<Void, UpdateDescriptor, Void> {

        /**
         * need thread safe access to 'running' state of async task (!)
         */
        private boolean running;

        public synchronized void setRunning(boolean running) {
            this.running = running;
        }

        public synchronized boolean isRunning() {
            return this.running;
        }

        private List<BezierPoint> demoPoints;

        @Override
        protected void onPreExecute() {

            // retrieve demo points (demo or for screen shot purposes)
            this.demoPoints = ControlPointsCalculator.getControlPointsForScreenshot(
                    ControlPointsCalculator.SCREENSHOT_CASCADING_RECTANGLES,
                    DemonstrationActivity.this.viewWidth,
                    DemonstrationActivity.this.viewHeight);
        }

        @Override
        protected Void doInBackground(Void... voids) {

            for (int i = 0; i < this.demoPoints.size(); i++) {

                if (!this.isRunning())
                    return null;

                try {
                    Thread.sleep(10 * TaskDelay);

                    BezierPoint p = this.demoPoints.get(i);
                    UpdateDescriptor dsc = new UpdateDescriptor(p, (float) 0.0, true, false);
                    this.publishProgress(dsc);

                } catch (InterruptedException e) {
                    Thread.interrupted();
                }
            }

            for (int i = 0; i <= 100; i++) {

                if (!this.isRunning())
                    return null;

                try {
                    Thread.sleep(TaskDelay);

                    UpdateDescriptor dsc = new UpdateDescriptor(null, (float) 0.01 * i, false, true);
                    this.publishProgress(dsc);

                } catch (InterruptedException e) {
                    Thread.interrupted();
                }
            }

            for (int i = 100; i >= 0; i--) {

                if (!this.isRunning())
                    return null;

                try {
                    Thread.sleep(TaskDelay);

                    UpdateDescriptor dsc = new UpdateDescriptor(null, (float) 0.01 * i, false, true);
                    this.publishProgress(dsc);

                } catch (InterruptedException e) {
                    Thread.interrupted();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void voids) {

            // enable another demo to run ...
            DemonstrationActivity.this.task = null;
        }

        @Override
        protected void onProgressUpdate(UpdateDescriptor... values) {

            // ignore late arriving progress updates
            if (!isRunning()) {

                Log.v(BezierGlobals.TAG, "info: ignored late arriving progress updates");
                return;
            }

            if (values.length == 1) {

                UpdateDescriptor dsc = values[0];

                if (dsc.isAddPoint()) {

                    DemonstrationActivity.this.bezierViewWithGrid.addControlPoint(dsc.getP());
                } else if (dsc.isChangeT()) {

                    DemonstrationActivity.this.bezierViewWithGrid.setT(dsc.getT());
                    String t = String.format(Locale.getDefault(), "%1.2f", dsc.getT());
                    Log.v(BezierGlobals.TAG, t);
                    DemonstrationActivity.this.textViewT.setText(t);
                }
            }
        }
    }

    class UpdateDescriptor {

        private boolean addPoint;
        private boolean changeT;
        private BezierPoint p;
        private float t;

        public UpdateDescriptor(BezierPoint p, float t, boolean addPoint, boolean changeT) {
            this.p = p;
            this.t = t;
            this.addPoint = addPoint;
            this.changeT = changeT;
        }

        public BezierPoint getP() {
            return this.p;
        }

        public float getT() {
            return this.t;
        }

        public boolean isAddPoint() {
            return this.addPoint;
        }

        public boolean isChangeT() {
            return this.changeT;
        }
    }

    /*
     * lifecycle methods
     */

    @Override
    protected void onResume() {

        Log.v(BezierGlobals.TAG, "DemonstrationActivity::onResume");
        super.onResume();
    }

    @Override
    protected void onPause() {

        Log.v(BezierGlobals.TAG, "DemonstrationActivity::onPause");

        if (this.task != null) {
            this.task.setRunning(false);
            this.task = null;
        }

        super.onPause();
    }

    @Override
    protected void onDestroy() {

        // main activity is going to be destroyed
        Log.v(BezierGlobals.TAG, "DemonstrationActivity::onDestroy");
        super.onDestroy();
    }

    @Override
    protected void onStart() {

        Log.v(BezierGlobals.TAG, "DemonstrationActivity::onStart");
        super.onStart();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        Log.v(BezierGlobals.TAG, "DemonstrationActivity::onSaveInstanceState");
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {

        Log.v(BezierGlobals.TAG, "DemonstrationActivity::onRestoreInstanceState");
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onStop() {

        Log.v(BezierGlobals.TAG, "DemonstrationActivity::onStop");
        super.onStop();
    }
}
