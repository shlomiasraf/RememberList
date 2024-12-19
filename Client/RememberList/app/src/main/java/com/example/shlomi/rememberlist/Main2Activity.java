package com.example.shlomi.rememberlist;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Arrays;
public class Main2Activity extends AppCompatActivity implements View.OnClickListener {
    public static int position1;
    int position2;
    private static ArrayAdapter adapter;
    ListView list;
    ArrayList<String> lists;
    EditText editText;
    Button sharelists;
    Button add;
    SharedPreferences sp;
    SharedPreferences sp2;
    SharedPreferences.Editor editor;
    SharedPreferences.Editor editor2;
    android.app.AlertDialog.Builder builder;
    String st;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        builder = new AlertDialog.Builder(this);
        add = (Button) findViewById(R.id.add);
        sharelists = (Button) findViewById(R.id.sharelists);
        editText = (EditText) findViewById(R.id.editText);
        editText.setOnClickListener(this);
        add.setOnClickListener(this);
        sharelists.setOnClickListener(this);
        list = (ListView) findViewById(R.id.listView);
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1);
        sp=getSharedPreferences("newLists",0);
        sp2=getSharedPreferences("newValues",0);
        editor = sp.edit();
        editor2 = sp2.edit();
        bulidlist();
        onClickList();
    }
    public void bulidlist(){
        takeFromSharedPreferences();
        lists = new ArrayList<String>(Arrays.asList(st.split(",")));
        for (int i = 0; i < lists.size(); i++) {

            adapter.add(lists.get(i));
        }
        list.setAdapter(adapter);
    }
    public void takeFromSharedPreferences()
    {
        for (int i = 0; i < sp.getAll().values().size(); i++)
        {
            st += sp.getString(String.valueOf(i), null);
            st += ",";
        }
        String b = "";
        for (int i = 0; i < st.length(); i++)
        {
            if (st.length() > i+4 && st.substring(i,i+4).equals("null"))
            {
                i += 4;
            }
            if(!Character.valueOf(st.charAt(i)).equals('[') && !Character.valueOf(st.charAt(i)).equals(']'))
            {
                b += st.charAt(i);
            }
        }
        st = b;
    }
    public void onClickList(){
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), Main3Activity.class);
                position1 = position;
                startActivity(intent);
            }
        });
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                position2 = position;
                builder.setMessage("האם אתה בטוח שאתה מעוניין למחוק את הרשימה? במידה ותמחק היא תימחק לצמיתות");
                builder.setCancelable(true);
                builder.setPositiveButton("מחק בכל זאת", new HandleAlertDialogClickListener());
                builder.setNegativeButton("ביטול", new HandleAlertDialogClickListener());
                AlertDialog dialog = builder.create();
                dialog.show();
                return false;
            }
        });
    }
    public String GetKey(String st)
    {
        for (int i = 0; i < lists.size(); i++) {
            if (lists.get(i).equals(st))
            {
                return String.valueOf(i);
            }
        }
        return String.valueOf(0);
    }
    public void changeKays(String key)
    {
        for (int i = Integer.valueOf(key) + 1; i <= sp.getAll().size(); i++) {
            editor.putString(String.valueOf(i - 1), (sp.getString(String.valueOf(i), null)));
        }
        editor.remove(String.valueOf(sp.getAll().size()));
        editor.commit();
    }
    public static String getList(){ return String.valueOf(adapter.getItem(position1)); }
    @Override
    public void onClick(View view)
    {
        if (view == add)
        {
            editor.putString(String.valueOf(sp.getAll().size()),editText.getText().toString());
            editor.commit();
            editor2.putString(editText.getText().toString()+String.valueOf(0),editText.getText().toString());
            editor2.commit();
            adapter.add(editText.getText().toString());
            lists.add((editText.getText().toString()));
            list.setAdapter(adapter);
            editText.setText("");
            Toast.makeText(Main2Activity.this,"יצרת רשימה חדשה", Toast.LENGTH_LONG).show();
        }
        else if(view == sharelists)
        {
            Intent intent = new Intent(Main2Activity.this, Main4Activity.class);
            startActivity(intent);
        }
    }
    public class HandleAlertDialogClickListener implements DialogInterface.OnClickListener
    {
        public void onClick(DialogInterface dialog, int which) {
            if (which == -1){
                Object s = adapter.getItem(position2);
                String key = GetKey(String.valueOf(s));
                adapter.remove(s);
                lists.remove(s);
                editor.remove(key);
                editor.commit();
                changeKays(key);
                adapter.notifyDataSetChanged();
                Toast.makeText(Main2Activity.this,"הרשימה נמחקה", Toast.LENGTH_LONG).show();
                int j = 0;
                while (!sp2.getAll().containsKey(s+String.valueOf(j))) {
                    editor2.remove(s + String.valueOf(j));
                    j++;
                }
                editor2.commit();
            }
        }
    }
}
