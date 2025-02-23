package RememberList.Codes;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class SharedListsProductsActivity extends AppCompatActivity implements View.OnClickListener
{


    private SharedListsProductsViewModel viewModel; // ViewModel instance
    private ListView contentListView; // ListView for displaying content
    private ArrayAdapter<String> adapter; // Adapter for ListView
    private ArrayList<String> valuesList; // Data list for the adapter
    private ProgressBar progressBar; // ProgressBar for loading indicator

    private String listKey; // Declare listKey as a class-level variable
    private String listsCount;
    private String listName; // Declare listName as a class-level variable
    private boolean isAdmin = false; // Track if user is admin
    private boolean fromAdmin = false;
    private ImageButton saveButton;
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
        listName = getIntent().getStringExtra("LIST_NAME");
        listKey = getIntent().getStringExtra("LIST_KEY");
        listsCount = getIntent().getStringExtra("LIST_COUNT");
        isAdmin = getIntent().getBooleanExtra("IS_ADMIN",false); // Get the key of the list passed via Intent
        fromAdmin = getIntent().getBooleanExtra("From_AdminMode",false); // Get the key of the list passed via Intent
        title.setText(listName);

        // Validate that a valid list ID was received
        if (listKey == null || listKey.isEmpty())
        {
            Toast.makeText(this, "חסר מזהה הרשימה", Toast.LENGTH_SHORT).show();
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

        //UI - listeners
        findViewById(R.id.back).setOnClickListener(this);
        saveButton = findViewById(R.id.save);
        saveButton.setOnClickListener(this);
        if (!fromAdmin)
        {
            saveButton.setVisibility(View.VISIBLE); // Show the button only if admin
        }
    }
    @Override
    public void onClick(View view)
    {
        if (view.getId() == R.id.back)
        {
            Intent intent;
            if (fromAdmin)
            {
                intent = new Intent(this, AdminSharedListsActivity.class);
            }
            else
            {
                intent = new Intent(this, SharedListsActivity.class);
            }
            if(isAdmin)
            {
                intent.putExtra("IS_ADMIN", true); // Sending boolean extra
            }
            startActivity(intent);
        }
        else if(view.getId() == R.id.save && !fromAdmin)
        {
            showSaveOptionsDialog();
        }
    }

    // Method to show the save options dialog
    private void showSaveOptionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Save List");

        builder.setItems(new CharSequence[]{"שמור בשם המקורי", "שמור בשם חדש", "בטל"}, (dialog, which) -> {
            if (which == 0)
            {
                saveListWithOriginalName();
            }
            else if (which == 1)
            {
                showNewNameInputDialog();
            }
            dialog.dismiss();
        });

        builder.create().show();
    }

    // Method to save with the original name
    private void saveListWithOriginalName()
    {
        Toast.makeText(this, "שומר בשם המקורי...", Toast.LENGTH_SHORT).show();
        viewModel.saveListToUser(listName,listName+listsCount, valuesList);
        listsCount = String.valueOf(Integer.valueOf(listsCount)+1);
    }


    // Method to show an input dialog for a new name
    private void showNewNameInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("הכנס שם חדש");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("שמור", (dialog, which) ->
        {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty())
            {
                Toast.makeText(this, "שומר בשם: " + newName, Toast.LENGTH_SHORT).show();
                viewModel.saveListToUser(newName,newName+listsCount, valuesList);
                listsCount = String.valueOf(Integer.valueOf(listsCount)+1);
            }
            else
            {
                Toast.makeText(this, "שם לא יכול להיות ריק!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("בטל", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }
}
