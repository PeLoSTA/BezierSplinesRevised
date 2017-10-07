package de.peterloos.beziersplines.utils;

import android.util.DisplayMetrics;
import android.util.Log;

import de.peterloos.beziersplines.BezierGlobals;

/**
 * Project: Bézier Splines Simulation
 * Copyright (c) 2017 by PeLo on 23.01.2017. All rights reserved.
 * Contact info: peter.loos@gmx.de
 */

public class BezierUtils {

    private static double cellLengths[];

    static {
        cellLengths = new double[3];
        cellLengths[0] = -1;
        cellLengths[1] = -1;
        cellLengths[2] = -1;
    }

    public static void snapPoint(BezierPoint p, double cellLength) {

        double cellLengthtHalf = cellLength / 2.0;

        double realLeft = Math.floor((p.getX() / cellLength)) * cellLength;
        float snapX = (p.getX() <= realLeft + cellLengthtHalf) ?
                (float) realLeft : (float) (realLeft + cellLength);
        double realUpper = Math.floor((p.getY() / cellLength)) * cellLength;
        float snapY = (p.getY() <= realUpper + cellLengthtHalf) ?
                (float) realUpper : (float) (realUpper + cellLength);

        p.setX(snapX);
        p.setY(snapY);
    }

    public static void calculateCellLengths(DisplayMetrics dm, int widthPx, int heightPx) {

        // calculate size of this display in cm
        double xInches = (double) widthPx / (double) dm.xdpi;
        double xCm = xInches * 2.54;
        double yInches = (double) heightPx / (double) dm.ydpi;
        double yCm = yInches * 2.54;
        Log.d(BezierGlobals.TAG, "View in cm (width): " + xCm);
        Log.d(BezierGlobals.TAG, "View in cm (height): " + yCm);

        // calculate cell size for bézier grid view in pixel
        double numPixelsHorizontalPerCm = (double) widthPx / xCm;
        double numPixelsVerticalPerCm = (double) heightPx / yCm;

        Log.d(BezierGlobals.TAG, "numPixelsHorizontalPerCm: " + numPixelsHorizontalPerCm);
        Log.d(BezierGlobals.TAG, "numPixelsVerticalPerCm:   " + numPixelsVerticalPerCm);

        cellLengths[0] = numPixelsHorizontalPerCm / 2.0;    // 1/2 cm
        cellLengths[1] = numPixelsHorizontalPerCm / 4.0;    // 1/4 cm
        cellLengths[2] = numPixelsHorizontalPerCm / 8.0;    // 1/8 cm
    }

    public static double getCellLength(int density) {
        return cellLengths[density];
    }
}
