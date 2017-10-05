package de.peterloos.beziersplines.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Peter on 22.09.2017.
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
    private ControlPointsHolder () {

        this.userData = new ArrayList<>();
        this.demoData = new ArrayList<>();
        this.data = this.userData;
    }

    public static synchronized ControlPointsHolder getInstance () {

        if (instance == null) {
            instance = new ControlPointsHolder ();
        }

        return instance;
    }

    // =====================================================================

    // public interface
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

        this.data = this.userData;
    }

    public synchronized void setDemoMode() {

        this.data = this.demoData;
    }
}
