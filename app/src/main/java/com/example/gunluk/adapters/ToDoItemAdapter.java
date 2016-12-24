package com.example.gunluk.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.gunluk.R;
import com.example.gunluk.models.ToDoItem;

import java.util.Objects;

/**
 * Adapter to bind a ToDoItem List to a view
 */
public class ToDoItemAdapter extends ArrayAdapter<ToDoItem> {

    /**
     * Adapter context
     */
    Context mContext;

    /**
     * Adapter View layout
     */
    int mLayoutResourceId;

    public ToDoItemAdapter(Context context, int layoutResourceId) {
        super(context, layoutResourceId);
        mContext = context;
        mLayoutResourceId = layoutResourceId;
    }

    /**
     * Returns the view for a specific item on the list
     */
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View row = convertView;

        final ToDoItem currentItem = getItem (position);

        if (row == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater ( );
            row = inflater.inflate (mLayoutResourceId, parent, false);
        }


        row.setTag (currentItem);

    //    final CheckBox checkBox = (CheckBox) row.findViewById (R.id.checkToDoItem);
     //   checkBox.setChecked (false);
     //   checkBox.setEnabled (true);
        final TextView time = (TextView) row.findViewById (R.id.time);
        assert currentItem != null;
        time.setText (currentItem.getCreatedAt ( ).toString ( ));
        final ImageView imageList = (ImageView) row.findViewById (R.id.imageList);
        String image = currentItem.getDiaryImage ( );
        Bitmap bitmap = base64ToBitmap (image);
        imageList.setImageBitmap (bitmap);
        final TextView title = (TextView) row.findViewById (R.id.diaryTitle);
        title.setText (currentItem.getText ( ));
        final ImageView statusImage = (ImageView) row.findViewById (R.id.statusList);
        if (Objects.equals (currentItem.getState ( ), "Çok Mutlu")) {
            statusImage.setImageResource (R.drawable.veryhappy);
        } else if (Objects.equals (currentItem.getState ( ), "Mutlu")) {
            statusImage.setImageResource (R.drawable.happy);
        } else if (Objects.equals (currentItem.getState ( ), "Standart")) {
            statusImage.setImageResource (R.drawable.standart);
        } else if (Objects.equals (currentItem.getState ( ), "Kötü")) {
            statusImage.setImageResource (R.drawable.sad);
        } else if (Objects.equals (currentItem.getState ( ), "Kızgın")) {
            statusImage.setImageResource (R.drawable.angry);
        } else if (Objects.equals (currentItem.getState ( ), "Ağlamış")) {
            statusImage.setImageResource (R.drawable.verysad);
        }

        final TextView diary = (TextView) row.findViewById (R.id.diary);
        diary.setText (currentItem.getDiary ( ));

    //    checkBox.setOnClickListener(new View.OnClickListener() {

     //       @Override
    //        public void onClick(View arg0) {
      //          if (checkBox.isChecked()) {
       //             checkBox.setEnabled(false);
        //            if (mContext instanceof MainActivity) {
        //                Tab1Fragment tab1Fragment =new Tab1Fragment ();
        //                tab1Fragment.checkItem(currentItem);
        //            }
        //        }
        //    }
       // });
        return row;
    }
    private Bitmap base64ToBitmap(String b64) { //ŞİFRELİ METNİ RESME DÖNÜŞTÜRME
        byte[] imageAsBytes = Base64.decode(b64.getBytes(), Base64.DEFAULT);
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        return BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length,options);
    }
}