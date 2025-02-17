package RememberList.Codes;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.util.ArrayList;
import java.util.List;

public class SharedListsViewModel extends AndroidViewModel {

    private final Repository repository;
    private final MutableLiveData<List<String>> listsLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<String>> categoriesLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    private final MutableLiveData<List<String>> listCategoriesLiveData = new MutableLiveData<>();

    /**
     * A map to track which category belongs to each list title:
     * key   = list title
     * value = category string
     */

    public SharedListsViewModel(@NonNull Application application)
    {
        super(application);
        repository = new Repository(application);
    }

    // region LiveData Getters

    /**
     * Exposes LiveData for the list of categories.
     *
     * @return LiveData containing the list of categories.
     */
    public LiveData<List<String>> getCategoriesLiveData()
    {
        return categoriesLiveData;
    }

    /**
     * Exposes LiveData for the list of shared lists.
     *
     * @return LiveData containing the list of shared lists.
     */
    public LiveData<List<String>> getListsLiveData()
    {
        return listsLiveData;
    }

    /**
     * Exposes LiveData for the loading state.
     *
     * @return LiveData containing the loading state.
     */
    public LiveData<Boolean> getLoadingLiveData()
    {
        return loadingLiveData;
    }
    public LiveData<List<String>> getlistCategoriesLiveData()
    {
        return listCategoriesLiveData;
    }

    /**
     * Exposes LiveData for error messages.
     *
     * @return LiveData containing error messages.
     */
    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    // endregion

    // region Category Management

    /**
     * Loads categories from the repository and updates the LiveData.
     */
    public void loadCategories()
    {
        if (Boolean.TRUE.equals(loadingLiveData.getValue())) {
            return; // Prevent concurrent operations
        }

        loadingLiveData.setValue(true);

        try
        {
            repository.getCategories(categoriesLiveData, loadingLiveData, errorLiveData);
        } catch (Exception e) {
            errorLiveData.setValue("Failed to load categories: " + e.getMessage());
            loadingLiveData.setValue(false);
        }
    }

    /**
     * Adds a new category and refreshes the category list.
     *
     * @param categoryName The name of the category to add.
     */
    public void addCategory(String categoryName)
    {
        loadingLiveData.setValue(true); // Set loading state to true
        repository.addCategory(categoryName, loadingLiveData, errorLiveData);
        List<String> updatedCategories = categoriesLiveData.getValue();
        if (updatedCategories == null)
        {
            updatedCategories = new ArrayList<>();
        }
        if (!updatedCategories.contains(categoryName))
        {
            updatedCategories.add(categoryName);
            categoriesLiveData.setValue(updatedCategories);
        }
        loadingLiveData.observeForever(new Observer<Boolean>()
        {
            @Override
            public void onChanged(Boolean isLoading)
            {
                if (Boolean.FALSE.equals(isLoading))
                {
                    loadCategories();
                    loadingLiveData.removeObserver(this);
                }
            }
        });
    }

    /**
     * Deletes a category and refreshes the category list.
     *
     * @param categoryName The name of the category to delete.
     */
    public void deleteCategory(String categoryName)
    {
        loadingLiveData.setValue(true); // Set loading state to true
        // Add the category to the repository
        repository.deleteCategory(categoryName, loadingLiveData, errorLiveData);
        // Observe the loading state and reload the categories after the add operation completes
        loadingLiveData.observeForever(new Observer<Boolean>()
        {
            @Override
            public void onChanged(Boolean isLoading)
            {
                if (Boolean.FALSE.equals(isLoading))
                {
                    loadCategories(); // Refresh categories after deleting
                    // Remove the observer to avoid memory leaks
                    loadingLiveData.removeObserver(this);
                }
            }
        });
    }

    /**
     * get shared lists from the repository and updates the LiveData.
     */
    /**
     * get shared lists from the repository and updates the LiveData.
     */
    public void getSharedLists()
    {
        // Prevent loading lists if another operation is still in progress
        if (Boolean.TRUE.equals(loadingLiveData.getValue()))
        {
            return;
        }

        // Indicate loading has started
        loadingLiveData.setValue(true);

        // Fetch the lists from the repository
        try
        {
            repository.getLists("SharedLists",listsLiveData, loadingLiveData, errorLiveData);
        }
        catch (Exception e)
        {
            errorLiveData.setValue("Failed to load lists: " + e.getMessage());
        }
    }

    /**
     * Filters the shared lists based on a selected category.
     *
     * @param Categories The categories to filter by.
     */
    public void getFilteredLists(List<String> Categories)
    {
        if (Boolean.TRUE.equals(loadingLiveData.getValue()))
        {
            return;
        }

        // Indicate loading has started
        loadingLiveData.setValue(true);

        // Fetch the lists from the repository
        try
        {
            repository.getFilteredLists(Categories, loadingLiveData, listCategoriesLiveData, errorLiveData);
        }
        catch (Exception e)
        {
            errorLiveData.setValue("Failed to load lists: " + e.getMessage());
        }

    }

}
