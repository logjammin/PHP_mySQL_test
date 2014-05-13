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

public class Utils {



    public static int getFirstDayOfWeek(Context context) {
            return Time.MONDAY;
    }


    /**
     * @return true when week number should be shown.
     */
    public static boolean getShowWeekNumber(Context context) {

        return false;
    }


    public static int getWeekNumberFromTime(long millisSinceEpoch, Context context) {
        Time weekTime = new Time(Time.getCurrentTimezone());
        weekTime.set(millisSinceEpoch);
        weekTime.normalize(true);
        int firstDayOfWeek = Time.MONDAY;
        // if the date is on Saturday or Sunday and the start of the week
        // isn't Monday we may need to shift the date to be in the correct
        // week
        if (weekTime.weekDay == Time.SUNDAY
                && (firstDayOfWeek == Time.SUNDAY || firstDayOfWeek == Time.SATURDAY)) {
            weekTime.monthDay++;
            weekTime.normalize(true);
        } else if (weekTime.weekDay == Time.SATURDAY && firstDayOfWeek == Time.SATURDAY) {
            weekTime.monthDay += 2;
            weekTime.normalize(true);
        }
        return weekTime.getWeekNumber();
    }



}