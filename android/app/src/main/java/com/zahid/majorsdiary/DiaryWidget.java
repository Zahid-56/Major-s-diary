package com.zahid.majorsdiary;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.widget.RemoteViews;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/** Home-screen widget: today's full routine with tap-to-check items. */
public class DiaryWidget extends AppWidgetProvider {

    static final String EXTRA_INDEX = "index";
    private static final String ACTION_TOGGLE = "com.zahid.majorsdiary.TOGGLE";

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
        MainActivity.safe(() -> refreshAll(context));
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (ACTION_TOGGLE.equals(intent.getAction())) {
            toggle(context, intent.getIntExtra(EXTRA_INDEX, -1));
        }
    }

    /** Checks or unchecks one of today's items, then redraws every widget. */
    static void toggle(Context context, int index) {
        if (index < 0) return;
        Set<String> done = loadDone(context);
        String key = String.valueOf(index);
        if (!done.remove(key)) done.add(key);
        prefs(context).edit().putStringSet(todayKey(), done).apply();
        refreshAll(context);
    }

    static void refreshAll(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int[] ids = manager.getAppWidgetIds(new ComponentName(context, DiaryWidget.class));
        for (int id : ids) manager.updateAppWidget(id, build(context, id));
        manager.notifyAppWidgetViewDataChanged(ids, R.id.list);
        MiniWidget.refresh(context);
        scheduleMidnightRefresh(context);
    }

    private static RemoteViews build(Context context, int widgetId) {
        RemoteViews v = new RemoteViews(context.getPackageName(), R.layout.widget);
        int day = Schedule.today();
        int total = Schedule.items(context, day).length;
        int count = 0;
        for (String s : loadDone(context)) {
            try {
                if (Integer.parseInt(s) < total) count++;
            } catch (NumberFormatException ignored) {
            }
        }
        int pct = total == 0 ? 0 : Math.round(count * 100f / total);

        v.setTextViewText(R.id.title, Schedule.DAYS[day]);
        v.setTextViewText(R.id.sub, count + " of " + total + " done  ·  " + (Schedule.hasShift(context, day) ? "Shift day" : "No shift"));
        v.setTextViewText(R.id.pct, pct + "%");
        v.setProgressBar(R.id.prog, 100, pct, false);

        Intent service = new Intent(context, DiaryWidgetService.class)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        service.setData(Uri.parse(service.toUri(Intent.URI_INTENT_SCHEME)));
        v.setRemoteAdapter(R.id.list, service);

        Intent toggle = new Intent(context, DiaryWidget.class).setAction(ACTION_TOGGLE);
        v.setPendingIntentTemplate(R.id.list, PendingIntent.getBroadcast(context, 0, toggle,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE));

        Intent open = new Intent(context, MainActivity.class);
        v.setOnClickPendingIntent(R.id.header, PendingIntent.getActivity(context, 1, open, PendingIntent.FLAG_IMMUTABLE));
        return v;
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(Schedule.PREFS, Context.MODE_PRIVATE);
    }

    /** Called when the user saves a new routine: clears today's ticks and redraws. */
    static void routineChanged(Context context) {
        prefs(context).edit().remove(todayKey()).apply();
        refreshAll(context);
    }

    static Set<String> loadDone(Context context) {
        return new HashSet<>(prefs(context).getStringSet(todayKey(), new HashSet<>()));
    }

    private static String todayKey() {
        Calendar c = Calendar.getInstance();
        return String.format(Locale.US, "%d-%02d-%02d", c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));
    }

    /** Re-draws the widget just after midnight so the new day's routine appears. */
    private static void scheduleMidnightRefresh(Context context) {
        Calendar next = Calendar.getInstance();
        next.add(Calendar.DAY_OF_MONTH, 1);
        next.set(Calendar.HOUR_OF_DAY, 0);
        next.set(Calendar.MINUTE, 0);
        next.set(Calendar.SECOND, 5);

        Intent intent = new Intent(context, DiaryWidget.class).setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        PendingIntent pending = PendingIntent.getBroadcast(context, 7, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarms != null) alarms.set(AlarmManager.RTC, next.getTimeInMillis(), pending);
    }
}
