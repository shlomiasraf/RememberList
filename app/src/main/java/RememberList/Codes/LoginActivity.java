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

public class LoginActivity extends AppCompatActivity
{

    private static final int RC_SIGN_IN = 101;
    private LoginViewModel viewModel;
    private GoogleSignInClient googleSignInClient;

    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin, buttonRegister;
    private SignInButton buttonGoogleSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Initialize UI elements
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonRegister = findViewById(R.id.buttonRegister);
        buttonGoogleSignIn = findViewById(R.id.buttonGoogleSignIn);

        // Observers
        viewModel.getUserLiveData().observe(this, user -> {
            if (user != null)
            {
                navigateToMain();
            }
        });

        viewModel.getErrorLiveData().observe(this, error -> {
            if (error != null)
            {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getLoadingLiveData().observe(this, isLoading -> {
            // Implement loading indicator visibility logic here if needed
        });

        // Email/Password Login
        buttonLogin.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();
            viewModel.loginWithEmail(email, password);
        });

        // Register with Email/Password
        buttonRegister.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();
            viewModel.registerWithEmail(email, password);
        });

        // Google Sign-In
        buttonGoogleSignIn.setOnClickListener(v -> loginWithGoogle());
    }

    private void loginWithGoogle()
    {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN)
        {
            try
            {
                GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException.class);
                viewModel.firebaseAuthWithGoogle(account);
            }
            catch (ApiException e)
            {
                Toast.makeText(this, "Google sign-in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void navigateToMain()
    {
        Intent intent = new Intent(LoginActivity.this, MyListsActivity.class);
        startActivity(intent);
        finish();
    }
}
