package com.example.shlomi.rememberlist;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
	SharedPreferences sp1;
	SharedPreferences sp2;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		sp1=getSharedPreferences("newLists",0);
		sp2=getSharedPreferences("newValues",0);
		if(sp1.getAll().isEmpty() && sp2.getAll().isEmpty())
		{
			takeFromFile1();
			readFromfile2();
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					synchronized (this) {
						wait(2000);
						Intent intent = new Intent (MainActivity.this,Main2Activity.class);
						startActivity(intent);
						finish();

					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	public void takeFromFile1()
	{
		SharedPreferences.Editor editor1 = sp1.edit();
		try {
			InputStream input = getAssets().open("lists.txt");
			int size=input.available();
			byte[] buffer=new byte[size];
			input.read(buffer);
			input.close();
			String st = new String(buffer,"windows-1255");
			String[] lists = st.split(",");
			for(int i = 0; i < lists.length; i++)
			{
				editor1.putString(String.valueOf(i),lists[i]);
			}
			editor1.commit();
		}
		catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	public void readFromfile2()
	{
		SharedPreferences.Editor editor2 = sp2.edit();
		try {
			InputStream input = getAssets().open("values.txt");
			int size=input.available();
			byte[] buffer=new byte[size];
			input.read(buffer);
			input.close();
			String st = new String(buffer,"windows-1255");
			String[] list = st.split("#");
			for(int i = 0; i < list.length; i++)
			{
				String str  = list[i];
				String[] list2 = str.split(",");
				for (int j = 0; j < list2.length; j++)
				{
					editor2.putString(list2[0]+String.valueOf(j),list2[j]);
				}
			}
			editor2.commit();
		}
		catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
}

