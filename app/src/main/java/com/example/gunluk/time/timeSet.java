package com.example.gunluk.time;

/**
 * Created by sesumy on 13.12.2016.
 */

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.gunluk.R;
import com.example.gunluk.activities.MainActivity;
import com.example.gunluk.receiver.MyReceiver;

import java.util.Calendar;

public class timeSet extends Activity {
    private TimePicker timePicker1;
    private TextView time;
    int hour;
    int min;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.timeset);
        timePicker1 = (TimePicker) findViewById (R.id.timePicker1);
        time = (TextView) findViewById (R.id.textView1);

    }

    public void setTime(View view) throws InterruptedException {
        hour = timePicker1.getCurrentHour ( );
        min = timePicker1.getCurrentMinute ( );
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour );
        calendar.set(Calendar.MINUTE, min);
        calendar.set(Calendar.SECOND, 0);
        Intent intent1 = new Intent(timeSet.this, MyReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(timeSet.this, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) timeSet.this.getSystemService(ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),AlarmManager.INTERVAL_DAY, pendingIntent);
        showTime (hour, min);
        Toast.makeText (this,"Tebrikler alarm kuruldu",Toast.LENGTH_LONG).show ();
        Intent inIntent=new Intent (this, MainActivity.class);
        startActivity (inIntent);
    }
    public void showTime(int hour, int min) {
        String format = "";
        time.setText (new StringBuilder ( ).append (hour).append (" : ").append (min)
                .append (" ").append (format));




    }
}