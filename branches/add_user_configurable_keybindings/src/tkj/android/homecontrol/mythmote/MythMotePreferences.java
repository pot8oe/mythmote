package tkj.android.homecontrol.mythmote;

import tkj.android.homecontrol.mythmote.db.MythMoteDbHelper;
import tkj.android.homecontrol.mythmote.db.MythMoteDbManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceClickListener;
import android.database.Cursor;
import android.view.Menu;
import android.view.MenuItem;

public class MythMotePreferences extends PreferenceActivity
{

	public static final int NEW_LOCATION_ID = Menu.FIRST;
	public static final int DELETE_LOCATION_ID = Menu.FIRST + 1;
	public static final String MYTHMOTE_SHARED_PREFERENCES_ID = "mythmote.preferences";
	public static final String PREF_SELECTED_LOCATION = "selected-frontend";
	public static final String PREF_HAPTIC_FEEDBACK_ENABLED = "haptic-feedback-enabled";
	public static final int REQUEST_LOCATIONEDITOR = 0;

	private static int _idIndex;
	private static int _addressIndex;
	private static int _nameIndex;
	private static int _portIndex;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// //create base preference screen
		// PreferenceScreen prefScreen =
		// this.getPreferenceManager().createPreferenceScreen(this);
		//
		// //configure all preferences
		// setupPreferences(prefScreen);
		//
		// //set preference screen
		// this.setPreferenceScreen(prefScreen);
		this.getPreferenceManager().setSharedPreferencesName(
				MYTHMOTE_SHARED_PREFERENCES_ID);
	}

	@Override
	public void onResume()
	{
		super.onResume();
		// configure all preferences
		setupPreferences(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		boolean result = super.onCreateOptionsMenu(menu);
		menu.add(0, NEW_LOCATION_ID, 0, R.string.add_location_str).setIcon(
				R.drawable.menu_add);
		menu.add(0, DELETE_LOCATION_ID, 0, R.string.delete_location_str)
				.setIcon(R.drawable.menu_close_clear_cancel);
		return result;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		final PreferenceActivity context = this;

		if (item.getItemId() == NEW_LOCATION_ID)
		{
			showLocationEditDialog(context, null);
		} else if (item.getItemId() == DELETE_LOCATION_ID)
		{
			showDeleteLocationList(context);
		}
		return true;
	}

	private static void setupPreferences(PreferenceActivity context)
	{
		// create Categories
		PreferenceScreen prefScreen = context.getPreferenceManager()
				.createPreferenceScreen(context);
		prefScreen.removeAll();

		PreferenceCategory selectedCat = new PreferenceCategory(context);
		selectedCat.setTitle(R.string.selected_location_str);
		PreferenceCategory locationListCat = new PreferenceCategory(context);
		locationListCat.setTitle(R.string.location_list_str);
		PreferenceCategory generalCat = new PreferenceCategory(context);
		generalCat.setTitle(R.string.general_preferences_str);

		// add categories to preference screen
		prefScreen.addPreference(selectedCat);
		prefScreen.addPreference(locationListCat);
		prefScreen.addPreference(generalCat);

		// Create add and delete location preferences and add to location list
		locationListCat.addPreference(createAddLocationPreference(context,
				context.getString(R.string.add_location_str),
				context.getString(R.string.add_location_description_str)));
		locationListCat.addPreference(createDeleteLocationPreference(context,
				context.getString(R.string.delete_location_str),
				context.getString(R.string.delete_location_description_str)));

		// read haptic feedback shared preference
		generalCat.addPreference(createCheckBox(context,
				PREF_HAPTIC_FEEDBACK_ENABLED,
				R.string.haptic_feedback_enabled_str,
				R.string.haptic_feedback_enabled_description_str, false));

		// open DB
		MythMoteDbManager _dbAdapter = new MythMoteDbManager(context);
		_dbAdapter.open();

		// get list of locations
		Cursor cursor = _dbAdapter.fetchAllFrontendLocations();

		// get column indexes
		_idIndex = cursor.getColumnIndex(MythMoteDbHelper.KEY_ROWID);
		_addressIndex = cursor.getColumnIndex(MythMoteDbHelper.KEY_ADDRESS);
		_nameIndex = cursor.getColumnIndex(MythMoteDbHelper.KEY_NAME);
		_portIndex = cursor.getColumnIndex(MythMoteDbHelper.KEY_PORT);

		// determine if we have locations saved
		int count = cursor.getCount();
		if (count > 0 && cursor.moveToFirst())
		{
			// get selected frontend id
			int selected = context.getSharedPreferences(
					MYTHMOTE_SHARED_PREFERENCES_ID, MODE_PRIVATE).getInt(
					MythMotePreferences.PREF_SELECTED_LOCATION, -1);

			// if selected failed
			if (selected == -1)
			{
				// set to first in list
				selected = cursor.getInt(_idIndex);
				// save (defaulted) selected location
				SaveSelectedLocationId(context, selected);
			}

			// put each location in the preference list
			for (int i = 0; i < count; i++)
			{
				locationListCat.addPreference(MythMotePreferences
						.createLocationPreference(context,
								cursor.getString(_idIndex),
								cursor.getString(_nameIndex),
								cursor.getString(_addressIndex)));

				if (cursor.getInt(_idIndex) == selected)
				{
					// create preference for selected location
					selectedCat.addPreference(MythMotePreferences
							.createSelectedLocationPreference(context, context
									.getString(R.string.selected_location_str),
									cursor.getString(_nameIndex)));
				}

				cursor.moveToNext();
			}

			// the saved selected location was not found just pick the first one
			if (selectedCat.getPreferenceCount() <= 0)
			{
				cursor.moveToFirst();
				selectedCat.addPreference(MythMotePreferences
						.createSelectedLocationPreference(context, context
								.getString(R.string.selected_location_str),
								cursor.getString(_nameIndex)));

				// save location ID so that it is for real
				SaveSelectedLocationId(context, cursor.getInt(_idIndex));
			}
		} else
		{
			selectedCat
					.addPreference(MythMotePreferences.createSelectedLocationPreference(
							context,
							context.getString(R.string.selected_location_str),
							context.getString(R.string.no_frontend_locations_defined_str)));
		}

		cursor.close();
		_dbAdapter.close();

		// set preference screen
		context.setPreferenceScreen(prefScreen);
	}

	private static void showLocationEditDialog(Activity context,
			FrontendLocation location)
	{
		Intent intent = new Intent(context,
				tkj.android.homecontrol.mythmote.LocationEditor.class);

		// put extra information is needed
		if (location != null)
		{
			intent.putExtra(FrontendLocation.STR_ID, location.ID);
			intent.putExtra(FrontendLocation.STR_NAME, location.Name);
			intent.putExtra(FrontendLocation.STR_ADDRESS, location.Address);
			intent.putExtra(FrontendLocation.STR_PORT, location.Port);
		}

		// start activity
		context.startActivity(intent);
	}

	private static void showDeleteLocationList(final Activity context)
	{
		final MythMoteDbManager _dbAdapter = new MythMoteDbManager(context);
		_dbAdapter.open();
		final Cursor cursor = _dbAdapter.fetchAllFrontendLocations();

		int count = cursor.getCount();
		if (count > 0 && cursor.moveToFirst())
		{
			final String[] names = new String[count];
			final int[] ids = new int[count];
			for (int i = 0; i < count; i++)
			{
				names[i] = cursor.getString(cursor
						.getColumnIndex(MythMoteDbHelper.KEY_NAME));
				ids[i] = cursor.getInt(cursor
						.getColumnIndex(MythMoteDbHelper.KEY_ROWID));
				cursor.moveToNext();
			}

			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setTitle(R.string.delete_location_str);
			builder.setItems(names, new DialogInterface.OnClickListener()
			{

				public void onClick(DialogInterface dialog, int which)
				{
					MythMoteDbManager dbAdapter = new MythMoteDbManager(context);
					dbAdapter.open();
					dbAdapter.deleteFrontendLocation(ids[which]);
					dbAdapter.close();

					setupPreferences((PreferenceActivity) context);
				}

			});
			builder.show();
		}
		cursor.close();
		_dbAdapter.close();
	}

	private static CheckBoxPreference createCheckBox(Context context,
			String key, int title, int summary, Object defaultValue)
	{
		CheckBoxPreference pref = new CheckBoxPreference(context);
		pref.setKey(key);
		pref.setDefaultValue(defaultValue);
		pref.setTitle(title);
		pref.setSummary(summary);
		pref.setPersistent(true);
		return pref;
	}

	private static Preference createLocationPreference(final Activity context,
			String key, String name, String value)
	{
		Preference pref = new Preference(context);
		pref.setKey(key);
		pref.setTitle(name);
		pref.setDefaultValue(value);
		pref.setEnabled(true);
		pref.setSummary(value);
		pref.setOnPreferenceClickListener(new OnPreferenceClickListener()
		{

			public boolean onPreferenceClick(Preference preference)
			{
				// Open location edit dialog with a location loaded
				FrontendLocation location = new FrontendLocation();

				location.ID = Integer.parseInt(preference.getKey());

				MythMoteDbManager dbAdapter = new MythMoteDbManager(context);
				dbAdapter.open();
				Cursor cursor = dbAdapter.fetchFrontendLocation(location.ID);

				// get column indexes
				_idIndex = cursor.getColumnIndex(MythMoteDbHelper.KEY_ROWID);
				_addressIndex = cursor
						.getColumnIndex(MythMoteDbHelper.KEY_ADDRESS);
				_nameIndex = cursor.getColumnIndex(MythMoteDbHelper.KEY_NAME);
				_portIndex = cursor.getColumnIndex(MythMoteDbHelper.KEY_PORT);

				if (cursor != null && cursor.getCount() > 0)
				{
					location.Name = cursor.getString(_nameIndex);
					location.Address = cursor.getString(_addressIndex);
					location.Port = cursor.getInt(_portIndex);
					showLocationEditDialog(context, location);
				}
				return false;
			}

		});
		return pref;
	}

	private static Preference createAddLocationPreference(
			final Activity context, String name, String value)
	{
		Preference pref = new Preference(context);
		pref.setKey(name);
		pref.setTitle(name);
		pref.setDefaultValue(value);
		pref.setEnabled(true);
		pref.setSummary(value);
		pref.setOnPreferenceClickListener(new OnPreferenceClickListener()
		{

			public boolean onPreferenceClick(Preference preference)
			{

				showLocationEditDialog(context, null);

				return false;
			}

		});
		return pref;
	}

	private static Preference createDeleteLocationPreference(
			final Activity context, String name, String value)
	{
		Preference pref = new Preference(context);
		pref.setKey(name);
		pref.setTitle(name);
		pref.setDefaultValue(value);
		pref.setEnabled(true);
		pref.setSummary(value);
		pref.setOnPreferenceClickListener(new OnPreferenceClickListener()
		{

			public boolean onPreferenceClick(Preference preference)
			{
				showDeleteLocationList(context);
				return false;
			}

		});
		return pref;
	}

	private static Preference createSelectedLocationPreference(
			final PreferenceActivity context, String name, String value)
	{
		Preference pref = new Preference(context);
		pref.setKey(name);
		pref.setTitle(name);
		pref.setDefaultValue(value);
		pref.setEnabled(true);
		pref.setSummary(value);
		pref.setOnPreferenceClickListener(new OnPreferenceClickListener()
		{

			public boolean onPreferenceClick(Preference preference)
			{

				// Displays the list of configured frontend locations.
				// Fires the locationChanged event when the user selects a
				// location
				// even if the user selects the same location already selected.
				SelectLocation(context, new LocationChangedEventListener()
				{
					public void LocationChanged()
					{
						// reset preference list with updated selection
						setupPreferences(context);
					}

				});
				return false;
			}
		});
		return pref;
	}

	public static void SelectLocation(final Activity context,
			final LocationChangedEventListener listener)
	{
		MythMoteDbManager _dbAdapter = new MythMoteDbManager(context);
		_dbAdapter.open();
		final Cursor cursor = _dbAdapter.fetchAllFrontendLocations();

		int count = cursor.getCount();
		if (count > 0 && cursor.moveToFirst())
		{
			final String[] names = new String[count];
			final int[] ids = new int[count];
			for (int i = 0; i < count; i++)
			{
				names[i] = cursor.getString(cursor
						.getColumnIndex(MythMoteDbHelper.KEY_NAME));
				ids[i] = cursor.getInt(cursor
						.getColumnIndex(MythMoteDbHelper.KEY_ROWID));
				cursor.moveToNext();
			}

			// show list of locations as a single selected list
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setTitle(R.string.select_location_str);
			builder.setItems(names, new DialogInterface.OnClickListener()
			{

				public void onClick(DialogInterface dialog, int which)
				{

					// save selected location
					SaveSelectedLocationId(context, ids[which]);

					// notify that we selected a location
					listener.LocationChanged();
				}
			});
			builder.show();
		}
		cursor.close();
		_dbAdapter.close();
	}

	private static void SaveSelectedLocationId(Activity context, int id)
	{
		SharedPreferences settings = context.getSharedPreferences(
				MYTHMOTE_SHARED_PREFERENCES_ID, MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(MythMotePreferences.PREF_SELECTED_LOCATION, id);
		editor.commit();
	}

}