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

import java.io.IOException;
import java.util.ArrayList;

import tkj.android.homecontrol.mythmote.LocationChangedEventListener;
import tkj.android.homecontrol.mythmote.db.MythMoteDbHelper;
import tkj.android.homecontrol.mythmote.db.MythMoteDbManager;
import tkj.android.homecontrol.mythmote.keymanager.KeyBindingEntry;
import tkj.android.homecontrol.mythmote.keymanager.KeyBindingManager;
import tkj.android.homecontrol.mythmote.keymanager.KeyMapBinder;
import tkj.android.homecontrol.mythmote.ui.AutoRepeatButton;
import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
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
import android.view.ViewGroup.LayoutParams;

public class MythMote extends TabActivity implements TabHost.TabContentFactory,
		OnTabChangeListener, LocationChangedEventListener,
		MythCom.StatusChangedEventListener,
		KeyMapBinder {

	public static final int SETTINGS_ID = Menu.FIRST;
	public static final int RECONNECT_ID = Menu.FIRST + 1;
	public static final int SELECTLOCATION_ID = Menu.FIRST + 2;
	public static final int DONATE_ID = Menu.FIRST + 3;
	public static final int SENDWOL_ID = Menu.FIRST + 4;
	public static final int SENDWOL_RE_ID = Menu.FIRST + 5;
	public static final int SENDWOL_PJ_ID = Menu.FIRST + 6;
	public static final String NAME_NAV_TAB = "TabNavigation";
	public static final String NAME_MEDIA_TAB = "TabNMediaControl";
	public static final String NAME_NUMPAD_TAB = "TabNumberPad";
	public static final String LOG_TAG = "MythMote";

	private static final String KEY_VOLUME_DOWN = "[";
	private static final String KEY_VOLUME_UP = "]";
	private static final String DONATE_URL = "https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=TX7RH2TX6NJ2N&lc=US&item_name=mythmote%2ddonation&item_number=mythmote%2dgooglecodepage&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHosted";

	private KeyBindingManager mKeyManager;

	private static TabHost sTabHost;
	private static MythCom sComm;
	private static FrontendLocation sLocation = new FrontendLocation();
	private static int sSelected = -1;
	private static boolean sIsScreenLarge = false;
	private static boolean sShowDonateMenuItem = true;

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.main);

		// determine if large screen layouts are being used
		sIsScreenLarge = this.getResources().getString(R.string.screensize)
				.equals("large");

		if (sComm == null) {
			// create comm class
			sComm = new MythCom(this);
		}
		
		// set status changed event handler
		sComm.SetOnStatusChangeHandler(this);

		// create tab UI
		sTabHost = getTabHost();

		// create tabs
		this.createTabs();

		// setup on tab change event
		sTabHost.setOnTabChangedListener(this);

		// set navigation tab and setup events
		sTabHost.setCurrentTab(0);

		// create key manager and load keys from DB
		mKeyManager = new KeyBindingManager(this, this, sComm);
		mKeyManager.loadKeys();
	}

	/**
	 * Called when the activity is resumed
	 */
	@Override
	public void onResume() {
		super.onResume();
		
		// Here we disconnect if connected because the selected location
		// may have changed from the preference activity. setSelectedLocation()
		// will also trigger
		// loading any other changed preferences

		// disconnect if connected
		if (sComm != null && (sComm.IsConnected() || sComm.IsConnecting())) {
			// force disconnected state
			sComm.Disconnect();
		}

		// set selected location and connect
		if (this.setSelectedLocation())
			sComm.Connect(sLocation);
		
	}

	/**
	 * Called when the activity is paused
	 */
	@Override
	public void onPause() {
		super.onPause();
		
		if (sComm != null && sComm.IsConnected())
			sComm.Disconnect();
	}

	/**
	 * Called when the activity is being destroyed
	 */
	public void onDestroy() {
		super.onDestroy();

		if (sComm != null && sComm.IsConnected())
			sComm.Disconnect();
		
		sTabHost = null;

	}

	/**
	 * Called when device configuration changes occur. Configuration changes
	 * that cause this function to be called must be registered in
	 * AndroidManifest.xml
	 */
	public void onConfigurationChanged(Configuration config) {
		super.onConfigurationChanged(config);

		// make sure tabhost has been set
		if (sTabHost == null)
			sTabHost = this.getTabHost();

		// get current tab index
		int cTab = sTabHost.getCurrentTab();

		// set current tab to 0. Clear seems to fail when set to anything else
		sTabHost.setCurrentTab(0);

		// clear all tabs
		sTabHost.clearAllTabs();

		// create tabs
		this.createTabs();

		// set current tab back
		sTabHost.setCurrentTab(cTab);
	}

	/**
	 * Overridden to allow the hardware volume controls to influence the Myth
	 * front end volume control
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			sComm.SendKey(KEY_VOLUME_DOWN);
			return true;
		case KeyEvent.KEYCODE_VOLUME_UP:
			sComm.SendKey(KEY_VOLUME_UP);
			return true;
		default:
			return super.onKeyDown(keyCode, event);

		}

	}

	/**
	 * Called to create the options menu once.
	 */
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
				.setIcon(R.drawable.selected_location);
		
		//create wake on lan menu item
		menu.add(0, SENDWOL_ID, 0, R.string.send_wol_str).setIcon(R.drawable.ic_menu_sun);
		//menu.add(0, SENDWOL_RE_ID, 0, R.string.send_wol_re_str);
		//menu.add(0, SENDWOL_PJ_ID, 0, R.string.send_wol_pj_str);
		
		// return results
		return result;
	}
	
	/**
	 * Called when the menu is opened.
	 */
	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		
		menu.removeItem(DONATE_ID);
		
		//remove donate button if disabled
		if(sShowDonateMenuItem){
			// create donate menu item
			menu.add(0, DONATE_ID, 0, R.string.donate_menu_item_str).setIcon(R.drawable.paypal);
		}
		
		return super.onMenuOpened(featureId, menu);
	}

	/**
	 * Called when a menu item is selected
	 */
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
				if (sComm.IsConnected() || sComm.IsConnecting())
					sComm.Disconnect();

				// set selected location and connect
				if (this.setSelectedLocation())
					sComm.Connect(sLocation);
				break;

			case SELECTLOCATION_ID:
				// Displays the list of configured frontend locations.
				// Fires the locationChanged event when the user selects a
				// location
				// even if the user selects the same location already selected.
				MythMotePreferences.SelectLocation(this, this);
				break;

			case SENDWOL_ID:
				//This sends the PJRS implementation of WOL
				try {
					//PJRS WOL
					WOLPowerManager.sendWOL(this, sLocation.MAC, 2);
				} catch (IOException e) {
					Log.d(LOG_TAG, e.getMessage());
				}
				break;
			case SENDWOL_RE_ID:
				try {
					//Rob Elsner WOL
					WOLPowerManager.sendWOL(sLocation.MAC, this);
				} catch (IOException e) {
					Log.d(LOG_TAG, e.getMessage());
				}
				break;
			case SENDWOL_PJ_ID:
				try {
					//PJRS WOL
					WOLPowerManager.sendWOL(this, sLocation.MAC, 2);
				} catch (IOException e) {
					Log.d(LOG_TAG, e.getMessage());
				}
				break;
			case DONATE_ID:
				this.startDonateIntent();
				break;
				
			};
		} catch (android.content.ActivityNotFoundException ex) {
			// Show error when activity is not found
			AlertDialog.Builder diag = new AlertDialog.Builder(this);
			diag.setMessage(ex.getMessage());
			diag.setTitle("Error");
			diag.setNeutralButton("OK", null);
			diag.show();
		}
		return false;
	}

	/**
	 * Called when the selected tab page is changed
	 */
	public void onTabChanged(String arg0) {
		// load keybindings
		mKeyManager.loadKeys();

		// setup the media tab's send keyboard input button
		if (sTabHost.getCurrentTabTag().equals(NAME_NUMPAD_TAB)) {
			setupSendKeyboardInputButton();
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

	/**
	 * Called when the frontend location is changed
	 */
	public void LocationChanged() {
		if (sComm.IsConnected() || sComm.IsConnecting())
			sComm.Disconnect();

		// set selected location and connect
		if (this.setSelectedLocation())
			sComm.Connect(sLocation);
	}

	/**
	 * Called when MythCom status changes
	 */
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
	 * Enable the long click and normal click actions where a long click will
	 * configure the button, and a normal tap will perform the command
	 * 
	 * This is the callback from the {@link KeyBindingManager}
	 */
	public View bind(KeyBindingEntry entry) {
		View v = this.findViewById(entry.getMythKey().getButtonId());
		if (null == v)
			return null;
		v.setOnLongClickListener(mKeyManager);
		v.setOnClickListener(mKeyManager);
		return v;
	}

	/**
	 * Reads the selected frontend from preferences and sets sLocation values.
	 */
	private boolean setSelectedLocation() {

		// load shared preferences
		this.loadSharedPreferences();

		// _location should be initialized
		if (sLocation == null) {
			Log.e(LOG_TAG,
					"Cannot set location. Location object not initialized.");
		}

		// create location database adapter
		MythMoteDbManager dbManager = new MythMoteDbManager(this);

		// open connect
		dbManager.open();

		// get the selected location information by it's ID
		Cursor cursor = dbManager.fetchFrontendLocation(sSelected);

		// make sure returned cursor is valid
		if (cursor == null || cursor.getCount() <= 0)
			return false;
		// set selected location from Cursor
		sLocation.ID = cursor.getInt(cursor
				.getColumnIndex(MythMoteDbHelper.KEY_ROWID));
		sLocation.Name = cursor.getString(cursor
				.getColumnIndex(MythMoteDbHelper.KEY_NAME));
		sLocation.Address = cursor.getString(cursor
				.getColumnIndex(MythMoteDbHelper.KEY_ADDRESS));
		sLocation.Port = cursor.getInt(cursor
				.getColumnIndex(MythMoteDbHelper.KEY_PORT));
		sLocation.MAC = cursor.getString(cursor
				.getColumnIndex(MythMoteDbHelper.KEY_MAC));
		sLocation.WifiOnly = cursor.getInt(cursor.
				getColumnIndex(MythMoteDbHelper.KEY_WIFIONLY));

		// close cursor and db adapter
		cursor.close();
		dbManager.close();

		return true;
	}

	/**
	 * Creates and defines the OnClickListener for media tab's send keyboard
	 * input button.
	 */
	private void setupSendKeyboardInputButton() {
		// send keyboard input
		final Button buttonJump = (Button) this.findViewById(R.id.ButtonSend);
		final EditText textBox = (EditText) this
				.findViewById(R.id.EditTextKeyboardInput);
		if (buttonJump != null && textBox != null) {
			buttonJump.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {

					// get send keyboard text
					Editable text = textBox.getText();
					int count = text.length();

					// for each character
					for (int i = 0; i < count; i++) {
						// get char
						char c = text.charAt(i);

						// check if it's whitespace
						if (Character.isWhitespace(c)) {
							if (c == '\t')// tab
							{
								sComm.SendKey("tab");
							} else if (c == ' ')// space
							{
								sComm.SendKey("space");
							} else if (c == '\r')// enter/return
							{
								sComm.SendKey("enter");
							}
						} else// not white space. Just send as is
						{
							sComm.SendKey(c);
						}
					}
				}
			});
		}
	}

	/**
	 * Called to create and add tabs to the tabhost
	 */
	private void createTabs() {
		// create tabs. Media tab is only used when large layouts are inactive
		sTabHost.addTab(sTabHost.newTabSpec(NAME_NAV_TAB)
				.setIndicator(this.getString(R.string.navigation_str))
				.setContent(this));
		if (!sIsScreenLarge)
			sTabHost.addTab(sTabHost.newTabSpec(NAME_MEDIA_TAB)
					.setIndicator(this.getString(R.string.media_str))
					.setContent(this));
		sTabHost.addTab(sTabHost.newTabSpec(NAME_NUMPAD_TAB)
				.setIndicator(this.getString(R.string.numpad_str))
				.setContent(this));

		// resize tabs to remove useless space
		final int count = sTabHost.getTabWidget().getChildCount();
		LayoutParams params;
		for (int i = 0; i < count; i++){
			params = sTabHost.getTabWidget().getChildAt(i).getLayoutParams();
			params.height = params.height > 50 ? params.height/2 : 50;
		}
	}

	/**
	 * Reads the mythmote shared preferences and sets local members accordingly.
	 */
	private void loadSharedPreferences() {
		// get shared preferences reference
		SharedPreferences pref = this.getSharedPreferences(
				MythMotePreferences.MYTHMOTE_SHARED_PREFERENCES_ID,
				MODE_PRIVATE);

		// get selected frontend id
		sSelected = pref.getInt(MythMotePreferences.PREF_SELECTED_LOCATION, -1);
		
		// get if donate button should be visibl
		sShowDonateMenuItem = pref.getBoolean(MythMotePreferences.PREF_SHOW_DONATE_MENU_ITEM, true);

		//read long press action
		if(pref.getInt(MythMotePreferences.PREF_LONGPRESS_ACTION, 0) == 0){
			//auto repeat action
			
			//enable auto repeat
			AutoRepeatButton.SetAutoRepeatEnalbed(true);
			
			//disable editable
			this.mKeyManager.setEditingEnabled(false);
		}else{
			//edit key-bindings action
			
			//disable auto repeat
			AutoRepeatButton.SetAutoRepeatEnalbed(false);
			
			//set editable
			this.mKeyManager.setEditingEnabled(true);
		}

		// set the hapticfeedback setting in keymanager
		this.mKeyManager.setHapticFeedbackEnabled(pref.getBoolean(
				MythMotePreferences.PREF_HAPTIC_FEEDBACK_ENABLED, false));

		// done with pref ref
		pref = null;
	}
	
	private void startDonateIntent(){
		AlertDialog.Builder dBuilder = new AlertDialog.Builder(this);
		dBuilder.setMessage(R.string.donate_intent_alert_str);
		dBuilder.setTitle("Donate Beer Money?");
		dBuilder.setNegativeButton("Cancel", null);
		dBuilder.setNeutralButton("OK", new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//open browser at our paypal donation page.
				Uri uri = Uri.parse(DONATE_URL); 
				Intent webIntent = new Intent(Intent.ACTION_VIEW, uri); 
				startActivity(webIntent); 
			}
		});
		dBuilder.show();
	}

}
