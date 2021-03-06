package de.peterloos.beziersplines.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Locale;

import de.peterloos.beziersplines.R;
import de.peterloos.beziersplines.BezierGlobals;

/**
 * Project: Bézier Splines Simulation
 * Copyright (c) 2017 by PeLo on 23.01.2017. All rights reserved.
 * Contact info: peter.loos@gmx.de
 */

public class SharedPreferencesUtils {

    /*
     * gridlines settings
     */
    public static int getPersistedGridlinesFactor(Context context) {

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String key = context.getString(R.string.shared_pref_gridlines);
        int gridlinesFactor = sharedPref.getInt(key, BezierGlobals.GridlineIndexDefault);
        String msg = String.format(Locale.getDefault(), "reading gridlines factor ==> %d", gridlinesFactor);
        Log.v(BezierGlobals.TAG, msg);

        return gridlinesFactor;
    }

    public static void persistGridlinesFactor(Context context, int gridlinesFactor) {

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        String msg = String.format(Locale.getDefault(), "writing gridlines factor ==> %d", gridlinesFactor);
        Log.v(BezierGlobals.TAG, msg);

        SharedPreferences.Editor editor = sharedPref.edit();
        String key = context.getString(R.string.shared_pref_gridlines);
        editor.putInt(key, gridlinesFactor);
        editor.apply();
    }

    /*
     * strokewidth settings
     */
    public static int getPersistedStrokewidthFactor(Context context) {

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String key = context.getString(R.string.shared_pref_strokewidthfactor);
        int strokewidthFactor = sharedPref.getInt(key, BezierGlobals.DefaultStrokewidthFactor);
        String msg = String.format(Locale.getDefault(), "reading strokewidth factor ==> %d", strokewidthFactor);
        Log.v(BezierGlobals.TAG, msg);

        return strokewidthFactor;
    }

    public static void persistStrokewidthFactor(Context context, int strokewidthFactor) {

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        String msg = String.format(Locale.getDefault(), "writing strokewidth factor ==> %d", strokewidthFactor);
        Log.v(BezierGlobals.TAG, msg);

        SharedPreferences.Editor editor = sharedPref.edit();
        String key = context.getString(R.string.shared_pref_strokewidthfactor);
        editor.putInt(key, strokewidthFactor);
        editor.apply();
    }

    /*
     * currently saved spline
     */
    public static String getPersistedSpline(Context context) {

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String key = context.getString(R.string.shared_pref_spline);
        String spline = sharedPref.getString(key, "[]");
        String msg = String.format(Locale.getDefault(), "reading spline ==> %s", spline);
        Log.v(BezierGlobals.TAG, msg);

        return spline;
    }

    public static void persistSpline(Context context, String jsonSpline) {

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        String msg = String.format(Locale.getDefault(), "writing spline ==> %s", jsonSpline);
        Log.v(BezierGlobals.TAG, msg);

        SharedPreferences.Editor editor = sharedPref.edit();
        String key = context.getString(R.string.shared_pref_spline);
        editor.putString(key, jsonSpline);
        editor.apply();
    }

}
