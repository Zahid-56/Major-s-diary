package com.zahid.majorsdiary;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.view.View;
import android.widget.RemoteViews;

import java.util.Set;

/** Small 2×2 widget: what's happening now, what's next, and today's progress. */
public class MiniWidget extends AppWidgetProvider {

    private static final String ACTION_DONE = "com.zahid.majorsdiary.MINI_DONE";
    private static final long TICK_MS = 15 * 60 * 1000L;

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
        MainActivity.safe(() -> refresh(context));
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (ACTION_DONE.equals(intent.getAction())) {
            DiaryWidget.toggle(context, intent.getIntExtra(DiaryWidget.EXTRA_INDEX, -1));
        }
    }

    @Override
    public void onDisabled(Context context) {
        AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarms != null) alarms.cancel(tickIntent(context));
    }

    static void refresh(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int[] ids = manager.getAppWidgetIds(new ComponentName(context, MiniWidget.class));
        if (ids.length == 0) return;
        RemoteViews views = build(context);
        for (int id : ids) manager.updateAppWidget(id, views);

        // Keep "now / next" fresh through the day.
        AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarms != null) {
            alarms.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime() + TICK_MS, TICK_MS, tickIntent(context));
        }
    }

    private static PendingIntent tickIntent(Context context) {
        Intent tick = new Intent(context, MiniWidget.class).setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        return PendingIntent.getBroadcast(context, 9, tick, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private static RemoteViews build(Context context) {
        RemoteViews v = new RemoteViews(context.getPackageName(), R.layout.widget_mini);
        String[][] items = Schedule.items(context, Schedule.today());
        Set<String> done = DiaryWidget.loadDone(context);

        int count = 0;
        for (int i = 0; i < items.length; i++) if (done.contains(String.valueOf(i))) count++;
        int pct = items.length == 0 ? 0 : Math.round(count * 100f / items.length);
        v.setTextViewText(R.id.mini_pct, pct + "%");
        v.setProgressBar(R.id.mini_prog, 100, pct, false);

        Intent open = new Intent(context, MainActivity.class);
        v.setOnClickPendingIntent(R.id.mini_root, PendingIntent.getActivity(context, 2, open, PendingIntent.FLAG_IMMUTABLE));

        if (items.length == 0) {
            v.setTextViewText(R.id.mini_label, Schedule.DAYS[Schedule.today()].toUpperCase());
            v.setTextViewText(R.id.mini_title, "Free day ✨");
            v.setTextViewText(R.id.mini_next, "Nothing planned");
            v.setViewVisibility(R.id.mini_chk, View.GONE);
            return v;
        }

        int current = Schedule.currentIndex(items);
        int shown = Math.max(current, 0);
        boolean checked = done.contains(String.valueOf(shown));

        v.setTextViewText(R.id.mini_label, (current >= 0 ? "NOW · " : "FIRST UP · ") + items[shown][0]);
        v.setTextViewText(R.id.mini_title, items[shown][1]);
        if (shown + 1 < items.length) {
            v.setTextViewText(R.id.mini_next, "Next · " + items[shown + 1][0] + "  " + items[shown + 1][1]);
        } else {
            v.setTextViewText(R.id.mini_next, count == items.length ? "All done today 🎉" : "Last item of the day");
        }

        v.setViewVisibility(R.id.mini_chk, View.VISIBLE);
        v.setImageViewResource(R.id.mini_chk, checked ? R.drawable.check_on : R.drawable.check_off);
        Intent mark = new Intent(context, MiniWidget.class).setAction(ACTION_DONE).putExtra(DiaryWidget.EXTRA_INDEX, shown);
        v.setOnClickPendingIntent(R.id.mini_chk, PendingIntent.getBroadcast(context, 300, mark,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE));
        return v;
    }
}
