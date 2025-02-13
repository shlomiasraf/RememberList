package RememberList.Codes;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

public class SharedListsProductsViewModel extends AndroidViewModel {

    private final Repository repository; // Repository instance
    private final MutableLiveData<List<Product>> productsLiveData = new MutableLiveData<>(); // LiveData for list content
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(); // LiveData for loading state
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>(); // LiveData for error messages
    String listName;
    String listKey;
    public SharedListsProductsViewModel(@NonNull Application application)
    {
        super(application);
        repository = new Repository(application); // Initialize the Repository
    }
    public void init(String listName, String listKey)
    {
        this.listName = listName;
        this.listKey = listKey;
        getValues(); // Load products from the repository
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

    // Expose LiveData for loading state
    public LiveData<Boolean> getLoadingLiveData()
    {
        return loadingLiveData;
    }

    // Expose LiveData for error messages
    public LiveData<String> getErrorLiveData()
    {
        return errorLiveData;
    }

    // Fetch content for a specific list ID using the Repository
    public void getValues()
    {
        loadingLiveData.setValue(true); // Indicate loading started
        try
        {
            repository.getValues("SharedValues" ,(listName+listKey) ,productsLiveData, loadingLiveData, errorLiveData);
        }
        catch (Exception e)
        {
            errorLiveData.setValue("Failed to load lists: " + e.getMessage()); // Set error message
        }
        loadingLiveData.setValue(false); // Indicate loading finished
    }
}
