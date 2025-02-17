package RememberList.Codes;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.util.List;
public class MyListsViewModel extends AndroidViewModel {

    private final MutableLiveData<List<String>> listsLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>();
    private final Repository repository;

    public MyListsViewModel(@NonNull Application application) {
        super(application);
        repository = new Repository(application);
    }

    public LiveData<List<String>> getLists() {
        return listsLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public LiveData<Boolean> getLoadingLiveData() {
        return loadingLiveData;
    }

    /**
     * This method checks and loads data if necessary and ensures synchronization.
     */
    public void checkAndLoadData() {
        // Indicate loading has started
        loadingLiveData.setValue(true);

        // Use the repository to check and load data
        repository.checkAndLoadData(loadingLiveData, errorLiveData);

        // Observe `loadingLiveData` to determine when `checkAndLoadData` has finished
        loadingLiveData.observeForever(new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isLoading)
            {
                if (Boolean.FALSE.equals(isLoading)) {
                    // Call `loadLists` only after `checkAndLoadData` completes
                    loadLists();
                    // Remove the observer to avoid memory leaks
                    loadingLiveData.removeObserver(this);
                }
            }
        });
    }

    /**
     * This method loads the lists from the repository.
     */
    public void loadLists()
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
            repository.getLists("UserLists",listsLiveData, loadingLiveData, errorLiveData);
        } catch (Exception e)
        {
            errorLiveData.setValue("Failed to load lists: " + e.getMessage());
        }

    }

    /**
     * Adds a new list to the repository and reloads lists.
     *
     * @param listName The name of the list to add.
     */
    public void addList(String listName)
    {
        loadingLiveData.setValue(true); // Set loading state to true

        // Add the list to the repository
        repository.addList(listName, loadingLiveData, errorLiveData);

        // Observe the loading state and reload the lists after the add operation completes
        loadingLiveData.observeForever(new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isLoading) {
                if (Boolean.FALSE.equals(isLoading)) {
                    // Load the lists only after the add operation completes
                    loadLists();
                    // Remove the observer to avoid memory leaks
                    loadingLiveData.removeObserver(this);
                }
            }
        });
    }

    /**
     * Deletes a list from the repository and reloads lists.
     *
     * @param listName The name of the list to delete.
     */
    public void deleteList(String listName,String key)
    {
        loadingLiveData.setValue(true); // Set loading state to true

        // Add the list to the repository
        repository.deleteList(listName, key, loadingLiveData, errorLiveData);

        // Observe the loading state and reload the lists after the add operation completes
        loadingLiveData.observeForever(new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isLoading) {
                if (Boolean.FALSE.equals(isLoading))
                {
                    // Load the lists only after the add operation completes
                    loadLists();
                    // Remove the observer to avoid memory leaks
                    loadingLiveData.removeObserver(this);
                }
            }
        });
    }

    public void logout() {
        repository.logout();
    }
}
