package nl.tue.demothermostat;

import android.app.Activity;
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
import java.util.ArrayList;

/**
 * @author Adriaan Knapen <a.d.knapen@student.tue.nl>
 */
public class DayListViewAdapter extends ArrayAdapter<SwitchListItem> {
    Context context;
    DayEditor dayEditor;
    ViewHolder holder = null;
    SwitchListItem item, precedingItem, succeedingItem;

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
        item = getItem(position);

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

        // Set the title to the first time.
        String title = item.toString();
        holder.title.setText(title);

        if(holder.listItem != null && item.getType() != SwitchListItem.Type.last) {
            int verticalSpacing = ((succeedingItem.getTime() - item.getTime()) * 1000) / 2400;

            System.out.println(verticalSpacing);

            /*
            holder.title.setPadding(holder.title.getPaddingLeft(),
                    holder.title.getPaddingTop(),
                    holder.title.getPaddingRight(),
                    holder.title.getPaddingBottom() + verticalSpacing); */
            /*
            holder.listItem.setPadding(holder.listItem.getPaddingLeft(),
                    holder.listItem.getPaddingTop(),
                    holder.listItem.getPaddingRight(),
                    holder.listItem.getPaddingBottom() + verticalSpacing);
            */
            holder.listItem.setMinimumHeight(holder.listItem.getHeight() + verticalSpacing);
        }

        if(holder.listItem == null) {
            System.err.println("Listitem = null");
        }

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


    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static int convertDpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();

        return Math.round(dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }

}
