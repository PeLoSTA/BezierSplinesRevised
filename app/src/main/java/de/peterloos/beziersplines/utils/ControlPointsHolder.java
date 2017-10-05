package de.peterloos.beziersplines.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

    // =====================================================================

    /*
     *  calculate demo points (and some more lists of points for screen shot purposes)
     */

    private static final int NumDemoEdges = 8;

    private enum Direction {ToTheRight, ToTheBottom, ToTheLeft, ToTheTop}

    // just needed for the computation of concentric rectangles
    private static float currentX;
    private static float currentY;
    private static float nextX;
    private static float nextY;

    public void computeDemoRectangle(float viewWidth, float viewHeight) {

        float centerX = viewWidth / 2;
        float centerY = viewHeight / 2;

        float deltaX = viewWidth / (float) NumDemoEdges;
        float deltaY = viewHeight / (float) NumDemoEdges;

        demoData = getDemoRectangle (centerX, centerY, deltaX, deltaY);
    }


//    public static void computeDemoRectangle(float viewWidth, float viewHeight) {
//
//        List<BezierPoint> points = new ArrayList<>();
//
//        float centerX = viewWidth / 2;
//        float centerY = viewHeight / 2;
//
//        float deltaX = viewWidth / (float) NumDemoEdges;
//        float deltaY = viewHeight / (float) NumDemoEdges;
//
//        currentX = centerX;
//        currentY = centerY;
//        nextX = currentX;
//        nextY = currentY;
//
//        Direction direction = Direction.ToTheRight;
//
//        for (int i = 1; i < NumDemoEdges; i++) {
//            computeLine(points, i, direction, deltaX, deltaY);
//            direction = switchDirection(direction);
//            computeLine(points, i, direction, deltaX, deltaY);
//            direction = switchDirection(direction);
//        }
//    }



    public static List<BezierPoint> getDemoRectangle(float centerX, float centerY, float deltaX, float deltaY) {

        List<BezierPoint> points = new ArrayList<>();

        currentX = centerX;
        currentY = centerY;
        nextX = currentX;
        nextY = currentY;

        Direction direction = Direction.ToTheRight;

        for (int i = 1; i < NumDemoEdges; i++) {
            computeLine(points, i, direction, deltaX, deltaY);
            direction = switchDirection(direction);
            computeLine(points, i, direction, deltaX, deltaY);
            direction = switchDirection(direction);
        }

        return points;
    }


//    public static List<BezierPoint> getDemoRectangle(float viewWidth, float viewHeight) {
//
//        computeDemoRectangle(viewWidth, viewHeight);
//        return demoPoints;
//    }

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

    // =====================================================================


    /*
     *  test interface - screenshots for Google Play Services
     */

    // keys for generating screenshots (Google Play Store)
    public static final int SCREENSHOT_TOTALLY_RANDOM = 0;
    public static final int SCREENSHOT_SINGLE_CIRCLE = 1;
    public static final int SCREENSHOT_SINGLE_CIRCLE_OPPOSITE_CONNECTED = 2;
    public static final int SCREENSHOT_CONCENTRIC_CIRCLES = 3;
    public static final int SCREENSHOT_CASCADING_RECTANGLES = 4;
    public static final int SCREENSHOT_NICE_FIGURE = 5;
    public static final int SCREENSHOT_NICE_FIGURE_02 = 6;

    @SuppressWarnings("unused")
    public void setControlPointsForScreenshot(int number, int viewWidth, int viewHeight) {

        List<BezierPoint> points = getControlPointsForScreenshot(number, viewWidth, viewHeight);

        for (int i = 0; i < points.size(); i++) {

            this.add(points.get(i));
        }
    }

    @SuppressWarnings("unused")
    public List<BezierPoint> getControlPointsForScreenshot(int number, int viewWidth, int viewHeight) {

        List<BezierPoint> points;

        if (number == SCREENSHOT_SINGLE_CIRCLE) {
            points = this.showScreenshot_SingleCircle(viewWidth, viewHeight);
        } else if (number == SCREENSHOT_SINGLE_CIRCLE_OPPOSITE_CONNECTED) {
            points = this.showScreenshot_SingleCircle_OppositeConnected(viewWidth, viewHeight);
        } else if (number == SCREENSHOT_CONCENTRIC_CIRCLES) {
            points = this.showScreenshot_TwoConcentricCircles(viewWidth, viewHeight);
        } else if (number == SCREENSHOT_CASCADING_RECTANGLES) {
            points = this.showScreenshot_Cascading_Rectangles(viewWidth, viewHeight);
        } else if (number == SCREENSHOT_TOTALLY_RANDOM) {
            points = this.showScreenshotTotallyRandom(viewWidth, viewHeight);
        } else if (number == SCREENSHOT_NICE_FIGURE) {
            points = this.showScreenshot_NiceFigure(viewWidth, viewHeight);
        } else if (number == SCREENSHOT_NICE_FIGURE_02) {
            points = this.showScreenshot_NiceFigure_02(viewWidth, viewHeight);
        } else {
            points = this.showScreenshot_SingleCircle(viewWidth, viewHeight);
        }

        return points;
    }

    @SuppressWarnings("unused")
    private List<BezierPoint> showScreenshotTotallyRandom(int viewWidth, int viewHeight) {
        return getTotallyRandom(viewWidth, viewHeight, 50);
    }

    @SuppressWarnings("unused")
    private List<BezierPoint> showScreenshot_SingleCircle(int viewWidth, int viewHeight) {
        float centerX = viewWidth / 2;
        float centerY = viewHeight / 2;
        float squareLength = (viewWidth < viewHeight) ? viewWidth : viewHeight;
        float arcLength = (float) (2 * Math.PI / 25);
        return getDemo_SingleCircle(centerX, centerY, squareLength / 2 - 100, arcLength);
    }

    @SuppressWarnings("unused")
    private List<BezierPoint> showScreenshot_SingleCircle_OppositeConnected(int viewWidth, int viewHeight) {
        float centerX = viewWidth / 2;
        float centerY = viewHeight / 2;
        float squareLength = (viewWidth < viewHeight) ? viewWidth : viewHeight;
        float arcLength = (float) (2 * Math.PI / 25);
        return getDemo_SingleCircleOppositeConnected(centerX, centerY, squareLength / 2 - 100, arcLength);
    }

    @SuppressWarnings("unused")
    private List<BezierPoint> showScreenshot_TwoConcentricCircles(int viewWidth, int viewHeight) {
        float centerX = viewWidth / 2;
        float centerY = viewHeight / 2;
        float squareLength = (viewWidth < viewHeight) ? viewWidth : viewHeight;
        float arcLength = 0.5f;
        return getDemoConcentricCircles(centerX, centerY, squareLength / 5, squareLength / 2 - 150, arcLength);
    }

    @SuppressWarnings("unused")
    private List<BezierPoint> showScreenshot_Cascading_Rectangles(int viewWidth, int viewHeight) {

        float centerX = viewWidth / 2;
        float centerY = viewHeight / 2;
        float deltaX = viewWidth / (float) NumDemoEdges;
        float deltaY = viewHeight / (float) NumDemoEdges;
        return getDemoRectangle(centerX, centerY, deltaX, deltaY);
    }

    @SuppressWarnings("unused")
    private List<BezierPoint> showScreenshot_NiceFigure(int viewWidth, int viewHeight) {
        int numEdges = 7;
        float centerX = viewWidth / 2;
        float centerY = viewHeight / 2;
        float squareLength = (viewWidth < viewHeight) ? viewWidth : viewHeight;
        return getDemoNiceFigure(centerX, centerY, squareLength / 2 - 100, numEdges - 1);
    }

    @SuppressWarnings("unused")
    private List<BezierPoint> showScreenshot_NiceFigure_02(int viewWidth, int viewHeight) {
        int numEdges = 6;
        float centerX = viewWidth / 2;
        float centerY = viewHeight / 2;
        float squareLength = (viewWidth < viewHeight) ? viewWidth : viewHeight;
        return getDemoNiceFigure2(centerX, centerY, squareLength / 2 - 100, numEdges - 1);
    }

    // =====================================================================

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

    private static List<BezierPoint> getDemoConcentricCircles(float centerX, float centerY, float radius1, float radius2, float arcLength) {

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
