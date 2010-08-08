package tkj.android.homecontrol.mythmote;

import tkj.android.homecontrol.mythmote.LocationChangedEventListener;
import tkj.android.homecontrol.mythmote.db.MythMoteDbHelper;
import tkj.android.homecontrol.mythmote.db.MythMoteDbManager;
import tkj.android.homecontrol.mythmote.keymanager.KeyBindingEntry;
import tkj.android.homecontrol.mythmote.keymanager.KeyBindingManager;
import tkj.android.homecontrol.mythmote.keymanager.KeyMapBinder;
import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

public class MythMote extends TabActivity implements TabHost.TabContentFactory,
		OnTabChangeListener, LocationChangedEventListener,
		MythCom.StatusChangedEventListener, KeyMapBinder {

	public static final int SETTINGS_ID = Menu.FIRST;
	public static final int RECONNECT_ID = Menu.FIRST + 1;
	public static final int SELECTLOCATION_ID = Menu.FIRST + 2;
	public static final String NAME_NAV_TAB = "TabNavigation";
	public static final String NAME_MEDIA_TAB = "TabNMediaControl";
	public static final String NAME_NUMPAD_TAB = "TabNumberPad";
	
	private static final String KEY_VOLUME_DOWN = "[";
	private static final String KEY_VOLUME_UP = "]";
	
	private KeyBindingManager keyManager;

	private static TabHost _tabHost;
	private static MythCom _comm;
	private static FrontendLocation _location = new FrontendLocation();
	private static int selected = -1;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// create comm class
		_comm = new MythCom(this);

		// set status changed event handler
		_comm.SetOnStatusChangeHandler(this);
		
		keyManager = new KeyBindingManager(this, this, _comm);
		keyManager.loadKeys();

		// create tab UI
		_tabHost = getTabHost();
		_tabHost.addTab(_tabHost
				.newTabSpec(NAME_NAV_TAB)
				.setIndicator(this.getString(R.string.navigation_str),
						this.getResources().getDrawable(R.drawable.starsmall))
				.setContent(this));
		_tabHost.addTab(_tabHost
				.newTabSpec(NAME_MEDIA_TAB)
				.setIndicator(this.getString(R.string.media_str),
						this.getResources().getDrawable(R.drawable.media))
				.setContent(this));
		_tabHost.addTab(_tabHost
				.newTabSpec(NAME_NUMPAD_TAB)
				.setIndicator(this.getString(R.string.numpad_str),
						this.getResources().getDrawable(R.drawable.numberpad))
				.setContent(this));

		// setup on tab change event
		_tabHost.setOnTabChangedListener(this);

		// register ourselves as a "media"
		//this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

		// set navigation tab and setup events
		_tabHost.setCurrentTab(0);
		keyManager.loadKeys();
	}

	/** Called when the activity is resumed **/
	@Override
	public void onResume() {
		super.onResume();

		// connect to saved location
		connectToSelectedLocation();
	}

	/** Called when the activity is paused **/
	@Override
	public void onPause() {
		super.onPause();

		// disconnect from frontend
		_comm.Disconnect();

	}

	/**
	 * Called when device configuration changes occur. Configuration changes
	 * that cause this function to be called must be registered in
	 * AndroidManifest.xml
	 **/
	public void onConfigurationChanged(Configuration config) {
		super.onConfigurationChanged(config);
	}

	/** Called to create the options menu once. **/
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);

		// create settings menu item
		menu.add(0, SETTINGS_ID, 0, R.string.settings_menu_str).setIcon(
				R.drawable.settings);

		// create reconnect menu item
		menu.add(0, RECONNECT_ID, 0, R.string.reconnect_str).setIcon(
				R.drawable.menu_refresh);

		// create select location menu item
		menu.add(0, SELECTLOCATION_ID, 0, R.string.selected_location_str)
				.setIcon(R.drawable.home);

		// return results
		return result;
	}

	/** Called when a menu item is selected **/
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		try {
			// Check which menu item was selected
			switch (item.getItemId()) {
			case SETTINGS_ID:
				// Create mythmote preferences intent and start the activity
				Intent intent = new Intent(
						this,
						tkj.android.homecontrol.mythmote.MythMotePreferences.class);
				this.startActivity(intent);
				break;

			case RECONNECT_ID:
				// connect to selected location
				this.connectToSelectedLocation();
				break;

			case SELECTLOCATION_ID:
				// Displays the list of configured frontend locations.
				// Fires the locationChanged event when the user selects a
				// location
				// even if the user selects the same location already selected.
				MythMotePreferences.SelectLocation(this, this);
				break;
			}
			;
		} catch (android.content.ActivityNotFoundException ex) {
			// Show error when actibity is not found
			AlertDialog.Builder diag = new AlertDialog.Builder(this);
			diag.setMessage(ex.getMessage());
			diag.setTitle("Error");
			diag.setNeutralButton("OK", null);
			diag.show();
		}
		return false;
	}

	/** Called when the selected tab page is changed **/
	public void onTabChanged(String arg0) {

		// get tab tag
		String tabTag = _tabHost.getCurrentTabTag();

		// check for which tab has been selected
		if (tabTag.equals(NAME_NAV_TAB)) {
			// setup navigation tab button events
//			setupNavigationPanelButtonEvents();
		} else if (tabTag.equals(NAME_MEDIA_TAB)) {
			// setup media tab button events
//			setupMediaPanelButtonEvents();
		} else if (tabTag.equals(NAME_NUMPAD_TAB)) {
			// setup number pad button events
//			setupNumberPadButtonEvents();
		}
	}

	/**
	 * Called when a tab is selected. Returns the layout for the selected tab.
	 * Default is navigation tab
	 */
	public View createTabContent(String tag) {

		// check which tab content to return
		if (tag == NAME_NAV_TAB) {
			// get navigation tab view
			return this.getLayoutInflater().inflate(R.layout.navigation,
					this.getTabHost().getTabContentView(), false);
		} else if (tag == NAME_MEDIA_TAB) {
			// return media tab view
			return this.getLayoutInflater().inflate(R.layout.mediacontrol,
					this.getTabHost().getTabContentView(), false);
		} else if (tag == NAME_NUMPAD_TAB) {
			// return number pad view
			return this.getLayoutInflater().inflate(R.layout.numberpad,
					this.getTabHost().getTabContentView(), false);
		} else {
			// default to navigation tab view
			return this.getLayoutInflater().inflate(R.layout.navigation,
					this.getTabHost().getTabContentView(), false);
		}
	}

	/** Called when the frontend location is changed */
	public void LocationChanged() {

		// connect to selected location
		this.connectToSelectedLocation();
	}

	/** Called when MythCom status changes **/
	public void StatusChanged(String StatusMsg, int statusCode) {
		// set titleJUMPPOINT_guidegrid
		setTitle(StatusMsg);

		// change color based on status code
		if (statusCode == MythCom.STATUS_ERROR) {
			setTitleColor(Color.RED);
		} else if (statusCode == MythCom.STATUS_DISCONNECTED) {
			setTitleColor(Color.RED);
		} else if (statusCode == MythCom.STATUS_CONNECTED) {
			setTitleColor(Color.GREEN);
		} else if (statusCode == MythCom.STATUS_CONNECTING) {
			setTitleColor(Color.YELLOW);
		}
	}

	/**
	 * Reads the selected frontend from preferences and attempts to connect with
	 * MythCom.Connect()
	 **/
	private void connectToSelectedLocation() {

		// get selected frontend id
		selected = this.getSharedPreferences(
				MythMotePreferences.MYTHMOTE_SHARED_PREFERENCES_ID,
				MODE_PRIVATE).getInt(
				MythMotePreferences.PREF_SELECTED_LOCATION, -1);

		// create location database adapter
		MythMoteDbManager dbManager = new MythMoteDbManager(this);

		// open connect
		dbManager.open();

		// get the selected location information by it's ID
		Cursor cursor = dbManager.fetchFrontendLocation(selected);

		// make sure returned cursor is valid
		if (cursor != null && cursor.getCount() > 0) {
			// set selected location from Cursor
			_location.ID = cursor.getInt(cursor
					.getColumnIndex(MythMoteDbHelper.KEY_ROWID));
			_location.Name = cursor.getString(cursor
					.getColumnIndex(MythMoteDbHelper.KEY_NAME));
			_location.Address = cursor.getString(cursor
					.getColumnIndex(MythMoteDbHelper.KEY_ADDRESS));
			_location.Port = cursor.getInt(cursor
					.getColumnIndex(MythMoteDbHelper.KEY_PORT));
		}

		// close cursor and db adapter
		cursor.close();
		dbManager.close();

		// connect to location if it is not null
		if (_location != null)
			_comm.Connect(_location);
	}

	/** Sets up a mythcom jump button click event **/
	private final void setupJumpButtonEvent(int buttonViewId,
			final String jumpPoint) {
		final Button buttonJump = (Button) this.findViewById(buttonViewId);

		buttonJump.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Perform action on clicks
				_comm.SendJumpCommand(jumpPoint);
			}
		});
	}

	/** Sets up a mythcom keyboard button click event **/
	private final void setupKeyButtonEvent(int buttonViewId,
			final String sendKey) {
		final Button button = (Button) this.findViewById(buttonViewId);
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Perform action on clicks
				_comm.SendKey(sendKey);
			}
		});
	}

	/** Sets up a mythcom playback command button click event **/
	private final void setupPlaybackCmdButtonEvent(int buttonViewId,
			final String sendCmd) {
		final Button button = (Button) this.findViewById(buttonViewId);
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Perform action on clicks
				_comm.SendPlaybackCmd(sendCmd);
			}
		});
	}

	/**
	 * Overridden to allow the hardware volume controls to influence the Myth front end 
	 * volume control
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			_comm.SendKey(KEY_VOLUME_DOWN);
			return true;
		case KeyEvent.KEYCODE_VOLUME_UP:
			_comm.SendKey(KEY_VOLUME_UP);
			return true;
		default:
			return super.onKeyDown(keyCode, event);

		}

	}

	/**
	 * Enable the long click and normal click actions where
	 * a long click will configure the button, and a normal tap
	 * will perform the command
	 * 
	 * This is the callback from the {@link KeyBindingManager}
	 */
	public View bind(KeyBindingEntry entry) {
		View v = this.findViewById(entry.getMythKey().getButtonId());
		if ( null == v )
			return null;
		v.setLongClickable(true);
		v.setOnLongClickListener(keyManager);
		v.setOnClickListener(keyManager);
		return v;
	}

}
