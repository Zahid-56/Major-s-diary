package com.zahid.majorsdiary;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

/**
 * Runs roughly every 15 minutes (when online) and notifies about new notes, quizzes and
 * announcements from the CR, plus quizzes that close within the next few hours.
 */
public class ClassSyncJob extends JobService {

    static final String API = "https://vharzqunlmafscsxueym.supabase.co/rest/v1/";
    static final String KEY = "sb_publishable_QHLFNjbFjgGLE2ksuR_3vg_O0yFn101";
    private static final int JOB_ID = 51;
    private static final String PREFS = "class_sync";

    static void schedule(Context context) {
        JobScheduler js = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (js == null) return;
        for (JobInfo j : js.getAllPendingJobs()) if (j.getId() == JOB_ID) return;
        JobInfo job = new JobInfo.Builder(JOB_ID, new ComponentName(context, ClassSyncJob.class))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(15 * 60 * 1000L)
                .setPersisted(true)
                .build();
        js.schedule(job);
    }

    static void setJoined(Context context, boolean joined) {
        SharedPreferences p = context.getSharedPreferences(PREFS, MODE_PRIVATE);
        SharedPreferences.Editor e = p.edit().putBoolean("joined", joined);
        if (joined && !p.contains("seen")) e.putLong("seen", System.currentTimeMillis());
        e.apply();
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        new Thread(() -> {
            try {
                sync(getApplicationContext());
            } catch (Throwable ignored) {
            }
            jobFinished(params, false);
        }).start();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }

    static void sync(Context context) throws Exception {
        SharedPreferences p = context.getSharedPreferences(PREFS, MODE_PRIVATE);
        if (!p.getBoolean("joined", false)) return;
        long seen = p.getLong("seen", 0);
        if (seen == 0) {
            p.edit().putLong("seen", System.currentTimeMillis()).apply();
            return;
        }
        String since = iso(seen);
        long newest = seen;
        int notifId = 3000;

        JSONArray notes = get("notes?select=title,subject,created_at&created_at=gt." + since + "&order=created_at.asc&limit=10");
        for (int i = 0; i < notes.length(); i++) {
            JSONObject n = notes.getJSONObject(i);
            newest = Math.max(newest, parse(n.getString("created_at")));
            Notify.show(context, Notify.CH_UPDATES, notifId++, "📄 New notes · " + shortName(n.optString("subject")),
                    n.optString("title"), true);
        }
        JSONArray quizzes = get("quizzes?select=title,subject,created_at,due_at&published=eq.true&created_at=gt." + since + "&order=created_at.asc&limit=10");
        for (int i = 0; i < quizzes.length(); i++) {
            JSONObject q = quizzes.getJSONObject(i);
            newest = Math.max(newest, parse(q.getString("created_at")));
            Notify.show(context, Notify.CH_UPDATES, notifId++, "📝 New quiz · " + shortName(q.optString("subject")),
                    q.optString("title") + " — open the Class tab to attempt it", true);
        }
        JSONArray anns = get("announcements?select=message,created_at&created_at=gt." + since + "&order=created_at.asc&limit=5");
        for (int i = 0; i < anns.length(); i++) {
            JSONObject a = anns.getJSONObject(i);
            newest = Math.max(newest, parse(a.getString("created_at")));
            Notify.show(context, Notify.CH_UPDATES, notifId++, "📢 From your CR", a.optString("message"), true);
        }

        // Quizzes closing within the next 6 hours — remind once.
        Set<String> reminded = new HashSet<>(p.getStringSet("due_reminded", new HashSet<>()));
        String now = iso(System.currentTimeMillis()), soon = iso(System.currentTimeMillis() + 6 * 3600_000L);
        JSONArray due = get("quizzes?select=id,title,due_at&published=eq.true&due_at=gt." + now + "&due_at=lt." + soon);
        for (int i = 0; i < due.length(); i++) {
            JSONObject q = due.getJSONObject(i);
            String id = q.getString("id");
            if (reminded.contains(id)) continue;
            reminded.add(id);
            Notify.show(context, Notify.CH_UPDATES, notifId++, "⏰ Quiz closing soon",
                    q.optString("title") + " closes today — don't miss it", true);
        }

        p.edit().putLong("seen", newest).putStringSet("due_reminded", reminded).apply();
    }

    private static JSONArray get(String path) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(API + path).openConnection();
        c.setConnectTimeout(15000);
        c.setReadTimeout(15000);
        c.setRequestProperty("apikey", KEY);
        c.setRequestProperty("Accept", "application/json");
        try (InputStream in = c.getInputStream()) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
            return new JSONArray(out.toString("UTF-8"));
        } finally {
            c.disconnect();
        }
    }

    private static String iso(long ms) {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        f.setTimeZone(TimeZone.getTimeZone("UTC"));
        return f.format(new Date(ms));
    }

    /** Parses Postgres timestamps like 2026-10-29T10:47:50.123456+00:00 (always UTC) to epoch millis. */
    private static long parse(String ts) {
        try {
            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            f.setTimeZone(TimeZone.getTimeZone("UTC"));
            long t = f.parse(ts.substring(0, 19)).getTime();
            if (ts.length() > 20 && ts.charAt(19) == '.') {
                StringBuilder d = new StringBuilder();
                for (int i = 20; i < ts.length() && Character.isDigit(ts.charAt(i)) && d.length() < 3; i++) d.append(ts.charAt(i));
                while (d.length() < 3) d.append('0');
                t += Integer.parseInt(d.toString());
            }
            return t + 1;   // +1 ms so the same row (with microseconds) is never fetched twice
        } catch (Exception e) {
            return System.currentTimeMillis();
        }
    }

    private static String shortName(String subject) {
        switch (subject) {
            case "Web Engineering": return "Web Eng";
            case "Data Structure & Algorithm": return "DSA";
            case "Operating System": return "OS";
            case "Computer Org. & Assembly": return "COAL";
            case "Design & Analysis of Algorithm": return "DAA";
            case "Agile Software Development": return "Agile";
            default: return subject;
        }
    }
}
