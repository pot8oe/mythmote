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
import java.util.List;

import tkj.android.homecontrol.mythmote.LocationChangedEventListener;
import tkj.android.homecontrol.mythmote.db.MythMoteDbHelper;
import tkj.android.homecontrol.mythmote.db.MythMoteDbManager;
import tkj.android.homecontrol.mythmote.keymanager.KeyBindingEntry;
import tkj.android.homecontrol.mythmote.keymanager.KeyBindingManager;
import tkj.android.homecontrol.mythmote.keymanager.KeyMapBinder;
import tkj.android.homecontrol.mythmote.ui.AutoRepeatButton;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TabHost.OnTabChangeListener;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;

public class MythMote extends FragmentActivity implements
		LocationChangedEventListener, MythCom.StatusChangedEventListener {

	public static final int SETTINGS_ID = Menu.FIRST;
	public static final int RECONNECT_ID = Menu.FIRST + 1;
	public static final int SELECTLOCATION_ID = Menu.FIRST + 2;
	public static final int DONATE_ID = Menu.FIRST + 3;
	public static final int SENDWOL_ID = Menu.FIRST + 4;
	public static final int SENDWOL_RE_ID = Menu.FIRST + 5;
	public static final int SENDWOL_PJ_ID = Menu.FIRST + 6;
	public static final int KEYBOARD_INPUT_ID = Menu.FIRST + 7;
	public static final String LOG_TAG = "MythMote";

	private static final String KEY_VOLUME_DOWN = "[";
	private static final String KEY_VOLUME_UP = "]";
	private static final String DONATE_URL = "https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=TX7RH2TX6NJ2N&lc=US&item_name=mythmote%2ddonation&item_number=mythmote%2dgooglecodepage&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHosted";

	private static MythCom sComm;
	private static FrontendLocation sLocation = new FrontendLocation();
	private static int sSelected = -1;
	private static boolean sIsScreenLarge = false;
	private static boolean sShowDonateMenuItem = true;
//	private static PowerManager powerManager;
//	private static PowerManager.WakeLock wakeLock;
	private static List<Fragment> sFragmentArrayList;
	private static List<String> sHeaderArrayList;

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//allow mythmote to be shown ontop of lock screen
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		
		// determine if large screen layouts are being used
		sIsScreenLarge = this.getResources().getString(R.string.screensize)
				.equals("large");

		// Get mythcom object
		if (sComm == null) {
			sComm = MythCom.GetMythCom(this);
		}
		
		// set status changed event handler
		sComm.SetOnStatusChangeHandler(this);
		
		
	}

	/**
	 * Called when the activity is resumed
	 */
	@Override
	public void onResume() {
		super.onResume();
		
		//load and configure user interface
		this.setupContentView();
				
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
	@Override
	public void onDestroy() {
		super.onDestroy();

		if (sComm != null){
			sComm.ActivityOnDestroy();
			sComm = null;
		}

	}

	/**
	 * Called when device configuration changes occur. Configuration changes
	 * that cause this function to be called must be registered in
	 * AndroidManifest.xml
	 */
	public void onConfigurationChanged(Configuration config) {
		super.onConfigurationChanged(config);

		this.setupContentView();
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
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);

		// create reconnect menu item
		MenuItem menuItem = menu.add(0, RECONNECT_ID, 0, R.string.reconnect_str).setIcon(
				R.drawable.ic_menu_refresh);
		if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ) {
	    	menuItem.setShowAsAction( MenuItem.SHOW_AS_ACTION_ALWAYS );
	    }

		// create settings menu item
		menuItem = menu.add(0, SETTINGS_ID, 0, R.string.settings_menu_str).setIcon(
				android.R.drawable.ic_menu_preferences);
		if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ) {
	    	menuItem.setShowAsAction( MenuItem.SHOW_AS_ACTION_IF_ROOM );
	    }
		
		// create keyboard input menu item
		menuItem = menu.add(0, KEYBOARD_INPUT_ID, 0, R.string.keyboard_input_str).setIcon(
				R.drawable.ic_notification_ime_default);
		if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ) {
	    	menuItem.setShowAsAction( MenuItem.SHOW_AS_ACTION_IF_ROOM );
	    }

		// create select location menu item
		menuItem = menu.add(0, SELECTLOCATION_ID, 0, R.string.selected_location_str)
				.setIcon(R.drawable.ic_menu_home);
		if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ) {
	    	menuItem.setShowAsAction( MenuItem.SHOW_AS_ACTION_NEVER );
	    }
		
		//create wake on lan menu item
		menuItem = menu.add(0, SENDWOL_ID, 0, R.string.send_wol_str).setIcon(R.drawable.ic_menu_sun);
		if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ) {
	    	menuItem.setShowAsAction( MenuItem.SHOW_AS_ACTION_NEVER);
	    	
	    }
		//menu.add(0, SENDWOL_RE_ID, 0, R.string.send_wol_re_str);
		//menu.add(0, SENDWOL_PJ_ID, 0, R.string.send_wol_pj_str);
		
		//add donate button if enabled
		if(sShowDonateMenuItem){
			
		}
		
		// return results
		return result;
	}
	
	/**
	 * Called when the menu is opened.
	 */
	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		
		if(null != menu){
			
			//get donate menu item
			MenuItem menuItem = menu.findItem(DONATE_ID);
			
			//if donate menu item exists and the user wants it gone remove it
			if(null != menuItem && !sShowDonateMenuItem)
				menu.removeItem(DONATE_ID);

			//if the donate button is missing and the user wants it add it back
			if(null == menuItem && sShowDonateMenuItem)
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
				Intent settingsIntent = new Intent(
						this,
						tkj.android.homecontrol.mythmote.MythMotePreferences.class);
				this.startActivity(settingsIntent);
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
					//WOLPowerManager.sendWOL(sLocation.Address, sLocation.MAC, 2);
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
					//WOLPowerManager.sendWOL(sLocation.Address, sLocation.MAC, 2);
					WOLPowerManager.sendWOL(this, sLocation.MAC, 2);
				} catch (IOException e) {
					Log.d(LOG_TAG, e.getMessage());
				}
				break;
			case DONATE_ID:
				this.startDonateIntent();
				break;
				
			case KEYBOARD_INPUT_ID:
			    DialogFragment newFragment = new MythmoteKeyboardInputFragment();
			    newFragment.show(getSupportFragmentManager(), "keyboard_input");
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
	
	private void setupContentView(){
		//load main layout
		this.setContentView(R.layout.main);

		// try to setup view pager
		if(this.setupViewPager()) return;
		
		// if viewpager wasn't found try to setup large layout
		if(this.setupLargeLayout()) return;
	}

	/**
	 * Setups up the viewpager and MythmotePagerAdapter if
	 * the current layout contains the mythmote_pager view pager.
	 */
	private boolean setupViewPager() {

		// get viewpager from layout
		ViewPager pager = (ViewPager) findViewById(R.id.mythmote_pager);

		// if there is a viewpager set it up
		if (null == pager)
			return false;

		//get current view pager page
		int cItem = pager.getCurrentItem();
		cItem = cItem >= 0 ? cItem : 0;

		// create fragment and header arrays
		sFragmentArrayList = new ArrayList<Fragment>();
		sHeaderArrayList = new ArrayList<String>();

		// mythmote navigation page fragment
		Fragment nav = Fragment.instantiate(this,
				MythmoteNavigationFragment.class.getName());
		sFragmentArrayList.add(nav);
		sHeaderArrayList.add(this.getString(R.string.navigation_str));

		// mythmote numbers page fragment
		Fragment num = Fragment.instantiate(this,
				MythmoteNumberPadFragment.class.getName());
		sFragmentArrayList.add(num);
		sHeaderArrayList.add(this.getString(R.string.numpad_str));

		// mythmote numbers page fragment
		Fragment jump = Fragment.instantiate(this,
				MythmoteQuickJumpFragment.class.getName());
		sFragmentArrayList.add(jump);
		sHeaderArrayList.add(this.getString(R.string.quickjump_str));

//		// mythmote keyboard input page fragment
//		Fragment keyboard = Fragment.instantiate(this,
//				MythmoteKeyboardInputFragment.class.getName());
//		sFragmentArrayList.add(keyboard);
//		sHeaderArrayList.add(this.getString(R.string.keyboard_input_str));

		// set pager adapter and initial item
		pager.setAdapter(new MythmotePagerAdapter(this
				.getSupportFragmentManager()));
		pager.setCurrentItem(cItem);

		return true;
	}

	/**
	 * Setups up the large layout that does not
	 * use the ViewPager
	 */
	private boolean setupLargeLayout() {
		
		//get framgment manager and start a transaction
		FragmentManager fragMgr = this.getSupportFragmentManager();
		FragmentTransaction fTran = fragMgr.beginTransaction();

		//Setup nav fragment
		Fragment nav = fragMgr.findFragmentById(R.layout.fragment_mythmote_navigation);
		if (null == nav) {
			nav = Fragment.instantiate(this,
					MythmoteNavigationFragment.class.getName());
			fTran.add(R.id.framelayout_navigation_fragment, nav);
		}else{
			fTran.replace(R.id.framelayout_navigation_fragment, nav);
		}
		
		//setup number pad
		Fragment num = fragMgr.findFragmentById(R.layout.fragment_mythmote_numbers);
		if (null == num) {
			num = Fragment.instantiate(this,
					MythmoteNumberPadFragment.class.getName());
			fTran.add(R.id.framelayout_numberpad_fragment, num);
		}else{
			fTran.replace(R.id.framelayout_numberpad_fragment, num);
		}
		
		//setup quick jump
		Fragment jump = fragMgr.findFragmentById(R.layout.fragment_mythmote_quickjump);
		if (null == jump) {
			jump = Fragment.instantiate(this,
					MythmoteQuickJumpFragment.class.getName());
			fTran.add(R.id.framelayout_quickjump_fragment, jump);
		}else{
			fTran.replace(R.id.framelayout_quickjump_fragment, jump);
		}
		
		//finalize fragment transaction
		fTran.commit();
		
		return true;
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
		
		final String msg = StatusMsg;
		final int code = statusCode;
		
		this.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// set titleJUMPPOINT_guidegrid
				setTitle(msg);

				// change color based on status code
				if (code == MythCom.STATUS_ERROR) {
					setTitleColor(Color.RED);
				} else if (code == MythCom.STATUS_DISCONNECTED) {
					setTitleColor(Color.RED);
				} else if (code == MythCom.STATUS_CONNECTED) {
					setTitleColor(Color.GREEN);
				} else if (code == MythCom.STATUS_CONNECTING) {
					setTitleColor(Color.YELLOW);
				}
			}

		});
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
			KeyBindingManager.EditingEnabled = false;
		}else{
			//edit key-bindings action
			
			//disable auto repeat
			AutoRepeatButton.SetAutoRepeatEnalbed(false);
			
			//set editable
			KeyBindingManager.EditingEnabled = true;
		}

		// set the hapticfeedback setting in keymanager
		KeyBindingManager.HapticFeedbackEnabled = pref.getBoolean(
				MythMotePreferences.PREF_HAPTIC_FEEDBACK_ENABLED, false);
		
		//set autorepeat interval
		AutoRepeatButton.SetRepeatInterval(pref.getInt(
				MythMotePreferences.PREF_KEY_REPEAT_INTERVAL, 
				AutoRepeatButton.DEFAULT_REPEAT_INTERVAL));

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

	
	
	
	class MythmotePagerAdapter extends FragmentStatePagerAdapter {
		
        public MythmotePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return sFragmentArrayList.size();
        }

        @Override
        public Fragment getItem(int position) {
            return sFragmentArrayList.get(position);
        }
        
        @Override
		public CharSequence getPageTitle(int position) {
			return sHeaderArrayList.get(position);
		}

    }


}
