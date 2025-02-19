package RememberList.Codes;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.ArrayAdapter;
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


    private ArrayList<String> valuesList; // Data list for the adapter

    private boolean notificationSent = false; // Flag to prevent duplicate notifications

    private static final String CHANNEL_ID = "test_channel";


    private ArrayList<String> categories = new ArrayList<>();

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

        valuesList = new ArrayList<>();


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
            }
            else
            {
                boxAdapter = new ListAdapter(ListProductsActivity.this, listName + listKey, new ArrayList<>(), true);
            }
            lvMain.setAdapter(boxAdapter); // Set the adapter for the ListView
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
        findViewById(R.id.share).setOnClickListener(this);
        findViewById(R.id.record).setOnClickListener(this);




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
                Toast.makeText(this, "נוסף ערך בהצלחה", Toast.LENGTH_SHORT).show(); // Show confirmation
            }
            else
            {
                Toast.makeText(this, "הכנס את שם הערך", Toast.LENGTH_SHORT).show(); // Prompt for input
            }
        }
        else if (view.getId() == R.id.delete)
        {
            showDeleteConfirmationDialog();
        }
        else if (view.getId() == R.id.back)
        {
            checkAndShowNotification();
            // Navigate back to MyListsActivity
            Intent intent = new Intent(this, MyListsActivity.class);
            startActivity(intent);
        }
        else if (view.getId() == R.id.share)
        {
            //Add list to shared lists:
            showShareOptionsDialog();
        }
        else if(view.getId() == R.id.record)
        {
            // Create an intent for speech recognition
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "he-IL"); // Set language to Hebrew, if needed

            // Start the speech recognition activity
            startActivityForResult(intent, 100);

        }
    }
    private void showCategoryInputDialog() {
        categories.clear(); // Clear previous selections
        askForCategory();
    }
    private void showShareOptionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("שיתוף רשימה");

        builder.setItems(new CharSequence[]{"שתף", "בטל"}, (dialog, which) ->
        {
            dialog.dismiss(); // Close the first dialog
            if (which == 0)
            {
                showCategoryInputDialog(); // Open the second dialog();
            }
            dialog.dismiss();
        });

        builder.create().show();
    }

    private void askForCategory() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("בחר קטגוריה לרשימה");

        EditText categoryInput = new EditText(this);
        categoryInput.setHint("הקלד קטגוריה כאן");
        builder.setView(categoryInput);

        builder.setPositiveButton("אישור", (dialog, which) -> {
            String category = categoryInput.getText().toString().trim();
            if (!category.isEmpty()) {
                categories.add(category);

                if (categories.size() < 3) {
                    askToAddAnotherCategory();
                } else {
                    shareList(categories); // Max reached, proceed with sharing
                }
            } else
            {
                Toast.makeText(this, "נא להזין קטגוריה", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("ביטול", (dialog, which) -> {
            if (!categories.isEmpty()) {
                shareList(categories);
            }
            dialog.dismiss();
        });

        builder.create().show();
    }


    private void askToAddAnotherCategory() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("האם תרצה להוסיף קטגוריה נוספת?");
        builder.setMessage("ניתן להוסיף עד 3 קטגוריות.");

        builder.setPositiveButton("כן", (dialog, which) -> askForCategory());
        builder.setNegativeButton("לא", (dialog, which) -> shareList(categories));

        builder.create().show();
    }


    // Method to share list to shared lists page
    private void shareList(ArrayList<String> categories)
    {
        Toast.makeText(this, "משתף רשימה...", Toast.LENGTH_SHORT).show();
        for (int i = 0; i < boxAdapter.getCount(); i++)
        {
            valuesList.add(boxAdapter.getProduct(i).name);
        }
        viewModel.shareList(listName, valuesList, categories);
    }




    /**
     * Checks if all items are checked and shows a notification if not.
     */
    private void checkAndShowNotification()
    {
        boolean allChecked = true;
        boolean oneChecked = false;
        // Check if all items in the list are checked
        if (boxAdapter != null) { // Ensure adapter is not null to prevent crashes
            for (int i = 0; i < boxAdapter.getCount(); i++)
            {
                Product product = boxAdapter.getProduct(i);
                if (!product.box)
                { // If any product is unchecked
                    allChecked = false;
                }
                else
                {
                    oneChecked = true;
                }
                if(!allChecked && oneChecked)
                {
                    break;
                }
            }
        }

        // If some items are unchecked and no notification has been sent yet, show notification
        if (!allChecked && !notificationSent && oneChecked)
        {
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
                .setPositiveButton("Delete", (dialog, which) ->
                {
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
                    if(selectedProducts.size() > 0)
                    {
                        viewModel.deleteProducts(selectedProducts, boxAdapter.getCount());
                        selectedProducts.clear(); // Clear the selected products list
                        Toast.makeText(this, "נמחקו הערכים המסומנים", Toast.LENGTH_SHORT).show(); // Show confirmation
                    }
                    else
                    {
                        Toast.makeText(this, "לא סימנת ערכים למחיקה", Toast.LENGTH_SHORT).show();
                    }

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
    private void showNotification()
    {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_foreground)
                .setContentTitle("שים לב!")
                .setContentText("לא לקחת את כל הפריטים שברשימה.")
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            // Get the recognized speech text
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                // Set the recognized speech to the EditText
                editText.setText(results.get(0)); // Display the first result in EditText
            }
        }
    }


}
