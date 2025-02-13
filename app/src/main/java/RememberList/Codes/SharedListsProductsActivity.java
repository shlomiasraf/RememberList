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
    private ArrayList<String> valuesList; // Data list for the adapter
    private ProgressBar progressBar; // ProgressBar for loading indicator

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sharedlistsproducts);

        // Initialize UI components
        contentListView = findViewById(R.id.contentListView);
        progressBar = findViewById(R.id.progressBar);

        valuesList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, valuesList);
        contentListView.setAdapter(adapter);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(SharedListsProductsViewModel.class);
        TextView title = findViewById(R.id.title);
        // Get the list ID passed from the previous activity
        String listName = getIntent().getStringExtra("LIST_NAME");
        String listKey = getIntent().getStringExtra("LIST_KEY");
        title.setText(listName);

        // Validate that a valid list ID was received
        if (listKey == null || listKey.isEmpty())
        {
            Toast.makeText(this, "Unable to display content - Missing list ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Observe LiveData for content updates
        viewModel.init(listName,listKey);
                viewModel.getProducts().observe(this, values -> {
            valuesList.clear(); // Clear current data
            for (Product value: values)
            {
                valuesList.add(value.name);
            }
            adapter.notifyDataSetChanged(); // Update ListView
        });

        // Observe LiveData for loading state
        viewModel.getLoadingLiveData().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? ProgressBar.VISIBLE : ProgressBar.GONE);
        });

        // Fetch content for the specified list ID
        viewModel.getProducts();
        findViewById(R.id.back).setOnClickListener(this);
    }
    @Override
    public void onClick(View view)
    {
        if (view.getId() == R.id.back)
        {
            Intent intent = new Intent(this, SharedListsActivity.class);
            startActivity(intent);
        }
    }
}
