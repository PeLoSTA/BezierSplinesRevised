package de.peterloos.beziersplines.utils;

import android.util.DisplayMetrics;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.peterloos.beziersplines.BezierGlobals;

/**
 * Project: Bézier Splines Simulation
 * Copyright (c) 2017 by PeLo on 23.01.2017. All rights reserved.
 * Contact info: peterloos@gmx.de
 */

public class BezierUtils {

    private static double cellLengths[];

    static {
        cellLengths = new double[3];
        cellLengths[0] = -1;
        cellLengths[1] = -1;
        cellLengths[2] = -1;
    }

//    private enum Direction {ToTheRight, ToTheBottom, ToTheLeft, ToTheTop}
//
//    // just for concentric rectangles
//    private static float currentX;
//    private static float currentY;
//    private static float nextX;
//    private static float nextY;
//
//    public static List<BezierPoint> getDemoRectangle(float centerX, float centerY, float deltaX, float deltaY, int numEdges) {
//
//        List<BezierPoint> result = new ArrayList<>();
//
//        currentX = centerX;
//        currentY = centerY;
//        nextX = currentX;
//        nextY = currentY;
//
//        Direction direction = Direction.ToTheRight;
//
//        for (int i = 1; i < numEdges; i++) {
//            computeLine(result, i, direction, deltaX, deltaY);
//            direction = switchDirection(direction);
//            computeLine(result, i, direction, deltaX, deltaY);
//            direction = switchDirection(direction);
//        }
//
//        return result;
//    }
//
//    private static void computeLine(List<BezierPoint> list, int count, Direction direction, float deltaX, float deltaY) {
//        for (int j = 0; j < count; j++) {
//            if (direction == Direction.ToTheRight) {
//                nextX += deltaX;
//            } else if (direction == Direction.ToTheBottom) {
//                nextY += deltaY;
//            } else if (direction == Direction.ToTheLeft) {
//                nextX -= deltaX;
//            } else if (direction == Direction.ToTheTop) {
//                nextY -= deltaY;
//            }
//
//            BezierPoint point = new BezierPoint(currentX, currentY);
//            list.add(point);
//
//            // advance last point
//            currentX = nextX;
//            currentY = nextY;
//        }
//    }
//
//    private static Direction switchDirection(Direction direction) {
//        // switch direction
//        if (direction == Direction.ToTheRight) {
//            return Direction.ToTheBottom;
//        } else if (direction == Direction.ToTheBottom) {
//            return Direction.ToTheLeft;
//        } else if (direction == Direction.ToTheLeft) {
//            return Direction.ToTheTop;
//        } else {
//            return Direction.ToTheRight;
//        }
//    }

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

    public static void calculateCellLengths (DisplayMetrics dm, int widthPx, int heightPx) {

        // calculate size of this display in cm
        double xInches = (double) widthPx / (double) dm.xdpi;
        double xCm = xInches * 2.54;
        double yInches = (double) heightPx / (double) dm.ydpi;
        double yCm = yInches * 2.54;
        Log.d(BezierGlobals.TAG,"View in cm (width): " + xCm);
        Log.d(BezierGlobals.TAG,"View in cm (height): " + yCm);

        // calculate cell size for bézier grid view in pixel
        double numPixelsHorizontalPerCm = (double) widthPx / xCm;
        double numPixelsVerticalPerCm = (double) heightPx / yCm;

        Log.d(BezierGlobals.TAG,"numPixelsHorizontalPerCm: " + numPixelsHorizontalPerCm);
        Log.d(BezierGlobals.TAG,"numPixelsVerticalPerCm:   " + numPixelsVerticalPerCm);

        cellLengths[0] = numPixelsHorizontalPerCm / 2.0;    // 1/2 cm
        cellLengths[1] = numPixelsHorizontalPerCm / 4.0;    // 1/4 cm
        cellLengths[2] = numPixelsHorizontalPerCm / 8.0;    // 1/8 cm
    }

    public static double getCellLength (int density) {
        return cellLengths[density];
    }
}
