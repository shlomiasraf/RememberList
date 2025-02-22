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

public class AdminSharedListsActivity extends AppCompatActivity implements View.OnClickListener {
    private AdminSharedListsViewModel viewModel;
    private ListView listView;
    private ProgressBar progressBar;
    private ImageButton backButton;
    private ImageButton refreshButton;
    private ImageButton deleteButton;
    private ListProductsAdapter adapter;
    private HashMap<Integer, Integer> positionMap;
    private List<ListSharedObject> originalList;
    private List<Integer> indexesLists = new ArrayList<>();
    private List<String> selectedLists = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adminsharedlists);

        // Initialize UI elements
        listView = findViewById(R.id.listView);
        progressBar = findViewById(R.id.progressBar);
        backButton = findViewById(R.id.back);
        refreshButton = findViewById(R.id.refresh);
        deleteButton = findViewById(R.id.delete);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(AdminSharedListsViewModel.class);
        viewModel.getSharedLists();

        // Observe loading state
        viewModel.getLoadingLiveData().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        // Observe lists data
        viewModel.getListsLiveData().observe(this, lists -> {
            if (lists != null)
            {
                positionMap = new HashMap<>();
                originalList = new ArrayList<>(lists);
                Collections.sort(lists, (a, b) -> Integer.compare(b.getSaves(), a.getSaves())); // Sort by saves
                ArrayList<Product> adapterList = new ArrayList<>();
                for (ListSharedObject list: lists)
                {
                    adapterList.add(new Product(list.toString(), false));
                }
                adapter = new ListProductsAdapter(this, "SharedLists/", adapterList, false);
                // Store new indexes as keys and original indexes as values
                for (int newIndex = 0; newIndex < lists.size(); newIndex++)
                {
                    int originalIndex = originalList.indexOf(lists.get(newIndex));
                    positionMap.put(newIndex, originalIndex);
                }
                listView.setAdapter(adapter);
            }
        });
        // Set button listeners
        backButton.setOnClickListener(this);
        refreshButton.setOnClickListener(v -> viewModel.getSharedLists());
        deleteButton.setOnClickListener(v -> confirmDeleteLists());
        // ListView item click listener
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Product product = adapter.getProduct(position);

            if (product != null)
            {
                String listName = product.name.substring(0, product.name.indexOf(",")); // Extract list name
                Intent intent = new Intent(AdminSharedListsActivity.this, SharedListsProductsActivity.class);
                intent.putExtra("IS_ADMIN", true); // Sending admin flag
                intent.putExtra("LIST_NAME", listName);
                intent.putExtra("LIST_KEY", String.valueOf(positionMap.get(position))); // Add list ID to intent
                intent.putExtra("From_AdminMode", true);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onClick(View view)
    {
        if (view.getId() == R.id.back)
        {
            Intent intent = new Intent(this, MyListsActivity.class);
            intent.putExtra("IS_ADMIN", true); // Sending boolean extra
            startActivity(intent);
        }
    }
    private void confirmDeleteLists()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("אישור מחיקה")
                .setMessage("האם אתה בטוח שאתה רוצה למחוק את הרשימות המשותפות האלו?")
                .setPositiveButton("מחיקה", (dialog, which) ->
                {
                    Boolean getToast = false;
                    // Collect selected products
                    for (int i = 0; i < adapter.getCount(); i++)
                    {
                        Product product = adapter.getProduct(i);
                        if (product.box) // Check if the product is selected (box checked)
                        {
                            String listName = product.name.substring(0, product.name.indexOf(","));
                            String keyPrefix =  listName + positionMap.get(i);
                            if (keyPrefix.equals("רשימת ספקים לחתונה4") ||
                                    keyPrefix.equals("רשימת ציוד לחו\"ל1") ||
                                    keyPrefix.equals("רשימת ציוד למתגייס2") ||
                                    keyPrefix.equals("רשימת קניות לסופר3") ||
                                    keyPrefix.equals("רשימת קניות לעל האש0"))
                            {
                                Toast.makeText(this, "לא ניתן למחוק רשימה מהרשימות ההתחלתיות", Toast.LENGTH_SHORT).show();
                                getToast = true;
                            }
                            else
                            {
                                selectedLists.add(listName);
                                indexesLists.add(positionMap.get(i));
                            }


                        }
                    }
                    if (selectedLists.isEmpty())
                    {
                        if(!getToast)
                        {
                            Toast.makeText(this, "לא סומנו ערכים למחיקה", Toast.LENGTH_SHORT).show();
                        }
                        return;
                    }
                    else
                    {
                        viewModel.deleteSharedLists(selectedLists, indexesLists, adapter.getCount());
                        selectedLists.clear();
                    }
                })
                .setNegativeButton("ביטול", null)
                .show();
    }
}
