package RememberList.Codes;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import android.util.Log;

public class SharedListsActivity extends AppCompatActivity implements View.OnClickListener {
    private SharedListsViewModel viewModel;
    private ListView listView;
    private ProgressBar progressBar;
    private Button filterButton;
    private ImageButton backButton, refreshButton;
    private String listsCount;
    private SharedListAdapter adapter; // Define adapter as a class-level variable
    private HashMap<Integer, Integer> positionMap;
    private List<ListSaveObject> originalList;
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
        filterButton = findViewById(R.id.filter);
        backButton = findViewById(R.id.back);
        refreshButton = findViewById(R.id.refresh);


        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(SharedListsViewModel.class);
        listsCount = getIntent().getStringExtra("LIST_COUNT");
        viewModel.getSharedLists();

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
                if(selectedCategories == null && positionMap == null)
                {
                    viewModel.getListsLiveData().observe(this, lists ->
                    {
                        if (lists != null)
                        {
                            positionMap = new HashMap<>();

                            // Store original list before sorting
                            originalList = new ArrayList<>(lists);

                            // Sort by 'saves' in descending order (highest saves first)
                            Collections.sort(lists, (a, b) -> Integer.compare(b.getSaves(), a.getSaves()));

                            // Store new indexes as keys and original indexes as values
                            for (int newIndex = 0; newIndex < lists.size(); newIndex++)
                            {
                                int originalIndex = originalList.indexOf(lists.get(newIndex));
                                positionMap.put(newIndex, originalIndex);
                            }

                            adapter = new SharedListAdapter(this, lists);
                            // Store new indexes as keys and original indexes as values
                            for (int newIndex = 0; newIndex < lists.size(); newIndex++) {
                                int originalIndex = originalList.indexOf(lists.get(newIndex));
                                positionMap.put(newIndex, originalIndex);
                            }
                            listView.setAdapter(adapter);
                        }
                        viewModel.getListsLiveData().removeObservers(this);
                    });
                }
            }
        });

        // ListView item click listener
        listView.setOnItemClickListener((parent, view, position, id) ->
        {
            ListSaveObject selectedItem = adapter.getItem(position); // Correct type
            String selectedListName = selectedItem.getListName();
            Intent intent = new Intent(SharedListsActivity.this, SharedListsProductsActivity.class);
            intent.putExtra("LIST_NAME", selectedListName); // Pass the list name
            intent.putExtra("LIST_KEY", String.valueOf(positionMap.get(position))); // Add list ID to intent
            intent.putExtra("LIST_COUNT", listsCount);
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
        final boolean[] isConfirmed = {false};
        viewModel.getCategoriesLiveData().observe(this, categories ->
        {
            viewModel.getCategoriesLiveData().removeObservers(this);
            if (categories != null)
            {
                boolean[] checkedItems = new boolean[categories.size()];
                String[] categoryArray = categories.toArray(new String[0]);

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("בחר קטגוריות");
                builder.setMultiChoiceItems(categoryArray, checkedItems, (dialog, which, isChecked) ->
                {
                    if (isChecked)
                    {
                        if (selectedCategories == null)
                        {
                            selectedCategories = new ArrayList<>();
                        }
                        if (!selectedCategories.contains(categoryArray[which]))
                        {
                            selectedCategories.add(categoryArray[which]);
                        }
                        List<String> updatedSelection = new ArrayList<>();
                        for (int i = 0; i < categoryArray.length; i++) {
                            if (checkedItems[i])
                            {
                                updatedSelection.add(categoryArray[i]);
                            }
                        }
                        selectedCategories.clear();
                        selectedCategories.addAll(updatedSelection);
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
                builder.setPositiveButton("אישור", null);

                // Neutral button for adding a category
                builder.setNeutralButton("הוסף", null);

                // Neutral button for deleting a category
                builder.setNegativeButton("מחק", null);

                AlertDialog dialog = builder.create();
                dialog.show();

                viewModel.getlistCategoriesLiveData().removeObservers(this);
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v ->
                {
                    isConfirmed[0] = true;
                    // Fetch categories asynchronously
                    viewModel.getFilteredLists(selectedCategories);
                    viewModel.getlistCategoriesLiveData().observe(this, filteredLists ->
                    {
                        // Sort by 'saves' in descending order (highest saves first)
                        Collections.sort(filteredLists, (a, b) -> Integer.compare(b.getSaves(), a.getSaves()));

                        // Store new indexes as keys and original indexes as values
                        for (int newIndex = 0; newIndex < filteredLists.size(); newIndex++)
                        {
                            int originalIndex = originalList.indexOf(filteredLists.get(newIndex));
                            positionMap.put(newIndex, originalIndex);
                        }
                        adapter.clear();
                        adapter.addAll(filteredLists);
                        listView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                        dialog.dismiss();
                    });
                });
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
                            dialog.dismiss();
                            viewModel.deleteCategory(category);
                        }
                        Toast.makeText(this, "Selected categories deleted successfully", Toast.LENGTH_SHORT).show();
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
}
