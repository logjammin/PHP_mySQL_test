/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.powereng.receiving;

import android.content.Context;
import android.text.format.Time;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.Date;
import java.util.WeakHashMap;

public class Utils {

    private static WeakHashMap<Context, WeakReference<Utils>> instances =
            new WeakHashMap<Context, WeakReference<Utils>>();
    private int mViewType = -1;
    private final Time mTime = new Time();
    private final Context mContext;
    /**
     * Creates and/or returns an instance of CalendarController associated with
     * the supplied context. It is best to pass in the current Activity.
     *
     * @param context The activity if at all possible.
     */
    public static Utils getInstance(Context context) {
        synchronized (instances) {
            Utils utils = null;
            WeakReference<Utils> weakController = instances.get(context);
            if (weakController != null) {
                utils = weakController.get();
            }

            if (utils == null) {
                utils = new Utils(context);
                instances.put(context, new WeakReference(utils));
            }
            return utils;
        }
    }

    /**
     * Removes an instance when it is no longer needed. This should be called in
     * an activity's onDestroy method.
     *
     * @param context The activity used to create the controller
     */
    public static void removeInstance(Context context) {
        instances.remove(context);
    }

    private Utils(Context context) {
        mContext = context;
        mTime.setToNow();

    }

    /**
     * @return the time that this controller is currently pointed at
     */
    public long getTime() {
        return mTime.toMillis(false);
    }

    /**
     * Set the time this controller is currently pointed at
     *
     * @param millisTime Time since epoch in millis
     */
    public void setTime(long millisTime) {
        mTime.set(millisTime);
    }

    public static int getFirstDayOfWeek(Context context) {
            return Time.MONDAY;
    }


    /**
     * @return true when week number should be shown.
     */
    public static boolean getShowWeekNumber(Context context) {

        return true;
    }

    public static int getWeekNumberFromTime(long millisSinceEpoch, Context context) {
        Time weekTime = new Time(Time.getCurrentTimezone());
        weekTime.set(millisSinceEpoch);
        weekTime.normalize(true);
        int firstDayOfWeek = Time.MONDAY;
        // if the date is on Saturday or Sunday and the start of the week
        // isn't Monday we may need to shift the date to be in the correct
        // week
        if (weekTime.weekDay == Time.FRIDAY
                && (firstDayOfWeek == Time.SUNDAY || firstDayOfWeek == Time.SATURDAY)) {
            weekTime.monthDay++;
            weekTime.normalize(true);
        } else if (weekTime.weekDay == Time.SATURDAY && firstDayOfWeek == Time.SUNDAY) {
            weekTime.monthDay += 2;
            weekTime.normalize(true);
        }
        return weekTime.getWeekNumber();
    }

    public static Date getWeekStart(Date date, int weekStart) {
        Calendar calendar = Calendar.getInstance();
        while (calendar.get(Calendar.DAY_OF_WEEK) != weekStart) {

            calendar.add(Calendar.DATE, -1);
        }
        //startOfDay(calendar);
        return calendar.getTime();
    }

    public static Date getWeekEnd(Date date, int weekStart) {
        Calendar calendar = Calendar.getInstance();
        while (calendar.get(Calendar.DAY_OF_WEEK) != weekStart) {
            calendar.add(Calendar.DATE, 1);
        }
        calendar.add(Calendar.DATE, -1);
        //endOfDay(calendar);
        return calendar.getTime();
    }


}