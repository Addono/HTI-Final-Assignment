package nl.tue.demothermostat;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.triggertrap.seekarc.SeekArc;
import android.widget.ViewFlipper;


import org.thermostatapp.util.*;
import java.util.Timer;
import java.util.TimerTask;

public class ThermostatActivity extends Activity {

    private SeekArc seekArcTempTemp, seekArcDayTemp, seekArcNightTemp;
    private SeekArc[] seekArc;
    private TextView dayTempText, tempTempText, nightTempText,
        serverDayTempText, serverNightTempText, serverCurrTempText;
    private TextView[] tempText;
    private LinearLayout dayTempBtn, tempTempBtn, nightTempBtn;
    private ViewFlipper arcView;

    private int[] angle = new int[3], angle_prev = new int[3];
    private int angle_min = 0;
    private int angle_max = 250;
    private int angle_extreme_threshhold = 14;
    private double min_temp = 5;
    private double max_temp = 30;

    private int poll_time = 1000; // The time inbetween each poll cycle in ms.

    private int groupNumber = 29;

    private String[] temp = new String[3];


    private int day_arc_tab = 0;
    private int temp_arc_tab = 1;
    private int night_arc_tab = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_temperature);

        // Define the seekArcs
        seekArcDayTemp = (SeekArc) findViewById(R.id.seekArcDayTemp);
        seekArcTempTemp = (SeekArc) findViewById(R.id.seekArcTempTemp);
        seekArcNightTemp = (SeekArc) findViewById(R.id.seekArcNightTemp);

        seekArc = new SeekArc[3];
        seekArc[0] = seekArcDayTemp;
        seekArc[1] = seekArcTempTemp;
        seekArc[2] = seekArcNightTemp;

        // Define the temparture texts
        dayTempText= (TextView) findViewById(R.id.dayTemp);
        tempTempText = (TextView) findViewById(R.id.tempTemp);
        nightTempText = (TextView) findViewById(R.id.nightTemp);

        tempText =  new TextView[3];
        tempText[0] = dayTempText;
        tempText[1] = tempTempText;
        tempText[2] = nightTempText;

        serverDayTempText = (TextView) findViewById(R.id.serverDayTemp);
        serverCurrTempText = (TextView) findViewById(R.id.serverCurrentTemp);
        serverNightTempText = (TextView) findViewById(R.id.serverNightTemp);

        dayTempBtn = (LinearLayout) findViewById(R.id.dayTempBtn);
        tempTempBtn = (LinearLayout) findViewById(R.id.currentTempBtn);
        nightTempBtn = (LinearLayout) findViewById(R.id.nightTempBtn);

        arcView = (ViewFlipper) findViewById(R.id.seekArcs);

        String main_server = "http://wwwis.win.tue.nl/2id40-ws/";
        String backup_server = "http://pcwin889.win.tue.nl/2id40-ws/";

        HeatingSystem.BASE_ADDRESS = main_server + groupNumber;
        HeatingSystem.WEEK_PROGRAM_ADDRESS = HeatingSystem.BASE_ADDRESS + "/weekProgram";

        // Get the current temperature.
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String[] tempOptions = {
                            "dayTemperature",
                            "targetTemperature",
                            "nightTemperature"
                    };

                    for(int i = 0; i < tempOptions.length; i++) {
                        setTemp(i, Double.parseDouble(HeatingSystem.get(tempOptions[i])));
                    }
                } catch (Exception e) {
                    System.err.println("Error from getdata " + e);
                }
            }
        }).start();

        Timer updateCurrentTemp = new Timer();
        updateCurrentTemp.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String[] tempOptions = {
                                    "dayTemperature",
                                    "currentTemperature",
                                    "nightTemperature"
                            };

                            for(int i = 0; i < tempOptions.length; i++) {
                                setTemp(i, HeatingSystem.get(tempOptions[i]));
                            }
                        } catch (Exception e) {
                            System.err.println("Error from getdata " + e);
                        }
                    }
                }).start();

                // Redraw the current temperature, if it is set (thus it has to be not null).
                if(getCurrTemp() != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateAllTempText();
                        }
                    });
                }
            }
        }, 0, poll_time);


        for(int b = 0; b < seekArc.length; b++) {
            final int i = b;
            seekArc[i].setOnSeekArcChangeListener(new SeekArc.OnSeekArcChangeListener() {

                @Override
                public void onStopTrackingTouch(SeekArc seekArc) {
                    /**
                     * Tries to anticipate if the user wanted to select a
                     * maximum or minimum. If so, set the angle to the
                     * the corresponding value.
                     **/
                    if (Math.abs(angle[i] - angle_max) <= angle_extreme_threshhold
                            && angle[i] - angle_prev[i] > 0) {
                        setTemp(i, max_temp);
                    }

                    if (angle[i] <= angle_extreme_threshhold
                            && angle[i] - angle_prev[i] < 0) {
                        setTemp(i, min_temp);
                    }

                    postTemp(i, angleToTemp(angle[i]));
                }

                @Override
                public void onStartTrackingTouch(SeekArc seekArc) {
                }

                @Override
                public void onProgressChanged(SeekArc seekArc, int progress,
                                              boolean fromUser) {
                    /**
                     * Update the temporary temperature values when
                     * the arcs position is changed.
                     */
                    angle_prev[i] = angle[i];
                    angle[i] = seekArc.getProgress();
                    tempText[i].setText(getTemp(i));
                }
            });
        }

        dayTempBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motion) {
                if(motion.getAction() == MotionEvent.ACTION_UP) {
                    arcView.setDisplayedChild(day_arc_tab);
                }
                return false;
            }
        });

        tempTempBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motion) {
                if(motion.getAction() == MotionEvent.ACTION_UP) {
                    arcView.setDisplayedChild(temp_arc_tab);
                }
                return false;
            }
        });

        nightTempBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motion) {
                if(motion.getAction() == MotionEvent.ACTION_UP) {
                    arcView.setDisplayedChild(night_arc_tab);
                }
                return false;
            }
        });
    }

    /**
     * Converts a percentage to a temperature.
     * @param The percentage between the minimum and maximum temperature as an integer.
     * @return The corresponding temperature as a float.
     */
    private double angleToTemp(int angle) {
        double delta_temp = max_temp - min_temp;

        return Math.round(10.0 * (min_temp + angle * delta_temp / angle_max)) / 10.0;
    }

    private int tempToAngle(double temp) {
        return (int) Math.round(((temp - min_temp) * (double) angle_max) / (max_temp - min_temp));
    }

    private String getTemp(int target) {
        return angleToTemp(angle[target]) + "℃";
    }

    private void setTemp(int target, double temp) {
        int angle = tempToAngle(temp);
        System.out.println("Set " + target + " to " + temp + "C");
        seekArc[target].setProgress(angle);
        setAngle(target, angle);
        tempText[target].setText(getTemp(target));
    }

    private void setAngle(int target, int angle) {
        this.angle[target] = angle;
    }

    private void setTemp(int target, String temp) {
        this.temp[target] = temp;
    }

    private String getCurrTemp() {
        return temp[1];
    }

    private void updateAllTempText() {
        String unit = "℃";
        serverDayTempText.setText(temp[0] + unit);
        serverCurrTempText.setText(temp[1] + unit);
        serverNightTempText.setText(temp[2] + unit);
    }

    private void postTemp(int target, final String temp) {
        String[] allServerVars = {
                "dayTemperature",
                "targetTemperature",
                "nightTemperature"
        };

        final String serverVarName = allServerVars[target];

        // Get the current temperature.
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HeatingSystem.put(serverVarName, temp);
                } catch (Exception e) {
                    System.err.println("Error from getdata " + e);
                }
            }
        }).start();
    }

    private void postTemp(int i, double temp) {
        postTemp(i, String.valueOf(temp));
    }
        /**
        ImageView bPlus = (ImageView)findViewById(R.id.bPlus);
        bPlus.setImageResource(R.drawable.add_button);


        ImageView bMinus = (ImageView)findViewById(R.id.bMinus);
        temp = (TextView)findViewById(R.id.temp);
        Button weekOverview = (Button)findViewById(R.id.week_overview);

        weekOverview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), WeekOverview.class);
                startActivity(intent);
            }
        });

        Button testingWS = (Button)findViewById(R.id.testing_ws);

        testingWS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), TestingWS.class);
                startActivity(intent);
            }
        });

        seekBar = (SeekBar)findViewById(R.id.seekBar);
        seekBar.setProgress(vtemp);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                temp.setText(i + " \u2103");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
         */
}