package RememberList.Codes;

import static org.junit.Assert.assertEquals;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.lifecycle.MutableLiveData;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(AndroidJUnit4.class)
public class RepositoryTest {

    @Test
    public void addList() {
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

        repository.addList("Shopping List", errorLiveData);

        Mockito.verify(editorMock).putString(Mockito.anyString(), Mockito.eq("Shopping List"));
        Mockito.verify(editorMock).apply();

        assertEquals(null, errorLiveData.getValue());
    }
}
