package nl.tue.demothermostat;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;
import android.widget.TimePicker;

import java.util.ArrayList;

/**
 * @author Adriaan Knapen <a.d.knapen@student.tue.nl>
 */
public class DayListViewAdapter extends ArrayAdapter<SwitchListItem> {
    Context context;
    DayEditor dayEditor;
    ViewHolder holder = null;
    SwitchListItem precedingItem, succeedingItem;

    public DayListViewAdapter(Context context, int resourceId,
                              ArrayList<SwitchListItem> items,
                              DayEditor dayEditor) {
        super(context, resourceId, items);
        this.dayEditor = dayEditor;
        this.context = context;
    }

    private class ViewHolder {
        TextView title;
        View dot, lineUp, lineDown;
        Button button;
        LinearLayout listItem;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        final SwitchListItem item = getItem(position);

        if(item.getType() != SwitchListItem.Type.first) {
            precedingItem = getItem(position - 1);
        }

        if(item.getType() != SwitchListItem.Type.last) {
            succeedingItem = getItem(position + 1);
        }

        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.switch_list_element, null);

            holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.title);
            holder.dot = (View) convertView.findViewById(R.id.dot);
            holder.lineUp = (View) convertView.findViewById(R.id.lineUp);
            holder.lineDown = (View) convertView.findViewById(R.id.lineDown);
            holder.button = (Button) convertView.findViewById(R.id.remove);
            holder.listItem = (LinearLayout) convertView.findViewById(R.id.listItem);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Add the remove button to all day periods.
        if(!item.isOuter() && item.isDay()) {
            holder.button.setVisibility(View.VISIBLE);
            holder.button.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dayEditor.removeItem(position);
                        }
                    }
            );
        }

        if(!item.isOuter()) {
            holder.title.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // If clicked on a title show a time picker dialog
                            new TimePickerDialog(context,
                                    new TimePickerDialog.OnTimeSetListener() {
                                        @Override
                                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                            dayEditor.changeTime(position, hourOfDay, minute);
                                        }
                                    }, item.getHour(), item.getMinute(), true).show();
                        }
                    }
            );
        }

        // Set the title to the first time.
        String title = item.toString();
        holder.title.setText(title);

        // Add some vertical spacing to show different time period lengths.
        if(holder.listItem != null && item.getType() != SwitchListItem.Type.last) {
            int minHeight = ((succeedingItem.getTime() - item.getTime()) * 1000) / 2400;

            holder.listItem.setMinimumHeight(minHeight);
        }

        // Add the dot in front in the correct color.
        if (item.isDay()) {
            holder.dot.setBackgroundResource(R.drawable.dot_day);
        } else {
            holder.dot.setBackgroundResource(R.drawable.dot_night);
        }

        // Set the color of the upper part of the vertical line.
        if(item.getType() != SwitchListItem.Type.first) {
            if(precedingItem.isDay()) {
                holder.lineUp.setBackgroundResource(R.color.day);
            } else {
                holder.lineUp.setBackgroundResource(R.color.night);
            }
        }

        // Set the color of the lower part of the vertical line.
        if(item.getType() != SwitchListItem.Type.last) {
            if(item.isDay()) {
                holder.lineDown.setBackgroundResource(R.color.day);
            } else {
                holder.lineDown.setBackgroundResource(R.color.night);
            }
        }

        return convertView;
    }

}
