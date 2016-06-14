package nl.tue.demothermostat;

/**
 * Created by s150376 on 6/14/2016.
 */
public class SwitchListItem {
    private boolean isDay;
    private int time;

    public SwitchListItem(boolean isDay, int time) {
        this.isDay = isDay;
        this.time = time;
    }

    public int getTime() {
        return this.time;
    }

    public boolean getIsDay() {
        return isDay;
    }

    @Override
    public String toString() {
        return String.valueOf(getTime());
    }
}
