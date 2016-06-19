/**
 * Class for testing Web Service (http://wwwis.win.tue.nl/2id40-ws), 
 * gives a few examples of 
 * getting data from and uploading data to the server
 */
package nl.tue.demothermostat;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.content.Intent;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.borax12.materialdaterangepicker.time.RadialPickerLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;

import org.thermostatapp.util.*;

/*
 * TODO: Add support to upload the day schedule.(Implemented)
 * TODO: Add support to insert a switch.
 * TODO: Add support to switch a switch from day to night for the initial and final stage.
 * TODO: Add revert option after switches have been altered.
 * TODO: Add support for changing already present times. (Implemented)
 * TODO: Add titles to pages. (easy)
 */

/**
 * @author Adriaan Knapen <a.d.knapen@student.tue.nl>
 */
public class DayEditor extends Activity implements com.borax12.materialdaterangepicker.time.TimePickerDialog.OnTimeSetListener {
    Intent intent;
    static String day;

    private static ArrayList<SwitchListItem> items = new ArrayList<SwitchListItem>();
    private DayListViewAdapter adapter;
    ListView listView;

    RelativeLayout addItem;
    com.borax12.materialdaterangepicker.time.TimePickerDialog tpd;

    WeekProgram wpg;
    ArrayList<Switch> switches;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.day_editor);

        listView = (ListView) findViewById(R.id.list);
        addItem = (RelativeLayout) findViewById(R.id.add_item);

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


                            // Give the first item the "first" type, all other the "center" type.
                            if (i == 0) {
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

        // Create the time range picker.
        tpd = com.borax12.materialdaterangepicker.time.TimePickerDialog.newInstance(
                DayEditor.this,
                12, 00,
                true
        );

        tpd.setTitle("Add day period");

        addItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddItem();
            }
        });

        // Wait until the WPG fetch thread is finished to prevent the thread from pausing to early, and therefore missing UI items.
        while(items.size() == 0) {
            System.out.println("Waiting for WPG fetch thread to finish");
        }
    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int hourOfDayEnd, int minuteEnd) {
        int startTime = combineHourMinute(hourOfDay, minute);
        int endTime = combineHourMinute(hourOfDayEnd, minuteEnd);

        if(startTime == endTime) {
            Toast.makeText(this, "Item not added.\nStart and end time shouldn't be equal.", Toast.LENGTH_LONG).show();
            tpd.setStartTime(hourOfDayEnd, minuteEnd);
        } else if(startTime < endTime) {
            Toast.makeText(this, "Item not added.\nStart time should be before end time.", Toast.LENGTH_LONG).show();
            tpd.setStartTime(hourOfDayEnd, minuteEnd);
        } else {
            insertPeriod(true, startTime, endTime);
        }
    }

    public void showAddItem() {
        tpd.show(getFragmentManager(), "Timepickerdialog");
    }

    public void addItem(boolean isDay, int time, SwitchListItem.Type type) {
        SwitchListItem period = new SwitchListItem(isDay, time, type);

        if(type == SwitchListItem.Type.center) {
            adapter.insert(period, 1);
        } else {
            adapter.add(period);
        }
    }

    public void changeTime(int position, int time) {
        items.get(position).setTime(time);

        sortItems();

        if(removeDuplicates()) {
            resetListViewAdapter();
        }
    }

    public void changeTime(int position, int hour, int minute) {
        int time = hour * 100 + (int) Math.floor(minute * 100 / 60);

        changeTime(position, time);
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

        // First sort, since the algorithm assumes a sorted list of elements
        sortItems();

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

            postDaySchedule();
        }
    }


    /*
     * TODO: Allow changing the type of first and last element.
     */
    /**
     * Inserts a period, either day or night. Can create up to two
     * @param boolean If the inserted period should be a night or day period.
     * @param int The start time as combined hours and minutes between 0 and 2400.
     * @param int The end time as combined hours and minutes between 0 and 2400.
     */
    public void insertPeriod(boolean isDay, int startTime, int endTime) {
        boolean removed = false;

        for(int i = 0; i < items.size(); i++) {
            SwitchListItem s = items.get(i);

            if(s.getTime() >= startTime && s.getTime() <= endTime) {
                removeItem(i);
                removed = true;
            }
        }

        addItem(isDay, startTime, SwitchListItem.Type.center);
        addItem(!isDay, endTime, SwitchListItem.Type.center);

        sortItems();

        // Only update the adapter if items where removed.
        if(removeDuplicates()) {
            resetListViewAdapter();
        }

        // Push the schedule to the server.
        postDaySchedule();
    }

    /**
     * Sorts all currently displayed items.
     */
    private void sortItems() {
        adapter.sort(new Comparator<SwitchListItem>() {
            @Override
            public int compare(SwitchListItem lhs, SwitchListItem rhs) {
                if (lhs.getTime() == rhs.getTime()) {
                    return 0;
                } else if (lhs.getTime() > rhs.getTime()) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
    }

    private void resetListViewAdapter() {
        adapter = new DayListViewAdapter(this,
                R.layout.switch_list_element,
                items,
                this);
        listView.setAdapter(adapter);
    }

    public void postDaySchedule() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    WeekProgram wpg = HeatingSystem.getWeekProgram();

                    for(int i = 0, day = 0, night = 0; i < 10; i++) {
                        if(i + 1 < items.size() && items.get(i).getTime() <= 2400) {
                            SwitchListItem item = items.get(i + 1);

                            String dayNight;
                            String time = item.toString();

                            if(item.isDay()) {
                                dayNight = "day";
                                day++;
                            } else {
                                night++;
                                dayNight = "night";
                            }

                            System.err.println(dayNight);

                            wpg.data.get(DayEditor.day).set(i, new Switch(dayNight, true, time));
                        } else {
                            String dayNight;

                            if(day < night) {
                                day++;
                                dayNight = "day";
                            } else {
                                night++;
                                dayNight = "night";
                            }

                            System.err.println(dayNight);

                            wpg.data.get(DayEditor.day).set(i, new Switch(dayNight, false, "00:00"));
                        }
                    }

                    HeatingSystem.setWeekProgram(wpg);
                } catch (Exception e) {
                    System.err.println("Error from getdata " + e);
                }
            }
        }).start();
    }

    public String hourMinuteToString(int hour, int minute) {
        String hourAsString = String.valueOf(hour);
        String minuteAsString = String.valueOf(minute);

        // Make sure the hours consist of two characters.
        if(hour < 10) {
            hourAsString = "0" + hourAsString;
        }

        // Make sure the minute consists of two characters.
        if(minute < 10) {
            minuteAsString = "0" + minuteAsString;
        }


        return hourAsString + ":" + minuteAsString;
    }

    public int combineHourMinute(int hour, int minute) {
        return hour * 100 + (minute * 100) / 60;
    }
}