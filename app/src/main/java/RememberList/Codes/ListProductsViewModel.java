package RememberList.Codes;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.util.ArrayList;
import java.util.List;

public class ListProductsViewModel extends AndroidViewModel {

    private final Repository repository; // Repository to manage data operations
    private final MutableLiveData<List<Product>> productsLiveData = new MutableLiveData<>(); // LiveData for the list of products
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(); // LiveData for the loading state
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>(); // LiveData for error messages
    private String listName; // The name of the list being managed
    private String listKey; // The key of the list being managed
    /**
     * ViewModel for managing and interacting with a list of products.
     * Provides data to the UI and handles interactions with the repository.
     */
    public ListProductsViewModel(@NonNull Application application) {
        super(application);
        repository = new Repository(application); // Initialize the repository
    }

    /**
     * Initializes the ViewModel with the name of the list to manage.
     *
     * @param listName the name of the list
     */
    public void init(String listName, String listKey)
    {
        this.listName = listName;
        this.listKey = listKey;
        loadProducts(); // Load products from the repository
    }

    /**
     * Loads products from the repository for the specified list.
     */
    private void loadProducts()
    {
        loadingLiveData.setValue(true); // Indicate loading started
        try
        {
            repository.getValues("UserValues" ,(listName+listKey) ,productsLiveData, loadingLiveData, errorLiveData);
        }
        catch (Exception e)
        {
            errorLiveData.setValue("Failed to load lists: " + e.getMessage()); // Set error message
        }
        loadingLiveData.setValue(false); // Indicate loading finished
    }

    /**
     * Returns LiveData for the list of products.
     *
     * @return LiveData containing the list of products
     */
    public LiveData<List<Product>> getProducts()
    {
        return productsLiveData;
    }

    /**
     * Returns LiveData for the loading state.
     *
     * @return LiveData containing the loading state
     */
    public LiveData<Boolean> getLoadingLiveData() {
        return loadingLiveData;
    }

    /**
     * Returns LiveData for error messages.
     *
     * @return LiveData containing error messages
     */
    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    /**
     * Adds a new product to the list and updates the repository.
     *
     * @param productName the name of the product to add
     */
    public void addProduct(String productName)
    {
        loadingLiveData.setValue(true); // Set loading state to true
        ArrayList<String> valuesToAdd = new ArrayList<>();
        valuesToAdd.add(productName);
        repository.addValues((listName+listKey), valuesToAdd, loadingLiveData, errorLiveData); // Add the product to the repository
        // Observe the loading state and reload the lists after the add operation completes
        loadingLiveData.observeForever(new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isLoading)
            {
                if (Boolean.FALSE.equals(isLoading))
                {
                    // Load the lists only after the add operation completes
                    loadProducts();
                    // Remove the observer to avoid memory leaks
                    loadingLiveData.removeObserver(this);
                }
            }
        });
    }

    /**
     * Deletes selected products from the list and updates the repository.
     *
     * @param selectedProducts the list of products to delete
     */
    public void deleteProducts(List<Product> selectedProducts)
    {
        loadingLiveData.setValue(true); // Set loading state to true
        // Reverse loop to avoid index shifting issues
        int[] indexes = new int[selectedProducts.size()];
        for (int i = 0; i < selectedProducts.size(); i++)
        {
            Product product = selectedProducts.get(i);
            indexes[i] = productsLiveData.getValue() != null ? productsLiveData.getValue().indexOf(product) : -1;
        }
        repository.deleteValues((listName+listKey), indexes,loadingLiveData, errorLiveData); // Delete the products from the repository
        // Observe the loading state and reload the lists after the add operation completes
        loadingLiveData.observeForever(new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isLoading)
            {
                if (Boolean.FALSE.equals(isLoading))
                {
                    // Load the lists only after the add operation completes
                    loadProducts();
                    // Remove the observer to avoid memory leaks
                    loadingLiveData.removeObserver(this);
                }
            }
        });
    }
}
