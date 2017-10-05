package de.peterloos.beziersplines.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;

import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;

import java.util.List;
import java.util.Locale;

import de.peterloos.beziersplines.R;
import de.peterloos.beziersplines.BezierGlobals;
import de.peterloos.beziersplines.activities.MainActivity;
import de.peterloos.beziersplines.utils.BezierMode;
import de.peterloos.beziersplines.utils.BezierPoint;
import de.peterloos.beziersplines.utils.BezierUtils;
import de.peterloos.beziersplines.utils.ControlPointsHolder;

/**
 * Project: Bézier Splines Simulation
 * Copyright (c) 2017 by PeLo on 23.01.2017. All rights reserved.
 * Contact info: peterloos@gmx.de
 */

public class BezierView extends View implements View.OnTouchListener {

    // controlling behaviour of this view ("read", "write" mode ...)
    private BezierMode mode;

    private boolean showControlPoints;
    private boolean showBezierCurve;
    private boolean showConstruction;

    // paint objects
    private Paint linePaint;
    private Paint circlePaint;
    private Paint textPaint;

    // colors
    private int colorControlPoints;
    private int colorCurveLine;
    private int colorConstructionLine;

    // real pixel densities for lines
    private float strokeWidthControlPoints;
    private float strokeWidthCurveLines;
    private float strokeWidthConstructionLines;

    // real pixel densities for circles
    private float strokeWidthCircle;
    private float strokeWidthBorderWidth;
    private float distanceFromNumber;
    private float nearestDistanceMaximum;

    // real pixel densities for text
    private float strokeTextSize;

    // bézier curve specific parameters
    private int resolution;
    private float constructionPosition;

    // size of this view
    protected int viewWidth;
    protected int viewHeight;

    // share controls points between different views (singleton object)
    protected ControlPointsHolder holder;

    // some bézier view specific informations can be read (event based)
    private BezierListener listener;

    // miscellaneous
    private float startX;
    private float startY;
    private float touchSlop;
    private Resources res;

    // c'tor
    public BezierView(Context context, AttributeSet attrs) {
        super(context, attrs);

        Log.v(BezierGlobals.TAG, "c'tor BezierView");

        this.res = this.getResources();

        this.mode = BezierMode.Create;

        ViewConfiguration vc = ViewConfiguration.get(context);
        this.touchSlop = vc.getScaledTouchSlop();

        this.showControlPoints = true;
        this.showBezierCurve = true;
        this.showConstruction = true;

        this.setOnTouchListener(this);

        // convert density independent pixels to real pixels
        this.strokeWidthCircle = convertDpToPixel(this.res, BezierGlobals.StrokeWidthCircleRadiusDp);
        this.strokeWidthBorderWidth = convertDpToPixel(this.res, BezierGlobals.StrokeWidthBorderWidthDp);
        this.strokeTextSize = convertDpToPixel(this.res, BezierGlobals.StrokeWidthTextSizeDp);
        this.distanceFromNumber = convertDpToPixel(this.res, BezierGlobals.DistanceFromNumberDp);
        this.nearestDistanceMaximum = convertDpToPixel(this.res, BezierGlobals.NearestDistanceMaximumDp);

        // color settings
        this.colorControlPoints = getColorWrapper(context, R.color.material_blue_grey_500);
        this.colorCurveLine = getColorWrapper(context, R.color.material_red_700);
        this.colorConstructionLine = getColorWrapper(context, R.color.material_blue_700);

        // setup paint objects
        this.linePaint = new Paint();
        this.linePaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        this.linePaint.setStrokeCap(Paint.Cap.ROUND);

        this.circlePaint = new Paint();
        this.circlePaint.setFlags(Paint.ANTI_ALIAS_FLAG);

        this.textPaint = new Paint();
        this.textPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        this.textPaint.setColor(Color.BLACK);
        this.textPaint.setTextSize(this.strokeTextSize);

        // miscellaneous
        this.resolution = 50;
        this.constructionPosition = (float) 0.5;

        // retrieve singleton data object, to access control points
        this.holder = ControlPointsHolder.getInstance();

        //  need size of this view (when view is visible)
        ViewTreeObserver vto = this.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                BezierView.this.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                BezierView.this.viewWidth = BezierView.this.getMeasuredWidth();;
                BezierView.this.viewHeight = BezierView.this.getMeasuredHeight();;

                if (BezierView.this.listener != null) {
                    BezierView.this.listener.setSize(BezierView.this.viewWidth, BezierView.this.viewHeight);
                }

                if (BezierView.this.mode == BezierMode.Demo) {

                    // TODO: Hier haben wir eine Race condition: Was ist, wenn der Mode
                    // später gesetzt wird ?!?!?!?
                    ControlPointsHolder.computeDemoRectangle(BezierView.this.viewWidth, BezierView.this.viewHeight);
                }
            }
        });
    }

    // getter/setter
    public void setResolution(int resolution) {
        this.resolution = resolution;
        this.invalidate();
    }

    public void setT(float t) {
        this.constructionPosition = t;
        this.invalidate();
    }

    public void setShowConstruction(boolean showConstruction) {
        this.showConstruction = showConstruction;
        this.invalidate();
    }

    public void setMode(BezierMode mode) {
        this.mode = mode;
    }

    public void setStrokewidthFactor(int setStrokewidthFactor) {

        float strokeWidthControlPointsDp =
                BezierGlobals.StrokeWidthControlPointsDp *
                        BezierGlobals.StrokewidthFactors[setStrokewidthFactor];
        this.strokeWidthControlPoints = convertDpToPixel(this.res, strokeWidthControlPointsDp);

        float strokeWidthCurveLineDp =
                BezierGlobals.StrokeWidthCurveLineDp *
                        BezierGlobals.StrokewidthFactors[setStrokewidthFactor];
        this.strokeWidthCurveLines = convertDpToPixel(this.res, strokeWidthCurveLineDp);

        float strokeWidthConstructionLinesDp =
                BezierGlobals.StrokeWidthConstructionLinesDp *
                        BezierGlobals.StrokewidthFactors[setStrokewidthFactor];
        this.strokeWidthConstructionLines = convertDpToPixel(this.res, strokeWidthConstructionLinesDp);
    }

    // public interface
    public void clear() {
        this.holder.clear();
        this.clearTouchPosition();
        this.invalidate();
    }

    public void addControlPoint(BezierPoint p) {
        this.holder.add(p);
        this.invalidate();
    }

    public void addControlPoints(List<BezierPoint> points) {
        for (BezierPoint point : points) {
            this.addControlPoint(point);
        }
    }

    public void removeControlPoint(int index) {
        this.holder.remove(index);
        this.invalidate();
    }

    public void updateControlPoint(int index, BezierPoint p) {
        this.holder.update(index, p);
        this.invalidate();
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (this.showControlPoints)
            this.drawControlPoints(canvas);
        if (this.showBezierCurve)
            this.drawBezierCurve(canvas);
        if (this.showConstruction)
            this.drawConstructionPoints(canvas);
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {

        if (this.mode == BezierMode.Demo)
            return true;

        int action = event.getAction();

        // prevent touch events during moving outside the bounds of the view
        if (action == MotionEvent.ACTION_MOVE) {
            if (event.getX() <= this.strokeWidthCircle)
                return true;
            if (event.getX() >= this.getWidth() - this.strokeWidthCircle)
                return true;
            if (event.getY() <= this.strokeWidthCircle)
                return true;
            if (event.getY() >= this.getHeight() - this.strokeWidthCircle)
                return true;
        }

        if (action == MotionEvent.ACTION_DOWN) {

            this.startX = event.getX();
            this.startY = event.getY();
        } else if (action == MotionEvent.ACTION_UP) {

            if (this.mode == BezierMode.Create || this.mode == BezierMode.Delete) {
                float endX = event.getX();
                float endY = event.getY();
                float dX = Math.abs(endX - this.startX);
                float dY = Math.abs(endY - this.startY);

                if (Math.sqrt(dX * dX + dY * dY) <= this.touchSlop) {
                    BezierPoint p = new BezierPoint(this.startX, this.startY);
                    if (this.mode == BezierMode.Create) {
                        // add new control point
                        this.addControlPoint(p);
                        this.setTouchPosition((int) p.getX(), (int) p.getY());
                    } else if (this.mode == BezierMode.Delete) {
                        int index = this.getNearestPointIndex(p);
                        if (index == -1) {
                            return true;
                        }

                        // remove this control point
                        p = this.holder.get(index);
                        if (this.holder.size() > 1) {
                            this.setTouchPosition((int) p.getX(), (int) p.getY());
                        } else {
                            this.clearTouchPosition();
                            this.onChangeBezierMode(BezierMode.Create);
                        }
                        this.removeControlPoint(index);
                    }
                }
            } else if (this.mode == BezierMode.Edit) {
                BezierPoint p = new BezierPoint((int) event.getX(), (int) event.getY());
                if (this.mode == BezierMode.Edit) {
                    int dragIndex = this.getNearestPointIndex(p);
                    if (dragIndex == -1) {
                        return true;
                    }

                    // update control point
                    this.updateControlPoint(dragIndex, p);
                    this.setTouchPosition((int) p.getX(), (int) p.getY());
                }
            }
        } else if (action == MotionEvent.ACTION_MOVE) {
            BezierPoint p = new BezierPoint((int) event.getX(), (int) event.getY());
            if (this.mode == BezierMode.Edit) {
                int dragIndex = this.getNearestPointIndex(p);
                if (dragIndex == -1) {
                    return true;
                }

                // update control point
                this.setTouchPosition((int) p.getX(), (int) p.getY());
                this.updateControlPoint(dragIndex, p);
            }
        }

        return true;
    }

    // private helper methods
    private void drawControlPoints(Canvas canvas) {
        int numPoints = this.holder.size();
        if (numPoints == 0)
            return;

        BezierPoint p0, p1;
        p0 = this.holder.get(0);
        this.drawControlPoint(canvas, p0.getX(), p0.getY(), "0");

        for (int i = 1; i < numPoints; i++) {
            p1 = this.holder.get(i);
            String s = Integer.toString(i);
            this.drawControlPoint(canvas, p1.getX(), p1.getY(), s);
            this.drawLineBetweenControlPoints(canvas, p0, p1);
            p0 = p1;
        }
    }

    private void drawBezierCurve(Canvas canvas) {
        int numPoints = this.holder.size();
        if (numPoints < 3)
            return;

        BezierPoint p0 = this.holder.get(0);
        for (int i = 1; i <= this.resolution; i++) {
            float t = ((float) i) / ((float) this.resolution);
            BezierPoint p1 = this.computeBezierPoint(t);
            this.drawCurveLine(canvas, p0, p1);
            p0 = p1;
        }
    }

    // helper function to compute a bezier point defined by the control polygon at value t
    private BezierPoint computeBezierPoint(float t) {
        int numPoints = this.holder.size();
        BezierPoint[] dest = new BezierPoint[numPoints];
        BezierPoint[] src = new BezierPoint[numPoints];

        this.holder.toArray(src);
        System.arraycopy(src, 0, dest, 0, numPoints);

        for (int n = dest.length; n > 0; n--)
            for (int i = 1; i < n; i++)
                dest[i - 1] = BezierPoint.Interpolate(dest[i - 1], dest[i], t);

        return dest[0];
    }

    private void drawConstructionPoints(Canvas canvas) {
        int numPoints = this.holder.size();
        if (numPoints < 3)
            return;

        BezierPoint[][] constructionPoints = this.computeConstructionPoints(this.constructionPosition);
        numPoints = constructionPoints.length;

        for (int i = numPoints - 2; i >= 0; i--) {
            BezierPoint[] array = constructionPoints[i];

            BezierPoint p0 = array[0];
            for (int j = 1; j <= i; j++) {
                BezierPoint p1 = array[j];
                this.drawConstructionLine(canvas, p0, p1);
                p0 = p1;
            }
        }

        // drawing Bezier point itself
        BezierPoint pBezier = constructionPoints[0][0];
        String pos = String.format(Locale.getDefault(), "%.2f", this.constructionPosition);
        this.drawConstructionPoint(canvas, pBezier.getX(), pBezier.getY(), pos);
    }

    // helper function to compute all the construction points
    // needed to compute a single bezier point at value t
    private BezierPoint[][] computeConstructionPoints(float t) {
        int numPoints = holder.size();
        BezierPoint[][] constructionPoints = new BezierPoint[numPoints][];

        BezierPoint[] tmpArray = new BezierPoint[numPoints];
        this.holder.toArray(tmpArray);
        constructionPoints[numPoints - 1] = tmpArray;

        for (int n = numPoints - 1; n > 0; n--) {
            tmpArray = new BezierPoint[n];
            for (int i = 1; i <= n; i++) {
                BezierPoint p0 = constructionPoints[n][i - 1];
                BezierPoint p1 = constructionPoints[n][i];
                BezierPoint p = BezierPoint.Interpolate(p0, p1, t);
                tmpArray[i - 1] = p;
            }
            constructionPoints[n - 1] = tmpArray;
        }
        return constructionPoints;
    }

    // helper function to find the nearest control point
    private int getNearestPointIndex(BezierPoint p) {
        int numPoints = holder.size();
        if (numPoints == 0)
            return -1;

        int index = -1;
        float dist = Float.MAX_VALUE;

        for (int i = 0; i < numPoints; i++) {
            BezierPoint p0 = this.holder.get(i);
            float newDist = BezierPoint.Distance(p, p0);
            if (newDist < dist) {
                dist = newDist;
                index = i;
            }
        }
        return (dist < this.nearestDistanceMaximum) ? index : -1;
    }

    // drawing helper methods for points
    private void drawControlPoint(Canvas canvas, float cx, float cy, String text) {
        this.drawPoint(canvas, cx, cy, Color.LTGRAY, Color.BLACK, text, false);
    }

    private void drawConstructionPoint(Canvas canvas, float cx, float cy, String text) {
        this.drawPoint(canvas, cx, cy, Color.RED, Color.DKGRAY, text, false);
    }

    private void drawPoint(Canvas canvas, float cx, float cy, int colorStart, int colorEnd, String text, boolean drawBorder) {

        Shader shader = new LinearGradient(
                cx,
                cy,
                cx + this.strokeWidthCircle,
                cy + this.strokeWidthCircle,
                colorStart,
                colorEnd,
                Shader.TileMode.CLAMP);

        // draw point
        this.circlePaint.setShader(shader);
        this.circlePaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(cx, cy, this.strokeWidthCircle, this.circlePaint);

        // draw number
        canvas.drawText(text, cx + this.distanceFromNumber, cy - this.distanceFromNumber, this.textPaint);

        // draw border, if any
        if (drawBorder) {
            this.circlePaint.setShader(null);
            this.circlePaint.setColor(Color.BLACK);
            this.circlePaint.setStyle(Paint.Style.STROKE);
            this.circlePaint.setStrokeWidth(this.strokeWidthBorderWidth);
            canvas.drawCircle(cx, cy, this.strokeWidthCircle - (this.strokeWidthBorderWidth / 2), this.circlePaint);
        }
    }

    private void setTouchPosition(int x, int y) {
        String info = String.format(Locale.getDefault(), "%d, %d", x, y);
        this.onBezierPointChanged(info);
    }

    private void clearTouchPosition() {
        this.onBezierPointChanged("");
    }

    // drawing helper methods for lines
    private void drawConstructionLine(Canvas canvas, BezierPoint p0, BezierPoint p1) {
        this.drawLine(canvas, p0, p1, this.colorConstructionLine, this.strokeWidthConstructionLines);
    }

    private void drawCurveLine(Canvas canvas, BezierPoint p0, BezierPoint p1) {
        this.drawLine(canvas, p0, p1, this.colorCurveLine, this.strokeWidthCurveLines);
    }

    private void drawLineBetweenControlPoints(Canvas canvas, BezierPoint p0, BezierPoint p1) {
        this.drawLine(canvas, p0, p1, this.colorControlPoints, this.strokeWidthControlPoints);
    }

    private void drawLine(Canvas canvas, BezierPoint p0, BezierPoint p1, int color, float strokeWidth) {
        this.linePaint.setColor(color);
        this.linePaint.setStrokeWidth(strokeWidth);
        canvas.drawLine(p0.getX(), p0.getY(), p1.getX(), p1.getY(), this.linePaint);
    }

    public static float convertDpToPixel(Resources res, float dpSize) {
        DisplayMetrics dm = res.getDisplayMetrics();
        float strokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpSize, dm);
        return strokeWidth;
    }

    @SuppressWarnings("unused")
    public static float convertDpToPixel_V2(Resources res, int dpSize) {
        DisplayMetrics dm = res.getDisplayMetrics();
        float strokeWidth = dpSize * dm.density + 0.5f;
        return strokeWidth;
    }

    public static int getColorWrapper(Context context, int id) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= Build.VERSION_CODES.M) {
            return ContextCompat.getColor(context, id);
        } else {
            //noinspection deprecation
            return context.getResources().getColor(id);
        }
    }

    // support handling of interface 'BezierListener'
    public void registerListener(BezierListener listener) {
        this.listener = listener;
    }

    @SuppressWarnings("unused")
    public void unregisterListener(BezierListener listener) {
        this.listener = null;
    }

    private void onBezierPointChanged(String info) {
        if (this.listener != null) {
            listener.setInfo(info);
        }
    }

    private void onChangeBezierMode(BezierMode mode) {
        if (this.listener != null) {
            listener.changeMode(mode);
        }
    }
}
