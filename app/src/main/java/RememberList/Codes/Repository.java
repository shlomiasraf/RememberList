package RememberList.Codes;

import android.content.Context;
import androidx.lifecycle.MutableLiveData;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.*;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Repository {

    // Firebase Authentication instance for managing auth operations
    private final FirebaseAuth mAuth;
    // Firebase Realtime Database reference (pointing to the "Posts" node)
    private final DatabaseReference databaseReference;
    // Map to store list IDs (or keys) to titles (for filtering, etc.)

    private final Context context;
    public Repository(Context context)
    {
        this.context = context;
        // Initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();
        // Initialize Firebase Database reference pointing to the "Posts" node
        databaseReference = FirebaseDatabase.getInstance().getReference("Lists");
    }

    // ---------------- Authentication Logic ----------------

    /**
     * Logs in a user using email and password.
     *
     * @param email The user's email.
     * @param password The user's password.
     * @param userLiveData LiveData to post the authenticated FirebaseUser.
     * @param errorLiveData LiveData to post error messages.
     * @param loadingLiveData LiveData to indicate loading status.
     */
    public void loginWithEmail(String email, String password,
                               MutableLiveData<FirebaseUser> userLiveData,
                               MutableLiveData<String> errorLiveData,
                               MutableLiveData<Boolean> loadingLiveData) {
        loadingLiveData.setValue(true);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    loadingLiveData.setValue(false);
                    if (task.isSuccessful()) {
                        userLiveData.setValue(mAuth.getCurrentUser());
                    } else {
                        errorLiveData.setValue("Login failed: " + task.getException().getMessage());
                    }
                });
    }

    /**
     * Registers a user using email and password.
     *
     * @param email The user's email.
     * @param password The user's password.
     * @param userLiveData LiveData to post the newly registered FirebaseUser.
     * @param errorLiveData LiveData to post error messages.
     * @param loadingLiveData LiveData to indicate loading status.
     */
    public void registerWithEmail(String email, String password,
                                  MutableLiveData<FirebaseUser> userLiveData,
                                  MutableLiveData<String> errorLiveData,
                                  MutableLiveData<Boolean> loadingLiveData) {
        loadingLiveData.setValue(true);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    loadingLiveData.setValue(false);
                    if (task.isSuccessful()) {
                        userLiveData.setValue(mAuth.getCurrentUser());
                    } else {
                        errorLiveData.setValue("Registration failed: " + task.getException().getMessage());
                    }
                });
    }

    /**
     * Logs in a user using Google Sign-In.
     *
     * @param account The GoogleSignInAccount obtained from Google Sign-In.
     * @param userLiveData LiveData to post the authenticated FirebaseUser.
     * @param errorLiveData LiveData to post error messages.
     * @param loadingLiveData LiveData to indicate loading status.
     */
    public void loginWithGoogle(GoogleSignInAccount account,
                                MutableLiveData<FirebaseUser> userLiveData,
                                MutableLiveData<String> errorLiveData,
                                MutableLiveData<Boolean> loadingLiveData) {
        loadingLiveData.setValue(true);
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    loadingLiveData.setValue(false);
                    if (task.isSuccessful()) {
                        userLiveData.setValue(mAuth.getCurrentUser());
                    } else {
                        errorLiveData.setValue("Google login failed: " + task.getException().getMessage());
                    }
                });
    }

    /**
     * Logs out the currently signed-in user.
     */
    public void logout() {
        mAuth.signOut();
    }

    /**
     * Retrieves the currently signed-in user.
     *
     * @return The current FirebaseUser.
     */
    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

// ---------------- Check and Load Data ----------------

    /**
     * Checks if the data exists in Firebase under "UsersDatabase/{userEmail}".
     * If not, it loads the data from asset files and uploads them.
     */
    public void checkAndLoadData(final MutableLiveData<Boolean> loadingLiveData, final MutableLiveData<String> errorLiveData) {
        loadingLiveData.setValue(true);
        // Replace '.' in email with '_' to make it a valid Firebase node
        if(getCurrentUser() != null)
        {
            String userEmail = mAuth.getCurrentUser().getEmail();
            String emailNode = userEmail.replace(".", "_");
            // Check if the user's node exists in "UsersDatabase"
            databaseReference.child("UsersDatabase").child(emailNode).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (!snapshot.exists()) {
                        // If user's node does not exist, load data from asset files
                        readFromFile1();
                        readFromFile2();
                        InitialCategories();
                    }
                    loadingLiveData.setValue(false); // Indicate loading is done
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    loadingLiveData.setValue(false);
                    errorLiveData.setValue("Failed to load user data: " + error.getMessage());
                }
            });
        }
    }

    // ---------------- Reading Data from Asset Files and Uploading to Firebase ----------------

    /**
     * Reads the "lists.txt" file from the assets folder, splits the content by commas,
     * and uploads the lists to the "UsersDatabase/{userEmail}" node in Firebase.
     */
    public void readFromFile1()
    {
        try {
            InputStream input = context.getAssets().open("lists.txt");
            int size = input.available();
            byte[] buffer = new byte[size];
            input.read(buffer); // Read file content into buffer
            input.close();
            // Convert byte array to String using "windows-1255" encoding (for Hebrew)
            String st = new String(buffer, "windows-1255");
            // Split the string by commas to get individual list names
            String[] lists = st.split(",");
            // Replace '.' in email with '_' to make it a valid Firebase node
            for (int i = 0; i < lists.length; i++)
            {
                // Save each list under its index as the key
                getListsRef().child(String.valueOf(i)).setValue(lists[i]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads the "values.txt" file from the assets folder, splits the content by "#" to separate different lists,
     * then splits each list by commas to get individual values, and uploads the values
     * to the "UsersDatabase/{userEmail}/values" node in Firebase.
     */
    public void readFromFile2()
    {
        try {
            InputStream input = context.getAssets().open("values.txt");
            int size = input.available();
            byte[] buffer = new byte[size];
            input.read(buffer); // Read file content into buffer
            input.close();
            // Convert byte array to String using "windows-1255" encoding
            String st = new String(buffer, "windows-1255");
            // Split the file content by "#" to separate different lists
            String[] list = st.split("#");
            for (int i = 0; i < list.length; i++)
            {
                String str = list[i];
                // Split each list by commas to get individual values
                String[] list2 = str.split(",");
                // Use the first element as the key (list name)
                DatabaseReference listValuesRef = getValuesRef().child(list2[0]+String.valueOf(i));
                for (int j = 1; j < list2.length; j++)
                {
                    Map<String, Object> valueWithBoolean = new HashMap<>();
                    valueWithBoolean.put("value", list2[j]);
                    valueWithBoolean.put("isChecked", false);
                    listValuesRef.child(String.valueOf(j-1)).setValue(valueWithBoolean);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates initial categories for each user who connects.
     */
    public void InitialCategories()
    {
        getCategoriesRef().child(String.valueOf(0)).setValue("כל הקטגוריות");
        getCategoriesRef().child(String.valueOf(1)).setValue("טיולים");
        getCategoriesRef().child(String.valueOf(2)).setValue("קניות");
        getCategoriesRef().child(String.valueOf(3)).setValue("אירועים מיוחדים");
    }
    public void getCategories(final MutableLiveData<List<String>> CategoriesLiveData,
                              final MutableLiveData<Boolean> loadingLiveData,
                              final MutableLiveData<String> errorLiveData) {
        loadingLiveData.setValue(true);
        getCategoriesRef().addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<String> Categories = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    String CategoryStr = child.getValue(String.class);
                    if (CategoryStr != null)
                    {
                        Categories.add(CategoryStr);
                    }
                }
                CategoriesLiveData.setValue(Categories);
                loadingLiveData.setValue(false);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                loadingLiveData.setValue(false);
                errorLiveData.setValue("Failed to fetch content: " + databaseError.getMessage());
            }
        });
    }

    /**
     * Retrieves all saved lists from Firebase under the "lists" node
     * and posts them to the provided LiveData.
     */
    public void getUserLists(final MutableLiveData<List<String>> listsLiveData,
                         final MutableLiveData<Boolean> loadingLiveData,
                         final MutableLiveData<String> errorLiveData)
    {
        loadingLiveData.setValue(true);
        DatabaseReference getListsRef = getListsRef();
        getListsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot)
            {
                List<String> lists = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren())
                {
                    String listStr = child.getValue(String.class);
                    if (listStr != null)
                    {
                        lists.add(listStr);
                    }
                }
                listsLiveData.setValue(lists);
                loadingLiveData.setValue(false);
            }
            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                loadingLiveData.setValue(false);
                errorLiveData.setValue("Failed to fetch content: " + databaseError.getMessage());
            }
        });
    }
    public void getSharedLists(final MutableLiveData<List<ListSaveObject>> listsLiveData, final MutableLiveData<Boolean> loadingLiveData, final MutableLiveData<String> errorLiveData)
    {
        loadingLiveData.setValue(true);
        DatabaseReference getListsRef = getSharedListsRef();
        getListsRef.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot snapshot)
            {
                List<ListSaveObject> lists = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren())
                {
                    String listStr = child.child("Name").getValue(String.class);
                    int listSaveCount = child.child("listSaveCount").getValue(Integer.class);
                    if (listStr != null)
                    {
                        lists.add(new ListSaveObject(listSaveCount, listStr));
                    }
                }
                listsLiveData.setValue(lists);
                loadingLiveData.setValue(false);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                loadingLiveData.setValue(false);
                errorLiveData.setValue("Failed to fetch content: " + databaseError.getMessage());
            }
        });
    }

    /**
     * Retrieves the values for a specific list from the "values/{keyPrefix}" node in Firebase
     * and updates the provided LiveData.
     *
     * @param keyPrefix The key for the list (usually the list name).
     */
    public void getValues(String valuesKind, final String keyPrefix,
                          final MutableLiveData<List<Product>> valuesLiveData,
                          final MutableLiveData<Boolean> loadingLiveData,
                          final MutableLiveData<String> errorLiveData) {
        loadingLiveData.setValue(true);
        DatabaseReference getValuesRef;
        if(valuesKind.equals("SharedValues"))
        {
            getValuesRef = getSharedValuesRef();
        }
        else
        {
            getValuesRef = getValuesRef();
        }

        getValuesRef.child(keyPrefix).addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot snapshot)
            {
                List<Product> values = new ArrayList<>();
                // Iterate over each child in the node
                for (DataSnapshot child : snapshot.getChildren())
                {
                    String value;
                    Boolean isChecked = false;
                    if(valuesKind.equals("UserValues"))
                    {
                        isChecked = child.child("isChecked").getValue(Boolean.class);
                        value = child.child("value").getValue(String.class);
                    }
                    else
                    {
                        value = child.getValue(String.class);
                    }
                    if (value != null && isChecked != null)
                    {
                        values.add(new Product(value, isChecked));
                    }
                }
                // Update the LiveData with the fetched values
                valuesLiveData.setValue(values);
                loadingLiveData.setValue(false);
            }

            @Override
            public void onCancelled(DatabaseError error)
            {
                // Update error LiveData and stop loading state
                loadingLiveData.setValue(false);
                errorLiveData.setValue("Failed to fetch values: " + error.getMessage());
            }
        });
    }
// ---------------- Firebase "Add" Methods ----------------

    /**
     * Adds a new list to Firebase under the "lists" node.
     * We use the list name as the key (assuming list names are unique).
     *
     * @param listName The name of the list to add.
     * @param errorLiveData LiveData to capture error messages.
     */
    public void addList(String listName, MutableLiveData<Boolean> loadingLiveData, MutableLiveData<String> errorLiveData) {
        try
        {
            // Set loading state to true before starting the operation
            loadingLiveData.setValue(true);
            DatabaseReference getListsRef = getListsRef();
            // Determine the next available index by reading the current children count
            getListsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    long count = snapshot.getChildrenCount();
                    // Next index = count + 1 (assuming keys start at 1)
                    Map<String, Object> valueWithBoolean = new HashMap<>();
                    valueWithBoolean.put("value", "");
                    valueWithBoolean.put("isChecked", false);
                    getValuesRef().child(listName + String.valueOf(count)).child(String.valueOf(0)).setValue(valueWithBoolean);
                    getListsRef.child(String.valueOf(count)).setValue(listName)
                            .addOnSuccessListener(unused -> {
                                // Set loading state to false after the operation is successful
                                loadingLiveData.setValue(false);
                            })
                            .addOnFailureListener(e -> {
                                // Set an error message and loading state to false if the operation fails
                                errorLiveData.setValue("Failed to add list: " + e.getMessage());
                                loadingLiveData.setValue(false);
                            });
                }

                @Override
                public void onCancelled(DatabaseError error)
                {
                    // Set an error message and loading state to false if the listener is cancelled
                    errorLiveData.setValue("Failed to add value: " + error.getMessage());
                    loadingLiveData.setValue(false);
                }
            });
        }
        catch (Exception e)
        {
            // Set an error message and loading state to false if an exception occurs
            errorLiveData.setValue("Failed to add value: " + e.getMessage());
            loadingLiveData.setValue(false);
        }
    }
    public void addSharedList(String listName,ArrayList<String> categories, ArrayList<String> valuesList, MutableLiveData<Boolean> loadingLiveData, MutableLiveData<String> errorLiveData)
    {
        try
        {
            // Set loading state to true before starting the operation
            loadingLiveData.setValue(true);
            DatabaseReference getListsRef = getSharedListsRef();
            DatabaseReference getValuesRef = getSharedValuesRef();
            // Determine the next available index by reading the current children count
            getListsRef.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot snapshot)
                {
                    long count = snapshot.getChildrenCount();
                    // Next index = count + 1 (assuming keys start at 1)
                    Map<String, Object> valueWithBoolean = new HashMap<>();
                    valueWithBoolean.put("Categories",categories);
                    String userEmail = mAuth.getCurrentUser().getEmail();
                    String emailNode = userEmail.replace(".", "_");
                    valueWithBoolean.put("CreatedBy", emailNode);
                    valueWithBoolean.put("Name",listName);
                    valueWithBoolean.put("listSaveCount",0);
                    getListsRef.child(String.valueOf(count)).setValue(valueWithBoolean)
                            .addOnSuccessListener(unused -> {
                                // Set loading state to false after the operation is successful
                                loadingLiveData.setValue(false);
                                int i = 0;
                                for (String value : valuesList)
                                {
                                    getValuesRef.child(listName + String.valueOf(count)).child(String.valueOf(i)).setValue(value);
                                    i++;
                                }

                            })
                            .addOnFailureListener(e -> {
                                // Set an error message and loading state to false if the operation fails
                                errorLiveData.setValue("Failed to add list: " + e.getMessage());
                                loadingLiveData.setValue(false);
                            });
                }

                @Override
                public void onCancelled(DatabaseError error)
                {
                    // Set an error message and loading state to false if the listener is cancelled
                    errorLiveData.setValue("Failed to add value: " + error.getMessage());
                    loadingLiveData.setValue(false);
                }
            });
        }
        catch (Exception e)
        {
            // Set an error message and loading state to false if an exception occurs
            errorLiveData.setValue("Failed to add value: " + e.getMessage());
            loadingLiveData.setValue(false);
        }
    }

    /**
     * Adds a new value under a specific list in Firebase.
     * Values are stored under "values/{listName}" with keys formatted as listName1, listName2, etc.
     *
     * @param keyPrefix The list name (used as the key prefix).
     * @param valuesList The values to add.
     * @param errorLiveData LiveData to capture error messages.
     */
    public void addValues(String keyPrefix, ArrayList<String> valuesList, MutableLiveData<Boolean> loadingLiveData, MutableLiveData<String> errorLiveData)
    {
        try
        {
            // Set loading state to true before starting the operation
            loadingLiveData.setValue(true);
            final DatabaseReference listValuesRef = getValuesRef().child(keyPrefix);
            // Determine the next available index by reading the current children count
            listValuesRef.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot snapshot)
                {
                    long count = snapshot.getChildrenCount();
                    // Next index = count + 1 (assuming keys start at 1)
                    if(count == 1)
                    {
                        DataSnapshot child = snapshot.getChildren().iterator().next();
                        if (child.getValue() != null && child.child("value").getValue().equals(""))
                        {
                            count = 0;
                        }
                    }
                    int i = 0;
                    for(String value : valuesList)
                    {
                        Map<String, Object> valueWithBoolean = new HashMap<>();
                        valueWithBoolean.put("value", value);
                        valueWithBoolean.put("isChecked", false);
                        listValuesRef.child(String.valueOf(count+i)).setValue(valueWithBoolean);
                        i++;
                    }
                    // Set loading state to false after the operation is successful
                    loadingLiveData.setValue(false);
                }
                @Override
                public void onCancelled(DatabaseError error)
                {
                    errorLiveData.setValue("Failed to add value: " + error.getMessage());
                    loadingLiveData.setValue(false);
                }
            });
        }
        catch (Exception e) {
            errorLiveData.setValue("Failed to add value: " + e.getMessage());
            loadingLiveData.setValue(false);
        }
    }
    /**
     * Adds a new category to Firebase under the "categories" node.
     * The category name is used as the value, and keys are generated incrementally.
     *
     * @param categoryName  The name of the category to add.
     * @param loadingLiveData LiveData to indicate the loading state.
     * @param errorLiveData LiveData to capture error messages.
     */
    public void addCategory(String categoryName, MutableLiveData<Boolean> loadingLiveData, MutableLiveData<String> errorLiveData)
    {
        try {
            // Set loading state to true before starting the operation
            loadingLiveData.setValue(true);
            DatabaseReference getCategoriesRef = getCategoriesRef();
            // Determine the next available index by reading the current children count
            getCategoriesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    long count = snapshot.getChildrenCount(); // Get the number of existing categories

                    // Add the new category at the next available index
                    getCategoriesRef.child(String.valueOf(count)).setValue(categoryName)
                            .addOnSuccessListener(unused -> {
                                // Set loading state to false after the operation is successful
                                loadingLiveData.setValue(false);
                            })
                            .addOnFailureListener(e -> {
                                // Set an error message and loading state to false if the operation fails
                                errorLiveData.setValue("Failed to add category: " + e.getMessage());
                                loadingLiveData.setValue(false);
                            });
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Set an error message and loading state to false if the listener is cancelled
                    errorLiveData.setValue("Failed to add category: " + error.getMessage());
                    loadingLiveData.setValue(false);
                }
            });
        } catch (Exception e) {
            // Set an error message and loading state to false if an exception occurs
            errorLiveData.setValue("Failed to add category: " + e.getMessage());
            loadingLiveData.setValue(false);
        }
    }

    // ---------------- Firebase "Delete" Methods ----------------

    /**
     * Deletes a list and its associated values from Firebase.
     *
     * @param listName The name of the list to delete.
     * @param errorLiveData LiveData to capture error messages.
     */
    public void deleteList(String listName, String key, MutableLiveData<Boolean> loadingLiveData, MutableLiveData<String> errorLiveData) {
        try {
            // Set loading state to true before starting the operation
            loadingLiveData.setValue(true);

            // Attempt to remove values from the database
            getListsRef().child(key).removeValue()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            getValuesRef().child(listName + key).removeValue()
                                    .addOnCompleteListener(task2 -> {
                                        if (task2.isSuccessful()) {
                                            // Success, set loading state to false and clear any errors
                                            loadingLiveData.setValue(false);
                                        } else {
                                            // If removing from getValuesRef() failed, set an error message
                                            loadingLiveData.setValue(false);
                                            errorLiveData.setValue("Failed to delete list from values reference.");
                                        }
                                    });
                        } else {
                            // If removing from getListsRef() failed, set an error message
                            loadingLiveData.setValue(false);
                            errorLiveData.setValue("Failed to delete list from lists reference.");
                        }
                    });

        } catch (Exception e) {
            // Handle any unexpected exceptions
            loadingLiveData.setValue(false);
            errorLiveData.setValue("Failed to delete list: " + e.getMessage());
        }
    }

    /**
     * Deletes a value from a specific list in Firebase and shifts subsequent keys to maintain a continuous sequence.
     *
     * @param keyPrefix The list name used as the key prefix.
     * @param indexes The 1-based indexes of the values to delete.
     * @param errorLiveData LiveData to capture error messages.
     */
    public void deleteValues(String keyPrefix, int[] indexes, int listSize, MutableLiveData<Boolean> loadingLiveData, MutableLiveData<String> errorLiveData)
    {
        // Set loading state to true before starting the operation
        loadingLiveData.setValue(true);
        DatabaseReference listValuesRef = getValuesRef().child(keyPrefix);
        Boolean listEmpty = false;
        // Remove the values at the given indexes
        for (int i = 0; i < indexes.length; i++)
        {
            if (listSize == indexes.length && i == indexes.length-1)
            {
                Map<String, Object> valueWithBoolean = new HashMap<>();
                valueWithBoolean.put("value", "");
                valueWithBoolean.put("isChecked", false);
                listValuesRef.child(String.valueOf(indexes[i])).setValue(valueWithBoolean);
                listEmpty = true;
                loadingLiveData.setValue(false);
            }
            else
            {
                listValuesRef.child(String.valueOf(indexes[i])).removeValue();
            }
        }
        if(!listEmpty)
        {
            // Add a listener to handle re-indexing after deletion
            listValuesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        int counter = 0; // Keeps track of how many gaps exist (null values)

                        // Iterate through the remaining items
                        for (int i = indexes[0]; i < snapshot.getChildrenCount() + indexes.length; i++) {
                            String value = snapshot.child(String.valueOf(i)).child("value").getValue(String.class);
                            if (value == null) {
                                // Increment the counter for null (deleted) values
                                counter++;
                            } else {
                                Map<String, Object> valueWithBoolean = new HashMap<>();
                                valueWithBoolean.put("value", value);
                                valueWithBoolean.put("isChecked", snapshot.child(String.valueOf(i)).child("isChecked").getValue(Boolean.class));
                                // Move the current value to fill the gap
                                listValuesRef.child(String.valueOf(i - counter)).setValue(valueWithBoolean);
                                // Remove the old key to avoid duplicates
                                listValuesRef.child(String.valueOf(i)).removeValue();
                            }
                        }

                        // Notify completion of loading
                        loadingLiveData.setValue(false);
                    } else {
                        // If the snapshot doesn't exist, notify error
                        errorLiveData.setValue("No data found in the list.");
                        loadingLiveData.setValue(false);
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Handle database access errors
                    errorLiveData.setValue("Failed to access database: " + error.getMessage());
                }
            });
        }
    }
    /**
     * Deletes a category and its associated data from Firebase.
     *
     * @param categoryName   The name of the category to delete.
     * @param loadingLiveData LiveData to indicate the loading state.
     * @param errorLiveData   LiveData to capture error messages.
     */
    public void deleteCategory(String categoryName, MutableLiveData<Boolean> loadingLiveData, MutableLiveData<String> errorLiveData) {
        try {
            // Set loading state to true before starting the operation
            loadingLiveData.setValue(true);
            // Access the "categories" node and find the category by its name
            getCategoriesRef().addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    for (DataSnapshot child : snapshot.getChildren()) {String value = child.getValue(String.class);
                        if (value != null && value.equals(categoryName)) {
                            // Remove the category from the "categories" node

                            child.getRef().removeValue().addOnSuccessListener(aVoid -> {
                                        loadingLiveData.setValue(false);
                                    })
                                    .addOnFailureListener(e -> {
                                        loadingLiveData.setValue(false);
                                        errorLiveData.setValue("Failed to delete category: " + e.getMessage());
                                    });
                            break;
                        }
                    }
                    loadingLiveData.setValue(false);
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Handle cancellation or access errors
                    loadingLiveData.setValue(false);
                    errorLiveData.setValue("Failed to access categories: " + error.getMessage());
                }
            });
        }
        catch (Exception e) {
            // Set an error message and loading state to false if an exception occurs
            errorLiveData.setValue("Failed to delete category: " + e.getMessage());
            loadingLiveData.setValue(false);
        }
    }
    public void getFilteredLists(List<String> filteredCategories,
                                 MutableLiveData<Boolean> loadingLiveData,
                                 MutableLiveData<List<ListSaveObject>> listsLiveData,
                                 MutableLiveData<String> errorLiveData)
    {
        loadingLiveData.setValue(true);

        getSharedListsRef().addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot snapshot)
            {
                List<ListSaveObject> lists = new ArrayList<>();

                if (snapshot.exists())
                {
                    for (DataSnapshot child : snapshot.getChildren())
                    {
                        GenericTypeIndicator<List<String>> t = new GenericTypeIndicator<List<String>>() {
                        };
                        List<String> categoriesOfList = child.child("Categories").getValue(t);
                        if (categoriesOfList == null)
                        {
                            categoriesOfList = new ArrayList<>();
                        }
                        for (String category : filteredCategories)
                        {
                            if (categoriesOfList.contains(category) || category.equals("כל הקטגוריות"))
                            {
                                String listName = child.child("Name").getValue(String.class);
                                int listSaveCount = child.child("listSaveCount").getValue(Integer.class);
                                if (listName != null)
                                {
                                    lists.add(new ListSaveObject(listSaveCount,listName));
                                }
                                break;
                            }
                        }
                    }
                }
                listsLiveData.setValue(lists);
                loadingLiveData.setValue(false);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                errorLiveData.setValue("Failed to fetch lists categories: " + error.getMessage());
                loadingLiveData.setValue(false);
            }
        });
    }
    public void updateListSaves(String key, MutableLiveData<Boolean> loadingLiveData, MutableLiveData<String> errorLiveData) {
        try {
            // Set loading state to true before starting the operation
            loadingLiveData.setValue(true);

            // Get reference to "listSaveCount"
            DatabaseReference savesReference = getSharedListsRef().child(key).child("listSaveCount");

            // Read the current value asynchronously
            savesReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    Integer listSaveCount = snapshot.getValue(Integer.class);

                    if (listSaveCount == null) {
                        listSaveCount = 0; // Default to 0 if no value exists
                    }

                    // Increment the count and update the database
                    savesReference.setValue(listSaveCount + 1)
                            .addOnSuccessListener(aVoid -> {
                                // Successfully updated the value
                                loadingLiveData.setValue(false);
                            })
                            .addOnFailureListener(e -> {
                                // Handle failure
                                errorLiveData.setValue("Failed to update value: " + e.getMessage());
                                loadingLiveData.setValue(false);
                            });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle read error
                    errorLiveData.setValue("Database read failed: " + databaseError.getMessage());
                    loadingLiveData.setValue(false);
                }
            });

        } catch (Exception e) {
            errorLiveData.setValue("Unexpected error: " + e.getMessage());
            loadingLiveData.setValue(false);
        }
    }


    public void ChangeProductBox(Product product,int position, String keyPrefix)
    {
        getValuesRef().child(keyPrefix).child(String.valueOf(position)).child("isChecked").setValue(product.isChecked());
    }

    public DatabaseReference getListsRef()
    {
        String userEmail = mAuth.getCurrentUser().getEmail();
        String emailNode = userEmail.replace(".", "_");
        DatabaseReference listsRef = databaseReference.child("UsersDatabase").child(emailNode).child("lists");
        return listsRef;
    }
    public DatabaseReference getValuesRef()
    {
        String userEmail = mAuth.getCurrentUser().getEmail();
        String emailNode = userEmail.replace(".", "_");
        DatabaseReference valuesRef = databaseReference.child("UsersDatabase").child(emailNode).child("values");
        return valuesRef;
    }
    public DatabaseReference getCategoriesRef()
    {
        String userEmail = mAuth.getCurrentUser().getEmail();
        String emailNode = userEmail.replace(".", "_");
        DatabaseReference CategoriesRef = databaseReference.child("UsersDatabase").child(emailNode).child("Categories");
        return CategoriesRef;
    }
    public DatabaseReference getSharedListsRef()
    {
        DatabaseReference SharedListsRef = databaseReference.child("SharedLists").child("lists");
        return SharedListsRef;
    }
    public DatabaseReference getSharedValuesRef()
    {
        DatabaseReference SharedValuesRef = databaseReference.child("SharedLists").child("values");
        return SharedValuesRef;
    }
}
