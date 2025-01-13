package RememberList.Codes;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

public class ListProductsViewModel extends AndroidViewModel {

    private final Repository repository; // Repository to manage data operations
    private final MutableLiveData<List<Product>> products = new MutableLiveData<>(); // LiveData for the list of products
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(); // LiveData for the loading state
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>(); // LiveData for error messages
    private String listName; // The name of the list being managed

    public ListProductsViewModel(@NonNull Application application)
    {
        super(application);
        repository = new Repository(application); // Initialize the repository
    }

    /**
     * Initializes the ViewModel with the name of the list to manage.
     * @param listName the name of the list
     */
    public void init(String listName)
    {
        this.listName = listName;
        loadProducts(); // Load products from the repository
    }

    /**
     * Loads products from the repository for the specified list.
     */
    private void loadProducts()
    {
        loadingLiveData.setValue(true); // Indicate that data loading has started
        try
        {
            List<String> items = repository.getValues(listName); // Retrieve raw data
            List<Product> productList = new ArrayList<>();
            for (String item : items)
            {
                productList.add(new Product(item, false)); // Convert raw data into Product objects
            }
            products.setValue(productList); // Update LiveData with the list of products
        }
        catch (Exception e)
        {
            errorLiveData.setValue("Failed to load products: " + e.getMessage()); // Handle errors
        }
        loadingLiveData.setValue(false); // Indicate that data loading has completed
    }

    /**
     * Returns LiveData for the list of products.
     * @return LiveData containing the list of products
     */
    public LiveData<List<Product>> getProducts()
    {
        return products;
    }

    /**
     * Returns LiveData for the loading state.
     * @return LiveData containing the loading state
     */
    public LiveData<Boolean> getLoadingLiveData()
    {
        return loadingLiveData;
    }

    /**
     * Returns LiveData for error messages.
     * @return LiveData containing error messages
     */
    public LiveData<String> getErrorLiveData()
    {
        return errorLiveData;
    }

    /**
     * Adds a new product to the list and updates the repository.
     * @param productName the name of the product to add
     */
    public void addProduct(String listName,String productName)
    {
        loadingLiveData.setValue(true); // Indicate that an operation is in progress
        try
        {
            repository.addValue(listName, productName, errorLiveData); // Add the product to the repository
            loadProducts(); // Reload products to reflect changes
        }
        catch (Exception e)
        {
            errorLiveData.setValue("Failed to add product: " + e.getMessage()); // Handle errors
        }
        loadingLiveData.setValue(false); // Indicate that the operation has completed
    }

    /**
     * Deletes selected products from the list and updates the repository.
     * @param selectedProducts the list of products to delete
     */
    public void deleteProducts(List<Product> selectedProducts)
    {
        loadingLiveData.setValue(true); // Indicate that an operation is in progress
        try
        {
            // Reverse loop to avoid index shifting issues
            for (int i = 0; i < selectedProducts.size(); i++)
            {
                Product product = selectedProducts.get(i);
                int index = products.getValue() != null ? products.getValue().indexOf(product) : -1;
                if (index >= 0)
                {
                    repository.deleteValue(listName, index-i, errorLiveData); // Delete the product from the repository
                }
            }
            loadProducts(); // Reload products to reflect changes
        }
        catch (Exception e)
        {
            errorLiveData.setValue("Failed to delete products: " + e.getMessage()); // Handle errors
        }
        loadingLiveData.setValue(false); // Indicate that the operation has completed
    }
}
