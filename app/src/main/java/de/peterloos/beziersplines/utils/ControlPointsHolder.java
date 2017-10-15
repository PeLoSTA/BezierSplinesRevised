package de.peterloos.beziersplines.utils;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.peterloos.beziersplines.BezierGlobals;

/**
 * Project: Bézier Splines Simulation
 * Copyright (c) 2017 by PeLo on 23.01.2017. All rights reserved.
 * Contact info: peter.loos@gmx.de
 */

public class ControlPointsHolder {

    // =====================================================================

    // implementation of 'singleton pattern'
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

    // bézier console method(s)
    public ArrayList<String> getAsListOfStrings() {

        ArrayList<String> result = new ArrayList<String>();
        for (int i = 0; i < this.size(); i++) {
            BezierPoint p = this.get(i);
            String s = String.format(Locale.getDefault(), "%.4f-%.4f", p.getX(), p.getY());
            result.add(s);
        }

        return result;
    }

    // =====================================================================

    // persistence methods (JSON)
    public String getAsJSON() {

        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < this.size(); i++) {

            BezierPoint point = this.get(i);
            JSONObject jsonObject = new JSONObject();

            try {
                jsonObject.put("x", point.getX());
                jsonObject.put("y", point.getY());
            } catch (JSONException e) {
                Log.v(BezierGlobals.TAG, "ControlPointsHolder::Internal Error: getAsJSON  failed !");
            }

            jsonArray.put(jsonObject);
        }

        return jsonArray.toString();
    }

    public boolean setAsJSON(String json) {

        List<BezierPoint> tmp = new ArrayList<>();

        try {
            JSONArray jsonArray = new JSONArray(json);

            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject jsonobject = jsonArray.getJSONObject(i);
                String sx = jsonobject.getString("x");
                String sy = jsonobject.getString("y");
                Log.v(BezierGlobals.TAG, "     found x=" + sx + ", x=" + sy);
                float x = Float.parseFloat(sx);
                float y = Float.parseFloat(sy);
                BezierPoint p = new BezierPoint(x, y);
                tmp.add(p);
            }

        } catch (JSONException e) {
            Log.v(BezierGlobals.TAG, "ControlPointsHolder::Internal Error: setAsJSON  failed !");
            return false;
        }

        // switch from current spline to saved spline
        this.userData = tmp;
        this.setNormalMode();

        return true;
    }

    // =====================================================================

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
