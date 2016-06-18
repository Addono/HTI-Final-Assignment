package nl.tue.demothermostat;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;
import java.util.ArrayList;

/**
 * @author Adriaan Knapen <a.d.knapen@student.tue.nl>
 */
public class DayListViewAdapter extends ArrayAdapter<SwitchListItem> {
    Context context;
    DayEditor dayEditor;
    ViewHolder holder = null;
    SwitchListItem item, succeedingItem, precedingItem;

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
        LinearLayout listItem;
        Button button;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        item = getItem(position);

        if(position > 0) {
            precedingItem = getItem(position - 1);
        } else {
            precedingItem = null;
        }

        if(getCount() > position + 1) {
            succeedingItem = getItem(position + 1);
        } else {
            succeedingItem = null;
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
            holder.listItem = (LinearLayout) convertView.findViewById(R.id.listItem);

            holder.button = (Button) convertView.findViewById(R.id.remove);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }


        if(!item.isOuter() ) {
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

        // Set the title to the first time.
        String title = item.toString();
        holder.title.setText(title);

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
