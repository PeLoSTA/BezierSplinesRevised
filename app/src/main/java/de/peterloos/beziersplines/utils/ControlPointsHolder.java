package de.peterloos.beziersplines.utils;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import de.peterloos.beziersplines.BezierGlobals;

/**
 * Created by Peter on 22.09.2017.
 */

public class ControlPointsHolder {

    private static ControlPointsHolder instance;

    static {
        instance = null;
    }

    // representing application global data
    private List<BezierPoint> model;

    // restrict the constructor from being instantiated
    private ControlPointsHolder () {

        this.model = new ArrayList<>();
    }

    public void set (List<BezierPoint> model) {
        this.model = model;
    }

    // TODO: DA MUSS MAN DOCH EINE KOPIE RAUSGEBEN ?!?!?!?!
//    public List<BezierPoint> get () {
//        return this.model;
//    }

    public List<BezierPoint> get () {
        return new ArrayList<> (this.model);
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
        this.model.clear();
    }

    public void add(BezierPoint p) {
        this.model.add(p);
    }

    public BezierPoint get(int index) {
        return this.model.get(index);
    }

    public void update(int index, BezierPoint p) {
        this.model.set(index, p);
    }

    public void remove(int index) {
        this.model.remove(index);
    }

    public int size() {
        return this.model.size();
    }

    public void toArray(BezierPoint[] src) {
        this.model.toArray(src);
    }

    public void snapAllPoints(double cellLength) {

        for (BezierPoint p : this.model) {

            BezierUtils.snapPoint(p, cellLength);
        }
    }
}
