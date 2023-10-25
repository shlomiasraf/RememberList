package com.example.shlomi.rememberlist;

import android.widget.CheckBox;

public class MyListView {
    private String text;
    private CheckBox checkBox;
    public MyListView(String text, CheckBox checkBox)
    {
        this.text = text;
        this.checkBox = checkBox;
    }
    public String getText() {
        return text;
    }
}
