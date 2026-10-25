package com.zahid.majorsdiary;

import android.content.Context;
import android.content.Intent;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StrikethroughSpan;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.Set;

/** Supplies the scrollable rows of the widget. */
public class DiaryWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new Factory(getApplicationContext());
    }

    private static final class Factory implements RemoteViewsFactory {
        private final Context context;
        private String[][] items = new String[0][];
        private Set<String> done;

        Factory(Context context) {
            this.context = context;
        }

        @Override public void onCreate() { load(); }
        @Override public void onDataSetChanged() { load(); }
        @Override public void onDestroy() {}
        @Override public int getCount() { return items.length; }
        @Override public RemoteViews getLoadingView() { return null; }
        @Override public int getViewTypeCount() { return 1; }
        @Override public long getItemId(int position) { return position; }
        @Override public boolean hasStableIds() { return true; }

        private void load() {
            items = Schedule.items(context, Schedule.today());
            done = DiaryWidget.loadDone(context);
        }

        @Override
        public RemoteViews getViewAt(int position) {
            RemoteViews row = new RemoteViews(context.getPackageName(), R.layout.widget_row);
            if (position >= items.length) return row;

            String[] item = items[position];
            boolean checked = done.contains(String.valueOf(position));

            SpannableString name = new SpannableString(item[1]);
            if (checked) name.setSpan(new StrikethroughSpan(), 0, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            row.setImageViewResource(R.id.chk, checked ? R.drawable.check_on : R.drawable.check_off);
            row.setTextViewText(R.id.time, item[0]);
            row.setTextColor(R.id.time, colorFor(item[2]));
            row.setTextViewText(R.id.name, name);
            row.setTextColor(R.id.name, checked ? 0xFF6B7186 : 0xFFF4F1EA);
            row.setOnClickFillInIntent(R.id.row, new Intent().putExtra(DiaryWidget.EXTRA_INDEX, position));
            return row;
        }

        private static int colorFor(String type) {
            switch (type) {
                case "class": return 0xFFE2C27D;
                case "study": return 0xFF9FBCFF;
                case "workout": return 0xFF7EE0B4;
                case "shift": return 0xFFF2A3A3;
                case "sleep": return 0xFFB4A7F0;
                default: return 0xFF8D93A5;
            }
        }
    }
}
