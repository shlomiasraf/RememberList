package RememberList.Codes;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseUser;

public class LoginViewModel extends AndroidViewModel
{

    private final Repository repository;
    private final MutableLiveData<FirebaseUser> userLiveData;
    private final MutableLiveData<String> errorLiveData;
    private final MutableLiveData<Boolean> loadingLiveData;

    public LoginViewModel(@NonNull Application application) {
        super(application);
        repository = new Repository(application); // Initialize Repository
        userLiveData = new MutableLiveData<>();
        errorLiveData = new MutableLiveData<>();
        loadingLiveData = new MutableLiveData<>();
    }

    // Expose LiveData for observing in UI
    public LiveData<FirebaseUser> getUserLiveData() {
        return userLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public LiveData<Boolean> getLoadingLiveData() {
        return loadingLiveData;
    }

    // Login with email and password
    public void loginWithEmail(String email, String password)
    {
        if (email.isEmpty() || password.isEmpty())
        {
            errorLiveData.setValue("Email and password cannot be empty");
            return;
        }
        repository.loginWithEmail(email, password, userLiveData, errorLiveData, loadingLiveData);
    }

    // Register with email and password
    public void registerWithEmail(String email, String password)
    {
        if (email.isEmpty() || password.isEmpty())
        {
            errorLiveData.setValue("Email and password cannot be empty");
            return;
        }
        repository.registerWithEmail(email, password, userLiveData, errorLiveData, loadingLiveData);
    }

    // Login with Google account
    public void firebaseAuthWithGoogle(GoogleSignInAccount account)
    {
        if (account == null)
        {
            errorLiveData.setValue("Google account is null");
            return;
        }
        repository.loginWithGoogle(account, userLiveData, errorLiveData, loadingLiveData);
    }

    // Logout the user
    public void logout()
    {
        repository.logout();
        userLiveData.setValue(null); // Clear current user
    }

    // Check if user is already logged in
    public void checkIfUserIsLoggedIn()
    {
        loadingLiveData.setValue(true);
        FirebaseUser currentUser = repository.getCurrentUser();
        if (currentUser != null)
        {
            userLiveData.setValue(currentUser); // User is logged in
        }
        else
        {
            errorLiveData.setValue("No user is currently logged in");
        }
        loadingLiveData.setValue(false);
    }
}
