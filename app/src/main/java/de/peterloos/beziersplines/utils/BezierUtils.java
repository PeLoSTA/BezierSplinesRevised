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

    private enum Direction {ToTheRight, ToTheBottom, ToTheLeft, ToTheTop}

    // just for concentric rectangles
    private static float currentX;
    private static float currentY;
    private static float nextX;
    private static float nextY;

    public static List<BezierPoint> getDemoRectangle(float centerX, float centerY, float deltaX, float deltaY, int numEdges) {

        List<BezierPoint> result = new ArrayList<>();

        currentX = centerX;
        currentY = centerY;
        nextX = currentX;
        nextY = currentY;

        Direction direction = Direction.ToTheRight;

        for (int i = 1; i < numEdges; i++) {
            computeLine(result, i, direction, deltaX, deltaY);
            direction = switchDirection(direction);
            computeLine(result, i, direction, deltaX, deltaY);
            direction = switchDirection(direction);
        }

        return result;
    }

    private static void computeLine(List<BezierPoint> list, int count, Direction direction, float deltaX, float deltaY) {
        for (int j = 0; j < count; j++) {
            if (direction == Direction.ToTheRight) {
                nextX += deltaX;
            } else if (direction == Direction.ToTheBottom) {
                nextY += deltaY;
            } else if (direction == Direction.ToTheLeft) {
                nextX -= deltaX;
            } else if (direction == Direction.ToTheTop) {
                nextY -= deltaY;
            }

            BezierPoint point = new BezierPoint(currentX, currentY);
            list.add(point);

            // advance last point
            currentX = nextX;
            currentY = nextY;
        }
    }

    private static Direction switchDirection(Direction direction) {
        // switch direction
        if (direction == Direction.ToTheRight) {
            return Direction.ToTheBottom;
        } else if (direction == Direction.ToTheBottom) {
            return Direction.ToTheLeft;
        } else if (direction == Direction.ToTheLeft) {
            return Direction.ToTheTop;
        } else {
            return Direction.ToTheRight;
        }
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

    public static double calculateGridCellLength (DisplayMetrics dm, int widthPx, int heightPx) {

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

        // TODO: Das soll jetzt mal ein Viertel CM sein ...
        // TODO: Das muss noch entsprechend erweitert werden !!!!!!!!!!!!!!!!
        // this.bezierViewWithGrid.setCellLength(numPixelsHorizontalPerCm / 4.0);

        return numPixelsHorizontalPerCm / 4.0;
    }

    // ============================================================================================
    //  methods for test pictures (Google Play Store)
    // ============================================================================================


    // TODO: Hier eine andere Klasse machen !!! Zum Beispiel BezierDemonstrationsSplines
    // TODO !!!

    // looks boring
    public static List<BezierPoint> getTotallyRandom(float width, float height, int number) {

        Random rand = new Random();

        // create a border
        int border = 100;

        width -= border;
        height -= border;

        List<BezierPoint> result = new ArrayList<>();

        for (int i = 0; i < number; i++) {

            float x = rand.nextFloat() * width + border / 2;
            float y = rand.nextFloat() * height + border / 2;

            BezierPoint p = new BezierPoint(x, y);
            result.add(p);
        }

        return result;
    }

    public static List<BezierPoint> getDemo_SingleCircle(float centerX, float centerY, float radius, float arcLength) {

        List<BezierPoint> result = new ArrayList<>();

        for (double z = 2 * Math.PI; z >= 0.1; z -= arcLength) {

            float x = centerX + radius * (float) Math.sin(z);
            float y = centerY + radius * (float) Math.cos(z);

            BezierPoint p = new BezierPoint(x, y);
            result.add(p);
        }

        return result;
    }

    public static List<BezierPoint> getDemo_SingleCircleOppositeConnected(float centerX, float centerY, float radius, float arcLength) {

        List<BezierPoint> result = new ArrayList<>();

        float x = centerX + radius * (float) Math.sin(2 * Math.PI);
        float y = centerY + radius * (float) Math.cos(2 * Math.PI);
        BezierPoint p = new BezierPoint(x, y);
        result.add(p);

        for (double z1 = 2 * Math.PI; z1 >= 2 * Math.PI * 3 / 4; z1 -= arcLength) {

            float x1 = centerX + radius * (float) Math.sin(z1);
            float y1 = centerY + radius * (float) Math.cos(z1);
            BezierPoint p1 = new BezierPoint(x1, y1);
            result.add(p1);

            float x2 = centerX + radius * (float) Math.sin(Math.PI - z1);
            float y2 = centerY + radius * (float) Math.cos(Math.PI - z1);
            BezierPoint p2 = new BezierPoint(x2, y2);
            result.add(p2);
        }

        for (double z1 = 2 * Math.PI / 4; z1 >= 0.1; z1 -= arcLength) {

            float x1 = centerX + radius * (float) Math.sin(z1);
            float y1 = centerY + radius * (float) Math.cos(z1);
            BezierPoint p1 = new BezierPoint(x1, y1);
            result.add(p1);

            float x2 = centerX + radius * (float) Math.sin(Math.PI - z1);
            float y2 = centerY + radius * (float) Math.cos(Math.PI - z1);
            BezierPoint p2 = new BezierPoint(x2, y2);
            result.add(p2);
        }

        return result;
    }

    public static List<BezierPoint> getDemoConcentricCircles(float centerX, float centerY, float radius1, float radius2, float arcLength) {

        List<BezierPoint> result = new ArrayList<>();

        // inner circle
        for (double z = 2 * Math.PI; z >= 0.0; z = z - arcLength) {
            float x = centerX + radius1 * (float) Math.sin(z);
            float y = centerY + radius1 * (float) Math.cos(z);

            BezierPoint p = new BezierPoint(x, y);
            result.add(p);
        }

        // outer circle
        for (double z = 0.0; z < 2 * Math.PI - 0.1; z = z + arcLength / 1.5) {
            float x = centerX + radius2 * (float) Math.sin(z);
            float y = centerY + radius2 * (float) Math.cos(z);

            BezierPoint p = new BezierPoint(x, y);
            result.add(p);
        }

        return result;
    }

    @SuppressWarnings("unused")
    public static List<BezierPoint> getDemoNiceFigure(float centerX, float centerY, float radius, int partitions) {

        List<BezierPoint> result = new ArrayList<>();

        float x, y;
        BezierPoint p;

        BezierPoint center = new BezierPoint(centerX, centerY);

        double delta = 2 * Math.PI / partitions;
        double z = 0;

        for (int i = 0; i < partitions; i += 2) {
            result.add(center);

            x = centerX + radius * (float) Math.sin(z);
            y = centerY + radius * (float) Math.cos(z);
            p = new BezierPoint(x, y);
            result.add(p);

            z -= delta;

            x = centerX + radius * (float) Math.sin(z);
            y = centerY + radius * (float) Math.cos(z);
            p = new BezierPoint((int) x, (int) y);
            result.add(p);

            z -= delta;
        }

        // close figure with center point
        result.add(center);

        return result;
    }

    @SuppressWarnings("unused")
    public static List<BezierPoint> getDemoNiceFigure2(float centerX, float centerY, float radius, int partition) {

        List<BezierPoint> result = new ArrayList<>();

        BezierPoint center = new BezierPoint(centerX, centerY);

        float x, y;
        BezierPoint p;

        double delta = 2 * Math.PI / partition;
        double z = 0;

        for (int i = 0; i < partition; i++) {
            result.add(center);

            x = centerX + radius * (float) Math.sin(z);
            y = centerY + radius * (float) Math.cos(z);
            p = new BezierPoint(x, y);
            result.add(p);

            z -= delta;

            x = centerX + radius * (float) Math.sin(z);
            y = centerY + radius * (float) Math.cos(z);
            p = new BezierPoint((int) x, (int) y);
            result.add(p);
        }

        // close figure with center point
        result.add(center);

        return result;
    }
}
