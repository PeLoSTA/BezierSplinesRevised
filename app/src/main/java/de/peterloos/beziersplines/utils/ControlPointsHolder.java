package de.peterloos.beziersplines.utils;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import de.peterloos.beziersplines.BezierGlobals;

/**
 * Project: Bézier Splines Simulation
 * Copyright (c) 2017 by PeLo on 23.01.2017. All rights reserved.
 * Contact info: peter.loos@gmx.de
 */

public class ControlPointsHolder {

    private static ControlPointsHolder instance;

    static {
        instance = null;
    }

    // representing application global data
    private List<BezierPoint> userData;  /* current users beziér spline */
    private List<BezierPoint> demoData;  /* current demo beziér spline */
    private List<BezierPoint> data;      /* referencing users or demo spline */

    // restrict the constructor from being instantiated
    private ControlPointsHolder() {

        this.userData = new ArrayList<>();
        this.demoData = new ArrayList<>();
        this.data = this.userData;
    }

    public static synchronized ControlPointsHolder getInstance() {

        if (instance == null) {
            instance = new ControlPointsHolder();
        }

        return instance;
    }

    // =====================================================================

    // switching between user and demo mode
    public synchronized void setNormalMode() {
        Log.v(BezierGlobals.TAG, "ControlPointsHolder: set normal mode");
        this.data = this.userData;
    }

    public synchronized void setDemoMode() {
        Log.v(BezierGlobals.TAG, "ControlPointsHolder: set demonstration mode");
        this.data = this.demoData;
    }

    // =====================================================================

    /*
     *  public interface
     */

    public void clear() {
        this.data.clear();
    }

    public void add(BezierPoint p) {
        this.data.add(p);
    }

    public BezierPoint get(int index) {
        return this.data.get(index);
    }

    public void update(int index, BezierPoint p) {
        this.data.set(index, p);
    }

    public void remove(int index) {
        this.data.remove(index);
    }

    public int size() {
        return this.data.size();
    }

    public void toArray(BezierPoint[] src) {
        this.data.toArray(src);
    }

    public void snapAllPoints(double cellLength) {

        for (BezierPoint p : this.data) {
            BezierUtils.snapPoint(p, cellLength);
        }
    }

    public void setDemoSpline(float viewWidth, float viewHeight) {

        if (this.demoData != null && this.demoData.size() == 0) {
            this.demoData = ControlPointsCalculator.computeDemoRectangle(viewWidth, viewHeight);
        }
    }

    @SuppressWarnings("unused")
    public void setScreenshotSpline(int number, int viewWidth, int viewHeight) {

        List<BezierPoint> points =
                ControlPointsCalculator.getControlPointsForScreenshot(number, viewWidth, viewHeight);

        for (int i = 0; i < points.size(); i++) {

            this.add(points.get(i));
        }
    }
}
