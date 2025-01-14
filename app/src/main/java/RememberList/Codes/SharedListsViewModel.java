package RememberList.Codes;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.HashMap;
import java.util.List;

public class SharedListsViewModel extends AndroidViewModel {

    private final Repository repository; // Repository instance
    private final MutableLiveData<List<String>> listsLiveData = new MutableLiveData<>(); // LiveData for list titles
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(); // LiveData for loading state
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>(); // LiveData for error messages

    public SharedListsViewModel(@NonNull Application application)
    {
        super(application);
        repository = new Repository(application); // Initialize the Repository
    }

    // Expose LiveData for list titles
    public LiveData<List<String>> getListsLiveData()
    {
        return listsLiveData;
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

    // Fetch lists via the Repository
    public void fetchListsFromFirebase()
    {
        loadingLiveData.setValue(true);
        repository.fetchLists(listsLiveData, errorLiveData, loadingLiveData);
    }

    // Expose the list ID to title mapping
    public HashMap<String, String> getListIdToTitleMap()
    {
        return repository.getListIdToTitleMap();
    }
}
