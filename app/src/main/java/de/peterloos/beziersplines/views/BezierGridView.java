package de.peterloos.beziersplines.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;

import java.util.Locale;

import de.peterloos.beziersplines.BezierGlobals;
import de.peterloos.beziersplines.utils.BezierPoint;
import de.peterloos.beziersplines.utils.BezierUtils;

/**
 * Project: Bézier Splines Simulation
 * Copyright (c) 2017 by PeLo on 28.01.2017. All rights reserved.
 * Contact info: peter.loos@gmx.de
 */

public class BezierGridView extends BezierView {

    private int indexGridLines;

    // grid specific variables
    private double cellLength;
    private int numCellRows;
    private int numCellCols;

    // paint object(s)
    private Paint linePaint;

    // c'tor
    public BezierGridView(Context context, AttributeSet attrs) {
        super(context, attrs);

        Log.v(BezierGlobals.TAG, "c'tor BezierGridView");

        // setup grid details
        this.cellLength = 0;
        this.numCellRows = -1;
        this.numCellCols = -1;

        // setup paint object
        this.linePaint = new Paint();
        this.linePaint.setStrokeWidth(2);
        this.linePaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        this.linePaint.setColor(Color.WHITE);
        this.linePaint.setStrokeCap(Paint.Cap.ROUND);

        // setup density of grid lines
        this.indexGridLines = BezierGlobals.GridlineIndexDefault;
    }

    @Override
    public void addControlPoint(BezierPoint p) {
        BezierUtils.snapPoint(p, this.cellLength);
        super.addControlPoint(p);
    }

    @Override
    public void updateControlPoint(int index, BezierPoint p) {
        BezierUtils.snapPoint(p, this.cellLength);
        super.updateControlPoint(index, p);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        this.drawGrid(canvas);
        super.onDraw(canvas);
    }

    // public interface
    public void setDensityOfGridlines(int index) {

        this.cellLength = BezierUtils.getCellLength(index);

        if (index < this.indexGridLines) {
            this.snapAllPoints();
        }

        this.indexGridLines = index;

        this.calculateNumOfGridLines();
        this.invalidate();
    }

    // private helper methods
    private void calculateNumOfGridLines() {

        // calculate number of grid lines (horizontally and vertically) -
        // cut off decimal part, no rounding
        this.numCellCols = (int) (this.viewWidth / this.cellLength);
        this.numCellRows = (int) (this.viewHeight / this.cellLength);

        String msg = String.format(Locale.getDefault(),
                "BezierGridView::calculateNumOfGridLines: Cols=%d - Rows=%d",
                this.numCellCols, this.numCellRows);
        Log.v(BezierGlobals.TAG, msg);
    }

    private void drawGrid(Canvas canvas) {

        if (this.cellLength == 0)
            return;

        // draw horizontal lines
        for (int i = 0; i <= this.numCellRows; i++) {
            canvas.drawLine(
                0,
                (float) (i * this.cellLength),
                (float) this.viewWidth,
                (float) (i * this.cellLength),
                this.linePaint);
        }

        // draw vertical lines
        for (int i = 0; i <= this.numCellCols; i++) {
            canvas.drawLine(
                (float) (i * this.cellLength),
                0,
                (float) (i * this.cellLength),
                (float) this.viewHeight,
                this.linePaint);
        }
    }

    public void snapAllPoints() {

        this.holder.snapAllPoints(this.cellLength);
    }
}
