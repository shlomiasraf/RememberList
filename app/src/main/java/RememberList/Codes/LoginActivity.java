package RememberList.Codes;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;

// The LoginActivity class manages the login screen for the app

public class LoginActivity extends AppCompatActivity
{
    // Constant used to identify Google sign-in requests(arbitrary variable)
    private static final int RC_SIGN_IN = 101;

    // Variables for managing ViewModel and Google sign-in:
    private LoginViewModel viewModel; // Manages login-related logic
    private GoogleSignInClient googleSignInClient; //Manages Google sign-in client

    // UI elements:
    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin, buttonRegister;
    private SignInButton buttonGoogleSignIn;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);  // Link the activity to its XML layout

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Request client ID from server
                .requestEmail()// Request user's email
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso); // Create Google sign-in client

        // Link UI elements from XML
        editTextEmail = findViewById(R.id.editTextEmail); // Email text field
        editTextPassword = findViewById(R.id.editTextPassword); // Password text field
        buttonLogin = findViewById(R.id.buttonLogin); // Login button
        buttonRegister = findViewById(R.id.buttonRegister); // Register button
        buttonGoogleSignIn = findViewById(R.id.buttonGoogleSignIn);// Google sign-in button

        // Observe changes in the ViewModel data:
        viewModel.getUserLiveData().observe(this, user -> {
            if (user != null)
            {
                navigateToMain(); // Navigate to the main screen
            }
        });

        viewModel.getErrorLiveData().observe(this, error -> {
            if (error != null) // If there is an error
            {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show(); // Display error message
            }
        });

        viewModel.getLoadingLiveData().observe(this, isLoading -> {
            // Logic to show or hide a loading indicator if needed
        });

        // Handle login with email and password
        buttonLogin.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();// Get email input
            String password = editTextPassword.getText().toString().trim();// Get password input
            viewModel.loginWithEmail(email, password); // Call login method in ViewModel
        });

        // Handle registration with email and password
        buttonRegister.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();// Get email input
            String password = editTextPassword.getText().toString().trim(); // Get password input
            viewModel.registerWithEmail(email, password); // Call registration method in ViewModel
        });

        // Handle Google sign-in
        buttonGoogleSignIn.setOnClickListener(v -> loginWithGoogle());
    }

    // Function to handle Google sign-in
    private void loginWithGoogle()
    {
        Intent signInIntent = googleSignInClient.getSignInIntent();// Get the intent for Google sign-in
        startActivityForResult(signInIntent, RC_SIGN_IN);// Start the activity expecting a result
    }

    // Function to handle the result from an activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) // Check if the result is from Google sign-in
        {
            try
            {
                // Get the Google account from the result
                GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException.class);
                viewModel.firebaseAuthWithGoogle(account); // Authenticate with Firebase using the Google account
            }
            catch (ApiException e)// Handle sign-in errors
            {
                Toast.makeText(this, "Google sign-in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();// Display error message
            }
        }
    }
    // Function to navigate to the main screen
    private void navigateToMain()
    {
        Intent intent = new Intent(LoginActivity.this, MyListsActivity.class); // Create an intent for the main screen
        startActivity(intent); // Start the main screen activity
        finish(); // Close the current activity
    }
}
