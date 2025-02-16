package RememberList.Codes;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.ViewModelProvider;
import android.widget.TextView;
import java.util.ArrayList;

/**
 * Activity for managing the products within a specific list.
 * Displays the products, allows adding new ones, and supports deletion of selected items.
 */
public class ListProductsActivity extends AppCompatActivity implements View.OnClickListener
{
    private ListProductsViewModel viewModel; // ViewModel for managing data
    private ListAdapter boxAdapter; // Adapter for the ListView
    private EditText editText; // Input field for adding products
    private ListView lvMain; // ListView for displaying products
    private ArrayList<Product> selectedProducts = new ArrayList<>(); // List to hold selected products for deletion
    private String listName; // The name of the list being managed
    private String listKey; // The key of the list being managed

    private boolean notificationSent = false; // Flag to prevent duplicate notifications

    private static final String CHANNEL_ID = "test_channel";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        //Calls the onCreate method of the superclass (AppCompatActivity)
        // to ensure that the base class's initialization (e.g., setting up the activity context)
        // is performed.
        super.onCreate(savedInstanceState);
        //Specifies the XML layout file (activity_listsproducts) that defines the UI of the activity.
        setContentView(R.layout.activity_listsproducts);

        // Initialize UI elements
        editText = findViewById(R.id.editText);
        lvMain = findViewById(R.id.listView);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(ListProductsViewModel.class);
        listName = getIntent().getStringExtra("LIST_NAME"); // Get the list name passed via Intent
        listKey = getIntent().getStringExtra("LIST_KEY"); // Get the key of the list passed via Intent
        TextView title = findViewById(R.id.textview);
        title.setText(listName);
        viewModel.init(listName,listKey); // Initialize ViewModel with the list name
        // Observe products LiveData to update the UI when data changes
        viewModel.getProducts().observe(this, products ->
        {
            if (products != null && !products.get(0).name.equals(""))
            {
                boxAdapter = new ListAdapter(ListProductsActivity.this, listName+listKey, new ArrayList<>(products),true); // Ensure a fresh ArrayList
                lvMain.setAdapter(boxAdapter); // Set the adapter for the ListView
            }
        });

        // Create the Notification Channel (Only for Android 8.0+)
        createNotificationChannel();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1001);
            }
        }

        // Set click listeners for the buttons
        findViewById(R.id.add).setOnClickListener(this);
        findViewById(R.id.delete).setOnClickListener(this);
        findViewById(R.id.back).setOnClickListener(this);




    }

    @Override
    public void onClick(View view)
    {
        if (view.getId() == R.id.add)
        {
            // Handle adding a new product
            String productName = editText.getText().toString().trim();
            if (!productName.isEmpty())
            {
                viewModel.addProduct(productName); // Add product via ViewModel
                editText.setText(""); // Clear the input field
                Toast.makeText(this, "Product added", Toast.LENGTH_SHORT).show(); // Show confirmation
            }
            else
            {
                Toast.makeText(this, "Enter a product name", Toast.LENGTH_SHORT).show(); // Prompt for input
            }
        }
        else if (view.getId() == R.id.delete)
        {
            // Handle deleting selected products
            showDeleteConfirmationDialog();
        }
        else if (view.getId() == R.id.back)
        {
            checkAndShowNotification();
            // Navigate back to MyListsActivity
            Intent intent = new Intent(this, MyListsActivity.class);
            startActivity(intent);
        }
    }


    /**
     * Checks if all items are checked and shows a notification if not.
     */
    private void checkAndShowNotification() {
        boolean allChecked = true;

        // Check if all items in the list are checked
        if (boxAdapter != null) { // Ensure adapter is not null to prevent crashes
            for (int i = 0; i < boxAdapter.getCount(); i++) {
                Product product = boxAdapter.getProduct(i);
                if (!product.box) { // If any product is unchecked
                    allChecked = false;
                    break;
                }
            }
        }

        // If some items are unchecked and no notification has been sent yet, show notification
        if (!allChecked && !notificationSent) {
            showNotification();
            notificationSent = true; // Prevent duplicate notifications
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        notificationSent = false; // Reset flag when user returns to the app
    }

    @Override
    protected void onStop() {
        super.onStop();
        checkAndShowNotification();
    }

    /**
     * Displays a confirmation dialog to confirm deletion of selected items.
     */
    private void showDeleteConfirmationDialog()
    {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to delete the selected items?") // Dialog message
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Collect selected products
                    for (int i = 0; i < boxAdapter.getCount(); i++)
                    {
                        Product product = boxAdapter.getProduct(i);
                        if (product.box) // Check if the product is selected (box checked)
                        {
                            selectedProducts.add(product);
                        }
                    }
                    // Delete selected products via ViewModel
                    viewModel.deleteProducts(selectedProducts);
                    selectedProducts.clear(); // Clear the selected products list
                    Toast.makeText(this, "Selected items deleted", Toast.LENGTH_SHORT).show(); // Show confirmation
                })
                .setNegativeButton("Cancel", null) // Do nothing on cancel
                .show();
    }


    // Function to create a notification channel (Required for Android 8.0+)
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Test Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
    // Function to show a notification
    private void showNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_foreground)
                .setContentTitle("שים לב!")
                .setContentText("חלק מהפריטים ברשימה לא מסומנים.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }


}
