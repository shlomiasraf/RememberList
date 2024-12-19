package com.example.shlomi.rememberlist;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.Arrays;

public class Main3Activity extends AppCompatActivity implements View.OnClickListener {
    String st;
    ListAdapter boxAdapter;
    ListView lvMain;
    ArrayList<Product> arr;
    String listname;
    ArrayList<String> list;
    ImageButton back;
    ImageButton share;
    ImageButton delete;
    EditText editText;
    Button add;
    TextView textView;
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    android.app.AlertDialog.Builder builder;
    NotificationCompat.Builder notification;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        sp =getSharedPreferences("newValues",0);
        editor = sp.edit();
        listname = Main2Activity.getList();
        builder = new AlertDialog.Builder(this);
        back = (ImageButton) findViewById(R.id.back);
        add = (Button) findViewById(R.id.add);
        share = (ImageButton) findViewById(R.id.share);
        delete = (ImageButton) findViewById(R.id.delete);
        textView = (TextView) findViewById(R.id.textview);
        editText = (EditText) findViewById(R.id.editText);
        lvMain = (ListView) findViewById(R.id.listView);
        editText.setOnClickListener(this);
        add.setOnClickListener(this);
        back.setOnClickListener(this);
        share.setOnClickListener(this);
        delete.setOnClickListener(this);
        arr = new ArrayList<Product>();
        bulidlist();
        boxAdapter = new ListAdapter(Main3Activity.this, arr);
        lvMain.setAdapter(boxAdapter);
    }
    public void bulidlist() {
        st = takeFromSharedPreferences(sp);
        list = new ArrayList<String>(Arrays.asList(st.split(",")));
        textView.setText(listname);
        for (int i = 1; i < list.size(); i++) {
            Product m = new Product(list.get(i), false);
            arr.add(m);
        }
    }
    public String takeFromSharedPreferences(SharedPreferences sp)
    {
        String st = "";
        int j = 0;
        while (sp.getAll().containsKey(listname+String.valueOf(j)))
        {
            st += sp.getString(listname+String.valueOf(j), null);
            st += ",";
            j++;
        }
        String b = "";
        for (int i = 0; i < st.length(); i++)
        {
            if (st.length() > i+4 && st.substring(i,i+4).equals("null"))
            {
                i += 4;
            }
            if(!Character.valueOf(st.charAt(i)).equals('[') && !Character.valueOf(st.charAt(i)).equals(']'));
            {
                b += st.charAt(i);
            }
        }
        st = b;
        return st;
    }
    public String GetKey(String st)
    {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equals(st))
            {
                return String.valueOf(i);
            }
        }
        return String.valueOf(0);
    }
    public void changeKays(String key)
    {
        for (int i = Integer.valueOf(key) + 1; i <= sp.getAll().size(); i++) {
            editor.putString(listname+String.valueOf(i - 1), (sp.getString(listname+String.valueOf(i), null)));
        }
        editor.remove(String.valueOf(sp.getAll().size()));
        editor.commit();
    }
    @Override
    public void onClick(View view)
    {
        boxAdapter = new ListAdapter(Main3Activity.this, arr);
        lvMain.setAdapter(boxAdapter);
        if (view == add)
        {
            editor.putString(listname + String.valueOf(list.size()), editText.getText().toString());
            list.add(editText.getText().toString());
            Product m = new Product(editText.getText().toString(), false);
            arr.add(m);
            boxAdapter = new ListAdapter(Main3Activity.this, arr);
            lvMain.setAdapter(boxAdapter);
            editor.commit();
            editText.setText("");
            Toast.makeText(Main3Activity.this,"יצרת ערך חדש", Toast.LENGTH_LONG).show();
        }
        else if(view == delete)
        {
            builder.setMessage("האם אתה בטוח שאתה מעוניין למחוק את הערכים המסומנים? במידה ותמחק הם ימחקו לצמיתות מהרשימה");
            builder.setCancelable(true);
            builder.setPositiveButton("מחק בכל זאת", new HandleAlertDialogClickListener());
            builder.setNegativeButton("ביטול", new HandleAlertDialogClickListener());
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        else if (view == back)
        {
            Intent intent = new Intent(this, Main2Activity.class);
            startActivity(intent);
        }
    }
    public class HandleAlertDialogClickListener implements DialogInterface.OnClickListener
    {
        public void onClick(DialogInterface dialog, int which)
        {
            if (which == -1)
            {
                ArrayList<Integer> positions = new ArrayList<Integer>();
                for (int i = 0; i < arr.size(); i++)
                {
                    if(boxAdapter.getProduct(i).box)
                    {
                        positions.add(i);
                    }
                }
                for (int i = 0; i <positions.size(); i++)
                {
                    Product s =  boxAdapter.getProduct(positions.get(i)-i);
                    String key = GetKey(s.name);
                    list.remove(s.name);
                    arr.remove(s);
                    editor.remove(listname+key);
                    editor.commit();
                    changeKays(key);
                }
                boxAdapter = new ListAdapter(Main3Activity.this, arr);
                lvMain.setAdapter(boxAdapter);
                Toast.makeText(Main3Activity.this,"הערכים המסומנים נמחקו", Toast.LENGTH_LONG).show();
            }
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        int count = 0;
        for (int i = 0; i < arr.size(); i++)
        {
            if(!boxAdapter.getProduct(i).box)
            {
                count++;
            }
        }
        if(count != 0) {
            notification = new NotificationCompat.Builder(this);
            notification.setAutoCancel(true);
            notification.setSmallIcon(R.mipmap.logo);
            notification.setTicker("לא לקחת את כל הערכים!");
            notification.setWhen(System.currentTimeMillis());
            notification.setContentTitle("לא לקחת את כל הערכים שברשימה");
            notification.setContentText("לא לקחת " + count + " ערכים מהרשימה");
            Intent intent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
            notification.setContentIntent(pendingIntent);
            Uri alarmSound = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_NOTIFICATION);
            notification.setSound(alarmSound);
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            nm.notify(45612, notification.build());
        }
    }
}