package RememberList.Codes;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SharedListsActivity extends AppCompatActivity implements View.OnClickListener {
    private SharedListsViewModel viewModel;
    private ListView listView;
    private ProgressBar progressBar;
    private ArrayAdapter<String> adapter;
    private EditText searchEditText;
    private Button filterButton;
    private ImageButton backButton, refreshButton;

    // Multiple selected categories
    private List<String> selectedCategories = null;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sharedlists);

        // Initialize UI elements
        listView = findViewById(R.id.listView);
        progressBar = findViewById(R.id.progressBar);
        searchEditText = findViewById(R.id.editText);
        filterButton = findViewById(R.id.filter);
        backButton = findViewById(R.id.back);
        refreshButton = findViewById(R.id.refresh);

        // Adapter for shared lists
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listView.setAdapter(adapter);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(SharedListsViewModel.class);

        // Observe loading state
        viewModel.getLoadingLiveData().observe(this, isLoading ->
        {
            if (Boolean.TRUE.equals(isLoading))
            {
                // Optional: Show a loading indicator here
                Toast.makeText(this, "Loading data...", Toast.LENGTH_SHORT).show();
            }
            else
            {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                // Observe lists data
                viewModel.getListsLiveData().observe(this, lists ->
                {
                    // Observe and load the lists when loading completes
                    adapter.clear();
                    if (lists != null)
                    {
                        adapter.addAll(lists);
                    }
                });
            }
        });
        viewModel.getSharedLists();
        listView.setAdapter(adapter);

        // ListView item click listener
        listView.setOnItemClickListener((parent, view, position, id) ->
        {
            String selectedList = adapter.getItem(position);
            Intent intent = new Intent(SharedListsActivity.this, SharedListsProductsActivity.class);
            intent.putExtra("LIST_NAME", selectedList); // Pass the list name
            intent.putExtra("LIST_KEY", String.valueOf(position)); // Add list ID to intent
            startActivity(intent); // Start Main5Activity
        });

        // Set button listeners
        backButton.setOnClickListener(this);
        refreshButton.setOnClickListener(v -> viewModel.getSharedLists());
        filterButton.setOnClickListener(v -> showCategoryDialog());
    }

    @Override
    public void onClick(View view)
    {
        if (view.getId() == R.id.back)
        {
            // Return to the previous activity
            Intent intent = new Intent(this, MyListsActivity.class);
            startActivity(intent);
        }
    }

    public void showCategoryDialog()
    {
        viewModel.getCategoriesLiveData().observe(this, categories ->
        {
            if (categories != null)
            {
                boolean[] checkedItems = new boolean[categories.size()];
                String[] categoryArray = categories.toArray(new String[0]);

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("בחר קטגוריות");
                builder.setMultiChoiceItems(categoryArray, checkedItems, (dialog, which, isChecked) -> {
                    if (isChecked)
                    {
                        if (selectedCategories == null) selectedCategories = new ArrayList<>();
                        if (!selectedCategories.contains(categoryArray[which]))
                        {
                            selectedCategories.add(categoryArray[which]);
                        }
                    }
                    else
                    {
                        if (selectedCategories != null)
                        {
                            selectedCategories.remove(categoryArray[which]);
                        }
                    }
                });

                // Positive button: Apply filters
                builder.setPositiveButton("אישור", (dialog, which) ->
                {
                    applyFilters(searchEditText.getText().toString().trim(), selectedCategories);
                });

                // Neutral button for adding a category
                builder.setNeutralButton("הוסף", null);

                // Neutral button for deleting a category
                builder.setNegativeButton("מחק", null);

                AlertDialog dialog = builder.create();
                dialog.show();

                // Set up "Add Category" button behavior
                dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v ->
                {
                    showAddCategoryDialog(categories, dialog);
                });

                // Set up "Delete Category" button behavior
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v ->
                {
                    if (selectedCategories != null && !selectedCategories.isEmpty())
                    {
                        for (String category : selectedCategories)
                        {
                            viewModel.deleteCategory(category);
                        }
                        Toast.makeText(this, "Selected categories deleted successfully", Toast.LENGTH_SHORT).show();
                        dialog.dismiss(); // Close the dialog
                        showCategoryDialog(); // Refresh the dialog to show updated categories
                    }
                    else
                    {
                        Toast.makeText(this, "No categories selected for deletion", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        viewModel.loadCategories();
    }

    public void showAddCategoryDialog(List<String> categories, AlertDialog parentDialog)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("הוסף קטגוריה");

        final EditText input = new EditText(this);
        input.setHint("הכנס שם");
        builder.setView(input);

        builder.setPositiveButton("הוסף", (dialog, which) ->
        {
            String categoryName = input.getText().toString().trim();
            if (!categoryName.isEmpty() && !categories.contains(categoryName))
            {
                viewModel.addCategory(categoryName);
                Toast.makeText(this, "Category added successfully", Toast.LENGTH_SHORT).show();
                parentDialog.dismiss(); // Refresh parent dialog
                showCategoryDialog();
            }
            else
            {
                Toast.makeText(this, "Category already exists or name is empty", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("ביטול", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
    private void applyFilters(String query, List<String> categories) {
        List<String> filtered = new ArrayList<>();

        for (int index = 0; index < adapter.getCount(); index++)
        { // Use a new variable instead of 'i'
            final int currentIndex = index; // Make a final copy of the index
            String listName = adapter.getItem(index);

            // Text filter
            if (!listName.toLowerCase().contains(query.toLowerCase())) continue;

            // Fetch categories asynchronously
            viewModel.getCategoriesOfList(String.valueOf(index)).observe(this, listCategories -> {
                if (listCategories != null && !listCategories.isEmpty())
                {
                    for (String listCategory : listCategories)
                    {
                        if (categories != null && categories.contains(listCategory))
                        {
                            if (!filtered.contains(listName))
                            { // Avoid duplicates
                                filtered.add(listName);
                            }
                            break;
                        }
                    }
                }

                // Update the adapter only after processing all lists
                if (currentIndex == adapter.getCount() - 1)
                { // Use the final variable
                    adapter.clear();
                    adapter.addAll(filtered);
                    adapter.notifyDataSetChanged();
                }
            });
        }
    }
}
