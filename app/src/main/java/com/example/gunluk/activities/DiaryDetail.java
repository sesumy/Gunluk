package com.example.gunluk.activities;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.gunluk.R;


public class DiaryDetail extends Activity {
    TextView tv;
    String s;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_diary_detail);
        Bundle bundle = getIntent ( ).getExtras ( );
        //  Toolbar toolbar=(Toolbar)findViewBy(R.id.toolbar)
        tv = (TextView) findViewById (R.id.textView);
        if (bundle != null) {
        }
    }
}