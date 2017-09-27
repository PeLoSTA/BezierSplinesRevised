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
    private List<BezierPoint> userData;
    private List<BezierPoint> demoData;

    // restrict the constructor from being instantiated
    private ControlPointsHolder () {

        this.userData = new ArrayList<>();
        this.demoData = new ArrayList<>();
    }

//    public void set (List<BezierPoint> model) {
//        this.userData = model;
//    }
//
//    // TODO: DA MUSS MAN DOCH EINE KOPIE RAUSGEBEN ?!?!?!?!
////    public List<BezierPoint> get () {
////        return this.userData;
////    }
//
//    public List<BezierPoint> get () {
//        return new ArrayList<> (this.userData);
//    }

    public static synchronized ControlPointsHolder getInstance () {

        if (instance == null) {
            instance = new ControlPointsHolder ();
        }

        return instance;
    }

    // =====================================================================

    // public interface
    public void clear() {
        this.userData.clear();
    }

    public void add(BezierPoint p) {
        this.userData.add(p);
    }

    public BezierPoint get(int index) {
        return this.userData.get(index);
    }

    public void update(int index, BezierPoint p) {
        this.userData.set(index, p);
    }

    public void remove(int index) {
        this.userData.remove(index);
    }

    public int size() {
        return this.userData.size();
    }

    public void toArray(BezierPoint[] src) {
        this.userData.toArray(src);
    }

    public void snapAllPoints(double cellLength) {

        for (BezierPoint p : this.userData) {
            BezierUtils.snapPoint(p, cellLength);
        }
    }
}
