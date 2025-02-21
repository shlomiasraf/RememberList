package RememberList.Codes;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import com.google.firebase.auth.FirebaseUser;

// The LoadingActivity class handles user authentication checks and initial loading logic
public class LoadingActivity extends AppCompatActivity
{
	// ViewModel instance for managing data and state
	private final LoadingViewModel viewModel = new LoadingViewModel();

	// Called when the activity is first created
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_loading); // Set the layout for the activity

		// Initialize the ViewModel and provide application context for SharedPreferences or other dependencies
		viewModel.init(this);

		// Observe changes in the user authentication state
		viewModel.getUserLiveData().observe(this, new Observer<FirebaseUser>()
		{
			@Override
			public void onChanged(FirebaseUser user)
			{
				if (user != null) // If a user is logged in
				{
					viewModel.checkIfUserIsAdmin();
					viewModel.getIsAdminLiveData().observe(LoadingActivity.this, isAdmin -> {
						if (isAdmin) {
							Intent intent = new Intent(LoadingActivity.this, MyListsActivity.class);
							intent.putExtra("IS_ADMIN", true); // Sending boolean extra
							startActivity(intent);// Start the main activity
							finish();  // Close the current activity to prevent going back
						} else {
							navigateToMainScreen(); // Navigate to the main activity
						}
					});
				}
				else// If no user is logged in
				{
					navigateToLoginScreen();  // Navigate to the login activity
				}
			}
		});
	}

	// Navigate to the main screen (MyListsActivity) when the user is authenticated
	private void navigateToMainScreen()
	{
		Intent intent = new Intent(LoadingActivity.this, MyListsActivity.class);
		startActivity(intent);// Start the main activity
		finish();  // Close the current activity to prevent going back
	}

	// Navigate to the login screen (LoginActivity) when the user is not authenticated
	private void navigateToLoginScreen()
	{
		Intent intent = new Intent(LoadingActivity.this, LoginActivity.class);
		startActivity(intent); // Start the login activity
		finish();  // Close the current activity to prevent going back
	}
}
