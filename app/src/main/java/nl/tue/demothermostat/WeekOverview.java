package nl.tue.demothermostat;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import java.util.ArrayList;

/**
 * Created by nstash on 06/05/15.
 */
public class WeekOverview extends ListActivity {
    /**
     * This class describes an individual day (the name of the day, and the activity class that
     * demonstrates this sample).
     */
    private class Day {
        private CharSequence name;
        private Class<? extends Activity> activityClass;

        public Day(int titleResId, Class<? extends Activity> activityClass) {
            this.activityClass = activityClass;
            this.name = getResources().getString(titleResId);
        }

        @Override
        public String toString() {
            return name.toString();
        }
    }

    private static ArrayList<Day> days;
    private ArrayAdapter<Day> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.week_overview);

        // Create all the days of the week.
        days = new ArrayList<Day>() {{
                add(new Day(R.string.monday, DayEditor.class));
                add(new Day(R.string.tuesday, DayEditor.class));
                add(new Day(R.string.wednesday, DayEditor.class));
                add(new Day(R.string.thurday, DayEditor.class));
                add(new Day(R.string.friday, DayEditor.class));
                add(new Day(R.string.saturday, DayEditor.class));
                add(new Day(R.string.sunday, DayEditor.class));
        }};

        arrayAdapter = new ArrayAdapter<Day>(this,
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                days);

        // Create the listView adapter for our listView of days.
        setListAdapter(arrayAdapter);
    }

    @Override
    protected void onListItemClick(ListView listView, View view, int position, long id) {
        Intent intent = new Intent(this, days.get(position).activityClass);

        // Add the day on which is clicked to the intent before parsing.
        intent.putExtra("day", days.get(position).toString());

        // Launch the activity of the day associated with this listView position.
        startActivity(intent);
    }
}