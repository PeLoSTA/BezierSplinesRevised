package de.peterloos.beziersplines.activities;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.peterloos.beziersplines.BezierGlobals;
import de.peterloos.beziersplines.utils.BezierMode;
import de.peterloos.beziersplines.utils.BezierPoint;
import de.peterloos.beziersplines.R;
import de.peterloos.beziersplines.utils.BezierUtils;
import de.peterloos.beziersplines.utils.SharedPreferencesUtils;
import de.peterloos.beziersplines.views.BezierGridView;
import de.peterloos.beziersplines.views.BezierListener;
import de.peterloos.beziersplines.views.BezierView;

/**
 * Project: Bézier Splines Simulation
 * Copyright (c) 2017 by PeLo on 23.01.2017. All rights reserved.
 * Contact info: peterloos@gmx.de
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

    // TODO: Das muss irgendwie in einer Demo-Klasse !!!
    // TODO: Oder anders herum: Warum sind da unten mehrere unused Methoden ?!?!?!?
    private List<BezierPoint> demoControlPoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Log.v(BezierGlobals.TAG, "DemonstrationActivity::onCreate");

        this.viewWidth = -1;
        this.viewHeight = -1;

        // retrieve control references
        this.bezierViewWithGrid = (BezierGridView) this.findViewById(R.id.bezier_view_demo);
        this.bezierViewWithGrid.clear();

        // prefer action bar title with two lines
        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.title_activity_demo);
            actionBar.setSubtitle(this.getString(R.string.app_main_title));
        }

        // TODO: DAS MUSS ANGEPASST WERDEN !!!!!!!!!!!!!!!!
        // this.bezierView.setDensityOfGridlines(2);

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

        this.demoControlPoints = new ArrayList<>();
        this.task = null;

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
                BezierUtils.calculateCellLengths(dm, width, height);

                DemonstrationActivity.this.bezierViewWithGrid.setDensityOfGridlines(1);

                String info =
                        String.format(Locale.getDefault(),
                        "DemonstrationActivity: Size in Pixel: -------------> %d, %d",
                        DemonstrationActivity.this.viewWidth, DemonstrationActivity.this.viewHeight);
                Log.v(BezierGlobals.TAG, info);

                DemonstrationActivity.this.task = new DemoOperation();
                DemonstrationActivity.this.task.setRunning(true);

                // TODO: Das muss natürlich wieder weg
                // DemonstrationActivity.this.task.execute();
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
                // up arrow in action bar clicked - goto main activity
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // private helper methods
    @SuppressWarnings("unused")
    private void computeDemoControlPoints() {

        if (this.viewWidth == -1 || this.viewHeight == -1)
            return;

        int numEdges = 8;

        float deltaX = this.viewWidth / (float) numEdges;
        float deltaY = this.viewHeight / (float) numEdges;

        float centerX = this.viewWidth / 2;
        float centerY = this.viewHeight / 2;

        this.demoControlPoints = BezierUtils.getDemoRectangle(centerX, centerY, deltaX, deltaY, numEdges - 1);
        this.task = new DemoOperation();
        this.task.setRunning(true);
        this.task.execute();
    }

    @SuppressWarnings("unused")
    private void computeControlPointsTest_VariantWithCircle() {

        if (this.viewWidth == -1 || this.viewHeight == -1)
            return;

        int squareLength = (this.viewWidth < this.viewHeight) ? this.viewWidth : this.viewHeight;

        Resources res = this.getResources();
        float offsetFromBorder = BezierView.convertDpToPixel(res, OffsetFromBorderDp);
        float radius = squareLength / 2 - 2 * offsetFromBorder;

        float centerX = this.viewWidth / 2;
        float centerY = this.viewHeight / 2;

        float arcLength = (float) (2 * Math.PI / 40);

        this.demoControlPoints =
                BezierUtils.getDemo_SingleCircleOppositeConnected(
                        centerX,
                        centerY,
                        radius,
                        arcLength);

        this.task = new DemoOperation();
        this.task.setRunning(true);
        this.task.execute();
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

        private boolean running;

        private List<BezierPoint> demoPoints;

        public void setRunning(boolean running) {
            this.running = running;
        }

        @Override
        protected void onPreExecute() {

            int numEdges = 8;

            float deltaX = DemonstrationActivity.this.viewWidth / (float) numEdges;
            float deltaY = DemonstrationActivity.this.viewHeight / (float) numEdges;

            float centerX = DemonstrationActivity.this.viewWidth / 2;
            float centerY = DemonstrationActivity.this.viewHeight / 2;

            // TODO: DA FEHLT DAS SNAPPING NOCH ?!?!?!?!?!?

            this.demoPoints = BezierUtils.getDemoRectangle(centerX, centerY, deltaX, deltaY, numEdges - 1);
        }

        @Override
        protected Void doInBackground(Void... voids) {

            for (int i = 0; i < this.demoPoints.size(); i++) {

                if (!this.running)
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

                if (!this.running)
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

                if (!this.running)
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

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {

        if (this.task != null) {
            this.task.setRunning(false);
            this.task = null;
        }

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
}

// ==========================================================================================

//class DemonstrationActivity2
//        extends AppCompatActivity
//        implements OnClickListener, BezierListener {
//
//    private static final int DemoViewResolution = 200; // resolution of demonstration view
//    private static final int TaskDelay = 30; // animation velocity
//    private static final float OffsetFromBorderDp = 15F; // density-independent pixels: offset from border
//
//    // private DemonstrationActivity.DemoOperation task;
//    private DemoOperation task;
//
//    // size of bezier view(s) - class Size only at API level 21 supported
//    private int viewWidth;
//    private int viewHeight;
//
//    private BezierGridView bezierView;
//    private Button buttonStop;
//    private Button buttonRestart;
//    private TextView textViewResolution;
//    private TextView textViewT;
//
//    private List<BezierPoint> demoControlPoints;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_demo);
//        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//
//        // prefer action bar title with two lines
//        ActionBar actionBar = this.getSupportActionBar();
//        if (actionBar != null) {
//            actionBar.setTitle(R.string.title_activity_demo);
//            actionBar.setSubtitle(this.getString(R.string.app_main_title));
//        }
//
//        // retrieve control references
//        this.bezierView = (BezierGridView) this.findViewById(R.id.bezier_view_demo);
//
//        // connect event sink with client
//        // (both views - with and without grid - have same size, one sink is therefore sufficient)
//        this.bezierView.registerListener(this);
//
//        // TODO: DAS MUSS ANGEPASST WERDEN !!!!!!!!!!!!!!!!
//        // this.bezierView.setDensityOfGridlines(2);
//
//        this.textViewResolution = (TextView) this.findViewById(R.id.textview_resolution);
//        this.textViewT = (TextView) this.findViewById(R.id.textview_t);
//        this.buttonStop = (Button) this.findViewById(R.id.button_stop);
//        this.buttonRestart = (Button) this.findViewById(R.id.button_restart);
//
//        // connect with event handlers
//        this.buttonStop.setOnClickListener(this);
//        this.buttonRestart.setOnClickListener(this);
//
//        // initialize bezier view
//        this.bezierView.setMode(BezierMode.Demo);
//        this.bezierView.setResolution(DemoViewResolution);
//        this.bezierView.setT(0);
//        this.bezierView.setShowConstruction(true);
//
//        // retrieve shared preferences
//        Context context = this.getApplicationContext();
//        int strokewidthFactor = SharedPreferencesUtils.getPersistedStrokewidthFactor(context);
//        this.bezierView.setStrokewidthFactor(strokewidthFactor);
//
//        // initialize controls
//        String resolution = String.format(Locale.getDefault(), "%d", DemoViewResolution);
//        this.textViewResolution.setText(resolution);
//        String t = String.format(Locale.getDefault(), "%1.2f", 0.0);
//        this.textViewT.setText(t);
//
//        this.viewWidth = -1;
//        this.viewHeight = -1;
//
//        this.demoControlPoints = new ArrayList<>();
//        this.task = null;
//
////        ViewTreeObserver observer = this.bezierView.getViewTreeObserver();
////        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
////            @Override
////            public void onGlobalLayout() {
////
////                if (DemonstrationActivity.this.width == -1 && DemonstrationActivity.this.bezierView.getWidth() > 0) {
////
////                    /*
////                     * first invocation with (hopefully) final layout data
////                     */
////                    DemonstrationActivity.this.viewWidth = DemonstrationActivity.this.bezierView.getWidth();
////                    DemonstrationActivity.this.viewHeight = DemonstrationActivity.this.bezierView.getHeight();
////
////                    DemonstrationActivity.this.computeDemoControlPoints();
////                    // DemonstrationActivity.this.computeControlPointsTest_VariantWithCircle();
////                }
////            }
////        });
//    }
//
//    // private helper methods
//    @SuppressWarnings("unused")
//    private void computeDemoControlPoints() {
//
//        if (this.viewWidth == -1 || this.viewHeight == -1)
//            return;
//
//        int numEdges = 8;
//
//        float deltaX = this.viewWidth / (float) numEdges;
//        float deltaY = this.viewHeight / (float) numEdges;
//
//        float centerX = this.viewWidth / 2;
//        float centerY = this.viewHeight / 2;
//
//        this.demoControlPoints = BezierUtils.getDemoRectangle(centerX, centerY, deltaX, deltaY, numEdges - 1);
//        this.task = new DemoOperation();
//        this.task.setRunning(true);
//        this.task.execute();
//    }
//
//    @SuppressWarnings("unused")
//    private void computeControlPointsTest_VariantWithCircle() {
//
//        if (this.viewWidth == -1 || this.viewHeight == -1)
//            return;
//
//        int squareLength = (this.viewWidth < this.viewHeight) ? this.viewWidth : this.viewHeight;
//
//        Resources res = this.getResources();
//        float offsetFromBorder = BezierView.convertDpToPixel(res, OffsetFromBorderDp);
//        float radius = squareLength / 2 - 2 * offsetFromBorder;
//
//        float centerX = this.viewWidth / 2;
//        float centerY = this.viewHeight / 2;
//
//        float arcLength = (float) (2 * Math.PI / 40);
//
//        DemonstrationActivity.this.demoControlPoints =
//                BezierUtils.getDemo_SingleCircleOppositeConnected(
//                        centerX,
//                        centerY,
//                        radius,
//                        arcLength);
//
//        DemonstrationActivity.this.task = new DemoOperation();
//        DemonstrationActivity.this.task.setRunning(true);
//        DemonstrationActivity.this.task.execute();
//    }
//
//    @Override
//    public void onClick(View view) {
//
//        if (view == this.buttonStop) {
//
//            if (this.task != null) {
//
//                this.task.setRunning(false);
//                this.task = null;
//            }
//        } else if (view == this.buttonRestart) {
//
//            if (this.task == null) {
//
//                this.bezierView.clear();
//                this.bezierView.setT(0);
//                String t = String.format(Locale.getDefault(), "%1.2f", 0.0);
//                this.textViewT.setText(t);
//                this.task = new DemonstrationActivity.DemoOperation();
//                this.task.setRunning(true);
//                this.task.execute();
//            }
//        }
//    }
//
//    /*
//     * implementation of interface 'BezierListener'
//     */
//
//    @Override
//    public void setInfo(String info) {
//
//    }
//
//    @Override
//    public void setSize(int width, int height) {
//
//        this.viewWidth = width;
//        this.viewHeight = height;
//
//        // TODO: Das muss natürlich noch richtig berechnet werden !!!
//        // Die 100 ist nur zum Testen
//        this.bezierView.setCellLength(100);
//
//        this.computeDemoControlPoints();
//        // DemonstrationActivity.this.computeControlPointsTest_VariantWithCircle();
//
//        if (this.viewWidth == -1 || this.viewHeight == -1)
//            return;
//
////        int numEdges = 8;
////
////        float deltaX = this.viewWidth / (float) numEdges;
////        float deltaY = this.viewHeight / (float) numEdges;
////
////        float centerX = this.viewWidth / 2;
////        float centerY = this.viewHeight / 2;
//
//        // TODO: DA FEHLT DAS SNAPPING NOCH ?!?!?!?!?!?
//
////        List<BezierPoint> demoPoints = BezierUtils.getDemoRectangle(centerX, centerY, deltaX, deltaY, numEdges - 1);
////        this.bezierView.setControlPoints (demoPoints);
//
//        this.task = new DemoOperation();
//        this.task.setRunning(true);
//        this.task.execute();
//
//
//
//    }
//
//    @Override
//    public void changeMode(BezierMode mode) {
//
//    }
//
//    private class DemoOperation extends AsyncTask<Void, UpdateDescriptor, Void> {
//
//        private boolean running;
//
//        private List<BezierPoint> demoPoints;
//
//        public void setRunning(boolean running) {
//            this.running = running;
//        }
//
//        @Override
//        protected void onPreExecute() {
//
//            int numEdges = 8;
//
//            float deltaX = DemonstrationActivity.this.viewWidth / (float) numEdges;
//            float deltaY = DemonstrationActivity.this.viewHeight / (float) numEdges;
//
//            float centerX = DemonstrationActivity.this.viewWidth / 2;
//            float centerY = DemonstrationActivity.this.viewHeight / 2;
//
//            this.demoPoints = BezierUtils.getDemoRectangle(centerX, centerY, deltaX, deltaY, numEdges - 1);
//        }
//
//        @Override
//        protected Void doInBackground(Void... voids) {
//
//            for (int i = 0; i < this.demoPoints.size(); i++) {
//
//                if (!this.running)
//                    return null;
//
//                try {
//                    Thread.sleep(10 * TaskDelay);
//
//                    BezierPoint p = DemonstrationActivity.this.demoControlPoints.get(i);
//                    UpdateDescriptor dsc = new UpdateDescriptor(p, (float) 0.0, true, false);
//                    this.publishProgress(dsc);
//
//                } catch (InterruptedException e) {
//                    Thread.interrupted();
//                }
//            }
//
//            for (int i = 0; i <= 100; i++) {
//
//                if (!this.running)
//                    return null;
//
//                try {
//                    Thread.sleep(TaskDelay);
//
//                    UpdateDescriptor dsc = new UpdateDescriptor(null, (float) 0.01 * i, false, true);
//                    this.publishProgress(dsc);
//
//                } catch (InterruptedException e) {
//                    Thread.interrupted();
//                }
//            }
//
//            for (int i = 100; i >= 0; i--) {
//
//                if (!this.running)
//                    return null;
//
//                try {
//                    Thread.sleep(TaskDelay);
//
//                    UpdateDescriptor dsc = new UpdateDescriptor(null, (float) 0.01 * i, false, true);
//                    this.publishProgress(dsc);
//
//                } catch (InterruptedException e) {
//                    Thread.interrupted();
//                }
//            }
//
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void voids) {
//
//            // enable another demo to run ...
//            DemonstrationActivity.this.task = null;
//        }
//
//        @Override
//        protected void onProgressUpdate(UpdateDescriptor... values) {
//
//            if (values.length == 1) {
//
//                UpdateDescriptor dsc = values[0];
//
//                if (dsc.isAddPoint()) {
//                    DemonstrationActivity.this.bezierView.addControlPoint(dsc.getP());
//                } else if (dsc.isChangeT()) {
//                    DemonstrationActivity.this.bezierView.setT(dsc.getT());
//                    String t = String.format(Locale.getDefault(), "%1.2f", dsc.getT());
//                    DemonstrationActivity.this.textViewT.setText(t);
//                }
//            }
//        }
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//    }
//
//    @Override
//    protected void onStop() {
//
//        if (this.task != null) {
//            this.task.setRunning(false);
//            this.task = null;
//        }
//
//        super.onStop();
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//    }
//}
//
