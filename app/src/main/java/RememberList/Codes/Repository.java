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
         // Set loading indicator to true before starting the login process.
        loadingLiveData.setValue(true);
        // Attempt to sign in using Firebase Authentication with the provided email and password.
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                     // After the task completes, disable the loading indicator.
                    loadingLiveData.setValue(false);
                    // Check if the sign-in was successful.
                    if (task.isSuccessful()) {
                        // If successful, update userLiveData with the current authenticated user.
                        userLiveData.setValue(mAuth.getCurrentUser());
                    } else {
                        // If sign-in fails, update errorLiveData with the error message.
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
        // Set loading indicator to true before starting the registration process.
        loadingLiveData.setValue(true);
        // Attempt to create a new user with the provided email and password using Firebase Authentication.
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                     // Disable the loading indicator once the registration task is complete.
                    loadingLiveData.setValue(false);
                    // Check if the registration was successful.
                    if (task.isSuccessful()) {
                         // If registration is successful, update userLiveData with the new FirebaseUser.
                        userLiveData.setValue(mAuth.getCurrentUser());
                    } else {
                        // If registration fails, update errorLiveData with the error message.
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
         // Set loading indicator to true to show that the login process has started.
        loadingLiveData.setValue(true);
        // Get the credentials from the GoogleSignInAccount's ID token.
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        // Sign in with the obtained credential using Firebase Authentication.
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    // Once the sign-in task is complete, disable the loading indicator.
                    loadingLiveData.setValue(false);
                    // If the sign-in was successful, update userLiveData with the current FirebaseUser.
                    if (task.isSuccessful()) {
                        userLiveData.setValue(mAuth.getCurrentUser());
                    } else {
                         // If sign-in fails, update errorLiveData with the error message.
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
    public FirebaseUser getCurrentUser()
    {
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
                    // Disable the loading indicator as the operation is now finished (even though it failed).
                    loadingLiveData.setValue(false);
                    // Update the errorLiveData with a descriptive error message containing the error details.
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
        // Create the default category with key "0" and value "כל הקטגוריות" (All categories).
        getCategoriesRef().child(String.valueOf(0)).setValue("כל הקטגוריות");
        // Create a category with key "1" and value "טיולים" (Trips).
        getCategoriesRef().child(String.valueOf(1)).setValue("טיולים");
        // Create a category with key "2" and value "קניות" (Shopping).
        getCategoriesRef().child(String.valueOf(2)).setValue("קניות");
        // Create a category with key "3" and value "אירועים מיוחדים" (Special events).
        getCategoriesRef().child(String.valueOf(3)).setValue("אירועים מיוחדים");
    }

 /**
 * Retrieves the categories from the database and posts them to the provided LiveData objects.
 *
 * @param CategoriesLiveData LiveData to post the list of categories.
 * @param loadingLiveData LiveData to indicate the loading status.
 * @param errorLiveData LiveData to post error messages.
 */
    public void getCategories(final MutableLiveData<List<String>> CategoriesLiveData,
                              final MutableLiveData<Boolean> loadingLiveData,
                              final MutableLiveData<String> errorLiveData) {
        // Set loading indicator to true to show that data retrieval has started.
        loadingLiveData.setValue(true);
        // Attach a single event listener to fetch the categories from the database.
        getCategoriesRef().addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                // Initialize a list to hold the retrieved category names.
                List<String> Categories = new ArrayList<>();
                 // Iterate through each child node (each category) in the snapshot.
                for (DataSnapshot child : snapshot.getChildren()) {
                    // Retrieve the value of the category as a String.
                    String CategoryStr = child.getValue(String.class);
                    // If the category value is not null, add it to the list.
                    if (CategoryStr != null)
                    {
                        Categories.add(CategoryStr);
                    }
                }
                // Post the list of categories to the LiveData.
                CategoriesLiveData.setValue(Categories);
                 // Set loading indicator to false as data retrieval is complete.
                loadingLiveData.setValue(false);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                // In case of an error, set loading indicator to false.
                loadingLiveData.setValue(false);
                // Post an error message with the details from the DatabaseError.
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
        // Set the loading indicator to true as the retrieval process starts.
        loadingLiveData.setValue(true);
        // Get the DatabaseReference for the user's lists.
        DatabaseReference getListsRef = getListsRef();
        // Attach a single event listener to fetch the data from Firebase.
        getListsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot)
            {
                // Initialize a list to store the retrieved lists.
                List<String> lists = new ArrayList<>();
                // Iterate over each child snapshot, which represents an individual list.
                for (DataSnapshot child : snapshot.getChildren())
                {
                    // Convert the snapshot's value to a String.
                    String listStr = child.getValue(String.class);
                    // If the list string is not null, add it to the list.
                    if (listStr != null)
                    {
                        lists.add(listStr);
                    }
                }
                 // Post the retrieved lists to the provided LiveData.
                listsLiveData.setValue(lists);
                // Disable the loading indicator as data retrieval is complete.
                loadingLiveData.setValue(false);
            }
            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                // Disable the loading indicator since the operation has ended.
                loadingLiveData.setValue(false);
                // Post an error message with details about the failure.
                errorLiveData.setValue("Failed to fetch content: " + databaseError.getMessage());
            }
        });
    }
    public void getSharedLists(final MutableLiveData<List<ListSharedObject>> listsLiveData, final MutableLiveData<Boolean> loadingLiveData, final MutableLiveData<String> errorLiveData)
    {
        // Set the loading indicator to true as we start fetching shared lists.
        loadingLiveData.setValue(true);
        // Get the DatabaseReference for the shared lists.
        DatabaseReference getListsRef = getSharedListsRef();
         // Attach a listener to retrieve the shared lists data as a single event.
        getListsRef.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot snapshot)
            {
                // Initialize a list to hold the ListSharedObject items.
                List<ListSharedObject> lists = new ArrayList<>();
                // Iterate over each child node under the shared lists.
                for (DataSnapshot child : snapshot.getChildren())
                {
                    // Retrieve the list's name from the "Name" child node.
                    String listStr = child.child("Name").getValue(String.class);
                    // Retrieve the list of users who have saved the list.
                    List<String> userSaves = child.child("savedUsers").getValue(new GenericTypeIndicator<List<String>>() {});
                    // Get the count of saved users.
                    int listSaveCount = userSaves.size();
                    // If there is only one entry and it's an empty string, treat the count as zero.
                    if(listSaveCount == 1 && userSaves.get(0).equals(""))
                    {
                        listSaveCount = 0;
                    }
                    // Retrieve the list of categories associated with the list.
                    List<String> listCategoriesList = child.child("Categories").getValue(new GenericTypeIndicator<List<String>>() {});
                    // Convert the list of categories to an array.
                    String[] listCategories = listCategoriesList != null ? listCategoriesList.toArray(new String[0]) : new String[0];
                    // If the list name is not null, create a new ListSharedObject and add it to the list.
                    if (listStr != null)
                    {
                        lists.add(new ListSharedObject(listSaveCount, listStr, listCategories));
                    }
                }
                // Post the retrieved shared lists to the provided LiveData.
                listsLiveData.setValue(lists);
                // Disable the loading indicator as data retrieval is complete.
                loadingLiveData.setValue(false);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                // Disable the loading indicator since the operation has ended.
                loadingLiveData.setValue(false);
                // Post an error message with details about the failure.
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
        // Set the loading indicator to true at the start of data retrieval.
        loadingLiveData.setValue(true);
        // Determine the correct DatabaseReference based on the type of values.
        DatabaseReference getValuesRef;
        if(valuesKind.equals("SharedValues"))
        {
             // Use the shared values reference if valuesKind equals "SharedValues".
            getValuesRef = getSharedValuesRef();
        }
        else
        {
            // Otherwise, use the user-specific values reference.
            getValuesRef = getValuesRef();
        }
        // Attach a listener to retrieve the values for the given keyPrefix.
        getValuesRef.child(keyPrefix).addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot snapshot)
            {
                // Create a list to hold the retrieved Product objects.
                List<Product> values = new ArrayList<>();
                // Iterate over each child in the node
                for (DataSnapshot child : snapshot.getChildren())
                {
                    String value;
                    // Default isChecked to false.
                    Boolean isChecked = false;
                    if(valuesKind.equals("UserValues"))
                    {
                        // If using user-specific values, retrieve both "isChecked" and "value" fields.
                        isChecked = child.child("isChecked").getValue(Boolean.class);
                        
                        value = child.child("value").getValue(String.class);
                    }
                    else
                    {
                        // For shared values, only retrieve the value.
                        value = child.getValue(String.class);
                    }
                    // Only add the product if the value is not null and isChecked is available.
                    if (value != null && isChecked != null)
                    {
                        values.add(new Product(value, isChecked));
                    }
                }
                // Update the LiveData with the fetched values
                valuesLiveData.setValue(values);
                // Disable the loading indicator as the operation has completed.
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
                    Map<String, Object> sharedObject = new HashMap<>();
                    sharedObject.put("Categories",categories);
                    String userEmail = mAuth.getCurrentUser().getEmail();
                    String emailNode = userEmail.replace(".", "_");
                    sharedObject.put("CreatedBy", emailNode);
                    sharedObject.put("Name",listName);
                    ArrayList<String> listSaves = new ArrayList<>();
                    listSaves.add("");
                    sharedObject.put("savedUsers", listSaves);
                    getListsRef.child(String.valueOf(count)).setValue(sharedObject)
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
                public void onDataChange(DataSnapshot snapshot)
                {
                    long count = snapshot.getChildrenCount(); // Get the number of existing categories
                    if(count == 1)
                    {
                        DataSnapshot child = snapshot.getChildren().iterator().next();
                        if (child.getValue() != null && child.getValue().equals(""))
                        {
                            count = 0;
                        }
                    }
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
        // Set loading state to true before starting the operation
        loadingLiveData.setValue(true);
        DatabaseReference ListsRef = getListsRef();
        DatabaseReference valuesRef = getValuesRef();
        ListsRef.child(key).removeValue();
        valuesRef.child(listName + key).removeValue();
        ListsRef.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot snapshot)
            {
                if (snapshot.exists())
                {
                    int counter = 0; // Keeps track of how many gaps exist (null values)
                    // Iterate through the remaining items
                    for (int i = Integer.valueOf(key); i < snapshot.getChildrenCount() + 1; i++)
                    {
                        String listName = snapshot.child(String.valueOf(i)).getValue(String.class);
                        if (listName == null)
                        {
                            // Increment the counter for null (deleted) values
                            counter++;
                        }
                        else
                        {
                            // Move the current value to fill the gap
                            ListsRef.child(String.valueOf(i - counter)).setValue(listName);

                            // Move the values from sharedValuesRef as well
                            String oldValuesKey = listName + i;
                            String newValuesKey = listName + (i - counter);

                            valuesRef.child(oldValuesKey).addListenerForSingleValueEvent(new ValueEventListener()
                            {
                                @Override
                                public void onDataChange(DataSnapshot valueSnapshot)
                                {
                                    if (valueSnapshot.exists())
                                    {
                                        valuesRef.child(newValuesKey).setValue(valueSnapshot.getValue(), (databaseError, databaseReference) -> {
                                            if (databaseError == null)
                                            {
                                                // Remove the old key to avoid duplicates
                                                valuesRef.child(oldValuesKey).removeValue();
                                            }
                                        });
                                    }
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError)
                                {
                                    // Notify completion of loading
                                    loadingLiveData.setValue(false);
                                    System.out.println("Error moving shared values: " + databaseError.getMessage());
                                }
                            });
                            // Remove the old key to avoid duplicates
                            ListsRef.child(String.valueOf(i)).removeValue();
                        }
                    }
                    // Notify completion of loading
                    loadingLiveData.setValue(false);
                }
            }
            @Override
            public void onCancelled(DatabaseError error)
            {
                // Notify completion of loading
                loadingLiveData.setValue(false);
                System.out.println("Error: " + error.getMessage());
            }
        });
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
                            if (value == null)
                            {
                                // Increment the counter for null (deleted) values
                                counter++;
                            }
                            else
                            {
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
                public void onCancelled(DatabaseError error)
                {
                    // Handle database access errors
                    errorLiveData.setValue("Failed to access database: " + error.getMessage());
                }
            });
        }
    }
    /**
     * Deletes the categories and its associated data from Firebase.
     *
     * @param indexes   The inedexes of the categories to delete.
     * @param loadingLiveData LiveData to indicate the loading state.
     * @param errorLiveData   LiveData to capture error messages.
     */
    public void deleteCategories(ArrayList<Integer> indexes, int categoriesSize, MutableLiveData<Boolean> loadingLiveData, MutableLiveData<String> errorLiveData)
    {
        // Set loading state to true before starting the operation
        loadingLiveData.setValue(true);
        DatabaseReference categoriesRef = getCategoriesRef();
        Boolean categoriesEmpty = false;
        // Remove the values at the given indexes
        for (int i = 0; i < indexes.size(); i++)
        {
            if (categoriesSize == indexes.size() && i == indexes.size()-1)
            {

                categoriesRef.child(String.valueOf(indexes.get(i))).setValue("");
                categoriesEmpty = true;
                loadingLiveData.setValue(false);
            }
            else
            {
                categoriesRef.child(String.valueOf(indexes.get(i))).removeValue();
            }
        }
        categoriesRef.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot snapshot)
            {
                if (snapshot.exists())
                {
                    int counter = 0; // Keeps track of how many gaps exist (null values)

                    // Iterate through the remaining items
                    for (int i = indexes.get(0); i < snapshot.getChildrenCount() + indexes.size(); i++)
                    {
                        String value = snapshot.child(String.valueOf(i)).getValue(String.class);
                        if (value == null)
                        {
                            // Increment the counter for null (deleted) values
                            counter++;
                        }
                        else
                        {
                            // Move the current value to fill the gap
                            categoriesRef.child(String.valueOf(i - counter)).setValue(value);
                            // Remove the old key to avoid duplicates
                            categoriesRef.child(String.valueOf(i)).removeValue();
                        }
                    }

                    // Notify completion of loading
                    loadingLiveData.setValue(false);
                }
                else
                {
                    // If the snapshot doesn't exist, notify error
                    errorLiveData.setValue("No data found in the category.");
                    loadingLiveData.setValue(false);
                }
            }
            @Override
            public void onCancelled(DatabaseError error)
            {
                // Handle database access errors
                errorLiveData.setValue("Failed to access database: " + error.getMessage());
            }
        });
    }
    public void getFilteredLists(List<String> filteredCategories,
                                 MutableLiveData<Boolean> loadingLiveData,
                                 MutableLiveData<List<ListSharedObject>> listsLiveData,
                                 MutableLiveData<String> errorLiveData)
    {
        // Start by setting the loading indicator to true.
        loadingLiveData.setValue(true);
        // Retrieve the shared lists from Firebase.
        getSharedListsRef().addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot snapshot)
            {
                // Initialize a list to store the filtered ListSharedObject items.
                List<ListSharedObject> lists = new ArrayList<>();
                // Check if the snapshot contains data.
                if (snapshot.exists())
                {
                    // Iterate through each shared list node.
                    for (DataSnapshot child : snapshot.getChildren())
                    {
                        // Retrieve the list of categories for the current list.
                        GenericTypeIndicator<List<String>> t = new GenericTypeIndicator<List<String>>() {};
                        List<String> categoriesOfList = child.child("Categories").getValue(t);
                        // If no categories are found, initialize an empty list.
                        if (categoriesOfList == null)
                        {
                            categoriesOfList = new ArrayList<>();
                        }
                        // If filteredCategories is not null, perform filtering.
                        if (filteredCategories != null) {
                            // Assume the current list contains all filtered categories.
                            Boolean containsCategories = true;
                            // Check each category in the filtered list.
                            for (String category : filteredCategories) {
                                // If the current list's categories do not contain this category
                            // and the category is not "כל הקטגוריות" (meaning "All categories"),
                            // mark it as not containing all required categories.
                                if (!categoriesOfList.contains(category) && !category.equals("כל הקטגוריות")) {
                                    containsCategories = false;
                                    break;
                                }
                            }
                            // If the list contains all the required filtered categories.
                            if (containsCategories)
                            {
                                // Retrieve the list's name.
                                String listName = child.child("Name").getValue(String.class);
                                 // Retrieve the list of users who have saved this list.
                                List<String> userSaves = child.child("savedUsers").getValue(new GenericTypeIndicator<List<String>>() {});
                                // Calculate the number of users who saved the list.
                                int listSaveCount = userSaves.size();
                                // If there's only one entry and it is an empty string, treat it as zero.
                                if(listSaveCount == 1 && userSaves.get(0).equals(""))
                                {
                                    listSaveCount = 0;
                                }
                                // Retrieve the categories associated with the list.
                                List<String> listCategoriesList = child.child("Categories").getValue(new GenericTypeIndicator<List<String>>() {
                                });
                                // Convert the list of categories to an array.
                                String[] listCategories = listCategoriesList != null ? listCategoriesList.toArray(new String[0]) : new String[0];
                                // If the list name is not null, create a new ListSharedObject and add it to the list.
                                if (listName != null) {
                                    lists.add(new ListSharedObject(listSaveCount, listName, listCategories));
                                }
                            }
                        }
                    }
                }
                // Post the filtered lists to the LiveData.
                listsLiveData.setValue(lists);
                // Stop the loading indicator.
                loadingLiveData.setValue(false);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // In case of an error, post the error message.
                errorLiveData.setValue("Failed to fetch lists categories: " + error.getMessage());
                // Stop the loading indicator.
                loadingLiveData.setValue(false);
            }
        });
    }
    public void updateListSaves(String key, MutableLiveData<Boolean> loadingLiveData, MutableLiveData<String> errorLiveData) {
        try {
            // Set loading state to true before starting the operation
            loadingLiveData.setValue(true);

            // Get reference to "listSaveCount"
            DatabaseReference savesReference = getSharedListsRef().child(key).child("savedUsers");
            // Read the current value asynchronously
            savesReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot)
                {
                    List<String> userSaves = snapshot.getValue(new GenericTypeIndicator<List<String>>() {});
                    int listSaveCount = userSaves.size();
                    if(listSaveCount == 1 && userSaves.get(0).equals(""))
                    {
                        listSaveCount = 0;
                    }
                    String userEmail = mAuth.getCurrentUser().getEmail();
                    String emailNode = userEmail.replace(".", "_");
                    if(!userSaves.contains(emailNode))
                    {
                        // Increment the count and update the database
                        savesReference.child(String.valueOf(listSaveCount)).setValue(emailNode)
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
    public void checkIfUserIsAdmin(MutableLiveData<Boolean> isAdminLiveData, MutableLiveData<Boolean> loadingLiveData, MutableLiveData<String> errorLiveData) {
        // Start loading state to indicate an ongoing process
        loadingLiveData.setValue(true);

        // Get the currently authenticated user
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            // If no user is logged in, return false and stop loading
            isAdminLiveData.setValue(false);
            loadingLiveData.setValue(false);
            errorLiveData.setValue("No user logged in.");
            return;
        }

        // Retrieve the user's email and format it for Firebase
        String userEmail = currentUser.getEmail();
        if (userEmail == null || userEmail.isEmpty()) {
            isAdminLiveData.setValue(false);
            loadingLiveData.setValue(false);
            errorLiveData.setValue("User email not found.");
            return;
        }

        // Convert email to Firebase-compatible format (replace "." with "_")
        String emailNode = userEmail.replace(".", "_");

        // Reference to the specific user's admin status
        DatabaseReference adminRef = FirebaseDatabase.getInstance().getReference("adminUsers").child(emailNode);

        // Check if the key exists in Firebase
        adminRef.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot snapshot)
            {
                if (snapshot.exists() && Boolean.TRUE.equals(snapshot.getValue(Boolean.class)))
                {
                    // If the key exists and its value is true, the user is an admin
                    isAdminLiveData.setValue(true);
                } else {
                    // If the key does not exist or the value is not true, the user is not an admin
                    isAdminLiveData.setValue(false);
                }
                loadingLiveData.setValue(false);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Handle Firebase errors
                isAdminLiveData.setValue(false);
                loadingLiveData.setValue(false);
                errorLiveData.setValue("Error checking admin status: " + error.getMessage());
            }
        });
    }
    public void deleteSharedLists(List<Integer> indexes, List<String> listNames, MutableLiveData<Boolean> loadingLiveData, MutableLiveData<String> errorLiveData)
    {
        // Set loading state to true before starting the operation
        loadingLiveData.setValue(true);
        DatabaseReference sharedListsRef = getSharedListsRef();
        DatabaseReference sharedValuesRef = getSharedValuesRef();

        // Remove the values at the given indexes
        for (int i = 0; i < indexes.size(); i++)
        {
                String valuesKey = listNames.get(i) + String.valueOf(indexes.get(i));
                String listsKey = String.valueOf(indexes.get(i));
                sharedValuesRef.child(valuesKey).removeValue();
                sharedListsRef.child(listsKey).removeValue();
        }
        sharedListsRef.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot snapshot)
            {
                if (snapshot.exists())
                {
                    int counter = 0; // Keeps track of how many gaps exist (null values)
                    // Iterate through the remaining items
                    for (int i = indexes.get(0); i < snapshot.getChildrenCount() + indexes.size(); i++)
                    {
                        String listName = snapshot.child(String.valueOf(i)).child("Name").getValue(String.class);
                        if (listName == null)
                        {
                            // Increment the counter for null (deleted) values
                            counter++;
                        }
                        else
                        {
                            String userName = snapshot.child(String.valueOf(i)).child("CreatedBy").getValue(String.class);
                            List<String> userSaves = snapshot.child(String.valueOf(i)).child("savedUsers").getValue(new GenericTypeIndicator<List<String>>() {});
                            List<String> categories = snapshot.child(String.valueOf(i)).child("Categories").getValue(new GenericTypeIndicator<List<String>>() {});
                            // Create a map to store the values
                            Map<String, Object> sharedObject = new HashMap<>();
                            sharedObject.put("Categories", categories);
                            sharedObject.put("CreatedBy", userName);
                            sharedObject.put("Name", listName);
                            sharedObject.put("savedUsers", userSaves);

                            // Move the current value to fill the gap
                            sharedListsRef.child(String.valueOf(i - counter)).setValue(sharedObject);

                            // Move the values from sharedValuesRef as well
                            String oldValuesKey = listName + i;
                            String newValuesKey = listName + (i - counter);

                            sharedValuesRef.child(oldValuesKey).addListenerForSingleValueEvent(new ValueEventListener()
                            {
                                @Override
                                public void onDataChange(DataSnapshot valueSnapshot)
                                {
                                    if (valueSnapshot.exists())
                                    {
                                        sharedValuesRef.child(newValuesKey).setValue(valueSnapshot.getValue(), (databaseError, databaseReference) -> {
                                            if (databaseError == null)
                                            {
                                                // Remove the old key to avoid duplicates
                                                sharedValuesRef.child(oldValuesKey).removeValue();
                                            }
                                        });
                                    }
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError)
                                {
                                    // Notify completion of loading
                                    loadingLiveData.setValue(false);
                                    System.out.println("Error moving shared values: " + databaseError.getMessage());
                                }
                            });
                            // Remove the old key to avoid duplicates
                            sharedListsRef.child(String.valueOf(i)).removeValue();
                        }
                    }
                    // Notify completion of loading
                    loadingLiveData.setValue(false);
                }
            }
            @Override
            public void onCancelled(DatabaseError error)
            {
                // Notify completion of loading
                loadingLiveData.setValue(false);
                System.out.println("Error: " + error.getMessage());
            }
        });
    }

    // Returns a DatabaseReference to the "lists" node for the current user.
    public DatabaseReference getListsRef()
    {
          // Get the current user's email.
        String userEmail = mAuth.getCurrentUser().getEmail();
         // Replace any '.' in the email with '_' to make it a valid key for Firebase.
        String emailNode = userEmail.replace(".", "_");
        // Navigate to the user's lists under the "UsersDatabase" node.
        DatabaseReference listsRef = databaseReference.child("UsersDatabase").child(emailNode).child("lists");
        return listsRef;
    }
    // Returns a DatabaseReference to the "values" node for the current user.
    public DatabaseReference getValuesRef()
    {
        // Get the current user's email.
        String userEmail = mAuth.getCurrentUser().getEmail();
        // Replace any '.' in the email with '_' for Firebase compatibility.
        String emailNode = userEmail.replace(".", "_");
        // Navigate to the user's values under the "UsersDatabase" node.
        DatabaseReference valuesRef = databaseReference.child("UsersDatabase").child(emailNode).child("values");
        return valuesRef;
    }
    // Returns a DatabaseReference to the "Categories" node for the current user.
    public DatabaseReference getCategoriesRef()
    {
        // Get the current user's email.
        String userEmail = mAuth.getCurrentUser().getEmail();
         // Replace any '.' in the email with '_' to ensure valid Firebase key formatting.
        String emailNode = userEmail.replace(".", "_");
         // Navigate to the user's categories under the "UsersDatabase" node.
        DatabaseReference CategoriesRef = databaseReference.child("UsersDatabase").child(emailNode).child("Categories");
        return CategoriesRef;
    }
    // Returns a DatabaseReference to the shared "lists" node.
    public DatabaseReference getSharedListsRef()
    {
          // Navigate directly to the "lists" node under "SharedLists".
        DatabaseReference SharedListsRef = databaseReference.child("SharedLists").child("lists");
        return SharedListsRef;
    }
    // Returns a DatabaseReference to the shared "values" node.
    public DatabaseReference getSharedValuesRef()
    {
        // Navigate directly to the "values" node under "SharedLists".
        DatabaseReference SharedValuesRef = databaseReference.child("SharedLists").child("values");
        return SharedValuesRef;
    }
}
