package RememberList.Codes;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import org.junit.Before;

import static org.hamcrest.CoreMatchers.allOf;

import android.content.Intent;


@RunWith(AndroidJUnit4.class)
public class UITests
{

    @Rule
    public ActivityScenarioRule<MyListsActivity> activityRule =
            new ActivityScenarioRule<>(MyListsActivity.class);

    @Before
    public void setUp() {
        Intents.init();
    }

    @Test
    public void testSignOut() {
        // Find and click the sign-out button
        onView(withId(R.id.signOutButton)) // Replace with actual ID of sign-out button
                .perform(click());

        // Verify that the LoginActivity is launched after sign-out
        Intents.intended(allOf(
                IntentMatchers.hasComponent(LoginActivity.class.getName()),
                IntentMatchers.hasFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
        ));
    }
    @Test
    public void testNavigateToSharedLists() {
        // Click the Shared Lists button
        onView(withId(R.id.sharelists))
                .perform(click());

        // Verify that SharedListsActivity is launched
        Intents.intended(allOf(
                IntentMatchers.hasComponent(SharedListsActivity.class.getName()),
                IntentMatchers.hasFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
        ));
    }

    @After
    public void tearDown() {
        // Release Intents after each test
        Intents.release();
    }
}