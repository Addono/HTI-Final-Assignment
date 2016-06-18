package nl.tue.demothermostat;

/**
 * @author Adriaan Knapen <a.d.knapen@student.tue.nl>
 */
public class SwitchListItem {
    public enum Type {center, first, last};

    private boolean isDay;
    private int time;
    private Type type;

    public SwitchListItem(boolean isDay, int time, Type type) {
        this.isDay = isDay;
        this.time = time;
        this.type = type;
    }

    public int getTime() {
        return this.time;
    }

    public int getHour() { return (int) Math.floor(getTime() / 100);}

    public int getMinute() {return (int) Math.floor((getTime() % 100f) * 60f / 100f);}

    public boolean isDay() {
        return isDay;
    }

    public Type getType() {
        return type;
    }

    public boolean isOuter() {
        return type != Type.center;
    }

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
