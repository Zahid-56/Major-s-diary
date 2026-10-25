package com.zahid.majorsdiary;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Calendar;

/**
 * Weekly routine. Index 0 = Monday … 6 = Sunday. Each item: {time, title, type}.
 * The built-in sample (BSSE-5D timetable) is used until the user saves their own routine in the app.
 */
final class Schedule {

    static final String[] DAYS = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

    static final String[][][] ITEMS = {
        {{"8:00", "Wake up & get ready", "life"}, {"8:30", "Web Engineering", "class"}, {"9:30", "Data Structure & Algorithm", "class"}, {"10:30", "Breakfast + DSA recap", "life"}, {"11:30", "Operating System Lab", "class"}, {"1:30", "Computer Org. & Assembly", "class"}, {"2:30", "Design & Analysis of Algorithm", "class"}, {"3:30", "Lunch + rest", "life"}, {"5:00 PM", "Study · 1 hour", "study"}, {"6:30 PM", "Workout · Upper body (home)", "workout"}, {"8:00 PM", "Dinner", "life"}, {"9:00 PM", "Free time + prep for Tuesday", "life"}, {"11:00 PM", "Sleep", "sleep"}},
        {{"7:50", "Wake up & get ready", "life"}, {"8:30", "Data Structure & Algorithm Lab", "class"}, {"11:30", "Brunch", "life"}, {"12:15", "Study · 1 hour", "study"}, {"1:30", "Computer Org. & Assembly", "class"}, {"2:30", "Agile Software Development", "class"}, {"3:45", "Nap", "sleep"}, {"5:00 PM", "Mobility · 15 min", "workout"}, {"5:15 PM", "Meal", "life"}, {"5:45 PM", "Work shift (from home)", "shift"}, {"2:15 AM", "Sleep", "sleep"}},
        {{"9:00", "Wake up & breakfast", "life"}, {"9:30", "Operating System", "class"}, {"10:30", "Data Structure & Algorithm", "class"}, {"11:30", "Lunch", "life"}, {"12:15", "Study · 1 hour", "study"}, {"1:30", "Design & Analysis of Algorithm", "class"}, {"2:30", "Computer Org. & Assembly Lab", "class"}, {"4:35", "Power nap", "sleep"}, {"5:20 PM", "Meal", "life"}, {"5:45 PM", "Work shift (from home)", "shift"}, {"2:15 AM", "Sleep", "sleep"}},
        {{"9:30", "Wake up & breakfast", "life"}, {"9:50", "Study · 30 min", "study"}, {"10:30", "Operating System", "class"}, {"11:30", "Web Engineering Lab", "class"}, {"1:30", "Agile Software Development", "class"}, {"2:30", "Design & Analysis of Algorithm", "class"}, {"3:30", "Lunch", "life"}, {"4:15", "Workout · Lower body (home)", "workout"}, {"5:00 PM", "Shower + meal", "life"}, {"5:45 PM", "Work shift (from home)", "shift"}, {"2:15 AM", "Sleep", "sleep"}},
        {{"9:45", "Wake up & breakfast", "life"}, {"10:30", "Web Engineering", "class"}, {"11:30", "Data Structure & Algorithm", "class"}, {"12:30", "Lunch + break", "life"}, {"1:30", "Agile Software Development Lab", "class"}, {"3:30", "Study · 1 hour", "study"}, {"4:35", "Workout · Core (home)", "workout"}, {"5:00 PM", "Rest + meal", "life"}, {"5:45 PM", "Work shift (from home)", "shift"}, {"2:15 AM", "Sleep", "sleep"}},
        {{"10:00", "Wake up & breakfast", "life"}, {"11:00", "Deep study · 3 hours", "study"}, {"2:00", "Lunch", "life"}, {"3:00", "Errands / free time", "life"}, {"4:00", "Workout · Full body (home)", "workout"}, {"5:00 PM", "Shower + meal", "life"}, {"5:45 PM", "Work shift (from home)", "shift"}, {"2:15 AM", "Sleep", "sleep"}},
        {{"10:00", "Wake up & breakfast", "life"}, {"11:00", "Study · 3 hours", "study"}, {"2:00", "Lunch", "life"}, {"3:00", "Free time", "life"}, {"4:00", "Workout · HIIT (home)", "workout"}, {"5:00 PM", "Weekly review", "study"}, {"8:00 PM", "Dinner", "life"}, {"10:30 PM", "Sleep", "sleep"}}
    };

    static final String PREFS = "diary";
    static final String KEY_ROUTINE = "routine";

    private Schedule() {}

    /** Items for a day: the user's own routine if saved, otherwise the sample. */
    static String[][] items(Context context, int day) {
        String json = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_ROUTINE, "");
        if (json == null || json.isEmpty()) return ITEMS[day];
        try {
            JSONArray list = new JSONArray(json).getJSONObject(day).getJSONArray("items");
            String[][] out = new String[list.length()][];
            for (int i = 0; i < list.length(); i++) {
                JSONObject it = list.getJSONObject(i);
                out[i] = new String[]{label(it.getInt("s")), it.optString("n"), it.optString("k", "life")};
            }
            return out;
        } catch (Exception e) {
            return ITEMS[day];
        }
    }

    static boolean hasShift(Context context, int day) {
        for (String[] item : items(context, day)) if ("shift".equals(item[2])) return true;
        return false;
    }

    /** Compact time label, e.g. "8:30", "5:45 PM", "2:15 AM". */
    private static String label(int minutes) {
        int h = (minutes / 60) % 24;
        int m = minutes % 60;
        String t = (h % 12 == 0 ? 12 : h % 12) + ":" + (m < 10 ? "0" : "") + m;
        if (minutes >= 1440) return t + " AM";
        if (minutes >= 1020) return t + " PM";
        return t;
    }

    static int today() {
        return (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 5) % 7;
    }
}
