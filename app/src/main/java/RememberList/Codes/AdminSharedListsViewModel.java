package RememberList.Codes;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.util.List;

public class AdminSharedListsViewModel extends AndroidViewModel {
    private final Repository repository;
    private final MutableLiveData<List<ListSharedObject>> listsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    public AdminSharedListsViewModel(@NonNull Application application) {
        super(application);
        // Initialize the repository with the application context.
        repository = new Repository(application);
    }
    
     /**
     * Returns the LiveData containing the list of shared lists.
     *
     * @return LiveData of ListSharedObject list.
     */
    public LiveData<List<ListSharedObject>> getListsLiveData() {
        return listsLiveData;
    }
    
    /**
     * Returns the LiveData indicating the loading status.
     *
     * @return LiveData of a Boolean indicating loading status.
     */
    public LiveData<Boolean> getLoadingLiveData() {
        return loadingLiveData;
    }
    
    /**
     * Returns the LiveData containing error messages.
     *
     * @return LiveData of a String representing error messages.
     */
    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    /**
     * Fetches the shared lists from Firebase.
     */
    public void getSharedLists() {
        if (Boolean.TRUE.equals(loadingLiveData.getValue())) {
            return;
        }

        loadingLiveData.setValue(true);
        try {
            repository.getSharedLists(listsLiveData, loadingLiveData, errorLiveData);
        } catch (Exception e) {
            errorLiveData.setValue("Failed to load lists: " + e.getMessage());
            loadingLiveData.setValue(false);
        }
    }

    /**
     * Deletes selected shared lists.
     */
    public void deleteSharedLists(List<String> listsToDelete, List<Integer> indexesToDelete, int sharedListsSize)
    {
        if (listsToDelete.isEmpty()) {
            return;
        }

        loadingLiveData.setValue(true);
        try
        {
            repository.deleteSharedLists(indexesToDelete, listsToDelete, loadingLiveData, errorLiveData);
            // Observe the loading state and reload the lists after the add operation completes
            loadingLiveData.observeForever(new Observer<Boolean>()
            {
                @Override
                public void onChanged(Boolean isLoading)
                {
                    if (Boolean.FALSE.equals(isLoading))
                    {
                        // Load the lists only after the add operation completes
                        getSharedLists();
                        // Remove the observer to avoid memory leaks
                        loadingLiveData.removeObserver(this);
                    }
                }
            });
        }
        catch (Exception e)
        {
            errorLiveData.setValue("Failed to delete lists: " + e.getMessage());
            loadingLiveData.setValue(false);
        }
    }
}
