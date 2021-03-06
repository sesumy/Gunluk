package com.example.gunluk.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.gunluk.R;
import com.example.gunluk.models.UserModel;
import com.example.gunluk.utils.PrefUtils;
import com.facebook.login.LoginManager;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class LogoutActivity extends Activity {
    private ImageView profileImage;
        Bitmap bitmap;
    final UserModel user = PrefUtils.getCurrentUser (LogoutActivity.this);


    @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_logout);
            profileImage= (ImageView) findViewById(R.id.profileImage);

            // fetching facebook's profile picture
            new AsyncTask<Void,Void,Void> (){
                @Override
                protected Void doInBackground(Void... params) {
                    URL imageURL = null;
                    try {
                        imageURL = new URL("https://graph.facebook.com/" + user.facebookID + "/picture?type=large");
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                    try {
                        assert imageURL != null;
                        bitmap  = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    profileImage.setImageBitmap(bitmap);
                }
            }.execute();


        TextView btnLogout = (TextView) findViewById (R.id.btnLogout);
            btnLogout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PrefUtils.clearCurrentUser(LogoutActivity.this);


                    // We can logout from facebook by calling following method
                    LoginManager.getInstance().logOut();


                    Intent i= new Intent(LogoutActivity.this,LoginActivity.class);
                    startActivity(i);
                    finish();
                }
            });
        }
    }
