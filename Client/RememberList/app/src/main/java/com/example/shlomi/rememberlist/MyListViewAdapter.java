package com.example.shlomi.rememberlist;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;
public class MyListViewAdapter extends ArrayAdapter<MyListView> {
    Context context;
    List<MyListView> objects;
    public MyListViewAdapter(Context context, int resource, int textViewResourceId, List<MyListView> objects) {
        super(context, resource, textViewResourceId, objects);
        this.context=context;
        this.objects=objects;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = ((Activity) context).getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.custom_layout, parent, false);
        TextView text = (TextView) view.findViewById(R.id.textview);
        CheckBox checkBox = (CheckBox) view.findViewById(R.id.cbBox);
        MyListView temp = objects.get(position);
        text.setText(temp.getText());
        return view;
    }
}
