package com.example.shlomi.rememberlist;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainViewModel extends ViewModel {
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final MutableLiveData<FirebaseUser> userState = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingState = new MutableLiveData<>();
    private final MutableLiveData<String> errorState = new MutableLiveData<>();
    private Repository repository;

    public void init(Context context)
    {
        // Initialize the repository
        repository = new Repository(context);

        // Check the current user state
        checkUserState();
    }

    private void checkUserState()
    {
        loadingState.setValue(true); // Start loading
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null)
        {
            // Reload user data to ensure it is up-to-date
            currentUser.reload().addOnCompleteListener(task -> {
                loadingState.setValue(false); // Stop loading
                if (task.isSuccessful())
                {
                    userState.setValue(currentUser); // User is valid
                }
                else
                {
                    mAuth.signOut(); // Sign out if user is invalid
                    userState.setValue(null);
                    errorState.setValue("Failed to reload user data. Signing out.");
                }
            });
        }
        else
        {
            loadingState.setValue(false); // Stop loading
            userState.setValue(null); // No user signed in
        }
    }

    // Expose user state as LiveData for observers
    public LiveData<FirebaseUser> getUserState()
    {
        return userState;
    }

    // Expose loading state as LiveData for observers
    public LiveData<Boolean> getLoadingState()
    {
        return loadingState;
    }

    // Expose error state as LiveData for observers
    public LiveData<String> getErrorState()
    {
        return errorState;
    }

    // Check if data needs to be loaded and trigger the repository to load it
    public void checkAndLoadData()
    {
        loadingState.setValue(true); // Start loading
        repository.checkAndLoadData(loadingState, errorState);
    }
}
