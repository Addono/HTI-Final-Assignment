/**
 * @author Stevie van der Loo
 * @Description This class is used as a convenience class to store properties of the switch & use or alter them.
 */
package org.thermostatapp.util;

public class Switch {
    public String type;
    public boolean state;
    public String time;

    public int time_int;
    public int dur;

    /**
     * Constructor
     * @param type
     * @param state
     * @param time
     */
    public Switch(String type, boolean state, String time) {
        this.type = type;
        this.state = state;
        this.time = time;
        String front = time.substring(0, 2);
        // Get the first 2 digits if they are there.
        String back = time.substring(3, 5);
        // Get the last 2 digits if they are there.
        int front_int = Integer.parseInt(front);
        int back_int = Integer.parseInt(back);
        this.time_int = front_int * 100
                + (int) ((float) back_int / 60.0 * 100.0);
    }

    /*
     * Constructor
     * @param type
     * @param state
     * @param time
     * @param time_int
     * @param dur

    public Switch(String type, boolean state, String time, int time_int, int dur) {
        this.type = type;
        this.state = state;
        this.time = time;
        this.time_int = time_int;
        this.dur = dur;
    }
*/
    /** GET Methods */

    public String getType() {
        return this.type;
    }

    public boolean getState() {
        return this.state;
    }

    public String getTime() {
        return this.time;
    }

    public int getTime_Int() {
        return this.time_int;
    }

    public int getDur() {
        return this.dur;
    }

    /** SET Methods */
    public void setType(String type) {
        // Do a dimension check.
        if (type.equals("day") || type.equals("night"))
            this.type = type;
    }

    public void setDur(int dur) {
        this.dur = dur;
    }

    public void setState(boolean s) {
        this.state = s;
    }

    public void setTime(String t) {
        // Do a dimension check.
        if (Switch.isValidTimeSyntax(t)) {
            this.time = t;
            String front = t.substring(0, 2); // Get the first 2 digits if they
            // are there.
            String back = t.substring(3, 5); // Get the last 2 digits if they
            // are there.
            int front_int = Integer.parseInt(front);
            int back_int = Integer.parseInt(back);
            this.time_int = front_int * 100
                    + (int) ((float) back_int / 60.0 * 100.0);
        }
    }

    /**
     * Converts the objects properties to the appropriate XML string format
     * @return
     */
    public String toXMLString() {
        String status = "off";
        if (this.state)
            status = "on";

        return ("<switch type=\"" + this.type + "\" state=\"" + status + "\">"
                + this.time + "</switch>");
    }

    /**
     * Checks whether the syntax for time is correct
     * @param t
     * @return
     */
    public static boolean isValidTimeSyntax(String t) {
        boolean success = false;
        if (t.length() == 5) {
            String front = t.substring(0, 2);
            // Get the first 2 digits if they are there.
            String back = t.substring(3, 5);
            // Get the last 2 digits if they are there.

            try {
                int front_int = Integer.parseInt(front);
                int back_int = Integer.parseInt(back);

                // Range for the first 2 digits, is the hour range which goes
                // from 0 to 23. 24 is not counted, because 24:00 = 00:00.
                // Range for the last 2 digits, is the minutes range which goes
                // from 0 to 59. 60 not counted for the same reason.
                if (front_int >= 0 && front_int < 24 && back_int >= 0
                        && back_int < 60 && t.charAt(2) == ':')
                    // Char comparison at the end.
                    success = true;
            } catch (NumberFormatException e) {
                success = false;
            }
        }
        return success;
    }

/*
    public Switch getCopy() {
        return (new Switch(this.type, this.state, this.time, this.time_int,
                this.dur));
    }
*/
}