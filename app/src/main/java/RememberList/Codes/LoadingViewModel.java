package RememberList.Codes;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

// ViewModel class for the LoadingActivity
public class LoadingViewModel extends ViewModel
{
    // Firebase authentication instance
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    // LiveData to observe user state (logged in or not)
    private final MutableLiveData<FirebaseUser> userLiveData = new MutableLiveData<>();
    // LiveData to observe loading state (e.g., showing or hiding a progress bar)
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>();
    // LiveData to observe error messages
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isAdminLiveData = new MutableLiveData<>();
    // Repository for handling data operations
    private Repository repository;


    // Initialize the ViewModel
    public LiveData<Boolean> getIsAdminLiveData()
    {
        return isAdminLiveData;
    }
    public void init(Context context)
    {
        // Create the repository with the given context (e.g., for SharedPreferences or database access)
        repository = new Repository(context);

        // Check the current user's authentication state
        checkUserState();
    }

    // Check the authentication state of the current user
    private void checkUserState()
    {
        loadingLiveData.setValue(true);  // Indicate that a loading process has started
        FirebaseUser currentUser = mAuth.getCurrentUser(); // Get the currently signed-in user

        if (currentUser != null) // If a user is signed in
        {
            // Reload user data to ensure it is up-to-date
            currentUser.reload().addOnCompleteListener(task -> {
                loadingLiveData.setValue(false);  // Indicate that loading has completed
                if (task.isSuccessful()) // If reload was successful
                {
                    userLiveData.setValue(currentUser);  // Update user state with the current user
                }
                else // If reload failed
                {
                    mAuth.signOut();  // Sign out the user
                    userLiveData.setValue(null);// Clear the user state
                    errorLiveData.setValue("Failed to reload user data. Signing out.");// Notify about the error
                }
            });
        }
        else // If no user is signed in
        {
            loadingLiveData.setValue(false); // Indicate that loading has completed
            userLiveData.setValue(null);  // Clear the user state
        }
    }
    public void checkIfUserIsAdmin()
    {
        loadingLiveData.setValue(true); // Set loading state to true
        repository.checkIfUserIsAdmin(isAdminLiveData, loadingLiveData, errorLiveData);
    }
    // Expose the user state as LiveData so it can be observed by the UI
    public LiveData<FirebaseUser> getUserLiveData()
    {
        return userLiveData;
    }

    // Expose the loading state as LiveData so it can be observed by the UI
    public LiveData<Boolean> geLoadingLiveData()
    {
        return loadingLiveData;
    }

    // Expose the error state as LiveData so it can be observed by the UI
    public LiveData<String> getErrorLiveData()
    {
        return errorLiveData;
    }

}
