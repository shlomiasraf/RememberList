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

public class MyListsActivity extends AppCompatActivity implements View.OnClickListener
{

    private MyListsViewModel viewModel; // ViewModel instance
    private ArrayAdapter<String> adapter; // Adapter to display the lists
    private ListView list; // ListView for displaying lists
    private EditText editText; // EditText for input
    private Button sharelists, add; // Buttons for actions
    private String selectedList; // Selected list for deletion
    private int selectedPosition; // Selected position for deletion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        // Initialize UI elements
        add = findViewById(R.id.add);
        sharelists = findViewById(R.id.sharelists);
        editText = findViewById(R.id.editText);
        list = findViewById(R.id.listView);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(MyListsViewModel.class);

        // Initialize the adapter
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        list.setAdapter(adapter);

        // Observe the lists LiveData from the ViewModel
        viewModel.getLists().observe(this, lists -> {
            adapter.clear();
            if (lists != null)
            {
                adapter.addAll(lists);
            }
            adapter.notifyDataSetChanged();
        });

        // Set click listeners
        add.setOnClickListener(this);
        sharelists.setOnClickListener(this);

        // Handle list item clicks
        setupListClickListeners();
    }

    private void setupListClickListeners()
    {
        // Navigate to Main3Activity on item click
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

    private void showDeleteConfirmationDialog()
    {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to delete this list? Once deleted, it cannot be recovered.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (selectedList != null)
                    {
                        viewModel.deleteList(selectedList); // Delete the list using ViewModel
                        Toast.makeText(this, "List deleted", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onClick(View view)
    {
        if (view == add)
        {
            String listName = editText.getText().toString().trim();
            if (!listName.isEmpty())
            {
                viewModel.addList(listName); // Add the list using ViewModel
                editText.setText(""); // Clear input
                Toast.makeText(this, "New list created", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(this, "List name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        }
        else if (view == sharelists)
        {
            // Navigate to Main4Activity
            Intent intent = new Intent(this, SharedListsActivity.class);
            startActivity(intent);
        }
    }
}
