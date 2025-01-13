package RememberList.Codes;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import com.google.firebase.auth.FirebaseUser;

public class LoadingActivity extends AppCompatActivity {
	private final LoadingViewModel viewModel = new LoadingViewModel();

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Connect to the ViewModel
		viewModel.init(this); // Initialize ViewModel with context for SharedPreferences

		// Observe changes in user state
		viewModel.getUserState().observe(this, new Observer<FirebaseUser>()
		{
			@Override
			public void onChanged(FirebaseUser user)
			{
				if (user != null)
				{
					navigateToMainScreen(); // If user is authenticated, navigate to the main screen
				}
				else
				{
					navigateToLoginScreen(); // If user is not authenticated, navigate to login screen
				}
			}
		});

		// Check and load data if necessary
		viewModel.checkAndLoadData();
	}

	private void navigateToMainScreen()
	{
		Intent intent = new Intent(LoadingActivity.this, MyListsActivity.class);
		startActivity(intent);
		finish(); // Close the current activity
	}

	private void navigateToLoginScreen()
	{
		Intent intent = new Intent(LoadingActivity.this, LoginActivity.class);
		startActivity(intent);
		finish(); // Close the current activity
	}
}
