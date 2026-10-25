package com.zahid.majorsdiary;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Calendar;

/**
 * Weekly routine. Index 0 = Monday … 6 = Sunday. Each item: {time label, title, type, start minute}.
 * The built-in sample (BSSE-5D timetable) is used until the user saves their own routine in the app.
 */
final class Schedule {

    static final String[] DAYS = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

    static final String[][][] ITEMS = {
        {{"8:00", "Wake up & get ready", "life", "480"}, {"8:30", "Web Engineering", "class", "510"}, {"9:30", "Data Structure & Algorithm", "class", "570"}, {"10:30", "Breakfast + DSA recap", "life", "630"}, {"11:30", "Operating System Lab", "class", "690"}, {"1:30", "Computer Org. & Assembly", "class", "810"}, {"2:30", "Design & Analysis of Algorithm", "class", "870"}, {"3:30", "Lunch + rest", "life", "930"}, {"5:00 PM", "Study · 1 hour", "study", "1020"}, {"6:30 PM", "Workout · Upper body (home)", "workout", "1110"}, {"8:00 PM", "Dinner", "life", "1200"}, {"9:00 PM", "Free time + prep for Tuesday", "life", "1260"}, {"11:00 PM", "Sleep", "sleep", "1380"}},
        {{"7:50", "Wake up & get ready", "life", "470"}, {"8:30", "Data Structure & Algorithm Lab", "class", "510"}, {"11:30", "Brunch", "life", "690"}, {"12:15", "Study · 1 hour", "study", "735"}, {"1:30", "Computer Org. & Assembly", "class", "810"}, {"2:30", "Agile Software Development", "class", "870"}, {"3:45", "Nap", "sleep", "945"}, {"5:00 PM", "Mobility · 15 min", "workout", "1020"}, {"5:15 PM", "Meal", "life", "1035"}, {"5:45 PM", "Work shift (from home)", "shift", "1065"}, {"2:15 AM", "Sleep", "sleep", "1575"}},
        {{"9:00", "Wake up & breakfast", "life", "540"}, {"9:30", "Operating System", "class", "570"}, {"10:30", "Data Structure & Algorithm", "class", "630"}, {"11:30", "Lunch", "life", "690"}, {"12:15", "Study · 1 hour", "study", "735"}, {"1:30", "Design & Analysis of Algorithm", "class", "810"}, {"2:30", "Computer Org. & Assembly Lab", "class", "870"}, {"4:35", "Power nap", "sleep", "995"}, {"5:20 PM", "Meal", "life", "1040"}, {"5:45 PM", "Work shift (from home)", "shift", "1065"}, {"2:15 AM", "Sleep", "sleep", "1575"}},
        {{"9:30", "Wake up & breakfast", "life", "570"}, {"9:50", "Study · 30 min", "study", "590"}, {"10:30", "Operating System", "class", "630"}, {"11:30", "Web Engineering Lab", "class", "690"}, {"1:30", "Agile Software Development", "class", "810"}, {"2:30", "Design & Analysis of Algorithm", "class", "870"}, {"3:30", "Lunch", "life", "930"}, {"4:15", "Workout · Lower body (home)", "workout", "975"}, {"5:00 PM", "Shower + meal", "life", "1020"}, {"5:45 PM", "Work shift (from home)", "shift", "1065"}, {"2:15 AM", "Sleep", "sleep", "1575"}},
        {{"9:45", "Wake up & breakfast", "life", "585"}, {"10:30", "Web Engineering", "class", "630"}, {"11:30", "Data Structure & Algorithm", "class", "690"}, {"12:30", "Lunch + break", "life", "750"}, {"1:30", "Agile Software Development Lab", "class", "810"}, {"3:30", "Study · 1 hour", "study", "930"}, {"4:35", "Workout · Core (home)", "workout", "995"}, {"5:00 PM", "Rest + meal", "life", "1020"}, {"5:45 PM", "Work shift (from home)", "shift", "1065"}, {"2:15 AM", "Sleep", "sleep", "1575"}},
        {{"10:00", "Wake up & breakfast", "life", "600"}, {"11:00", "Deep study · 3 hours", "study", "660"}, {"2:00", "Lunch", "life", "840"}, {"3:00", "Errands / free time", "life", "900"}, {"4:00", "Workout · Full body (home)", "workout", "960"}, {"5:00 PM", "Shower + meal", "life", "1020"}, {"5:45 PM", "Work shift (from home)", "shift", "1065"}, {"2:15 AM", "Sleep", "sleep", "1575"}},
        {{"10:00", "Wake up & breakfast", "life", "600"}, {"11:00", "Study · 3 hours", "study", "660"}, {"2:00", "Lunch", "life", "840"}, {"3:00", "Free time", "life", "900"}, {"4:00", "Workout · HIIT (home)", "workout", "960"}, {"5:00 PM", "Weekly review", "study", "1020"}, {"8:00 PM", "Dinner", "life", "1200"}, {"10:30 PM", "Sleep", "sleep", "1350"}}
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
                int start = it.getInt("s");
                out[i] = new String[]{label(start), it.optString("n"), it.optString("k", "life"), String.valueOf(start)};
            }
            return out;
        } catch (Exception e) {
            return ITEMS[day];
        }
    }

    /** Index of the item happening now (last one that has started), or -1 before the first item. */
    static int currentIndex(String[][] items) {
        Calendar c = Calendar.getInstance();
        int now = c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE);
        if (now < 360) now += 1440;   // 0:00–6:00 counts as the end of the previous evening
        int index = -1;
        for (int i = 0; i < items.length; i++) {
            try {
                if (Integer.parseInt(items[i][3]) <= now) index = i;
            } catch (Exception ignored) {
            }
        }
        return index;
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
