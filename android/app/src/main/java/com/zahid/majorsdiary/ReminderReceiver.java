package com.zahid.majorsdiary;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

/** Sends a reminder 15 minutes before each class, then schedules the next one. */
public class ReminderReceiver extends BroadcastReceiver {

    private static final String ACTION_REMIND = "com.zahid.majorsdiary.REMIND";
    private static final int LEAD_MIN = 15;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (ACTION_REMIND.equals(action)) {
            String title = intent.getStringExtra("title");
            String time = intent.getStringExtra("time");
            if (title != null) {
                Notify.show(context, Notify.CH_REMINDERS, 2000 + (int) (System.currentTimeMillis() / 60000 % 1000),
                        title + " in " + LEAD_MIN + " min", "Starts at " + time + " — get ready 📚", false);
            }
        }
        // Boot, app update, time change or a reminder just fired: (re)schedule the next one.
        MainActivity.safe(() -> scheduleNext(context));
        MainActivity.safe(() -> ClassSyncJob.schedule(context));
    }

    /** Finds the next class in the routine (today or the coming week) and sets an alarm before it. */
    static void scheduleNext(Context context) {
        long now = System.currentTimeMillis();
        int today = Schedule.today();
        for (int offset = 0; offset < 8; offset++) {
            int day = (today + offset) % 7;
            String[][] items = Schedule.items(context, day);
            for (String[] item : items) {
                if (!"class".equals(item[2])) continue;
                int start;
                try {
                    start = Integer.parseInt(item[3]);
                } catch (Exception e) {
                    continue;
                }
                Calendar c = Calendar.getInstance();
                c.add(Calendar.DAY_OF_MONTH, offset);
                c.set(Calendar.HOUR_OF_DAY, 0);
                c.set(Calendar.MINUTE, 0);
                c.set(Calendar.SECOND, 0);
                c.set(Calendar.MILLISECOND, 0);
                c.add(Calendar.MINUTE, start - LEAD_MIN);
                if (c.getTimeInMillis() > now + 30_000) {
                    set(context, c.getTimeInMillis(), item[1], item[0]);
                    return;
                }
            }
        }
    }

    private static void set(Context context, long at, String title, String time) {
        Intent i = new Intent(context, ReminderReceiver.class).setAction(ACTION_REMIND)
                .putExtra("title", title).putExtra("time", time);
        PendingIntent pi = PendingIntent.getBroadcast(context, 77, i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am != null) am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi);
    }
}
