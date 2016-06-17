package nl.tue.demothermostat;

/**
 * Created by s150376 on 6/14/2016.
 */
public class SwitchListItem {
    private boolean isDay, intersection;
    private int time;

    public SwitchListItem(boolean isDay, boolean intersection, int time) {
        this.isDay = isDay;
        this.intersection = intersection;
        this.time = time;
    }

    public int getTime() {
        return this.time;
    }

    public int getHour() { return (int) Math.floor(getTime() / 100);}

    public int getMinute() {return (int) Math.floor((getTime() % 100f) * 60f / 100f);}

    public boolean getIsDay() {
        return isDay;
    }

    public boolean getIsIntersection() { return intersection; }

    @Override
    public String toString() {
        String hour = String.valueOf(getHour());
        String minute = String.valueOf(getMinute());

        // Make sure the minute consists of two characters.
        if(getMinute() < 10) {
            minute = "0" + minute;
        }

        return hour + ":" + minute;
    }

}
