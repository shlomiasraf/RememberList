package RememberList.Codes;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Repository
{

    // Firebase authentication instance
    private final FirebaseAuth mAuth;

    // Firebase Realtime Database reference
    private final DatabaseReference databaseReference;

    // Maps list IDs to their titles
    private final HashMap<String, String> listIdToTitleMap = new HashMap<>();

    // SharedPreferences for managing lists and values
    private final SharedPreferences sp1;
    private final SharedPreferences sp2;

    // Application context for accessing resources
    private final Context context;

    public Repository(Context context)
    {
        this.context = context;

        // Initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        // Initialize Firebase Database reference pointing to "Posts" node
        databaseReference = FirebaseDatabase.getInstance().getReference("Posts");

        // Initialize SharedPreferences for lists and values
        sp1 = context.getSharedPreferences("newLists", Context.MODE_PRIVATE);
        sp2 = context.getSharedPreferences("newValues", Context.MODE_PRIVATE);
    }

    // ---------------- Authentication Logic ----------------

    /**
     * Logs in a user using email and password.
     */
    public void loginWithEmail(String email, String password, MutableLiveData<FirebaseUser> userLiveData,
                               MutableLiveData<String> errorLiveData, MutableLiveData<Boolean> loadingLiveData)
    {
        loadingLiveData.setValue(true);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    loadingLiveData.setValue(false);
                    if (task.isSuccessful())
                    {
                        userLiveData.setValue(mAuth.getCurrentUser());
                    }
                    else
                    {
                        errorLiveData.setValue("Login failed: " + task.getException().getMessage());
                    }
                });
    }

    /**
     * Registers a user using email and password.
     */
    public void registerWithEmail(String email, String password, MutableLiveData<FirebaseUser> userLiveData,
                                  MutableLiveData<String> errorLiveData, MutableLiveData<Boolean> loadingLiveData)
    {
        loadingLiveData.setValue(true);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    loadingLiveData.setValue(false);
                    if (task.isSuccessful())
                    {
                        userLiveData.setValue(mAuth.getCurrentUser());
                    }
                    else
                    {
                        errorLiveData.setValue("Registration failed: " + task.getException().getMessage());
                    }
                });
    }

    /**
     * Logs in a user using Google Sign-In.
     */
    public void loginWithGoogle(GoogleSignInAccount account, MutableLiveData<FirebaseUser> userLiveData,
                                MutableLiveData<String> errorLiveData, MutableLiveData<Boolean> loadingLiveData)
    {
        loadingLiveData.setValue(true);
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    loadingLiveData.setValue(false);
                    if (task.isSuccessful())
                    {
                        userLiveData.setValue(mAuth.getCurrentUser());
                    }
                    else
                    {
                        errorLiveData.setValue("Google login failed: " + task.getException().getMessage());
                    }
                });
    }

    /**
     * Logs out the currently signed-in user.
     */
    public void logout()
    {
        mAuth.signOut();
    }

    /**
     * Retrieves the currently signed-in user.
     */
    public FirebaseUser getCurrentUser()
    {
        return mAuth.getCurrentUser();
    }

    // ---------------- SharedPreferences Logic ----------------

    /**
     * Checks and loads data from local SharedPreferences if needed.
     */
    public void checkAndLoadData(MutableLiveData<Boolean> loadingLiveData, MutableLiveData<String> errorLiveData) {
        try {
            loadingLiveData.setValue(true);
            if (sp1.getAll().isEmpty())
            {
                readFromFile1();
            }
            if (sp2.getAll().isEmpty())
            {
                readFromFile2();
            }
            loadingLiveData.setValue(false);
        }
        catch (Exception e)
        {
            loadingLiveData.setValue(false);
            errorLiveData.setValue("Failed to load data: " + e.getMessage());
        }
    }

    /**
     * Reads data from the "lists.txt" file and saves it to SharedPreferences.
     */
    // This method reads data from the "lists.txt" file and saves it in SharedPreferences
    public void readFromFile1()
    {
        SharedPreferences.Editor editor1 = sp1.edit();
        try {
            // Open the "lists.txt" file from assets folder
            InputStream input = context.getAssets().open("lists.txt");
            int size = input.available();
            byte[] buffer = new byte[size];
            input.read(buffer); // Read the file content into the buffer
            input.close();
            // Convert byte array to string with encoding "windows-1255"
            String st = new String(buffer, "windows-1255");
            // Split the string by commas to get individual lists
            String[] lists = st.split(",");
            // Store the lists in SharedPreferences
            for (int i = 0; i < lists.length; i++) {
                editor1.putString(String.valueOf(i), lists[i]);
            }
            editor1.commit(); // Commit changes to SharedPreferences
        } catch (Exception e)
        {
            // Handle exception if something goes wrong
            e.printStackTrace();
        }
    }

    /**
     * Reads data from the "values.txt" file and saves it to SharedPreferences.
     */
    public void readFromFile2()
    {
        SharedPreferences.Editor editor2 = sp2.edit();
        try {
            // Open the "values.txt" file from assets folder
            InputStream input = context.getAssets().open("values.txt");
            int size = input.available();
            byte[] buffer = new byte[size];
            input.read(buffer); // Read the file content into the buffer
            input.close();
            // Convert byte array to string with encoding "windows-1255"
            String st = new String(buffer, "windows-1255");
            // Split the string by "#" to separate different lists
            String[] list = st.split("#");
            // Loop through each list in the "values.txt" file
            for (int i = 0; i < list.length; i++)
            {
                String str = list[i];
                // Split each list by commas to get individual items
                String[] list2 = str.split(",");
                // Store each item in SharedPreferences
                for (int j = 0; j < list2.length; j++)
                {
                    editor2.putString(list2[0] + String.valueOf(j), list2[j]);
                }
            }
            editor2.commit(); // Commit changes to SharedPreferences
        } catch (Exception e) {
            // Handle exception if something goes wrong
            e.printStackTrace();
        }
    }

    /**
     * Retrieves a list of all saved lists.
     */
    public List<String> getLists()
    {
        List<String> lists = new ArrayList<>();
        for (int i = 0; i < sp1.getAll().size(); i++)
        {
            lists.add(sp1.getString(String.valueOf(i), ""));
        }
        return lists;
    }

    /**
     * Retrieves values for a specific key prefix.
     */
    public List<String> getValues(String keyPrefix)
    {
        List<String> values = new ArrayList<>();
        try
        {
            // Loop through the SharedPreferences entries
            for (int i = 1; sp2.contains(keyPrefix + i); i++)
            {
                String value = sp2.getString(keyPrefix + i, null);
                if (value != null)
                {
                    values.add(value); // Add the retrieved value to the list
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // Log any exceptions for debugging
        }
        return values; // Return the list of values
    }


    /**
     * Adds a new list to SharedPreferences.
     */
    public void addList(String listName, MutableLiveData<String> errorLiveData)
    {
        try
        {
            SharedPreferences.Editor editor = sp1.edit();
            editor.putString(String.valueOf(sp1.getAll().size()), listName);
            editor.apply();
        }
        catch (Exception e)
        {
            errorLiveData.setValue("Failed to add list: " + e.getMessage());
        }
    }
    /**
     * Adds a new value to SharedPreferences under a specific keyPrefix.
     * @param keyPrefix The prefix under which the value should be stored.
     * @param value The value to be added.
     * @param errorLiveData LiveData to handle and display errors if any occur.
     */
    public void addValue(String keyPrefix, String value, MutableLiveData<String> errorLiveData)
    {
        try
        {
            SharedPreferences.Editor editor = sp2.edit();
            int size = 1;
            // Find the next available index for the keyPrefix
            while (sp2.contains(keyPrefix + size))
            {
                size++;
            }

            // Add the new value with the calculated index
            editor.putString(keyPrefix + size, value);
            editor.apply();
        } catch (Exception e)
        {
            errorLiveData.setValue("Failed to add value: " + e.getMessage());
        }
    }
    /**
     * Deletes a list from SharedPreferences.
     */
    public void deleteList(String listName, MutableLiveData<String> errorLiveData) {
        try {
            SharedPreferences.Editor editor = sp1.edit();
            List<String> lists = getLists();
            lists.remove(listName);
            editor.clear();
            for (int i = 0; i < lists.size(); i++)
            {
                editor.putString(String.valueOf(i), lists.get(i));
            }
            editor.apply();
        }
        catch (Exception e)
        {
            errorLiveData.setValue("Failed to delete list: " + e.getMessage());
        }
    }
    /**
     * Deletes a value associated with a specific key and adjusts subsequent keys.
     *
     * @param keyPrefix      The prefix of the keys to delete.
     * @param index          The index of the key to delete.
     * @param errorLiveData  MutableLiveData to capture errors if they occur.
     */
    public void deleteValue(String keyPrefix, int index, MutableLiveData<String> errorLiveData)
    {
        try
        {
            SharedPreferences.Editor editor = sp2.edit();
            // Adjust the keys to maintain a continuous sequence
            String newKey = keyPrefix + index;
            for (int i = index + 2; i < sp2.getAll().size(); i++)
            {
                // Shift each value one step down in the sequence
                String currentKey = keyPrefix + i;        // Current key
                newKey = keyPrefix + (i - 1);      // New shifted key
                String currentValue = sp2.getString(currentKey, null);
                if (currentValue != null)
                {
                    editor.putString(newKey, currentValue); // Assign current value to the new key
                }
                else
                {
                    break;
                }
            }

            // Remove the last key in the sequence
            editor.remove(newKey);
            // Commit the changes to SharedPreferences
            editor.apply();
        }
        catch (Exception e)
        {
            // Capture and report any errors during the operation
            errorLiveData.setValue("Failed to delete values: " + e.getMessage());
        }
    }


    // ---------------- Firebase Logic ----------------

    /**
     * Fetches a list of items from Firebase Realtime Database.
     */
    public void fetchLists(MutableLiveData<List<String>> listsLiveData, MutableLiveData<String> errorLiveData, MutableLiveData<Boolean> loadingLiveData) {
        loadingLiveData.setValue(true);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                List<String> lists = new ArrayList<>();
                listIdToTitleMap.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    String listId = snapshot.getKey();
                    String title = snapshot.child("title").getValue(String.class);

                    if (listId != null && title != null)
                    {
                        listIdToTitleMap.put(listId, title);
                        lists.add(title);
                    }
                }

                listsLiveData.setValue(lists);
                loadingLiveData.setValue(false);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue("Failed to fetch lists: " + databaseError.getMessage());
            }
        });
    }

    /**
     * Fetches the content of a specific list from Firebase.
     */
    public void fetchContent(String listId, MutableLiveData<List<String>> contentLiveData, MutableLiveData<String> errorLiveData, MutableLiveData<Boolean> loadingLiveData) {
        loadingLiveData.setValue(true);

        DatabaseReference listReference = databaseReference.child(listId);

        listReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                List<String> contentList = new ArrayList<>();

                String body = dataSnapshot.child("body").getValue(String.class);
                if (body != null)
                {
                    String[] items = body.split(",");
                    contentList.addAll(Arrays.asList(items));
                }

                contentLiveData.setValue(contentList);
                loadingLiveData.setValue(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
                loadingLiveData.setValue(false);
                errorLiveData.setValue("Failed to fetch content: " + databaseError.getMessage());
            }
        });
    }

    /**
     * Retrieves the map of list IDs to titles.
     */
    public HashMap<String, String> getListIdToTitleMap()
    {
        return listIdToTitleMap;
    }
}
