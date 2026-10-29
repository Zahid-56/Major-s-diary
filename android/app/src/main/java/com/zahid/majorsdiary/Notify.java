package com.zahid.majorsdiary;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

/** Notification channels and a small helper to post notifications. */
final class Notify {

    static final String CH_REMINDERS = "class_reminders";
    static final String CH_UPDATES = "class_updates";

    private Notify() {}

    static void ensureChannels(Context context) {
        if (Build.VERSION.SDK_INT < 26) return;
        NotificationManager nm = context.getSystemService(NotificationManager.class);
        if (nm == null) return;
        NotificationChannel reminders = new NotificationChannel(CH_REMINDERS, "Class reminders", NotificationManager.IMPORTANCE_HIGH);
        reminders.setDescription("15 minutes before each class");
        NotificationChannel updates = new NotificationChannel(CH_UPDATES, "Notes, quizzes & announcements", NotificationManager.IMPORTANCE_HIGH);
        updates.setDescription("When your CR posts something new");
        nm.createNotificationChannel(reminders);
        nm.createNotificationChannel(updates);
    }

    static void show(Context context, String channel, int id, String title, String text, boolean openClass) {
        ensureChannels(context);
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;
        if (Build.VERSION.SDK_INT >= 24 && !nm.areNotificationsEnabled()) return;

        Intent open = new Intent(context, MainActivity.class)
                .putExtra(MainActivity.EXTRA_TAB, openClass ? "class" : "routine")
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(context, id, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification.Builder b = Build.VERSION.SDK_INT >= 26
                ? new Notification.Builder(context, channel)
                : new Notification.Builder(context).setPriority(Notification.PRIORITY_HIGH);
        b.setSmallIcon(R.drawable.ic_notify)
                .setColor(0xFFE2C27D)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(new Notification.BigTextStyle().bigText(text))
                .setAutoCancel(true)
                .setContentIntent(pi);
        nm.notify(id, b.build());
    }
}
