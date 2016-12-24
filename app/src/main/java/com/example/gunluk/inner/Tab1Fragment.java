package com.example.gunluk.inner;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.gunluk.R;
import com.example.gunluk.activities.DiaryDetail;
import com.example.gunluk.activities.ToDoActivity;
import com.example.gunluk.adapters.ToDoItemAdapter;
import com.example.gunluk.models.ToDoItem;
import com.example.gunluk.models.UserModel;
import com.example.gunluk.utils.PrefUtils;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.http.OkHttpClientFactory;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.MobileServiceLocalStoreException;
import com.squareup.okhttp.OkHttpClient;

import java.net.MalformedURLException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.microsoft.windowsazure.mobileservices.table.query.QueryOperations.val;
import static java.lang.System.out;

public class Tab1Fragment extends Fragment {
    private MobileServiceClient mClient;
    private MobileServiceTable<ToDoItem> mToDoTable;
    private ToDoItemAdapter mAdapter;
    String userId;
    int pos;
    final UserModel user = PrefUtils.getCurrentUser (getContext ());
    public Tab1Fragment() {
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate (R.layout.tab1fragment, container, false);
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated (savedInstanceState);
        ImageView add = (ImageView) getActivity ( ).findViewById (R.id.add);
        userId=user.facebookID;
        add.setOnClickListener (new View.OnClickListener ( ) {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent (getActivity ( ), ToDoActivity.class);
                startActivity (intent);
            }

        });
        try {
            // Create the Mobile Service Client instance, using the provided
            // Mobile Service URL and key
            mClient = new MobileServiceClient (
                    "https://gunluk.azurewebsites.net",getActivity ());
            // Extend timeout from default of 10s to 20s
            mClient.setAndroidHttpClientFactory(new OkHttpClientFactory () {
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
            // Create an adapter to bind the items with the view
            mAdapter = new ToDoItemAdapter (getActivity (), R.layout.row_list_to_do);
            final ListView listViewToDo = (ListView)getActivity ().findViewById(R.id.listViewToDo);
            listViewToDo.setAdapter(mAdapter);
            listViewToDo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                          public void onItemClick (AdapterView<?> parent, View view, int position, long id) {
                                              Intent intent = new Intent(getActivity(), DiaryDetail.class);
                                              ToDoItem item =  mAdapter.getItem(position);
                                              pos=position;
                                              assert item != null;
                                              out.println("Diary: "+item.getDiary ());
                                              out.println("DiarytTitle: "+item.getText ());
                                              out.println("DiaryDate: "+item.getCreatedAt ());
                                              out.println("ItemId: "+item.getId ());

                                              intent.putExtra ("diary",item.getDiary ());
                                              intent.putExtra ("diaryTitle",item.getText ());
                                              intent.putExtra ("diaryDate",item.getCreatedAt ().toString ());
                                              intent.putExtra ("itemId", item.getId ());
                                              intent.putExtra ("diaryImage",item.getDiaryImage ());
                                              intent.putExtra ("position", position);
                                              startActivityForResult(intent,position);
                                          }});
            refreshItemsFromTable();
        } catch (MalformedURLException e) {
            createAndShowDialog(new Exception("There was an error creating the Mobile Service. Verify the URL"), "Error");
        } catch (Exception e){
            createAndShowDialog(e, "Error");
        }
    }
    private Bitmap base64ToBitmap(String b64) { //ŞİFRELİ METNİ RESME DÖNÜŞTÜRME
        byte[] imageAsBytes = Base64.decode(b64.getBytes(), Base64.DEFAULT);
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        return BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length,options);
    }
    /**
     * Mark an item as completed
     *
     * @param item
     *            The item to mark
     */
    public void checkItem(final ToDoItem item) {
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
                    getActivity ().runOnUiThread(new Runnable() {
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
     */
    public void checkItemInTable(ToDoItem item) throws ExecutionException, InterruptedException {
        mToDoTable.update(item).get();
    }
    /**
     * Refresh the list with the items in the Table
     */
    public void refreshItemsFromTable() {

        // Get the items that weren't marked as completed and add them in the
        // adapter

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    final List<ToDoItem> results = refreshItemsFromMobileServiceTable();
                    //Offline Sync
                    //final List<ToDoItem> results = refreshItemsFromMobileServiceTableSyncTable();
                    getActivity ().runOnUiThread(new Runnable() {
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
     */
    private List<ToDoItem> refreshItemsFromMobileServiceTable() throws ExecutionException, InterruptedException {
            //List list = mToDoTable.where ( ).field ("complete").eq(val(false)).execute().get();
        return mToDoTable.where ().field ("userId").eq (val (userId)).and(mToDoTable.where ( ).field ("complete").eq(val(false))).execute().get();
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

    /**
     * Initialize local storage
     * @return
     * @throws MobileServiceLocalStoreException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    //Offline Sync
    /**
     * Sync the current context and the Mobile Service Sync Table
     * @return
     */
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

    /**
     * Creates a dialog and shows it
     *
     * @param exception
     *            The exception to show in the dialog
     * @param title
     *            The dialog title
     */private void createAndShowDialogFromTask(final Exception exception, String title) {
        getActivity ().runOnUiThread(new Runnable() {
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
     */private void createAndShowDialog(Exception exception, String title) {
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
     */private void createAndShowDialog(final String message, final String title) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity ());

        builder.setMessage(message);
        builder.setTitle(title);
        builder.create().show();
    }
    /**
     * Run an ASync task on the corresponding executor
     * @param task
     * @return
     */private AsyncTask<Void, Void, Void> runAsyncTask(AsyncTask<Void, Void, Void> task) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            return task.execute();
        }
    }
  }