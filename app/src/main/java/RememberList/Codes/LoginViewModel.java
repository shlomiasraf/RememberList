package RememberList.Codes;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseUser;

// The LoginViewModel class handles all logic and state management for login operations
public class LoginViewModel extends AndroidViewModel
{
    // Repository instance for handling data operations
    private final Repository repository;


    // LiveData variables to observe changes in user, errors, and loading state:
    private final MutableLiveData<FirebaseUser> userLiveData; // Holds the current logged-in user
    private final MutableLiveData<String> errorLiveData; // Holds error messages for UI display
    private final MutableLiveData<Boolean> loadingLiveData; // Indicates whether a process is loading

    // Constructor: Initializes the ViewModel and its components
    public LoginViewModel(@NonNull Application application) {
        super(application);
        repository = new Repository(application);  // Initialize the repository with the application context
        userLiveData = new MutableLiveData<>();// Initialize user LiveData
        errorLiveData = new MutableLiveData<>(); // Initialize error LiveData
        loadingLiveData = new MutableLiveData<>(); // Initialize loading LiveData
    }

    // Expose user LiveData for observing changes in the logged-in user
    public LiveData<FirebaseUser> getUserLiveData() {
        return userLiveData;
    }

    // Expose error LiveData for observing error messages
    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    // Expose loading LiveData for observing loading state
    public LiveData<Boolean> getLoadingLiveData() {
        return loadingLiveData;
    }

    // Method to log in with email and password
    public void loginWithEmail(String email, String password)
    {
        if (email.isEmpty() || password.isEmpty())// Validate email and password inputs
        {
            errorLiveData.setValue("Email and password cannot be empty"); // Set error message
            return;// Exit the method
        }
        // Delegate login logic to the repository
        repository.loginWithEmail(email, password, userLiveData, errorLiveData, loadingLiveData);
    }

    // Method to register a new user with email and password
    public void registerWithEmail(String email, String password)
    {
        if (email.isEmpty() || password.isEmpty()) // Validate email and password inputs
        {
            errorLiveData.setValue("Email and password cannot be empty");// Set error message
            return; // Exit the method
        }
        // Delegate registration logic to the repository
        repository.registerWithEmail(email, password, userLiveData, errorLiveData, loadingLiveData);
    }

    // Method to authenticate user with a Google account
    public void firebaseAuthWithGoogle(GoogleSignInAccount account)
    {
        if (account == null) // Validate the Google account object
        {
            errorLiveData.setValue("Google account is null");// Set error message
            return;// Exit the method
        }
        // Delegate Google sign-in logic to the repository
        repository.loginWithGoogle(account, userLiveData, errorLiveData, loadingLiveData);
    }

    // Method to log out the current user
    public void logout()
    {
        repository.logout();// Call the repository's logout method
        userLiveData.setValue(null); // Clear the current user from LiveData
    }

    // Method to check if a user is already logged in
    public void checkIfUserIsLoggedIn()
    {
        loadingLiveData.setValue(true); // Set loading state to true
        FirebaseUser currentUser = repository.getCurrentUser(); // Get the currently logged-in user
        if (currentUser != null)// If a user is logged in
        {
            userLiveData.setValue(currentUser);  // Update the user LiveData
        }
        else // If no user is logged in
        {
            errorLiveData.setValue("No user is currently logged in");// Set error message
        }
        loadingLiveData.setValue(false);// Set loading state to false
    }
}
