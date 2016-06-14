/**
 * Class for testing Web Service (http://wwwis.win.tue.nl/2id40-ws), 
 * gives a few examples of 
 * getting data from and uploading data to the server
 */
package nl.tue.demothermostat;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.content.Intent;
import java.util.ArrayList;
import nl.tue.demothermostat.DayListViewAdapter;

import org.thermostatapp.util.*;

public class DayEditor extends ListActivity {
    Intent intent;
    static String day;

    private static ArrayList<SwitchListItem> items = new ArrayList<SwitchListItem>();
    private DayListViewAdapter adapter;

    WeekProgram wpg;
    ArrayList<Switch> switches;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.day_editor);

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

                    System.out.println("Amount of switches: " + switches.size());

                    int prevTime = -1;
                    boolean prevDay = true;

                    for(int i = 0; i < switches.size(); i++) {
                        Switch s = switches.get(i);
                        boolean isDay = s.getType().equals("day");
                        int time = s.getTime_Int();

                        if(time > prevTime && isDay != prevDay) {
                            addItem(new SwitchListItem(isDay, time));

                            prevTime = time;
                            prevDay = isDay;
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error from getdata " + e);
                }
            }
        }).start();

        adapter = new DayListViewAdapter(this,
                R.layout.switch_list_element,
                items);

        // Create the list adapter for our list of days.
        setListAdapter(adapter);

        // Wait until the WPG fetch thread is finished.
        while(items.size() == 0) {
            System.out.println("Waiting for WPG fetch thread to finish");
        }
    }

    public void addItem(SwitchListItem item) {
        items.add(item);
    }
}