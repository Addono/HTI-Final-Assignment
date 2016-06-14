/**
 * Class for testing Web Service (http://wwwis.win.tue.nl/2id40-ws), 
 * gives a few examples of 
 * getting data from and uploading data to the server
 */
package nl.tue.demothermostat;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.content.Intent;

import org.thermostatapp.util.*;

public class DayEditor extends Activity {
    Intent intent;
    String day;

    Button getdata, putdata;
    TextView data1, data2;
    String getParam, oldv, newv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.day_editor);

        intent = getIntent();
        day = intent.getStringExtra("day");

        getdata = (Button) findViewById(R.id.getdata);
        putdata = (Button) findViewById(R.id.putdata);
        data1 = (TextView) findViewById(R.id.data1);
        data2 = (TextView) findViewById(R.id.data2);

        data1.setText(day);

        /*
         When the user clicks on GET Data button the value of the corresponding parameter is read from the server
         and displayed in TextView data1
         */
        getdata.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        getParam = "";
                        try {
                            getParam = HeatingSystem.get("currentTemperature");
                            /*
                                    HeatingSystem.get("day");
									HeatingSystem.get("time");
									HeatingSystem.get("targetTemperature");
									HeatingSystem.get("dayTemperature");
									HeatingSystem.get("nightTemperature");
									HeatingSystem.get("weekProgramState");
							*/
                            data1.post(new Runnable() {
                                @Override
                                public void run() {
                                    data1.setText(getParam);
                                }
                            });
                        } catch (Exception e) {
                            System.err.println("Error from getdata " + e);
                        }
                    }
                }).start();
            }
        });

        /* When the user clicks on PUT Data button the old value of the corresponding parameter is read from the server
        and displayed in TextView data1, the new uploaded value is displayed in TextView data2
         */
        putdata.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            oldv = HeatingSystem.get("targetTemperature");
                            HeatingSystem.put("targetTemperature", "18.0");
                            newv = HeatingSystem.get("targetTemperature");

                            /* Uncomment the following parts to see how to work with the properties of the week program */
                            // Get the week program
                            WeekProgram wpg = HeatingSystem.getWeekProgram();
                            // Set the week program to default
                            wpg.setDefault();
                            /*
                            wpg.data.get("Monday").set(5, new Switch("day", true, "07:30"));
                            wpg.data.get("Monday").set(1, new Switch("night", true, "08:30"));
                            wpg.data.get("Monday").set(6, new Switch("day", true, "18:00"));
                            wpg.data.get("Monday").set(7, new Switch("day", true, "12:00"));
                            wpg.data.get("Monday").set(8, new Switch("day", true, "18:00"));
                            boolean duplicates = wpg.duplicates(wpg.data.get("Monday"));
                            System.out.println("Duplicates found "+duplicates);
                            */
                            //Upload the updated program
                            //HeatingSystem.setWeekProgram(wpg);

                            data1.post(new Runnable() {
                                @Override
                                public void run() {
                                    data1.setText(oldv);
                                }
                            });
                            data2.post(new Runnable() {
                                @Override
                                public void run() {
                                    data2.setText(newv);
                                }
                            });
                        } catch (Exception e) {
                            System.err.println("Error from getdata " + e);
                        }
                    }
                }).start();

            }
        });
    }
}