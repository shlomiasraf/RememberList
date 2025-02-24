package RememberList.Codes;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class SharedListsActivity extends AppCompatActivity implements View.OnClickListener {
    private SharedListsViewModel viewModel;
    private ListView listView;
    private ProgressBar progressBar;
    private ImageButton backButton;
    private ImageButton refreshButton;
    private String listsCount;
    private SharedListAdapter adapter; // Define adapter as a class-level variable
    private HashMap<Integer, Integer> positionMap;
    private List<ListSharedObject> originalList;
    // Multiple selected categories
    private List<String> selectedCategories = null;
    private Button filterButton;
    private ImageButton searchButton;
    private ImageButton recordButton;
    private EditText searchEditText;
    private ArrayList<Integer> indexes;
    private boolean isRefreshClicked = false;
    private boolean isSerachClicked = false;
    private  String searchText;
    private boolean isAdmin = false; // Track if user is admin

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
        searchButton = findViewById(R.id.search);
        searchEditText = findViewById(R.id.editText);
        recordButton = findViewById(R.id.record);


        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(SharedListsViewModel.class);
        listsCount = getIntent().getStringExtra("LIST_COUNT");
        isAdmin = getIntent().getBooleanExtra("IS_ADMIN",false); // Get the key of the list passed via Intent
        viewModel.getSharedLists();

        // Observe loading state
        viewModel.getLoadingLiveData().observe(this, isLoading ->
        {
            if (Boolean.TRUE.equals(isLoading))
            {
                // Optional: Show a loading indicator here
                Toast.makeText(this, "טוען נתונים...", Toast.LENGTH_SHORT).show();
            }
            else
            {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                // Observe lists data
                if((selectedCategories == null && positionMap == null) || isRefreshClicked || isSerachClicked)
                {
                    if (isRefreshClicked)
                    {
                        isRefreshClicked = false;
                    }

                    viewModel.getListsLiveData().observe(this, lists ->
                    {
                        if (lists != null)
                        {
                            positionMap = new HashMap<>();

                            // Store original list before sorting
                            originalList = new ArrayList<>(lists);

                            // Sort by 'saves' in descending order (highest saves first)
                            Collections.sort(lists, (a, b) -> Integer.compare(b.getSaves(), a.getSaves()));

                            adapter = new SharedListAdapter(this, lists);
                            if (isSerachClicked)
                            {
                                isSerachClicked = false;
                                for(int i = 0; i < adapter.getCount(); i++)
                                {
                                    if(!searchText.equals(adapter.getItem(i).getListName()))
                                    {
                                        adapter.remove(adapter.getItem(i));
                                        i--;
                                        listView.setAdapter(adapter);
                                    }
                                }
                                if(adapter.getCount() == 0)
                                {
                                    Toast.makeText(SharedListsActivity.this, "לא נמצאה אף רשימה בשם המתאים\n ברשימות המשותפות", Toast.LENGTH_SHORT).show();
                                }
                            }
                            // Store new indexes as keys and original indexes as values
                            for (int newIndex = 0; newIndex < adapter.getCount(); newIndex++)
                            {
                                int originalIndex = originalList.indexOf(adapter.getItem(newIndex));
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
            ListSharedObject selectedItem = adapter.getItem(position); // Correct type
            String selectedListName = selectedItem.getListName();
            Intent intent = new Intent(SharedListsActivity.this, SharedListsProductsActivity.class);
            if(isAdmin)
            {
                intent.putExtra("IS_ADMIN", true); // Sending boolean extra
            }
            intent.putExtra("LIST_NAME", selectedListName); // Pass the list name
            intent.putExtra("LIST_KEY", String.valueOf(positionMap.get(position))); // Add list ID to intent
            intent.putExtra("LIST_COUNT", listsCount);
            startActivity(intent); // Start Main5Activity
        });

        // Set button listeners
        backButton.setOnClickListener(this);
        refreshButton.setOnClickListener(v ->
        {
            isRefreshClicked = true;
            viewModel.getSharedLists();
        });
        filterButton.setOnClickListener(v -> showCategoryDialog());
        recordButton.setOnClickListener(this);

        searchButton.setOnClickListener(v ->
        {
            // Get the text the user typed into the search box
            searchText = searchEditText.getText().toString().trim();

            // Show the text in a Toast message
            if (!searchText.isEmpty())
            {
                isSerachClicked = true;
                viewModel.getSharedLists();
            }
            else
            {
                Toast.makeText(SharedListsActivity.this, "תיבת החיפוש ריקה", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View view)
    {
        if (view.getId() == R.id.back)
        {
            // Return to the previous activity
            Intent intent = new Intent(this, MyListsActivity.class);
            if(isAdmin)
            {
                intent.putExtra("IS_ADMIN", true); // Sending boolean extra
            }
            startActivity(intent);
        }
        else if (view.getId() == R.id.record)
        {

            // Create an intent for speech recognition
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "he-IL"); // Set language to Hebrew, if needed

            // Start the speech recognition activity
            startActivityForResult(intent, 100);


        }
    }

    public void showCategoryDialog()
    {
        // Flag to confirm dialog action, stored as an array to allow modification inside inner classes.
        final boolean[] isConfirmed = {false};
         // Observe the categories LiveData from the view model.
        viewModel.getCategoriesLiveData().observe(this, categories ->
        {
            // Remove the observer after receiving the data to prevent further updates.
            viewModel.getCategoriesLiveData().removeObservers(this);
            // Check if the retrieved categories are not null.
            if (categories != null)
            {
                // Create a boolean array for tracking which categories are checked.
                boolean[] checkedItems = new boolean[categories.size()];
                // Clean up the categories list if it contains an empty string entry.
                if(categories.size() == 1 && categories.get(0).equals(""))
                {
                    categories.clear();
                }
                if(categories.size() == 2 && categories.get(0).equals(""))
                {
                    categories.remove(0);
                }
                // Convert the categories list to an array for use in the AlertDialog.
                String[] categoryArray = categories.toArray(new String[0]);
                 // Build the AlertDialog for selecting categories.
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                // Set the title of the dialog (in Hebrew: "בחר קטגוריות" means "Choose Categories").
                builder.setTitle("בחר קטגוריות");
                // Set multi-choice items in the dialog.
                builder.setMultiChoiceItems(categoryArray, checkedItems, (dialog, which, isChecked) ->
                {
                    if (isChecked)
                    {
                        // Initialize the selectedCategories list and indexes if they haven't been already.
                        if (selectedCategories == null)
                        {
                            selectedCategories = new ArrayList<>();
                            indexes = new ArrayList<>();
                        }
                        // If the category isn't already in the selected list, add it along with its index.
                        if (!selectedCategories.contains(categoryArray[which]))
                        {
                            selectedCategories.add(categoryArray[which]);
                            indexes.add(which);
                        }
                        // Create a new list to hold the updated selection.
                        List<String> updatedSelection = new ArrayList<>();
                         // Clear indexes and update selections based on the current checkedItems state.
                        indexes.clear();
                        for (int i = 0; i < categoryArray.length; i++)
                        {
                            if (checkedItems[i])
                            {
                                updatedSelection.add(categoryArray[i]);
                                indexes.add(i);
                            }
                        }
                        // Clear the current selectedCategories and update it with the new selection.
                        selectedCategories.clear();
                        selectedCategories.addAll(updatedSelection);
                    }
                    else
                    {
                         // If an item is unchecked, remove it from the selectedCategories list.
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

                        adapter.clear();
                        adapter.addAll(filteredLists);
                        // Store new indexes as keys and original indexes as values
                        for (int newIndex = 0; newIndex < adapter.getCount(); newIndex++)
                        {
                            int originalIndex = originalList.indexOf(adapter.getItem(newIndex));
                            positionMap.put(newIndex, originalIndex);
                        }
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

                            dialog.dismiss();
                            viewModel.deleteCategories(indexes,categories.size());
                        Toast.makeText(this, "הקטגוריות המסומנות נמחקו בהצלחה", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(this, "לא נבחרו קטגוריות למחיקה", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        viewModel.loadCategories();
    }

    public void showAddCategoryDialog(List<String> categories, AlertDialog parentDialog)
    {
        // Create a builder for the add category dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Set the title of the dialog (in Hebrew: "הוסף קטגוריה" means "Add Category").
        builder.setTitle("הוסף קטגוריה");
        // Create an EditText for user input.
        final EditText input = new EditText(this);
        input.setHint("הכנס שם");
        // Attach the EditText to the dialog.
        builder.setView(input);
        // Set up the positive button with text "הוסף".
        builder.setPositiveButton("הוסף", (dialog, which) ->
        {
            // Get the trimmed input text.
            String categoryName = input.getText().toString().trim();
            // Check that the category name is not empty and doesn't already exist in the categories list.
            if (!categoryName.isEmpty() && !categories.contains(categoryName))
            {
                // Add the new category through the ViewModel.
                viewModel.addCategory(categoryName);
                // Show a success toast message.
                Toast.makeText(this, "הקטגוריה נוספה בהצלחה", Toast.LENGTH_SHORT).show();
                parentDialog.dismiss(); // Refresh parent dialog
                showCategoryDialog();
            }
            else
            {
                // Show an error toast if the category already exists or the name is empty.
                Toast.makeText(this, "הקטגוריה כבר קיימת או שהשם ריק", Toast.LENGTH_SHORT).show();
            }
        });
        // Set up the negative button with text "ביטול".
        builder.setNegativeButton("ביטול", (dialog, which) -> dialog.dismiss());
        // Finally, display the add category dialog.
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            // Get the recognized speech text
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                // Set the recognized speech to the EditText
                searchEditText.setText(results.get(0)); // Display the first result in EditText
            }
        }
    }

}
