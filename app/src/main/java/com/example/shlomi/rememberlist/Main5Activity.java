package com.example.shlomi.rememberlist;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;

public class Main5Activity extends AppCompatActivity {

    private Main5ViewModel viewModel; // ViewModel instance
    private ListView contentListView; // ListView for displaying content
    private ArrayAdapter<String> adapter; // Adapter for ListView
    private ArrayList<String> contentList; // Data list for the adapter
    private ProgressBar progressBar; // ProgressBar for loading indicator

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main5);

        // Initialize UI components
        contentListView = findViewById(R.id.contentListView);
        progressBar = findViewById(R.id.progressBar);

        contentList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, contentList);
        contentListView.setAdapter(adapter);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(Main5ViewModel.class);

        // Get the list ID passed from the previous activity
        String listId = getIntent().getStringExtra("LIST_ID");

        // Validate that a valid list ID was received
        if (listId == null || listId.isEmpty()) {
            Toast.makeText(this, "Unable to display content - Missing list ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Observe LiveData for content updates
        viewModel.getContentLiveData().observe(this, content -> {
            contentList.clear(); // Clear current data
            contentList.addAll(content); // Add new data
            adapter.notifyDataSetChanged(); // Update ListView
        });

        // Observe LiveData for loading state
        viewModel.getLoadingLiveData().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? ProgressBar.VISIBLE : ProgressBar.GONE);
        });

        // Fetch content for the specified list ID
        viewModel.fetchContent(listId);
    }
}
