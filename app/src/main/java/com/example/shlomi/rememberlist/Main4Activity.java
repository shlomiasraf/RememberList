package com.example.shlomi.rememberlist;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.util.Map;

public class Main4Activity extends AppCompatActivity {

    private Main4ViewModel viewModel; // ViewModel for data and logic
    private ListView listView; // ListView to display list titles
    private ProgressBar progressBar; // ProgressBar to indicate loading
    private ArrayAdapter<String> adapter; // Adapter for ListView

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);

        // Initialize UI components
        listView = findViewById(R.id.listView);
        progressBar = findViewById(R.id.progressBar);

        // Initialize adapter for ListView
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listView.setAdapter(adapter);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(Main4ViewModel.class);

        // Observe LiveData for list titles
        viewModel.getListsLiveData().observe(this, lists -> {
            adapter.clear(); // Clear existing items in the adapter
            adapter.addAll(lists); // Add new items to the adapter
            adapter.notifyDataSetChanged(); // Notify adapter of data change
        });

        // Observe LiveData for loading state
        viewModel.getLoadingLiveData().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE); // Show or hide the ProgressBar
        });

        // Fetch lists from Firebase through ViewModel
        viewModel.fetchListsFromFirebase();

        // Handle ListView item clicks
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedTitle = adapter.getItem(position); // Get the clicked title
                String selectedListId = null;

                // Find the corresponding list ID using the title
                for (Map.Entry<String, String> entry : viewModel.getListIdToTitleMap().entrySet())
                {
                    if (entry.getValue().equals(selectedTitle))
                    {
                        selectedListId = entry.getKey(); // Map title to list ID
                        break;
                    }
                }

                if (selectedListId != null)
                {
                    // Pass the selected list ID to Main5Activity
                    Intent intent = new Intent(Main4Activity.this, Main5Activity.class);
                    intent.putExtra("LIST_ID", selectedListId); // Add list ID to intent
                    startActivity(intent); // Start Main5Activity
                }
                else
                {
                    // Show an error message if no matching list ID is found
                    Toast.makeText(Main4Activity.this, "Failed to find list ID for the selected title", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
