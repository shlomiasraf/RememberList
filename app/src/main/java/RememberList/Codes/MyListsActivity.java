package RememberList.Codes;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
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
    private EditText editText; // EditText for user input
    private Button sharelists, add, signOutButton,record_btn; // Buttons for actions
    private String selectedList; // Currently selected list for deletion
    private int positionToDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mylists);

        // Initialize UI elements
        add = findViewById(R.id.add);
        sharelists = findViewById(R.id.sharelists);
        editText = findViewById(R.id.editText);
        list = findViewById(R.id.listView);
        signOutButton = findViewById(R.id.signOutButton);
        record_btn = findViewById(R.id.record_btn);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(MyListsViewModel.class);

        // Observe error messages to display them as Toast messages
        viewModel.getErrorLiveData().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });

        // Observe loading state and trigger actions accordingly
        viewModel.getLoadingLiveData().observe(this, isLoading ->
        {
            if (Boolean.TRUE.equals(isLoading))
            {
                // Optional: Show a loading indicator here
                Toast.makeText(this, "Loading data...", Toast.LENGTH_SHORT).show();
            }
            else
            {
                // Observe and load the lists when loading completes
                viewModel.getLists().observe(this, lists -> {
                    adapter.clear();
                    if (lists != null) {
                        adapter.addAll(lists);
                    }
                    adapter.notifyDataSetChanged(); // Refresh ListView
                });
            }
        });

        // Trigger data loading or initial checks
        viewModel.checkAndLoadData();

        // Setup the ArrayAdapter for the ListView
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        list.setAdapter(adapter);

        // Setup click and long-click listeners for the ListView
        setupListClickListeners();

        // Set click listeners for action buttons
        add.setOnClickListener(this);
        sharelists.setOnClickListener(this);
        signOutButton.setOnClickListener(this);
        record_btn.setOnClickListener(this);
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

    private void setupListClickListeners() {
        // Handle item clicks to navigate to another activity
        list.setOnItemClickListener((parent, view, position, id) -> {
            selectedList = adapter.getItem(position);
            Intent intent = new Intent(this, ListProductsActivity.class);
            intent.putExtra("LIST_NAME", selectedList); // Pass the list name to the next activity
            intent.putExtra("LIST_KEY", String.valueOf(position));
            startActivity(intent);
        });

        // Handle long item clicks to show a delete confirmation dialog
        list.setOnItemLongClickListener((parent, view, position, id) -> {
            selectedList = adapter.getItem(position);
            positionToDelete = position;
            showDeleteConfirmationDialog();
            return true;
        });
    }

    private void showDeleteConfirmationDialog()
    {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to delete this list? Once deleted, it cannot be recovered.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (selectedList != null)
                    {
                        viewModel.deleteList(selectedList,String.valueOf(positionToDelete)); // Delete the list using ViewModel
                        Toast.makeText(this, "List deleted", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onClick(View view) {
        if (view == add)
        {
            // Add a new list
            String listName = editText.getText().toString().trim();
            if (!listName.isEmpty()) {
                viewModel.addList(listName); // Add list
                editText.setText(""); // Clear the input field
            }
            else
            {
                Toast.makeText(this, "List name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        }
        else if (view == sharelists)
        {
            // Navigate to SharedListsActivity
            Intent intent = new Intent(this, SharedListsActivity.class);
            intent.putExtra("LIST_COUNT", String.valueOf(adapter.getCount()));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear activity stack
            startActivity(intent);
        } else if (view == signOutButton) {
            // Handle user sign-out
            handleSignOut();
        }
        else if (view == record_btn)
        {

            // Create an intent for speech recognition
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "he-IL"); // Set language to Hebrew, if needed

            // Start the speech recognition activity
            startActivityForResult(intent, 100);


        }
    }

    private void handleSignOut() {
        // Perform sign-out and navigate to LoginActivity
        viewModel.logout();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear activity stack
        startActivity(intent);
        Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show();
    }
}
