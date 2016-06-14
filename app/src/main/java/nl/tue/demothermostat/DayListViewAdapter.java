package nl.tue.demothermostat;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
        View dot;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        SwitchListItem item = getItem(position);

        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.switch_list_element, null);
            holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.title);
            holder.dot = (View) convertView.findViewById(R.id.dot);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String title = item.toString();

        if(item.getIsDay()) {
            /*
            TODO: Apply different style based on if it is a night or day switch.
             */
            //holder.title.setTextAppearance(R.style.);
            holder.dot.setBackgroundResource(R.drawable.dot_day);
        } else {
            holder.dot.setBackgroundResource(R.drawable.dot_night);
        }

        holder.title.setText(title);

        return convertView;
    }
}
