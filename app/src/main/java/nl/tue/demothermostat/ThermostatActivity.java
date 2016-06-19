package nl.tue.demothermostat;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;
import android.widget.ViewFlipper;
import android.content.Intent;
import com.triggertrap.seekarc.SeekArc;
import android.R.*;


import org.thermostatapp.util.*;
import java.util.Timer;
import java.util.TimerTask;

/**
 * TODO: Implement holliday mode.
 */
/**
 * @author Adriaan Knapen <a.d.knapen@student.tue.nl>
 */
public class ThermostatActivity extends Activity {

    private SeekArc seekArcTempTemp, seekArcDayTemp, seekArcNightTemp; // A seekArc for each temperature which can be set.
    private SeekArc[] seekArc;
    private TextView dayTempText, tempTempText, nightTempText,
            serverDayTempText, serverCurrTempText, serverNightTempText,
            serverDayTempTitleText, serverCurrTempTitleText, serverNightTempTitleText,
            prevDayTempText, prevTempTempText, prevNightTempText;
    private TextView[] tempText, serverTempText, serverTempTitleText, prevTempText;
    private LinearLayout dayTempBtn, tempTempBtn, nightTempBtn;
    private ViewFlipper arcView;
    private Animation slide_in_left, slide_in_right, slide_out_left, slide_out_right;
    private Button weekOverviewBtn, holidayModeBtn;
    private Intent intent;

    private int[] angle = new int[3], angle_prev = new int[3];
    private int angle_min = 0;
    private int angle_max = 250;
    private int angle_extreme_threshhold = 14;
    private double min_temp = 5;
    private double max_temp = 30;

    private int poll_time = 1000; // The time inbetween each poll cycle in ms.

    private int groupNumber = 29;

    private String[] temp = new String[3];
    private String[] prevTemp = new String[3];


    private int day_arc_tab = 0;
    private int temp_arc_tab = 1;
    private int night_arc_tab = 2;

    private int prevTarget = -1;

    private static boolean activityVisible;

    public static boolean isActivityVisible() {
        return activityVisible;
    }

    public static void activityResumed() {
        activityVisible = true;
    }

    public static void activityPaused() {
        activityVisible = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        activityResumed();
        System.out.println("App resumed");
    }

    @Override
    protected void onPause() {
        super.onPause();
        activityPaused();
        System.out.println("App paused");
    }

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

        // Define all server temperatures text elements of the header.
        serverDayTempText = (TextView) findViewById(R.id.serverDayTemp);
        serverCurrTempText = (TextView) findViewById(R.id.serverCurrentTemp);
        serverNightTempText = (TextView) findViewById(R.id.serverNightTemp);

        serverTempText = new TextView[3];
        serverTempText[0] = serverDayTempText;
        serverTempText[1] = serverCurrTempText;
        serverTempText[2] = serverNightTempText;

        // Define all server temperature title text elements of the header.
        serverDayTempTitleText = (TextView) findViewById(R.id.dayTempText);
        serverCurrTempTitleText = (TextView) findViewById(R.id.currentTempText);
        serverNightTempTitleText = (TextView) findViewById(R.id.nightTempText);

        serverTempTitleText = new TextView[]{
                serverDayTempTitleText,
                serverCurrTempTitleText,
                serverNightTempTitleText
        };

        // Define all temporary temperature text views.
        prevDayTempText = (TextView) findViewById(R.id.prevDayTemp);
        prevTempTempText = (TextView) findViewById(R.id.prevTempTemp);
        prevNightTempText = (TextView) findViewById(R.id.prevNightTemp);

        prevTempText = new TextView[] {
                prevDayTempText,
                prevTempTempText,
                prevNightTempText
        };

        dayTempBtn = (LinearLayout) findViewById(R.id.dayTempBtn);
        tempTempBtn = (LinearLayout) findViewById(R.id.currentTempBtn);
        nightTempBtn = (LinearLayout) findViewById(R.id.nightTempBtn);

        arcView = (ViewFlipper) findViewById(R.id.seekArcs);

        holidayModeBtn = (Button)findViewById(R.id.holiday_mode);
        weekOverviewBtn = (Button)findViewById(R.id.week_overview);

        String main_server = "http://wwwis.win.tue.nl/2id40-ws/";
        String backup_server = "http://pcwin889.win.tue.nl/2id40-ws/";

        HeatingSystem.BASE_ADDRESS = main_server + groupNumber;
        HeatingSystem.WEEK_PROGRAM_ADDRESS = HeatingSystem.BASE_ADDRESS + "/weekProgram";

        // Create animations
        slide_in_left = AnimationUtils.loadAnimation(this, R.anim.slide_in_left);
        slide_in_right = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
        slide_out_left = AnimationUtils.loadAnimation(this, R.anim.slide_out_left);
        slide_out_right = AnimationUtils.loadAnimation(this, R.anim.slide_out_right);

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

        // Create a new thread which keeps polling the server for new data.
        new Thread(new Runnable() {
                @Override
                public void run() {
                    Timer updateCurrentTemp = new Timer();
                    updateCurrentTemp.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            // Check if the app runs in the foreground, if so request updated values of the day, current, and night temperature.
                            if(ThermostatActivity.isActivityVisible()) {
                                try {
                                    String[] tempOptions = {
                                            "dayTemperature",
                                            "currentTemperature",
                                            "nightTemperature"
                                    };

                                    for (int i = 0; i < tempOptions.length; i++) {
                                        setTemp(i, HeatingSystem.get(tempOptions[i]));
                                    }
                                } catch (Exception e) {
                                    System.err.println("Error from getdata " + e);
                                }
                            }

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
            }
        }).start();

        // Initialise listener for all seek arcs.
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

                    // Post the new temperature to the server.
                    postTemp(i, angleToTemp(angle[i]));

                    /**
                     * TODO Add revert option when temperature is changed (use a "snackbar").
                     */
                }

                @Override
                public void onStartTrackingTouch(SeekArc seekArc) {
                    setPrevTemp(i);
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

        // Add an on touch listener to the day temperature button.
        dayTempBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motion) {
                if(motion.getAction() == MotionEvent.ACTION_UP) {
                    setDisplayedArc(day_arc_tab);
                }
                return false;
            }
        });

        // Add an on touch listener to the temperoray temperature button.
        tempTempBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motion) {
                if(motion.getAction() == MotionEvent.ACTION_UP) {
                    setDisplayedArc(temp_arc_tab);
                }
                return false;
            }
        });

        // Add an on touch listener to the night temperature button.
        nightTempBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motion) {
                if(motion.getAction() == MotionEvent.ACTION_UP) {
                    setDisplayedArc(night_arc_tab);
                }
                return false;
            }
        });

        // Set the default arc to the temporary temperature arc.
        setDisplayedArc(temp_arc_tab);

        // Add a listener for the week overview button.
        weekOverviewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), WeekOverview.class);
                startActivity(intent);
            }
        });

        // Add a listener for the holiday mode toggle.
        holidayModeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                 * TODO: Implement holiday mode button.
                 */

                /*
                 * if inHolidayMode
                 *      inHolidayMode = false
                 *      serverSetHolidayMode(false)
                 *      disableAllButtons(false) // Re-enable all buttons.
                 * else
                 *      inHolidayMode = true
                 *      serverSetHolidayMode(true)
                 *      disableAllButtons(true) // Enable all buttons.
                 */

            }
        });
    }

    private void setDisplayedArc(int target) {
        // Prevent transition to the current arc.
        if (prevTarget != target) {
            // Set the animations.
            if (prevTarget != -1) { // Do not animate if this is the initial setup.
                // Check if we have to transition to the left or right, adjust animations accordingly.
                if (prevTarget < target) {
                    arcView.setInAnimation(slide_in_right);
                    arcView.setOutAnimation(slide_out_left);
                } else {
                    arcView.setInAnimation(slide_in_left);
                    arcView.setOutAnimation(slide_out_right);
                }
            }

            arcView.setDisplayedChild(target);

            // Set the style of each arc such that only the target arc has the selected style.
            for (int i = 0; i < seekArc.length; i++) {
                if (target == i) {
                    serverTempText[i].setTextAppearance(this, R.style.headerContentSelected);
                    serverTempTitleText[i].setTextAppearance(this, R.style.headerTitleSmallSelected);
                } else {
                    serverTempText[i].setTextAppearance(this, R.style.headerContent);
                    serverTempTitleText[i].setTextAppearance(this, R.style.headerTitleSmall);
                }
            }

            prevTarget = target; // Update the previous target variable for the next transition.
        }
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

        // Post the new temperature to the server.
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

        //
        if(allServerVars[target] != "targetTemperature") {
            setTemp(target, temp);
        }
    }

    private void postTemp(int i, double temp) { postTemp(i, String.valueOf(temp)); }

    private void setPrevTemp(int target) {
        this.prevTemp[target] = this.temp[target];
        this.prevTempText[target].setText("Previous: " + this.prevTemp[target] + "℃");
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
                Intent intent = new Intent(view.getContext(), DayEditor.class);
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