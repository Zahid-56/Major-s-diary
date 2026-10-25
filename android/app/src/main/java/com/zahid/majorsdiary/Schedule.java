package com.zahid.majorsdiary;

import java.util.Calendar;

/** Weekly routine (BSSE-5D timetable). Index 0 = Monday … 6 = Sunday. Each item: {time, title, type}. */
final class Schedule {

    static final String[] DAYS = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

    /** Work shift 5:45 PM – 2:00 AM (from home). */
    static final boolean[] SHIFT = {false, true, true, true, true, true, false};

    static final String[][][] ITEMS = {
        {{"8:00", "Wake up & get ready", "life"}, {"8:30", "Web Engineering", "class"}, {"9:30", "Data Structure & Algorithm", "class"}, {"10:30", "Breakfast + DSA recap", "life"}, {"11:30", "Operating System Lab", "class"}, {"1:30", "Computer Org. & Assembly", "class"}, {"2:30", "Design & Analysis of Algorithm", "class"}, {"3:30", "Lunch + rest", "life"}, {"5:00 PM", "Study · 1 hour", "study"}, {"6:30 PM", "Workout · Upper body (home)", "workout"}, {"8:00 PM", "Dinner", "life"}, {"9:00 PM", "Free time + prep for Tuesday", "life"}, {"11:00 PM", "Sleep", "sleep"}},
        {{"7:50", "Wake up & get ready", "life"}, {"8:30", "Data Structure & Algorithm Lab", "class"}, {"11:30", "Brunch", "life"}, {"12:15", "Study · 1 hour", "study"}, {"1:30", "Computer Org. & Assembly", "class"}, {"2:30", "Agile Software Development", "class"}, {"3:45", "Nap", "sleep"}, {"5:00 PM", "Mobility · 15 min", "workout"}, {"5:15 PM", "Meal", "life"}, {"5:45 PM", "Work shift (from home)", "shift"}, {"2:15 AM", "Sleep", "sleep"}},
        {{"9:00", "Wake up & breakfast", "life"}, {"9:30", "Operating System", "class"}, {"10:30", "Data Structure & Algorithm", "class"}, {"11:30", "Lunch", "life"}, {"12:15", "Study · 1 hour", "study"}, {"1:30", "Design & Analysis of Algorithm", "class"}, {"2:30", "Computer Org. & Assembly Lab", "class"}, {"4:35", "Power nap", "sleep"}, {"5:20 PM", "Meal", "life"}, {"5:45 PM", "Work shift (from home)", "shift"}, {"2:15 AM", "Sleep", "sleep"}},
        {{"9:30", "Wake up & breakfast", "life"}, {"9:50", "Study · 30 min", "study"}, {"10:30", "Operating System", "class"}, {"11:30", "Web Engineering Lab", "class"}, {"1:30", "Agile Software Development", "class"}, {"2:30", "Design & Analysis of Algorithm", "class"}, {"3:30", "Lunch", "life"}, {"4:15", "Workout · Lower body (home)", "workout"}, {"5:00 PM", "Shower + meal", "life"}, {"5:45 PM", "Work shift (from home)", "shift"}, {"2:15 AM", "Sleep", "sleep"}},
        {{"9:45", "Wake up & breakfast", "life"}, {"10:30", "Web Engineering", "class"}, {"11:30", "Data Structure & Algorithm", "class"}, {"12:30", "Lunch + break", "life"}, {"1:30", "Agile Software Development Lab", "class"}, {"3:30", "Study · 1 hour", "study"}, {"4:35", "Workout · Core (home)", "workout"}, {"5:00 PM", "Rest + meal", "life"}, {"5:45 PM", "Work shift (from home)", "shift"}, {"2:15 AM", "Sleep", "sleep"}},
        {{"10:00", "Wake up & breakfast", "life"}, {"11:00", "Deep study · 3 hours", "study"}, {"2:00", "Lunch", "life"}, {"3:00", "Errands / free time", "life"}, {"4:00", "Workout · Full body (home)", "workout"}, {"5:00 PM", "Shower + meal", "life"}, {"5:45 PM", "Work shift (from home)", "shift"}, {"2:15 AM", "Sleep", "sleep"}},
        {{"10:00", "Wake up & breakfast", "life"}, {"11:00", "Study · 3 hours", "study"}, {"2:00", "Lunch", "life"}, {"3:00", "Free time", "life"}, {"4:00", "Workout · HIIT (home)", "workout"}, {"5:00 PM", "Weekly review", "study"}, {"8:00 PM", "Dinner", "life"}, {"10:30 PM", "Sleep", "sleep"}}
    };

    private Schedule() {}

    static int today() {
        return (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 5) % 7;
    }
}
