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
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.day_list);

//        timePicker = (TimePicker) findViewById(R.id.timePicker);
        listView = (ListView) findViewById(R.id.list);

        intent = getIntent();
        day = intent.getStringExtra("day");
        items.clear();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    wpg = HeatingSystem.getWeekProgram();

                    if(wpg == null) {
                        System.err.println("WPG not fetched");
                    }

                    switches = wpg.data.get(DayEditor.day);


                    int prevTime = -1;
                    boolean prevDay = true;

                    for(int i = 0; i < switches.size(); i++) {
                        Switch s = switches.get(i);
                        boolean isDay = s.getType().equals("day");
                        int time = s.getTime_Int();

                        if(time > prevTime && isDay != prevDay) {
                            addItem(isDay, time);

                            prevTime = time;
                            prevDay = isDay;
                        }
                    }

                    // Add the midnight switch.
                    addItem(false, 2400);
                } catch (Exception e) {
                    System.err.println("Error from getdata " + e);
                }
            }
        }).start();

        adapter = new DayListViewAdapter(this,
                R.layout.switch_list_element,
                items);

        // Create the list adapter for our list of temperature switches.
        listView.setAdapter(adapter);

        // Add an click listener to the list view.
        listView.setOnItemClickListener(this);


        //timePicker.setIs24HourView(true);
        //timePicker.setEnabled(false);

        // Wait until the WPG fetch thread is finished.
        while(items.size() == 0) {
            System.out.println("Waiting for WPG fetch thread to finish");
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        if(position % 2 == 1) {
            addItem(false, 2401);
        } else {
            SwitchListItem item = adapter.getItem(position);
            new TimePickerDialog(this,
                    new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        }
                    }, item.getHour(), item.getMinute(), true).show();
        }
    }

    public void addItem(boolean isDay, int time) {
        SwitchListItem period = new SwitchListItem(isDay, false, time);
        adapter.add(period);

        // Do not add an intersection item after midnight.
        if(time < 2400) {
            SwitchListItem intersection = new SwitchListItem(isDay, true, time);
            adapter.add(intersection);
        }
    }

    /**
     * TODO: Implement
     */
    public void removeDuplicates() {

    }

    public void sortItems() {
        adapter.sort(new Comparator<SwitchListItem>() {
            @Override
            public int compare(SwitchListItem lhs, SwitchListItem rhs) {
                if(lhs.getTime() == rhs.getTime()) {
                    if(rhs.getIsIntersection()) {
                        return -1;
                    } else {
                        return 1;
                    }
                } else if(lhs.getTime() > rhs.getTime()) {
                    return 1;
                } else {
                    return -1;
                }

            }
        });
    }
}