/**
 * @author HTI students, Spring 2013, adjusted by N.Stash
 *
 */
package org.thermostatapp.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WeekProgram {
    /* Switches are stored in a hashmap, mapping every day to its
    corresponding set of switches */
    public Map<String, ArrayList<Switch>> data = new HashMap<String, ArrayList<Switch>>();
    private int[] nr_switches_active;
    public static String[] valid_days = { "Monday", "Tuesday", "Wednesday",
            "Thursday", "Friday", "Saturday", "Sunday" };

    /**
     * Constructor
     */
    public WeekProgram() {
        setDefault();
    }

    /**
     * Creates the default week program
     */
    public void setDefault() {
        nr_switches_active = new int[7];
        for (int i = 0; i < this.valid_days.length; i++) {
            nr_switches_active[i] = 5;
            String day = this.valid_days[i];
            this.data.put(day, new ArrayList<Switch>());
            this.data.get(day).add(new Switch("night", false, "00:00"));
            this.data.get(day).add(new Switch("night", false, "00:00"));
            this.data.get(day).add(new Switch("night", false, "00:00"));
            this.data.get(day).add(new Switch("night", false, "00:00"));
            this.data.get(day).add(new Switch("night", false, "00:00"));
            this.data.get(day).add(new Switch("day", false, "00:00"));
            this.data.get(day).add(new Switch("day", false, "00:00"));
            this.data.get(day).add(new Switch("day", false, "00:00"));
            this.data.get(day).add(new Switch("day", false, "00:00"));
            this.data.get(day).add(new Switch("day", false, "00:00"));
        }
		/* Create the default switches settings*/
        set_durations();
    }

    public String toXML() throws NullPointerException {
        StringBuilder build = new StringBuilder();
        String prefix;
        String suffix = "</week_program>";
        if (!HeatingSystem.getVacationMode())
            prefix = "<week_program state=\"on\">";
        else
            prefix = "<week_program state=\"off\">";

        // Add prefix.
        build.append(prefix).append("\n");
        // Construct all the days.
        for (int i = 0; i < this.valid_days.length; i++) {
            // Add the day
            String day = this.valid_days[i];

            build.append("<day name=\"" + day + "\">").append("\n");

            // Add the switches.
            ArrayList<Switch> switches = this.data.get(day);
            if (switches != null) {
                for (Switch sw : switches) {
                    build.append(sw.toXMLString()).append("\n");
                }
            }
            // Closing day tag.
            build.append("</day>").append("\n");
        }

        // Add suffix.
        build.append(suffix);

        return build.toString();
    }

    public boolean duplicates(ArrayList<Switch> switches) {
        boolean duplicatesFound = false;
        for (int i = 0; i < (switches.size() - 2) &&!duplicatesFound ; i++) {
            for (int j = i+1; j < switches.size() - 1; j++) {
                if ( switches.get(i).getState() && switches.get(j).getState() &&
                        switches.get(i).getType().equals(switches.get(j).getType()) &&
                        switches.get(i).getTime().equals(switches.get(j).getTime()) ) {
                    duplicatesFound = true;
                    break;
                }
            }
        }
        return duplicatesFound;
    }

/*
    public void check_duplicates(ArrayList<Switch> new_switches) {
        for (int i = 0; i < new_switches.size() - 1; i++) {
            if (new_switches.get(i).getState()
                    && new_switches.get(i + 1).getState())
                if (new_switches.get(i).getType() == new_switches.get(i + 1)
                        .getType()) {
                    for (int j = i + 1; j < new_switches.size() - 1; j++)
                        new_switches.set(j, new_switches.get(j + 1));
                    if (new_switches.get(new_switches.size() - 2).getType()
                            .equalsIgnoreCase("day"))
                        new_switches.set(new_switches.size() - 1, new Switch(
                                "night", false, "23:00"));
                    else
                        new_switches.set(new_switches.size() - 1, new Switch(
                                "day", false, "23:00"));
                    i -= 1;
                }
        }
    }
*/
    public void set_durations() {
        for (int i = 0; i < this.valid_days.length; i++) {

            for (int j = 0; j < data.get(valid_days[i]).size() - 1; j++) {
                if (data.get(valid_days[i]).get(j + 1).getState())
                    data.get(valid_days[i])
                            .get(j)
                            .setDur(data.get(valid_days[i]).get(j + 1)
                                    .getTime_Int()
                                    - data.get(valid_days[i]).get(j)
                                    .getTime_Int());
                else
                    data.get(valid_days[i])
                            .get(j)
                            .setDur(2400 - data.get(valid_days[i]).get(j)
                                    .getTime_Int());
            }
            if (this.nr_switches_active[i] == 10)
                data.get(valid_days[i])
                        .get(9)
                        .setDur(2400 - data.get(valid_days[i]).get(9)
                                .getTime_Int());
        }

    }

    public void set_switches_active(int i, int nr) {
        this.nr_switches_active[i] = nr;
    }

    //Setting switches. Switches list should always exactly consist out of 10 elements.
     //* @param day
     //* @param switches_list
     //* @param nr_switches
    public void setSwitches(String day, ArrayList<Switch> switches_list,
                            int nr_switches) {
        // Validate input???
        for (String d : this.valid_days) {
            if (d.equalsIgnoreCase(day)) {
                this.data.put(d, switches_list);
                for (int i = 0; i < 7; i++)
                    if (day.equalsIgnoreCase(WeekProgram.valid_days[i]))
                        this.nr_switches_active[i] = nr_switches;
            }
        }
        set_durations();
    }

/*

    public int get_nr_switches_active(int i) {
        return this.nr_switches_active[i];
    }

    public boolean AddSwitch(int start_time, int end_time, String type,
                             String day) {
        int selected_day = 0;
        for (int i = 0; i < valid_days.length; i++)
            if (day == valid_days[i])
                selected_day = i;

        ArrayList<Switch> new_switches = new ArrayList<Switch>();

        for (int i = 0; i < 10; i++) {

            if (data.get(day).get(i).getTime_Int() < start_time
                    && data.get(day).get(i).getState())
                new_switches.add(data.get(day).get(i));
            else {
                new_switches.add(new Switch(type, true,
                        int_time_to_string(start_time)));
                if (type == "day")
                    new_switches.add(new Switch("night", true,
                            int_time_to_string(end_time)));
                else
                    new_switches.add(new Switch("day", true,
                            int_time_to_string(end_time)));

                i = 10;
            }
        }
        for (int i = 0; i < this.nr_switches_active[selected_day]; i++) {
            if (data.get(day).get(i).getTime_Int() > end_time)
                new_switches.add(data.get(day).get(i));
        }
        check_duplicates(new_switches);
        int bu = nr_switches_active[selected_day];
        nr_switches_active[selected_day] = new_switches.size();
        if (nr_switches_active[selected_day] <= 10)
            for (int i = 0; i < 10 - nr_switches_active[selected_day]; i++) {
                if (data.get(day).get(data.get(day).size() - 2).getType()
                        .equalsIgnoreCase("day"))
                    new_switches.add(new Switch("night", false, "23:00"));
                else
                    new_switches.add(new Switch("day", false, "23:00"));
            }

        else {
            nr_switches_active[selected_day] = bu;
            return false;
        }
        while (new_switches.size() != 10)
            new_switches.remove(new_switches.size() - 1);
        check_duplicates(new_switches);
        int count_active_days = 0;
        int day_to_night = 0;
        int night_to_day = 0;
        while (count_active_days < 10
                && new_switches.get(count_active_days).getState()) {
            if (count_active_days != 0) {
                if (new_switches.get(count_active_days).getType() == "day")
                    day_to_night++;
                else
                    night_to_day++;
            }
            count_active_days++;
        }
        if (count_active_days <= 10 && day_to_night <= 5 && night_to_day <= 5) {
            nr_switches_active[selected_day] = count_active_days;
            data.put(day, new_switches);
            set_durations();
            return true;
        }
        return false;
    }

    public void RemoveFirstSwitch(String day) {
        for (int i = 0; i < 9; i++) {
            data.get(day).set(i, data.get(day).get(i + 1));
        }
        if (data.get(day).get(9).getType().equalsIgnoreCase("day"))
            data.get(day).set(data.get(day).size() - 1,
                    new Switch("night", false, "23:00"));
        else
            data.get(day).set(data.get(day).size() - 1,
                    new Switch("day", false, "23:00"));
        data.get(day).set(0,
                new Switch(data.get(day).get(0).getType(), true, "00:00"));
        set_durations();
    }

    public void RemoveSwitch(int i, String day) {
        for (int j = i; j < data.get(day).size() - 1; j++) {
            data.get(day).set(j, data.get(day).get(j + 1));
        }
        if (data.get(day).get(data.get(day).size() - 2).getType()
                .equalsIgnoreCase("day"))
            data.get(day).set(data.get(day).size() - 1,
                    new Switch("night", false, "23:00"));
        else
            data.get(day).set(data.get(day).size() - 1,
                    new Switch("day", false, "23:00"));
        check_duplicates(data.get(day));
        set_durations();
    }

    private String int_time_to_string(int time_var) {
        String hours = Integer.toString(time_var / 100);
        String mins = Integer.toString(time_var - time_var / 100 * 100);
        if (time_var < 1000)
            hours = "0" + hours;
        if (time_var - time_var / 100 * 100 < 10)
            mins = "0" + mins;

        return hours + ":" + mins;
    }
*/
}