/*
 * Copyright (C) 2010 Thomas G. Kenny Jr
 *
 * Licensed under the GNU General Public License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 *
 *      http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

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

public class MythMotePreferences extends PreferenceActivity {

	public static final int NEW_LOCATION_ID = Menu.FIRST;
	public static final int DELETE_LOCATION_ID = Menu.FIRST + 1;
	public static final String MYTHMOTE_SHARED_PREFERENCES_ID = "mythmote.preferences";
	public static final String PREF_SELECTED_LOCATION = "selected-frontend";
	public static final String PREF_HAPTIC_FEEDBACK_ENABLED = "haptic-feedback-enabled";
	public static final String PREF_KEYBINDINGS_EDITABLE = "keybindings-editable";
	public static final String PREF_STATUS_UPDATE_INTERVAL = "status-update-interval";
	public static final String PREF_SHOW_DONATE_MENU_ITEM = "show-donate-menu-item";
	public static final int REQUEST_LOCATIONEDITOR = 0;

	private static int sIdIndex;
	private static int sAddressIndex;
	private static int sNameIndex;
	private static int sPortIndex;
	private static int sMacIndex;
	private static int sWifiOnlyIndex;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// set shared preference name
		this.getPreferenceManager().setSharedPreferencesName(
				MYTHMOTE_SHARED_PREFERENCES_ID);
	}

	@Override
	public void onResume() {
		super.onResume();

		// configure all preferences
		setupPreferences(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		menu.add(0, NEW_LOCATION_ID, 0, R.string.add_location_str).setIcon(
				R.drawable.menu_add);
		menu.add(0, DELETE_LOCATION_ID, 0, R.string.delete_location_str)
				.setIcon(R.drawable.menu_close_clear_cancel);
		return result;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final PreferenceActivity context = this;

		if (item.getItemId() == NEW_LOCATION_ID) {
			showLocationEditDialog(context, null);
		} else if (item.getItemId() == DELETE_LOCATION_ID) {
			showDeleteLocationList(context);
		}
		return true;
	}

	private static void setupPreferences(PreferenceActivity context) {
		// create Categories
		PreferenceScreen prefScreen = context.getPreferenceManager()
				.createPreferenceScreen(context);
		prefScreen.removeAll();

		//selected location category
		PreferenceCategory selectedCat = new PreferenceCategory(context);
		selectedCat.setTitle(R.string.selected_location_str);
		//location list category
		PreferenceCategory locationListCat = new PreferenceCategory(context);
		locationListCat.setTitle(R.string.location_list_str);
		//general category
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

		// create mythfrontend update status interval preference
		generalCat.addPreference(createIntListPreference(context,
				PREF_STATUS_UPDATE_INTERVAL,
				R.string.status_update_interval_str,
				R.string.status_update_interval_description_str,
				R.array.status_Update_Interval_strings,
				R.array.status_Update_Interval_values, "0"));

		// create haptic feedback shared preference
		generalCat.addPreference(createCheckBox(context,
				PREF_HAPTIC_FEEDBACK_ENABLED,
				R.string.haptic_feedback_enabled_str,
				R.string.haptic_feedback_enabled_description_str, false));
		
		// createkaybinding editing enabled
		generalCat.addPreference(createCheckBox(context,
				PREF_KEYBINDINGS_EDITABLE,
				R.string.keybindings_editable_str,
				R.string.keybindings_editable_descriptions_str, true));
		
		// create donate button visible checkbox
		generalCat.addPreference(createCheckBox(context,
				PREF_SHOW_DONATE_MENU_ITEM,
				R.string.show_donate_menu_item_str,
				R.string.show_donate_menu_item_str, true));

		// open DB
		MythMoteDbManager _dbAdapter = new MythMoteDbManager(context);
		_dbAdapter.open();

		// get list of locations
		Cursor cursor = _dbAdapter.fetchAllFrontendLocations();

		// get column indexes
		sIdIndex = cursor.getColumnIndex(MythMoteDbHelper.KEY_ROWID);
		sAddressIndex = cursor.getColumnIndex(MythMoteDbHelper.KEY_ADDRESS);
		sNameIndex = cursor.getColumnIndex(MythMoteDbHelper.KEY_NAME);
		sPortIndex = cursor.getColumnIndex(MythMoteDbHelper.KEY_PORT);
		sMacIndex = cursor.getColumnIndex(MythMoteDbHelper.KEY_MAC);
		sWifiOnlyIndex = cursor.getColumnIndex(MythMoteDbHelper.KEY_WIFIONLY);

		// determine if we have locations saved
		int count = cursor.getCount();
		if (count > 0 && cursor.moveToFirst()) {
			// get selected frontend id
			int selected = context.getSharedPreferences(
					MYTHMOTE_SHARED_PREFERENCES_ID, MODE_PRIVATE).getInt(
					MythMotePreferences.PREF_SELECTED_LOCATION, -1);

			// if selected failed
			if (selected == -1) {
				// set to first in list
				selected = cursor.getInt(sIdIndex);
				// save (defaulted) selected location
				SaveSelectedLocationId(context, selected);
			}

			// put each location in the preference list
			for (int i = 0; i < count; i++) {
				locationListCat.addPreference(MythMotePreferences
						.createLocationPreference(context,
								cursor.getString(sIdIndex),
								cursor.getString(sNameIndex),
								cursor.getString(sAddressIndex)));

				if (cursor.getInt(sIdIndex) == selected) {
					// create preference for selected location
					selectedCat.addPreference(MythMotePreferences
							.createSelectedLocationPreference(context, context
									.getString(R.string.selected_location_str),
									cursor.getString(sNameIndex)));
				}

				cursor.moveToNext();
			}

			// the saved selected location was not found just pick the first one
			if (selectedCat.getPreferenceCount() <= 0) {
				cursor.moveToFirst();
				selectedCat.addPreference(MythMotePreferences
						.createSelectedLocationPreference(context, context
								.getString(R.string.selected_location_str),
								cursor.getString(sNameIndex)));

				// save location ID so that it is for real
				SaveSelectedLocationId(context, cursor.getInt(sIdIndex));
			}
		} else {
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

	private static void showLocationEditDialog(Context context,
			FrontendLocation location) {
		Intent intent = new Intent(context,
				tkj.android.homecontrol.mythmote.LocationEditor.class);

		// put extra information is needed
		if (location != null) {
			intent.putExtra(FrontendLocation.STR_ID, location.ID);
			intent.putExtra(FrontendLocation.STR_NAME, location.Name);
			intent.putExtra(FrontendLocation.STR_ADDRESS, location.Address);
			intent.putExtra(FrontendLocation.STR_PORT, location.Port);
			intent.putExtra(FrontendLocation.STR_MAC, location.MAC);
			intent.putExtra(FrontendLocation.STR_WIFIONLY, location.WifiOnly);
		}

		// start activity
		context.startActivity(intent);
	}

	private static void showDeleteLocationList(final Activity context) {
		final MythMoteDbManager _dbAdapter = new MythMoteDbManager(context);
		_dbAdapter.open();
		final Cursor cursor = _dbAdapter.fetchAllFrontendLocations();

		int count = cursor.getCount();
		if (count > 0 && cursor.moveToFirst()) {
			final String[] names = new String[count];
			final int[] ids = new int[count];
			for (int i = 0; i < count; i++) {
				names[i] = cursor.getString(cursor
						.getColumnIndex(MythMoteDbHelper.KEY_NAME));
				ids[i] = cursor.getInt(cursor
						.getColumnIndex(MythMoteDbHelper.KEY_ROWID));
				cursor.moveToNext();
			}

			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setTitle(R.string.delete_location_str);
			builder.setItems(names, new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {
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
			String key, int title, int summary, Object defaultValue) {
		CheckBoxPreference pref = new CheckBoxPreference(context);
		pref.setKey(key);
		pref.setDefaultValue(defaultValue);
		pref.setTitle(title);
		pref.setSummary(summary);
		pref.setPersistent(true);
		return pref;
	}

	private static IntegerListPreference createIntListPreference(
			Context context, String key, int titleID, int summaryID,
			int entrysID, int valuesID, Object defaultVal) {
		IntegerListPreference pref = new IntegerListPreference(context);
		pref.setKey(key);
		pref.setTitle(titleID);
		pref.setSummary(summaryID);
		pref.setEntries(entrysID);
		pref.setEntryValues(valuesID);
		pref.setDefaultValue(defaultVal);

		return pref;
	}

	private static Preference createLocationPreference(final Activity context,
			String key, String name, String value) {
		Preference pref = new Preference(context);
		pref.setKey(key);
		pref.setTitle(name);
		pref.setDefaultValue(value);
		pref.setEnabled(true);
		pref.setSummary(value);
		pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				// Open location edit dialog with a location loaded
				FrontendLocation location = new FrontendLocation();

				location.ID = Integer.parseInt(preference.getKey());

				MythMoteDbManager dbAdapter = new MythMoteDbManager(context);
				dbAdapter.open();
				Cursor cursor = dbAdapter.fetchFrontendLocation(location.ID);

				if (cursor != null && cursor.getCount() > 0) {
					// get column indexes
					sIdIndex = cursor.getColumnIndex(MythMoteDbHelper.KEY_ROWID);
					sAddressIndex = cursor
					.getColumnIndex(MythMoteDbHelper.KEY_ADDRESS);
					sNameIndex = cursor.getColumnIndex(MythMoteDbHelper.KEY_NAME);
					sPortIndex = cursor.getColumnIndex(MythMoteDbHelper.KEY_PORT);
					sMacIndex = cursor.getColumnIndex(MythMoteDbHelper.KEY_MAC);
					sWifiOnlyIndex = cursor.getColumnIndex(MythMoteDbHelper.KEY_WIFIONLY);
					

					//get data
					location.Name = cursor.getString(sNameIndex);
					location.Address = cursor.getString(sAddressIndex);
					location.Port = cursor.getInt(sPortIndex);
					location.MAC = cursor.getString(sMacIndex);
					location.WifiOnly = cursor.getInt(sWifiOnlyIndex);

					//show location editor
					showLocationEditDialog(context, location);
					
					//close cursor
					cursor.close();
				}
				
				//close db adapter
				dbAdapter.close();
				return false;
			}

		});
		return pref;
	}

	private static Preference createAddLocationPreference(
			final Activity context, String name, String value) {
		Preference pref = new Preference(context);
		pref.setKey(name);
		pref.setTitle(name);
		pref.setDefaultValue(value);
		pref.setEnabled(true);
		pref.setSummary(value);
		pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {

				showLocationEditDialog(context, null);

				return false;
			}

		});
		return pref;
	}

	private static Preference createDeleteLocationPreference(
			final Activity context, String name, String value) {
		Preference pref = new Preference(context);
		pref.setKey(name);
		pref.setTitle(name);
		pref.setDefaultValue(value);
		pref.setEnabled(true);
		pref.setSummary(value);
		pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				showDeleteLocationList(context);
				return true;
			}

		});
		return pref;
	}

	private static Preference createSelectedLocationPreference(
			final PreferenceActivity context, String name, String value) {
		Preference pref = new Preference(context);
		pref.setKey(name);
		pref.setTitle(name);
		pref.setDefaultValue(value);
		pref.setEnabled(true);
		pref.setSummary(value);
		pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {

				// Displays the list of configured frontend locations.
				// Fires the locationChanged event when the user selects a
				// location even if the user selects the same location already 
				// selected.
				SelectLocation(context, new LocationChangedEventListener() {
					public void LocationChanged() {
						// reset preference list with updated selection
						setupPreferences(context);
					}

				});
				return true;
			}
		});
		return pref;
	}

	public static void SelectLocation(final Activity context,
			final LocationChangedEventListener listener) {
		MythMoteDbManager _dbAdapter = new MythMoteDbManager(context);
		_dbAdapter.open();
		final Cursor cursor = _dbAdapter.fetchAllFrontendLocations();

		int count = cursor.getCount();
		if (count > 0 && cursor.moveToFirst()) {
			final String[] names = new String[count];
			final int[] ids = new int[count];
			for (int i = 0; i < count; i++) {
				names[i] = cursor.getString(cursor
						.getColumnIndex(MythMoteDbHelper.KEY_NAME));
				ids[i] = cursor.getInt(cursor
						.getColumnIndex(MythMoteDbHelper.KEY_ROWID));
				cursor.moveToNext();
			}

			// show list of locations as a single selected list
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setTitle(R.string.select_location_str);
			builder.setItems(names, new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {

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

	private static void SaveSelectedLocationId(Activity context, int id) {
		SharedPreferences settings = context.getSharedPreferences(
				MYTHMOTE_SHARED_PREFERENCES_ID, MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(MythMotePreferences.PREF_SELECTED_LOCATION, id);
		editor.commit();
	}

}