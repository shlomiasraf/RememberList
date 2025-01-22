package RememberList.Codes;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;


public class MyListsActivity extends AppCompatActivity implements View.OnClickListener {

    private MyListsViewModel viewModel; // ViewModel instance
    private ArrayAdapter<String> adapter; // Adapter to display the lists
    private ListView list; // ListView for displaying lists
    private EditText editText; // EditText for input
    private Button sharelists, add, signOutButton, recordButton; // Buttons for actions
    private String selectedList; // Selected list for deletion
    private int selectedPosition; // Selected position for deletion

    //Bundle savedInstanceState parameter in the onCreate method of an Android activity
    // is used to save and restore the state of the activity.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);// Call the superclass onCreate method to initialize the activity
        setContentView(R.layout.activity_mylists);// Set the layout for this activity

        // Initialize UI elements
        add = findViewById(R.id.add);
        sharelists = findViewById(R.id.sharelists);
        editText = findViewById(R.id.editText);
        list = findViewById(R.id.listView);
        signOutButton = findViewById(R.id.signOutButton);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(MyListsViewModel.class);

        //Sets up an ArrayAdapter to manage and display a list of items in the ListView.
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        list.setAdapter(adapter);//Sets the created ArrayAdapter as the adapter for the ListView named list.
        //The adapter bridges the data (the list of strings) and the ListView, ensuring the data is displayed properly.

        // Observe the lists LiveData from the ViewModel
        viewModel.getLists().observe(this, lists -> {
            adapter.clear();
            if (lists != null) {
                adapter.addAll(lists);
            }
            //Ensures the ListView reflects the updated data after the LiveData change is observed and processed.
            adapter.notifyDataSetChanged();
        });

        // Set click listeners
        add.setOnClickListener(this);
        sharelists.setOnClickListener(this);
        signOutButton.setOnClickListener(this);

        // Handle list item clicks
        setupListClickListeners();

    }


    private void setupListClickListeners() {
        // Navigate to ListProductsActivity on item click
        list.setOnItemClickListener((parent, view, position, id) -> {
            selectedList = adapter.getItem(position);
            Intent intent = new Intent(this, ListProductsActivity.class);
            intent.putExtra("LIST_NAME", selectedList); // Pass the list name
            startActivity(intent);
        });

        // Show delete confirmation dialog on long click
        list.setOnItemLongClickListener((parent, view, position, id) -> {
            selectedList = adapter.getItem(position);
            selectedPosition = position;
            showDeleteConfirmationDialog();
            return true;
        });
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to delete this list? Once deleted, it cannot be recovered.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (selectedList != null) {
                        viewModel.deleteList(selectedList); // Delete the list using ViewModel
                        Toast.makeText(this, "List deleted", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onClick(View view) {
        if (view == add) {
            String listName = editText.getText().toString().trim();
            if (!listName.isEmpty()) {
                viewModel.addList(listName); // Add the list using ViewModel
                editText.setText(""); // Clear input
                Toast.makeText(this, "New list created", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "List name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        } else if (view == sharelists) {
            // Navigate to SharedListsActivity
            Intent intent = new Intent(this, SharedListsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear the activity stack
            startActivity(intent);
        } else if (view == signOutButton) {
            // Handle sign-out
            handleSignOut();
        }

    }

    private void handleSignOut() {
        // Call the logout function from the repository
        viewModel.logout(); // Assuming the ViewModel has a logout method that calls repository.logout()

        // Redirect to LoginActivity
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear the activity stack
        startActivity(intent);

        Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show();
    }
}
