package com.powereng.receiving;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.powereng.receiving.database.LogEntry;
import com.powereng.receiving.sync.SyncUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

public class MainScreenActivity extends Activity implements DialogInterface, ActionBar.OnNavigationListener {

    private AbsListView mListView;
    private DialogEditPackage editPackageFragment;
    private static String TAG = "MainScreenActivity";
    private String mSelection = "";
    public static final int DAY = 2;
    public static final int WEEK = 3;
    public static final int MONTH = 4;
    private LogViewAdapter mActionBarMenuSpinnerAdapter;
    private static final int BUTTON_DAY_INDEX = 0;
    private static final int BUTTON_WEEK_INDEX = 1;
    private static final int BUTTON_MONTH_INDEX = 2;
    private boolean mIsTabletConfig = true;
    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private CursorAdapter mAdapter;
    private ActionBar mActionBar;
    Boolean mShowing = false;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_log_list);

        SyncUtils.CreateSyncAccount(this);

        mActionBar = getActionBar();
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        //setting up the edit fragment to retain data on configuration change
        FragmentManager fm = getFragmentManager();
        editPackageFragment = (DialogEditPackage) fm.findFragmentByTag("editPackage");

        if (editPackageFragment != null) {
            LogEntry entry = editPackageFragment.getEntry();
            editPackageFragment.dismissAllowingStateLoss();
            dialogEditPackage(entry);

        }

        //SpinnerAdapter yodawg = ArrayAdapter.createFromResource(this, R.array.buttons_list,
         //       android.R.layout.simple_spinner_dropdown_item);
        //mActionBar.setListNavigationCallbacks(yodawg, mListener);
        configureActionBar(DAY);
        mAdapter = new SimpleCursorAdapter(this,
                R.layout.list_item, null,
                new String[] {LogEntry.COL_TRACKING, LogEntry.COL_CARRIER,
                        LogEntry.COL_SENDER, LogEntry.COL_RECIPIENT, LogEntry.COL_NUMPACKAGES,
                        LogEntry.COL_PONUM, LogEntry.COL_SIG, LogEntry.COL_TIMESTAMP},
                new int[] { R.id.tracking, R.id.carrier, R.id.sender,
                        R.id.recipient, R.id.numpackages, R.id.ponum, R.id.signature }, 0);

        // Set the adapter
        mListView = (AbsListView) findViewById(android.R.id.list);
        mListView.setAdapter(mAdapter);

        mListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int position, long id) {
                final LogEntry logEntry = new LogEntry((Cursor) mAdapter
                        .getItem(position));
                dialogEditPackage(logEntry);

            }
        });

        mListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

            HashMap<Long, LogEntry> entries = new HashMap<Long, LogEntry>();

            @Override
            public void onItemCheckedStateChanged(ActionMode mode,
                                                  int position, long id, boolean checked) {
                // Here you can do something when items are
                // selected/de-selected,
                // such as update the title in the CAB
                if (checked) {

                    entries.put(id,
                            new LogEntry((Cursor) mAdapter.getItem(position)));
                }
                else {
                    entries.remove(id);
                }
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                // Respond to clicks on the actions in the CAB
                switch (item.getItemId()) {
                    case R.id.action_delete:

                        deleteItems(entries.values());
                        mode.finish(); // Action picked, so close the CAB
                        return true;
                    case R.id.action_edit:
                        editItems(entries.values());
                        mode.finish();
                    default:
                        return false;
                }
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // Inflate the menu for the CAB
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.log_fragment_context, menu);
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                // Here you can make any necessary updates to the activity when
                // the CAB is removed. By default, selected items are
                // deselected/unchecked.
                entries.clear();
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                // Here you can perform updates to the CAB due to
                // an invalidate() request
                return false;
            }
        });

        // Load content
        getLoaderManager().initLoader(0, null, new LoaderManager.LoaderCallbacks<Cursor>() {

            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                return new CursorLoader(getApplicationContext(), LogEntry.URI(),
                        LogEntry.FIELDS, mSelection + LogEntry.COL_SYNC_STATUS + " IS NOT 3", null,
                        LogEntry.COL_TIMESTAMP + " DESC");
            }

            @Override
            public void onLoadFinished(Loader<Cursor> arg0, Cursor c) {
                mAdapter.swapCursor(c);
            }

            @Override
            public void onLoaderReset(Loader<Cursor> arg0) {
                mAdapter.swapCursor(null);
            }
        });
    }



    private void configureActionBar(int viewType) {
        createButtonsSpinner(viewType);
       // if (mIsMultipane) {
        //    mActionBar.setDisplayOptions(
         //           ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME);
       // } else {
            mActionBar.setDisplayOptions(0);
       // }
    }

    private void createButtonsSpinner(int viewType) {
        // If tablet configuration , show spinner with no dates
        mActionBarMenuSpinnerAdapter = new LogViewAdapter (this, viewType, true);
        mActionBar = getActionBar();
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        mActionBar.setListNavigationCallbacks(mActionBarMenuSpinnerAdapter, this);
        switch (viewType) {
            case DAY:
                mActionBar.setSelectedNavigationItem(BUTTON_DAY_INDEX);
                break;
            case WEEK:
                mActionBar.setSelectedNavigationItem(BUTTON_WEEK_INDEX);
                break;
            case MONTH:
                mActionBar.setSelectedNavigationItem(BUTTON_MONTH_INDEX);
                break;
            default:
                mActionBar.setSelectedNavigationItem(BUTTON_DAY_INDEX);
                break;
        }
    }



    public void dateView(){

        //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        Log.i(TAG, "" + c.get(Calendar.DAY_OF_MONTH));
        Log.i(TAG, "" + c.get(Calendar.DAY_OF_WEEK));
        int monday = c.get(Calendar.DAY_OF_MONTH) - c.get(Calendar.DAY_OF_WEEK)+2;
        int friday = monday + 5;
        DateFormat sdf = SimpleDateFormat.getInstance();



        c.set(Calendar.DAY_OF_MONTH, monday);

        Log.i(TAG, "Date "+c.getTime());
    }



    @Override
    protected void onDestroy() {

        Log.i("MAIN SCREEN ACTIVITY", " called onDestroy");

        super.onDestroy();
    }

    public void dialogEditPackage(LogEntry entry) {
        Log.d(TAG, "new edit dialog created");
        editPackageFragment = new DialogEditPackage();
        editPackageFragment.setLogEntry(entry);
        editPackageFragment.show(getFragmentManager(), "editPackage");
    }

    void deleteItems(Collection<LogEntry> entries) {
        for (LogEntry entry : entries) {
            this.getContentResolver().delete(entry.getUri(), null, null);
        }
    }

    void editItems(Collection<LogEntry> entries) {

        for (LogEntry entry : entries) {

            do {
                mShowing = true;
                dialogEditPackage(entry);

            } while (!mShowing);



        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.log_fragment, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void dismiss() {
        mShowing = false;
    }

    @Override
    public void cancel() {
        mShowing = false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean result = false;
        switch (item.getItemId()) {
            case R.id.action_add:
                showAddDialog();
                break;
            case R.id.action_sync:
                Toast.makeText(this, R.string.syncing_, Toast.LENGTH_SHORT).show();
                SyncUtils.TriggerRefresh();
                break;
        }
        return result;
    }

    void showAddDialog() {
        DialogFragment dialog = new DialogAddPackage();
        dialog.show(getFragmentManager(), "add_entry");
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
       /* switch (itemPosition) {
            case CalendarViewAdapter.DAY_BUTTON_INDEX:
                if (mCurrentView != ViewType.DAY) {
                    mController.sendEvent(this, EventType.GO_TO, null, null, -1, ViewType.DAY);
                }
                break;
            case CalendarViewAdapter.WEEK_BUTTON_INDEX:
                if (mCurrentView != ViewType.WEEK) {
                    mController.sendEvent(this, EventType.GO_TO, null, null, -1, ViewType.WEEK);
                }
                break;
            case CalendarViewAdapter.MONTH_BUTTON_INDEX:
                if (mCurrentView != ViewType.MONTH) {
                    mController.sendEvent(this, EventType.GO_TO, null, null, -1, ViewType.MONTH);
                }
                break;
            case CalendarViewAdapter.AGENDA_BUTTON_INDEX:
                if (mCurrentView != ViewType.AGENDA) {
                    mController.sendEvent(this, EventType.GO_TO, null, null, -1, ViewType.AGENDA);
                }
                break;
            default:
                Log.w(TAG, "ItemSelected event from unknown button: " + itemPosition);
                Log.w(TAG, "CurrentView:" + mCurrentView + " Button:" + itemPosition +
                        " Day:" + mDayTab + " Week:" + mWeekTab + " Month:" + mMonthTab +
                        " Agenda:" + mAgendaTab);
                break;
        }*/
        return false;
    }
}
