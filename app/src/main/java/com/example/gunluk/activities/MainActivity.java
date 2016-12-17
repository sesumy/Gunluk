package com.example.gunluk.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.gunluk.R;
import com.example.gunluk.adapters.NavListAdapter;
import com.example.gunluk.fragments.MyAbout;
import com.example.gunluk.fragments.MyHome;
import com.example.gunluk.fragments.MySettings;
import com.example.gunluk.models.NavItem;
import com.example.gunluk.models.UserModel;
import com.example.gunluk.utils.PrefUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.example.gunluk.R.menu.back;

public class MainActivity extends AppCompatActivity {
    //---- DRAWER LAYOUT AND ACTİON BAR----
    DrawerLayout drawerLayout;
    RelativeLayout drawerPane;
    ListView lvNav;
    List<NavItem> listNavItems;
    List<Fragment> listFragments;
    ActionBarDrawerToggle actionBarDrawerToggle;
    Bitmap bitmap;
    TextView tv,gender,name;
    ImageView profileImage;
    String email;
    String name1;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_diarise);
        tv=(TextView)findViewById(R.id.diarytab2);

        if(PrefUtils.getCurrentUser (MainActivity.this)==null){
            Intent intIntent=new Intent (this,LoginActivity.class);
            startActivity (intIntent);
        }
        final UserModel user = PrefUtils.getCurrentUser (MainActivity.this);
         profileImage= (ImageView) findViewById(R.id.icon);

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


        name1=user.name;
        email=user.email;

        gender=(TextView)findViewById (R.id.gender);
        gender.setText (name1);

        name=(TextView)findViewById (R.id.name);
        name.setText (email);


        //---- DRAWER LAYOUT AND ACTİON BAR----------------------
        drawerLayout = (DrawerLayout) findViewById (R.id.drawer_layout);
        drawerPane = (RelativeLayout) findViewById (R.id.drawer_pane);
        lvNav = (ListView) findViewById (R.id.nav_list);
        listNavItems = new ArrayList<> ( );
        listNavItems.add (new NavItem ("Ana Sayfa", "Günlük Yazma,Günlük Listesi Duygusal Sıralama", R.drawable.ic_action_home));
        listNavItems.add (new NavItem ("Ayarlar", "Ayarları değiştir", R.drawable.ic_action_setting));
        listNavItems.add (new NavItem ("Hakkında", "Diarise Hakkında", R.drawable.ic_action_about));
        NavListAdapter navListAdapter = new NavListAdapter (this, R.layout.item_nav_list, listNavItems);
        lvNav.setAdapter (navListAdapter);
        listFragments = new ArrayList<> ( );
        listFragments.add (new MyHome ( ));
        listFragments.add (new MySettings ( ));
        listFragments.add (new MyAbout ( ));
        FragmentManager fragmentManager = getSupportFragmentManager ( );
        fragmentManager.beginTransaction ( ).replace (R.id.main_content, listFragments.get (0)).commit ( );
        setTitle (listNavItems.get (0).getTitle ( ));
        lvNav.setItemChecked (0, true);
        drawerLayout.closeDrawer (drawerPane);
        // set listener for navigation items:
        lvNav.setOnItemClickListener (new OnItemClickListener ( ) {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // replace the fragment with the selection correspondingly:
                FragmentManager fragmentManager = getSupportFragmentManager ( );
                fragmentManager.beginTransaction ( ).replace (R.id.main_content, listFragments.get (position)).commit ( );
                setTitle (listNavItems.get (position).getTitle ( ));
                lvNav.setItemChecked (position, true);
                drawerLayout.closeDrawer (drawerPane);
            }
        });// create listener for drawer layout
        actionBarDrawerToggle = new ActionBarDrawerToggle (this, drawerLayout, R.string.drawer_opened, R.string.drawer_closed) {
            @Override
            public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu ( );
                super.onDrawerOpened (drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                invalidateOptionsMenu ( );
                super.onDrawerClosed (drawerView);
            }
        };
        drawerLayout.setDrawerListener (actionBarDrawerToggle);
        //-----DRAWER LAYOUT AND ACTİON BAR END-------------------------------------------------------------

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater ( ).inflate (back, menu);
            MenuItem login = menu.findItem (R.id.inAndout);
        if(PrefUtils.getCurrentUser (MainActivity.this)!=null) {
            UserModel user=PrefUtils.getCurrentUser (MainActivity.this);
            login.setTitle (user.name);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId ( )) {
            case R.id.inAndout:
                Intent intent = new Intent (this, LogoutActivity.class);
                this.startActivity (intent);
                break;
            default:
                return super.onOptionsItemSelected (item);
        }

        return true;
    }
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                    .setTitle("Unutma")
                    .setMessage("Yarın aynı saatte yine gel :) ")
                    .setNegativeButton(android.R.string.no, null)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener () {

                        public void onClick(DialogInterface arg0, int arg1) {
                            finish ();
                        }
                    }).create().show();
        }

    }