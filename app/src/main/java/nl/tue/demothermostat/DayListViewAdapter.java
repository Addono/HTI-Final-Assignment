package nl.tue.demothermostat;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;

/**
 * Created by s150376 on 6/14/2016.
 */
public class DayListViewAdapter extends ArrayAdapter<SwitchListItem> {
    Context context;

    public DayListViewAdapter(Context context, int resourceId,
                              ArrayList<SwitchListItem> items) {
        super(context, resourceId, items);
        this.context = context;
    }

    private class ViewHolder {
        TextView title;
        View dot, lineUp, lineDown;
        LinearLayout listItem;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        SwitchListItem item = getItem(position);
        SwitchListItem succeedingItem, precedingItem;

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

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if(!item.getIsIntersection()) {
            // Set the title to the start time.
            String title = item.toString();
            holder.title.setText(title);

            if (item.getIsDay()) {
                holder.dot.setBackgroundResource(R.drawable.dot_day);
            } else {
                holder.dot.setBackgroundResource(R.drawable.dot_night);
            }
        }

        // Set the color of the upper part of the vertical line.
        if(precedingItem != null) {
            if(precedingItem.getIsDay()) {
                holder.lineUp.setBackgroundResource(R.color.day);
            } else {
                holder.lineUp.setBackgroundResource(R.color.night);
            }
        }

        // Set the color of the lower part of the vertical line.
        if(succeedingItem != null) {
            if(item.getIsDay()) {
                holder.lineDown.setBackgroundResource(R.color.day);
            } else {
                holder.lineDown.setBackgroundResource(R.color.night);
            }
        }

        return convertView;
    }
}
