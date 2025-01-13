package RememberList.Codes;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;

public class SharedListsProductsActivity extends AppCompatActivity implements View.OnClickListener
{


    private SharedListsProductsViewModel viewModel; // ViewModel instance
    private ListView contentListView; // ListView for displaying content
    private ArrayAdapter<String> adapter; // Adapter for ListView
    private ArrayList<String> contentList; // Data list for the adapter
    private ProgressBar progressBar; // ProgressBar for loading indicator

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sharedlistsproducts);

        // Initialize UI components
        contentListView = findViewById(R.id.contentListView);
        progressBar = findViewById(R.id.progressBar);

        contentList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, contentList);
        contentListView.setAdapter(adapter);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(SharedListsProductsViewModel.class);
        TextView title = findViewById(R.id.title);
        // Get the list ID passed from the previous activity
        String listId = getIntent().getStringExtra("LIST_ID");
        String listTitle = getIntent().getStringExtra("LIST_NAME");
        title.setText(listTitle);

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
        findViewById(R.id.back).setOnClickListener(this);
    }
    @Override
    public void onClick(View view)
    {
        if (view.getId() == R.id.back)
        {
            Intent intent = new Intent(this, MyListsActivity.class);
            startActivity(intent);
        }
    }
}
