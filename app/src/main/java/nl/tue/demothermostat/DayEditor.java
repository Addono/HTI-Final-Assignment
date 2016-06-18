/**
 * Class for testing Web Service (http://wwwis.win.tue.nl/2id40-ws), 
 * gives a few examples of 
 * getting data from and uploading data to the server
 */
package nl.tue.demothermostat;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.content.Intent;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.Comparator;

import org.thermostatapp.util.*;

/*
 * TODO: Add support to upload the day schedule.
 * TODO: Add support to insert a switch.
 * TODO: Add support to switch a switch from day to night for the initial and final stage.
 * TODO: Add revert option after switches have been altered.
 * TODO: Add support for changing already present times.
 * TODO: Add titles to pages. (easy)
 */

/**
 * @author Adriaan Knapen <a.d.knapen@student.tue.nl>
 */
public class DayEditor extends Activity implements OnItemClickListener {
    Intent intent;
    static String day;

    private static ArrayList<SwitchListItem> items = new ArrayList<SwitchListItem>();
    private DayListViewAdapter adapter;
    DialogFragment newFragment;
    TimePickerDialog timePickerDialog;
    ListView listView;

    WeekProgram wpg;
    ArrayList<Switch> switches;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.day_list);

        listView = (ListView) findViewById(R.id.list);

        intent = getIntent();
        day = intent.getStringExtra("day");
        items.clear();

        // Fetch the week program and convert it into a day schedule.
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    wpg = HeatingSystem.getWeekProgram();

                    if(wpg == null) {
                        System.err.println("WPG not fetched");
                    }

                    switches = wpg.data.get(DayEditor.day);

                    for(int i = 0; i < switches.size(); i++) {
                        Switch s = switches.get(i);
                        boolean isDay = s.getType().equals("day");
                        int time = s.getTime_Int();
                        SwitchListItem.Type type;

                        System.err.println(s.getTime() + " " + s.getType());

                        // Give the first item the "first" type, all other the "center" type.
                        if(i == 0) {
                            type = SwitchListItem.Type.first;
                        } else {
                            type = SwitchListItem.Type.center;
                        }

                        addItem(isDay, time, type);
                    }

                    // Add the midnight switch.
                    addItem(false, 2400, SwitchListItem.Type.last);

                    removeDuplicates();
                } catch (Exception e) {
                    System.err.println("Error from getdata " + e);
                }
            }
        }).start();

        // Assign a new custom list view adapter to the list view object.
        resetListViewAdapter();

        // Add an click listener to the list view.
        listView.setOnItemClickListener(this);

        // Wait until the WPG fetch thread is finished to prevent the thread from pausing to early, and therefore missing UI items.
        while(items.size() == 0) {
            System.out.println("Waiting for WPG fetch thread to finish");
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        SwitchListItem item = adapter.getItem(position);

        // If clicked on a title show a time picker dialog
        new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                    }
                }, item.getHour(), item.getMinute(), true).show();
    }

    public void addItem(boolean isDay, int time, SwitchListItem.Type type) {
        SwitchListItem period = new SwitchListItem(isDay, time, type);
        adapter.add(period);
    }

    /**
     * Removes all duplicates and unnecessary switches from the "items" array list.
     * @return boolean True if an item was removed, else false.
     */
    public boolean removeDuplicates() {
        // Get the data of the first elements.
        boolean prevDay = !items.get(0).isDay();
        int prevTime = -1;
        boolean removed = false;

        for(int i = 0, size = items.size(); i < size; i++) {
            SwitchListItem item = items.get(i);

            boolean isDay = item.isDay();
            int time = item.getTime();
            SwitchListItem.Type type = item.getType();

            if(time > prevTime && isDay != prevDay || type != SwitchListItem.Type.center) {
                prevTime = time;
                prevDay = isDay;
            } else {
                // Remove the item from the list.
                items.remove(i);
                i--;
                size--;
                removed = true;
            }
        }

        return removed;
    }

    /**
     * Removes an item from the day editor.
     */
    public void removeItem(int position) {
        // Validate if we will not remove the first or last item.
        if(position <= 0 || position >= items.size()) {
            System.err.println("Attempted to remove first or last item from the day editor.");
        } else {
            items.remove(position);

            removeDuplicates();

            resetListViewAdapter();
        }
    }

    public void insertItem(boolean isDay, int time) {
        addItem(isDay, time, SwitchListItem.Type.center);

        sortItems();

        // Only update the adapter if items where removed.
        if(removeDuplicates()) {
            resetListViewAdapter();
        }
    }

    /**
     * Sorts all currently displayed items.
     */
    private void sortItems() {
        adapter.sort(new Comparator<SwitchListItem>() {
            @Override
            public int compare(SwitchListItem lhs, SwitchListItem rhs) {
                if(lhs.getTime() == rhs.getTime()) {
                    return 0;
                } else if(lhs.getTime() > rhs.getTime()) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
    }

    private void resetListViewAdapter() {
        adapter = new DayListViewAdapter(this,R.layout.switch_list_element,items, this);
        listView.setAdapter(adapter);
    }
}