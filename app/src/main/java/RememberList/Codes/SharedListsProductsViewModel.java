package RememberList.Codes;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

public class SharedListsProductsViewModel extends AndroidViewModel {

    private final Repository repository; // Repository instance
    private final MutableLiveData<List<String>> contentLiveData = new MutableLiveData<>(); // LiveData for list content
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(); // LiveData for loading state
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>(); // LiveData for error messages

    public SharedListsProductsViewModel(@NonNull Application application)
    {
        super(application);
        repository = new Repository(application); // Initialize the Repository
    }

    // Expose LiveData for list content
    public LiveData<List<String>> getContentLiveData()
    {
        return contentLiveData;
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
    public void fetchContent(String listId)
    {
        loadingLiveData.setValue(true); // Set loading state
        repository.fetchContent(listId, contentLiveData, errorLiveData, loadingLiveData);
    }
}
