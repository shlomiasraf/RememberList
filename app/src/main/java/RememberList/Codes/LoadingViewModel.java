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
    private final MutableLiveData<FirebaseUser> userState = new MutableLiveData<>();
    // LiveData to observe loading state (e.g., showing or hiding a progress bar)
    private final MutableLiveData<Boolean> loadingState = new MutableLiveData<>();
    // LiveData to observe error messages
    private final MutableLiveData<String> errorState = new MutableLiveData<>();
    // Repository for handling data operations
    private Repository repository;


    // Initialize the ViewModel
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
        loadingState.setValue(true);  // Indicate that a loading process has started
        FirebaseUser currentUser = mAuth.getCurrentUser(); // Get the currently signed-in user

        if (currentUser != null) // If a user is signed in
        {
            // Reload user data to ensure it is up-to-date
            currentUser.reload().addOnCompleteListener(task -> {
                loadingState.setValue(false);  // Indicate that loading has completed
                if (task.isSuccessful()) // If reload was successful
                {
                    userState.setValue(currentUser);  // Update user state with the current user
                }
                else // If reload failed
                {
                    mAuth.signOut();  // Sign out the user
                    userState.setValue(null);// Clear the user state
                    errorState.setValue("Failed to reload user data. Signing out.");// Notify about the error
                }
            });
        }
        else // If no user is signed in
        {
            loadingState.setValue(false); // Indicate that loading has completed
            userState.setValue(null);  // Clear the user state
        }
    }

    // Expose the user state as LiveData so it can be observed by the UI
    public LiveData<FirebaseUser> getUserState()
    {
        return userState;
    }

    // Expose the loading state as LiveData so it can be observed by the UI
    public LiveData<Boolean> getLoadingState()
    {
        return loadingState;
    }

    // Expose the error state as LiveData so it can be observed by the UI
    public LiveData<String> getErrorState()
    {
        return errorState;
    }

    // Check if any additional data needs to be loaded and trigger the repository to handle it
    public void checkAndLoadData()
    {
        loadingState.setValue(true);  // Indicate that a loading process has started
        repository.checkAndLoadData(loadingState, errorState); // Use the repository to load required data
    }
}
