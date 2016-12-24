package com.example.gunluk.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.gunluk.R;
import com.example.gunluk.adapters.ToDoItemAdapter;

import static com.example.gunluk.R.menu.detailmenu;


public class DiaryDetail extends Activity {
    String diary, diaryTitle,diaryDate;
    String diaryImageText;
    private ToDoItemAdapter mAdapter;
int pos;
    TextView diaryTextView, diaryTitleTextView, diaryDateTextView;
    ImageView diaryImageView;
String tit="Günlük Ayrıntı";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_diary_detail);
        ActionBar actionBar=getActionBar ();
        assert actionBar != null;
        actionBar.setTitle (tit);

        diaryTextView = (TextView) findViewById (R.id.diaryTextView);
        diaryTitleTextView = (TextView) findViewById (R.id.diaryTitleTextView);
        diaryDateTextView = (TextView) findViewById (R.id.diaryDateTextView);
        diaryImageView = (ImageView) findViewById (R.id.diaryImageView);
        Bundle bundle = getIntent ( ).getExtras ( );
        diary = bundle.getString ("diary");
        diaryTitle = bundle.getString ("diaryTitle");

        diaryDate = bundle.getString ("diaryDate");
        diaryImageText = bundle.getString ("diaryImage");
        Bitmap bitmap = base64ToBitmap (diaryImageText);
        if (diaryImageText != null) {
            System.out.print ("diaryImageText Boş Değil");
            if (bitmap != null) {
                System.out.print ("Bitmap Boş Değil");
                diaryImageView.setImageBitmap (bitmap);
            }
        }
        diaryDateTextView.setText (diaryDate);
        diaryTextView.setText (diary);
        diaryTitleTextView.setText (diaryTitle);
    }
        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            getMenuInflater ( ).inflate (detailmenu, menu);
            return true;
        }
        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId ( )) {
                case R.id.delete:
                    new AlertDialog.Builder(this)
                            .setTitle("Emin misin?")
                            .setMessage("Silmek istediğine emin misin?")
                            .setNegativeButton(android.R.string.no, null)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener () {

                                public void onClick(DialogInterface arg0, int arg1) {




                                }
                            }).create().show();

                    break;

                default:
                    return super.onOptionsItemSelected (item);
            }
            return true;
        }
    private Bitmap base64ToBitmap(String b64) { //ŞİFRELİ METNİ RESME DÖNÜŞTÜRME
        byte[] imageAsBytes = Base64.decode(b64.getBytes(), Base64.DEFAULT);
         Bitmap bit= BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
        return Bitmap.createScaledBitmap(bit, 1000, 1000, false);
    }


}