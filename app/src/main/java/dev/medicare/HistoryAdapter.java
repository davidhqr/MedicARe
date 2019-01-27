package dev.medicare;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.medicare.models.Dosage;

public class HistoryAdapter extends ArrayAdapter<Dosage> {
    private Context mContext;
    private List<Dosage> history;

    public HistoryAdapter(Context context, ArrayList<Dosage> list) {
        super(context, 0, list);
        mContext = context;
        ArrayList<Dosage> temp = new ArrayList<>(list);
        Collections.reverse(temp);
        history = temp;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null) {
            listItem = LayoutInflater.from(mContext).inflate(R.layout.list_item, parent, false);
        }

        Dosage curDosage = history.get(position);
        DateFormat formater = new SimpleDateFormat("hh:mm a");

        ImageView image = listItem.findViewById(R.id.list_item_image);
        TextView name = listItem.findViewById(R.id.list_item_name);
        TextView description = listItem.findViewById(R.id.list_item_description);
        TextView time = listItem.findViewById(R.id.list_item_time);
        TextView quantity = listItem.findViewById(R.id.list_item_quantity);

        name.setText(curDosage.getName());
        time.setText(formater.format(curDosage.getTimestamp()));
        quantity.setText(curDosage.getNumPills() + "");

        if (curDosage.getName().equalsIgnoreCase("Tylenol")) {
            image.setImageResource(R.drawable.tylenol);
            description.setText(R.string.label_description_tylenol);
        } else if (curDosage.getName().equalsIgnoreCase("Advil")) {
            image.setImageResource(R.drawable.advil);
            description.setText(R.string.label_description_advil);
        }

        return listItem;
    }
}
