package com.example.gunluk.activities;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.example.gunluk.R;
import com.example.gunluk.adapters.ToDoItemAdapter;
import com.example.gunluk.models.ToDoItem;
import com.example.gunluk.models.UserModel;
import com.example.gunluk.utils.PrefUtils;
import com.example.gunluk.utils.Utilities;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.http.NextServiceFilterCallback;
import com.microsoft.windowsazure.mobileservices.http.OkHttpClientFactory;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilter;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterRequest;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncContext;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.ColumnDataType;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.MobileServiceLocalStoreException;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.SQLiteLocalStore;
import com.microsoft.windowsazure.mobileservices.table.sync.synchandler.SimpleSyncHandler;
import com.squareup.okhttp.OkHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.example.gunluk.R.id.spinner;
import static com.microsoft.windowsazure.mobileservices.table.query.QueryOperations.val;

public class ToDoActivity extends Activity {
    private MobileServiceClient mClient;
    private MobileServiceTable<ToDoItem> mToDoTable;
    private ToDoItemAdapter mAdapter;
    private ProgressBar mProgressBar;
    private EditText mTextNewToDo;
    private ImageView mDiaryImage;
    private EditText mDiary;
    private Spinner mSpinner;
    private String userId;
    final UserModel user = PrefUtils.getCurrentUser (ToDoActivity.this);
final String diaTit="Günlük Başlığı Yok";
    final String diaInf="Günlük İçeriği Yok";


    private int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    private String userChoosenTask;

    //Offline Sync
    //private MobileServiceSyncTable<ToDoItem> mToDoTable;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_do);
        userId= user.facebookID;
        mProgressBar = (ProgressBar) findViewById(R.id.loadingProgressBar);
        // Initialize the progress bar
        mProgressBar.setVisibility(ProgressBar.GONE);
        try {
            //Create the Mobile Service Client instance, using the provided
            // Mobile Service URL and key
            mClient = new MobileServiceClient(
                    "https://gunluk.azurewebsites.net",
                    this).withFilter(new ProgressFilter());

            // Extend timeout from default of 10s to 20s
            mClient.setAndroidHttpClientFactory(new OkHttpClientFactory() {
                @Override
                public OkHttpClient createOkHttpClient() {
                    OkHttpClient client = new OkHttpClient();
                    client.setReadTimeout(20, TimeUnit.SECONDS);
                    client.setWriteTimeout(20, TimeUnit.SECONDS);
                    return client;
                }
            });

            // Get the Mobile Service Table instance to use

            mToDoTable = mClient.getTable(ToDoItem.class);

            // Offline Sync
            //mToDoTable = mClient.getSyncTable("ToDoItem", ToDoItem.class);

            //Init local storage
            initLocalStore().get();

//----------LAYOUT NESNElERİİ--------------------------------------
            mTextNewToDo = (EditText) findViewById(R.id.textNewToDo);
            mSpinner=(Spinner)findViewById (spinner);
             mDiary=(EditText)findViewById (R.id.diary);
            mDiaryImage = (ImageView) findViewById (R.id.diaryImage);                              //Günlük Resmi Ekledik
//--------------------------------------------------------------------------
            // Create an adapter to bind the items with the view
            mAdapter = new ToDoItemAdapter(this, R.layout.row_list_to_do);

            refreshItemsFromTable();

        } catch (MalformedURLException e) {
            createAndShowDialog(new Exception("There was an error creating the Mobile Service. Verify the URL"), "Error");
        } catch (Exception e){
            createAndShowDialog(e, "Error");
        }
        final String[] emoji_title = {"Çok Mutlu", "Mutlu", "Standart","Kızgın", "Kötü","Ağlamış"};
        //Üstte kalması sağlandı
        //Spinner ın içini doldurma
        ArrayAdapter<String> adapter = new ArrayAdapter<> (this, android.R.layout.simple_spinner_item, emoji_title);
        adapter.setDropDownViewResource (android.R.layout.simple_dropdown_item_1line);
        mSpinner.setAdapter (adapter);                                                                        //SPİNNER
        //END
        mDiaryImage.bringToFront ( );

        final Button imageUp = (Button) findViewById (R.id.imageUpload);
        imageUp.setOnClickListener (new View.OnClickListener ( ) {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });


    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Utilities.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(userChoosenTask.equals("Kamera"))
                        cameraIntent();
                    else if(userChoosenTask.equals("Galeri"))
                        galleryIntent();
                } else {
                    //code for deny
                }
                break;
        }
    }
    private void selectImage() {
        final CharSequence[] items = { "Kamera", "Galeri", "İptal" };
        AlertDialog.Builder builder = new AlertDialog.Builder(ToDoActivity.this);
        builder.setTitle("Fotoğraf Ekle");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result=Utilities.checkPermission(ToDoActivity.this);

                if (items[item].equals("Kamera")) {
                    userChoosenTask ="Kamera";
                    if(result)
                        cameraIntent();

                } else if (items[item].equals("Galeri")) {
                    userChoosenTask ="Galeri";
                    if(result)
                        galleryIntent();

                } else if (items[item].equals("İptal")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }
    private void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Resim Seç"),SELECT_FILE);
    }
    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data);
            else if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult(data);
        }
    }
    private void onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

        File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpeg");

        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream (destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Bitmap decrease_picture =Decrease_resolution(thumbnail);
        mDiaryImage.setImageBitmap(decrease_picture);
    }
    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {

        Bitmap bm=null;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Bitmap decrease_picture =Decrease_resolution(bm);
        mDiaryImage.setImageBitmap(decrease_picture);
    }
    public Bitmap Decrease_resolution(Bitmap photo) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream ( );
        photo.compress (Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray ( );
        return BitmapFactory.decodeByteArray (byteArray, 0, byteArray.length);
    }
    /**
     * Initializes the activity menu
     */@Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    /**
     * Select an option from the menu
     */@Override public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_refresh) {
            refreshItemsFromTable();
        }
        if (item.getItemId() == R.id.timeset) {
            Intent intent=new Intent (ToDoActivity.this, TimeSet.class);
            startActivity (intent);
        }


        return true;
    }
    /**
     * Add a new item
     *
     * @param view
     *            The view that originated the call
     */public void addItem(View view) {
        if (mClient == null) {
            return;
        }

        // Create a new item
        final ToDoItem item = new ToDoItem();

            if(mTextNewToDo.getText()!=null){
                item.setText(mTextNewToDo.getText().toString());
            }
        else{
                item.setText(diaTit);
            }

        if(mDiary.getText ()!=null){
            item.setDiary (mDiary.getText ().toString ());
        }
        else{
            item.setDiary (diaInf);
        }
        item.setState (mSpinner.getSelectedItem ().toString ());
        item.setUserId (userId);
        item.setComplete(false);

        if(mDiaryImage.getDrawable ()!= null) {
            mDiaryImage.buildDrawingCache();
            Bitmap bmap = mDiaryImage.getDrawingCache();
            item.setDiaryImage (bitmapToBase64 (bmap));
            String a= bitmapToBase64 (bmap);
            Log.e (a,toString ());
        }
        else{

            Drawable myDrawable = getResources().getDrawable(R.drawable.noimage);
            Bitmap noImage = ((BitmapDrawable) myDrawable).getBitmap();
            item.setDiaryImage (bitmapToBase64 (noImage));
        }
        // Insert the new item
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    final ToDoItem entity = addItemInTable(item);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(!entity.isComplete()){
                                mAdapter.add(entity);
                            }
                        }
                    });
                } catch (final Exception e) {
                    createAndShowDialogFromTask(e, "Error");
                }
                return null;
            }
        };

        runAsyncTask(task);
        //Intent intent=new Intent (this,MainActivity.class);
       // startActivity (intent);
    }
    /**
     * Add an item to the Mobile Service Table
     *
     * @param item
     *            The item to Add
     */public ToDoItem addItemInTable(ToDoItem item) throws ExecutionException, InterruptedException {
        ToDoItem entity = mToDoTable.insert(item).get();
        return entity;
    }
    /**
     * Mark an item as completed
     *
     * @param item
     *            The item to mark
     */public void checkItem(final ToDoItem item) {
        if (mClient == null) {
            return;
        }

        // Set the item as completed and update it in the table
        item.setComplete(true);

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                try {

                    checkItemInTable(item);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (item.isComplete()) {
                                mAdapter.remove(item);
                            }
                        }
                    });
                } catch (final Exception e) {
                    createAndShowDialogFromTask(e, "Error");
                }

                return null;
            }
        };
        runAsyncTask(task);
    }
    /**
     * Mark an item as completed in the Mobile Service Table
     *
     * @param item
     *            The item to mark
     */public void checkItemInTable(ToDoItem item) throws ExecutionException, InterruptedException {
        mToDoTable.update(item).get();
    }
    /**
     * Refresh the list with the items in the Table
     */private void refreshItemsFromTable() {
        // Get the items that weren't marked as completed and add them in the
        // adapter
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    final List<ToDoItem> results = refreshItemsFromMobileServiceTable();
                    //Offline Sync
                    //final List<ToDoItem> results = refreshItemsFromMobileServiceTableSyncTable();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mAdapter.clear();
                            for (ToDoItem item : results) {
                                mAdapter.add(item);
                            }
                        }
                    });
                } catch (final Exception e){
                    createAndShowDialogFromTask(e, "Error");
                }
                return null;
            }
        };
        runAsyncTask(task);
    }
    /**
     * Refresh the list with the items in the Mobile Service Table
     */public List<ToDoItem> refreshItemsFromMobileServiceTable() throws ExecutionException, InterruptedException {
        return mToDoTable.where().field("userId").eq(val(user.facebookID)).execute().get() ;
                 //mToDoTable.where().field("complete").eq(val(false)).execute().get();
    }//İD SINIRLAMASINI BURAYA YAZ
    /**
     * Initialize local storage
     * @return
     * @throws MobileServiceLocalStoreException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private AsyncTask<Void, Void, Void> initLocalStore() throws MobileServiceLocalStoreException, ExecutionException, InterruptedException {

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {

                    MobileServiceSyncContext syncContext = mClient.getSyncContext();

                    if (syncContext.isInitialized())
                        return null;

                    SQLiteLocalStore localStore = new SQLiteLocalStore(mClient.getContext(), "OfflineStore", null, 1);

                    Map<String, ColumnDataType> tableDefinition = new HashMap<String, ColumnDataType>();
                    tableDefinition.put("id", ColumnDataType.String);
                    tableDefinition.put("text", ColumnDataType.String);
                    tableDefinition.put("complete", ColumnDataType.Boolean);
                    tableDefinition.put("state", ColumnDataType.String);
                    tableDefinition.put("diary", ColumnDataType.String);
                    tableDefinition.put("userId", ColumnDataType.String);
                    tableDefinition.put("diaryImage", ColumnDataType.String);

                    localStore.defineTable("ToDoItem", tableDefinition);
                    SimpleSyncHandler handler = new SimpleSyncHandler();
                    syncContext.initialize(localStore, handler).get();

                } catch (final Exception e) {
                    createAndShowDialogFromTask(e, "Error");
                }

                return null;
            }
        };

        return runAsyncTask(task);

    }
    //Offline Sync
    /**
     * Sync the current context and the Mobile Service Sync Table
     * @return
     */
    /**
     * Creates a dialog and shows it
     *
     * @param exception
     *            The exception to show in the dialog
     * @param title
     *            The dialog title
     */
    private void createAndShowDialogFromTask(final Exception exception, String title) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                createAndShowDialog(exception, "Error");
            }
        });
    }
    /**
     * Creates a dialog and shows it
     *
     * @param exception
     *            The exception to show in the dialog
     * @param title
     *            The dialog title
     */
    private void createAndShowDialog(Exception exception, String title) {
        Throwable ex = exception;
        if(exception.getCause() != null){
            ex = exception.getCause();
        }
        createAndShowDialog(ex.getMessage(), title);
    }
    /**
     * Creates a dialog and shows it
     *
     * @param message
     *            The dialog message
     * @param title
     *            The dialog title
     */
    private void createAndShowDialog(final String message, final String title) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(message);
        builder.setTitle(title);
        builder.create().show();
    }
    private AsyncTask<Void, Void, Void> runAsyncTask(AsyncTask<Void, Void, Void> task) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            return task.execute();
        }
    }
    private class ProgressFilter implements ServiceFilter {

        @Override
        public ListenableFuture<ServiceFilterResponse> handleRequest(ServiceFilterRequest request, NextServiceFilterCallback nextServiceFilterCallback) {

            final SettableFuture<ServiceFilterResponse> resultFuture = SettableFuture.create();


            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (mProgressBar != null) mProgressBar.setVisibility(ProgressBar.VISIBLE);
                }
            });

            ListenableFuture<ServiceFilterResponse> future = nextServiceFilterCallback.onNext(request);

            Futures.addCallback(future, new FutureCallback<ServiceFilterResponse>() {
                @Override
                public void onFailure(Throwable e) {
                    resultFuture.setException(e);
                }

                @Override
                public void onSuccess(ServiceFilterResponse response) {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            if (mProgressBar != null) mProgressBar.setVisibility(ProgressBar.GONE);

                        }
                    });

                    resultFuture.set(response);

                }
            });

            return resultFuture;
        }
    }
    //Offline Sync
    /**
     * Refresh the list with the items in the Mobile Service Sync Table
     */
    /*private List<ToDoItem> refreshItemsFromMobileServiceTableSyncTable() throws ExecutionException, InterruptedException {
        //sync the data
        sync().get();
        Query query = QueryOperations.field("complete").
                eq(val(false));
        return mToDoTable.read(query).get();
    }*/
        /*
    private AsyncTask<Void, Void, Void> sync() {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    MobileServiceSyncContext syncContext = mClient.getSyncContext();
                    syncContext.push().get();
                    mToDoTable.pull(null).get();
                } catch (final Exception e) {
                    createAndShowDialogFromTask(e, "Error");
                }
                return null;
            }
        };
        return runAsyncTask(task);
    }
    */
    private String bitmapToBase64(Bitmap bitmap) {           // RESMİ ŞİFRELEME
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
}