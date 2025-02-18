package RememberList.Codes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class SharedListAdapter extends ArrayAdapter<ListSaveObject> {
    private final Context context;
    private final List<ListSaveObject> listEntries;

    // Constructor should match the class name and accept List<ListSaveObject>
    public SharedListAdapter(Context context, List<ListSaveObject> lists) {
        super(context, R.layout.activity_sharedlistadapter, lists);
        this.context = context;
        this.listEntries = lists;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.activity_sharedlistadapter, parent, false); // Ensure correct layout file
        }

        // Get UI elements
        TextView saveCount = convertView.findViewById(R.id.saveCount); // Integer (left)
        TextView listName = convertView.findViewById(R.id.listName); // String (right)

        // Get the corresponding ListSaveObject
        ListSaveObject entry = listEntries.get(position);
        saveCount.setText(String.valueOf(entry.getSaves())); // Set save count
        listName.setText(entry.getListName()); // Set list name

        return convertView;
    }
}
