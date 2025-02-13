package RememberList.Codes;

import static org.junit.Assert.assertEquals;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.lifecycle.MutableLiveData;

import org.junit.Test;
import org.mockito.Mockito;

public class UnitTests {

    @Test
    public void addList()
    {
        // Mock objects
        Context contextMock = Mockito.mock(Context.class);
        SharedPreferences sharedPreferencesMock = Mockito.mock(SharedPreferences.class);
        SharedPreferences.Editor editorMock = Mockito.mock(SharedPreferences.Editor.class);

        Mockito.when(contextMock.getSharedPreferences(Mockito.anyString(), Mockito.anyInt()))
                .thenReturn(sharedPreferencesMock);

        Mockito.when(sharedPreferencesMock.edit()).thenReturn(editorMock);
        Mockito.when(editorMock.putString(Mockito.anyString(), Mockito.anyString())).thenReturn(editorMock);

        Repository repository = new Repository(contextMock);
        MutableLiveData<String> errorLiveData = new MutableLiveData<>();
        MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>();

        repository.addList("Shopping List",loadingLiveData, errorLiveData);

        Mockito.verify(editorMock).putString(Mockito.anyString(), Mockito.eq("Shopping List"));
        Mockito.verify(editorMock).apply();

        assertEquals(null, errorLiveData.getValue());
    }

    @Test
    public void addValue()
    {
        // Mock objects
        Context contextMock = Mockito.mock(Context.class);
        SharedPreferences sharedPreferencesMock = Mockito.mock(SharedPreferences.class);
        SharedPreferences.Editor editorMock = Mockito.mock(SharedPreferences.Editor.class);

        Mockito.when(contextMock.getSharedPreferences(Mockito.anyString(), Mockito.anyInt()))
                .thenReturn(sharedPreferencesMock);

        Mockito.when(sharedPreferencesMock.edit()).thenReturn(editorMock);
        Mockito.when(editorMock.putString(Mockito.anyString(), Mockito.anyString())).thenReturn(editorMock);

        // Mocking contains method for sp2
        Mockito.when(sharedPreferencesMock.contains(Mockito.anyString())).thenReturn(false);

        Repository repository = new Repository(contextMock);
        MutableLiveData<String> errorLiveData = new MutableLiveData<>();
        MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>();

        // Call addValue
        repository.addValue("TestKey", "TestValue", loadingLiveData, errorLiveData);

        // Verify: Ensure the value was added with the correct key
        Mockito.verify(editorMock).putString(Mockito.eq("TestKey1"), Mockito.eq("TestValue"));

        // Verify: Ensure apply() is called
        Mockito.verify(editorMock).apply();

        // Assert: Ensure no error is set
        assertEquals(null, errorLiveData.getValue());
    }
    @Test
    public void deleteList()
    {
        // Mock objects
        Context contextMock = Mockito.mock(Context.class);
        SharedPreferences sharedPreferencesMock = Mockito.mock(SharedPreferences.class);
        SharedPreferences.Editor editorMock = Mockito.mock(SharedPreferences.Editor.class);

        Mockito.when(contextMock.getSharedPreferences(Mockito.anyString(), Mockito.anyInt()))
                .thenReturn(sharedPreferencesMock);

        Mockito.when(sharedPreferencesMock.edit()).thenReturn(editorMock);
        Mockito.when(editorMock.putString(Mockito.anyString(), Mockito.anyString())).thenReturn(editorMock);
        Mockito.when(sharedPreferencesMock.contains("Shopping List1")).thenReturn(true);

        Repository repository = new Repository(contextMock);
        MutableLiveData<String> errorLiveData = new MutableLiveData<>();
        MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>();

        // Step 1: Add a list
        repository.addList("Shopping List",loadingLiveData, errorLiveData);
        Mockito.verify(editorMock).putString(Mockito.anyString(), Mockito.eq("Shopping List"));

        // Step 2: Delete the list
        repository.deleteList("Shopping List", loadingLiveData, errorLiveData);

        // Verify: Ensure remove() was called for the correct keys
        Mockito.verify(editorMock).remove("Shopping List1");

        // Assert: Ensure the list deleted
        Mockito.verify(editorMock, Mockito.times(1)).clear();


        // Verify: Ensure apply() was called
        Mockito.verify(editorMock, Mockito.atLeastOnce()).apply();

        // Assert: Ensure no error
        assertEquals(null, errorLiveData.getValue());
    }
}
