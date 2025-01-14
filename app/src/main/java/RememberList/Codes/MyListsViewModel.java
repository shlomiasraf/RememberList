package RememberList.Codes;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

/**
 * ViewModel for managing the list of items in Main2Activity.
 * Handles interaction between the UI and the Repository for adding, deleting, and loading lists.
 */
public class MyListsViewModel extends AndroidViewModel
{
    // LiveData to hold the list of items
    private final MutableLiveData<List<String>> lists = new MutableLiveData<>();
    // LiveData to hold error messages
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    // LiveData to hold loading state
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>();
    // Repository instance for data operations
    private final Repository repository;

    /**
     * Constructor for initializing the ViewModel.
     *
     * @param application The application context
     */
    public MyListsViewModel(@NonNull Application application)
    {
        super(application);
        repository = new Repository(application); // Initialize Repository
        loadLists(); // Load initial lists
    }

    /**
     * Provides LiveData for the list of items.
     *
     * @return LiveData containing the list of items
     */
    public LiveData<List<String>> getLists()
    {
        return lists;
    }

    /**
     * Provides LiveData for error messages.
     *
     * @return LiveData containing error messages
     */
    public LiveData<String> getErrorLiveData()
    {
        return errorLiveData;
    }

    /**
     * Provides LiveData for loading state.
     *
     * @return LiveData indicating whether data is being loaded
     */
    public LiveData<Boolean> getLoadingLiveData()
    {
        return loadingLiveData;
    }

    /**
     * Adds a new list item.
     *
     * @param listName The name of the list to add
     */
    public void addList(String listName)
    {
        loadingLiveData.setValue(true); // Indicate loading started
        repository.addList(listName, errorLiveData); // Add list via Repository
        loadLists(); // Reload lists after adding
    }

    /**
     * Deletes an existing list item.
     *
     * @param listName The name of the list to delete
     */
    public void deleteList(String listName)
    {
        loadingLiveData.setValue(true); // Indicate loading started
        repository.deleteList(listName, errorLiveData); // Delete list via Repository
        loadLists(); // Reload lists after deletion
    }

    /**
     * Loads the lists from the Repository.
     */
    private void loadLists()
    {
        loadingLiveData.setValue(true); // Indicate loading started
        try
        {
            lists.setValue(repository.getLists()); // Retrieve lists from Repository
        }
        catch (Exception e)
        {
            errorLiveData.setValue("Failed to load lists: " + e.getMessage()); // Set error message
        }
        loadingLiveData.setValue(false); // Indicate loading finished
    }
}
