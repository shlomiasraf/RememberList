package com.example.shlomi.rememberlist;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

public class Client
{
    public static String SERVER_IP;
    public static int SERVER_PORT;
    private String mServerMessage = null;
    private OnMessageReceived mMessageListener = null;
    private boolean mRun = false;
    private PrintWriter mBufferOut;
    private BufferedReader mBufferIn;

    public String receive()
    {
        try
        {
            TimeUnit.SECONDS.sleep(1);
        }
        catch (Exception e)
        {
            e.getStackTrace();
        }
        String temp = mServerMessage;
        mServerMessage = "";
        return temp != null? temp :"";
    }
    public Client(OnMessageReceived listener,String host,int port)
    {
        SERVER_IP = host;
        SERVER_PORT = port;
        mMessageListener = listener;
    }
    public void send(String message) {
        if (mBufferOut != null && !mBufferOut.checkError()) {
            mBufferOut.println(message);
            mBufferOut.flush();
        }
    }
    public void stopClient()
    {
        mRun = false;
        if (mBufferOut != null) {
            mBufferOut.flush();
            mBufferOut.close();
        }
        mMessageListener = null;
        mBufferIn = null;
        mBufferOut = null;
        mServerMessage = null;
    }
    public void run()
    {

        mRun = true;

        try
        {
            InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
            Log.e("TCP Client", "C: Connecting...");
            Socket socket = new Socket(serverAddr, SERVER_PORT);
            try
            {
                mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                mBufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                while (mRun)
                {
                    mServerMessage = mBufferIn.readLine();
                    if (mServerMessage != null && mMessageListener != null)
                    {
                        mMessageListener.messageReceived(mServerMessage);
                    }
                }
                Log.e("RESPONSE FROM SERVER", "S: Received Message: '" + mServerMessage + "'");
            }
            catch (Exception e)
            {
                Log.e("TCP", "S: Error", e);
            }
            finally
            {
                socket.close();
            }

        }
        catch (Exception e)
        {
            Log.e("TCP", "C: Error", e);
        }
    }
    public interface OnMessageReceived
    {
        public void messageReceived(String message);
    }
}