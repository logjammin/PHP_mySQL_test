package com.powereng.receiving;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.powereng.receiving.database.LogEntry;
import com.powereng.receiving.sync.SyncUtils;

import java.util.Collection;
import java.util.HashMap;



public class LogViewFragment extends Fragment {

	/**
	 * The fragment's ListView/GridView.
	 */
	private AbsListView mListView;

	/**
	 * The Adapter which will be used to populate the ListView/GridView with
	 * Views.
	 */
	private CursorAdapter mAdapter;
    private Menu mOptionsMenu;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public LogViewFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		 setHasOptionsMenu(true);


		mAdapter = new SimpleCursorAdapter(getActivity(),
				R.layout.list_item, null,
				new String[] {LogEntry.COL_TRACKING, LogEntry.COL_CARRIER,
                LogEntry.COL_SENDER, LogEntry.COL_RECIPIENT, LogEntry.COL_NUMPACKAGES,
                LogEntry.COL_PONUM, LogEntry.COL_SIG, LogEntry.COL_TIMESTAMP},
				new int[] { R.id.tracking, R.id.carrier, R.id.sender,
                        R.id.recipient, R.id.numpackages, R.id.ponum, R.id.signature }, 0);
	}

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
       // menu.clear();
        super.onPrepareOptionsMenu(menu);
    }

    @Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.log_fragment, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_log_list, container, false);

		// Set the adapter
		mListView = (AbsListView) view.findViewById(android.R.id.list);
		mListView.setAdapter(mAdapter);

		mListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
		// Set OnItemClickListener so we can be notified on item clicks
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long id) {
				 final LogEntry logEntry = new LogEntry((Cursor) mAdapter
						.getItem(position));

                DialogFragment dialog = new DialogEditPackage(logEntry);
                dialog.show(getFragmentManager(), "edit_entry");

			}
		});

		mListView.setMultiChoiceModeListener(new MultiChoiceModeListener() {

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
				return new CursorLoader(getActivity(), LogEntry.URI(),
						LogEntry.FIELDS, LogEntry.COL_SYNC_STATUS + " IS NOT 3", null,
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

		return view;
	}

	void deleteItems(Collection<LogEntry> entries) {
		for (LogEntry entry : entries) {
			getActivity().getContentResolver().delete(entry.getUri(), null, null);
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
        SyncUtils.CreateSyncAccount(activity);
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean result = false;
		switch (item.getItemId()) {
		case R.id.action_add:
			showAddDialog();
			break;
		case R.id.action_sync:


				Toast.makeText(getActivity(), R.string.syncing_, Toast.LENGTH_SHORT).show();
				SyncUtils.TriggerRefresh();


			break;
		}
		return result;
	}



	void showAddDialog() {
		DialogFragment dialog = new DialogAddPackage();
		dialog.show(getFragmentManager(), "add_entry");
	}

}
