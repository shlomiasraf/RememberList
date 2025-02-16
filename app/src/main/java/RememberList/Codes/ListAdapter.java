package RememberList.Codes;

import android.app.Application;
import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

// ListAdapter extends BaseAdapter to manage the list of Product objects
public class ListAdapter extends BaseAdapter
{
    Context ctx;  // Context to access resources and system services
    LayoutInflater lInflater;  // Inflater to create views from XML layouts
    ArrayList<Product> objects;  // List of products to display
    ArrayList<TextView> arr;  // Array list to hold TextView references
    private final Repository repository; // Repository to manage data operations
    String keyPrefix;
    // <-- Add a field to control strike-through
    private boolean enableStrikeThrough;
    // Constructor to initialize the adapter with context and product list
    public ListAdapter(Context context,String keyPrefix, ArrayList<Product> products, boolean enableStrikeThrough)
    {
        ctx = context;
        objects = products;
        arr = new ArrayList<>();
        lInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        repository = new Repository(context); // Initialize the repository
        this.keyPrefix = keyPrefix;
        // Save the parameter
        this.enableStrikeThrough = enableStrikeThrough;
    }
    // Returns the total number of items in the list
    @Override
    public int getCount() {
        return objects.size();
    }

    // Returns the item at the specified position
    @Override
    public Object getItem(int position) {
        return objects.get(position);
    }

    // Returns the ID of the item at the specified position (in this case, the position itself)
    @Override
    public long getItemId(int position) {
        return position;
    }

    // This method is responsible for creating the view for each item in the list
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View view = convertView;
        if (view == null)
        {  // If no view is provided, inflate a new view
            view = lInflater.inflate(R.layout.activity_listadapter, parent, false);  // Inflate custom layout
        }

        // Get the Product object at the specified position
        Product p = getProduct(position);

        // Initialize the TextView to display the product name
        arr.add(position, (TextView) view.findViewById(R.id.tvDescr));
        arr.get(position).setText(p.name);  // Set the product name in the TextView

        // Initialize the CheckBox for the product
        CheckBox cbBuy = (CheckBox) view.findViewById(R.id.cbBox);
        cbBuy.setOnCheckedChangeListener(myCheckChangList);  // Set listener for CheckBox state changes
        cbBuy.setTag(position);  // Set the position as a tag to identify the CheckBox
        cbBuy.setChecked(p.box);  // Set the initial checked state of the CheckBox based on the product

        return view;  // Return the view for this item
    }

    // Helper method to get the Product object at a specific position
    Product getProduct(int position)
    {
        return ((Product) getItem(position));
    }

    // CheckBox state change listener to update the product's box status and apply strike-through to the TextView
    OnCheckedChangeListener myCheckChangList = new OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            int position = (Integer) buttonView.getTag();
            Product product = getProduct(position);

            if (product != null)
            {
                product.setChecked(isChecked); // Update the local object state using setter method
                // Update the 'isChecked' field in Firebase
                repository.ChangeProductBox(product, position, keyPrefix);
            }

            // Apply or remove strike-through if enabled
            if (enableStrikeThrough) {
                View row = (View) buttonView.getParent();
                TextView textView = row.findViewById(R.id.tvDescr);

                if (isChecked) {
                    textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                } else {
                    textView.setPaintFlags(textView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                }
            }
        }
    };
}
