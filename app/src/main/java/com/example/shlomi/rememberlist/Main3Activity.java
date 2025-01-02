package com.example.shlomi.rememberlist;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;

public class Main3Activity extends AppCompatActivity implements View.OnClickListener
{
    private Main3ViewModel viewModel; // ViewModel for managing data
    private ListAdapter boxAdapter; // Adapter for the ListView
    private EditText editText; // Input field for adding products
    private ListView lvMain; // ListView for displaying products
    private ArrayList<Product> selectedProducts = new ArrayList<>(); // List to hold selected products for deletion
    private String listName; // The name of the list being managed

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        // Initialize UI elements
        editText = findViewById(R.id.editText);
        lvMain = findViewById(R.id.listView);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(Main3ViewModel.class);
        listName = getIntent().getStringExtra("LIST_NAME"); // Get the list name passed via Intent
        viewModel.init(listName); // Initialize ViewModel with the list name

        // Observe products LiveData to update the UI when data changes
        viewModel.getProducts().observe(this, products -> {
            if (products != null)
            {
                boxAdapter = new ListAdapter(Main3Activity.this, new ArrayList<>(products)); // Ensure a fresh ArrayList
                lvMain.setAdapter(boxAdapter); // Set the adapter for the ListView
            }
        });

        // Set click listeners for the "Add" and "Delete" buttons
        findViewById(R.id.add).setOnClickListener(this);
        findViewById(R.id.delete).setOnClickListener(this);
    }

    @Override
    public void onClick(View view)
    {
        if (view.getId() == R.id.add)
        {
            // Handle adding a new product
            String productName = editText.getText().toString().trim();
            if (!productName.isEmpty())
            {
                viewModel.addProduct(listName, productName); // Add product via ViewModel
                editText.setText(""); // Clear the input field
                Toast.makeText(this, "Product added", Toast.LENGTH_SHORT).show(); // Show confirmation
            }
            else
            {
                Toast.makeText(this, "Enter a product name", Toast.LENGTH_SHORT).show(); // Prompt for input
            }
        }
        else if (view.getId() == R.id.delete)
        {
            // Handle deleting selected products
            showDeleteConfirmationDialog();
        }
    }

    /**
     * Displays a confirmation dialog to confirm deletion of selected items.
     */
    private void showDeleteConfirmationDialog()
    {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to delete the selected items?") // Dialog message
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Collect selected products
                    for (int i = 0; i < boxAdapter.getCount(); i++)
                    {
                        Product product = boxAdapter.getProduct(i);
                        if (product.box) // Check if the product is selected (box checked)
                        {
                            selectedProducts.add(product);
                        }
                    }
                    // Delete selected products via ViewModel
                    viewModel.deleteProducts(selectedProducts);
                    selectedProducts.clear(); // Clear the selected products list
                    Toast.makeText(this, "Selected items deleted", Toast.LENGTH_SHORT).show(); // Show confirmation
                })
                .setNegativeButton("Cancel", null) // Do nothing on cancel
                .show();
    }
}
