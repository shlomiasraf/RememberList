package com.example.shlomi.rememberlist;
import android.os.AsyncTask;
import android.util.Log;
import java.util.concurrent.TimeUnit;
public class connection {
    private Client client;
    public void connect() {
        new ConnectTask().execute("");
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (Exception e) {
            e.getStackTrace();
        }
    }
    public String getData(String message)
    {
        client.send(message);
        return client.receive();
    }
    public class ConnectTask extends AsyncTask<String, String, Client>
    {
        @Override
        protected Client doInBackground(String... message) {
            client = new Client(new Client.OnMessageReceived() {
                @Override
                public void messageReceived(String message) {
                    publishProgress(message);
                }
            }
                    ,"192.168.43.70", 8889);
            client.run();
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values)
        {
            super.onProgressUpdate(values);
            Log.d("test", "response " + values[0]);
        }
    }

}