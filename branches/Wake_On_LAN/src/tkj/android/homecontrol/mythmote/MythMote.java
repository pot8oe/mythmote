package tkj.android.homecontrol.mythmote;


import tkj.android.homecontrol.mythmote.LocationChangedEventListener;
import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.text.Editable;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;



public class MythMote extends TabActivity  implements 
	TabHost.TabContentFactory, 
	OnTabChangeListener, 
	LocationChangedEventListener, 
	MythCom.StatusChangedEventListener
	{	


	public static final int SETTINGS_ID = Menu.FIRST;
	public static final int RECONNECT_ID = Menu.FIRST + 1;
	public static final int SELECTLOCATION_ID = Menu.FIRST + 2;
	public static final int SENDWOL_ID = SELECTLOCATION_ID + 1;
	public static final String NAME_NAV_TAB = "TabNavigation";
	public static final String NAME_MEDIA_TAB = "TabNMediaControl";
	public static final String NAME_NUMPAD_TAB = "TabNumberPad";
	
	private static TabHost _tabHost;
	private static MythCom _comm;
	private static FrontendLocation _location = new FrontendLocation();
	private static int selected = -1;
	private static boolean hapticFeedbackEnabled = false;
	
	/** Called when the activity is first created.*/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if(_comm==null) {
            //create comm class
            _comm = new MythCom(this);
        }
        //set status changed event handler
        _comm.SetOnStatusChangeHandler(this);
        
        
        //this.setSelectedLocation();
        
        //create tab UI
        _tabHost = getTabHost();
        
        //create tabs
    	createTabs();
        
        //setup on tab change event
        _tabHost.setOnTabChangedListener(this);
        
        //set navigation tab and setup events
        _tabHost.setCurrentTab(0);
        
        //setup navigation panel button events
        setupNavigationPanelButtonEvents();
    }
    
    /** Called when the activity is resumed **/
    @Override
    public void onResume(){
    	super.onResume();

    	if(!_comm.IsConnected() && !_comm.IsConnecting()) {
    		_comm.Disconnect();
    		
        	if(this.setSelectedLocation())
        		_comm.Connect(_location);
    	}else{
    		this.StatusChanged(_location.Name + " - Connected", MythCom.STATUS_CONNECTED);
    	}
    }
    
	/** Called when the activity is paused **/
    @Override
    public void onPause(){
    	super.onPause();
    	
    }
    
    public void onDestroy(){
    	super.onDestroy();
    	
    	if(_comm.IsConnected())
    		_comm.Disconnect();
    	_tabHost = null;
    	
    }
    
    /** Called when device configuration changes occur. Configuration 
     * changes that cause this function to be called must be 
     * registered in AndroidManifest.xml **/
    public void onConfigurationChanged(Configuration config)
    {
    	super.onConfigurationChanged(config);
    	
    	//get current tab index
    	int cTab = _tabHost.getCurrentTab();
    	
    	//set current tab to 0. Clear seems to fail when set to anything else
    	_tabHost.setCurrentTab(0);
    	
    	//clear all tabs
    	_tabHost.clearAllTabs();
    	
    	//create tabs
    	createTabs();
        
        //set current tab back
        _tabHost.setCurrentTab(cTab);
    }
    
    /**
     * Overridden to allow the hardware volume controls to influence the Myth front end
     * volume control
     */
    //@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
            switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                    _comm.SendKey(MythCom.VOLUME_DOWN);
                    return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
                    _comm.SendKey(MythCom.VOLUME_UP);
                    return true;
            default:
                    return super.onKeyDown(keyCode, event);

            }

    }
    
    /** Called to create the options menu once.  **/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        
        //create settings  menu item
        menu.add(0, SETTINGS_ID, 0, R.string.settings_menu_str).setIcon(R.drawable.settings);
        
        //create reconnect menu item
        menu.add(0, RECONNECT_ID, 0, R.string.reconnect_str).setIcon(R.drawable.menu_refresh);
        
        //create select location menu item
        menu.add(0, SELECTLOCATION_ID, 0, R.string.selected_location_str).setIcon(R.drawable.home);
        
        menu.add(0, SENDWOL_ID, 0, R.string.send_wol_str);
        
        //return results
        return result;
    }
    
    /** Called when a menu item is selected **/
   @Override
    public boolean onOptionsItemSelected(MenuItem item){
	   try
	   {
		   //Check which menu item was selected
		   switch(item.getItemId())
		   {
		   		case SETTINGS_ID:
		   			//Create mythmote preferences intent and start the activity
		   			Intent intent = new Intent(this, tkj.android.homecontrol.mythmote.MythMotePreferences.class);
				   	this.startActivity(intent);
				   	break;

		   		case RECONNECT_ID:
		   			if(_comm.IsConnected())
		   			    _comm.Disconnect();
		   			
		   	    	if(this.setSelectedLocation())
		   	    		_comm.Connect(_location);
		   			break;
		   			
		   		case SELECTLOCATION_ID:
		   			//Displays the list of configured frontend locations.
					//Fires the locationChanged event when the user selects a location
					//even if the user selects the same location already selected.
					MythMotePreferences.SelectLocation(this, this);
		   			break;
		   		case SENDWOL_ID:
		   			WOLPowerManager pm = new WOLPowerManager();
		   			pm.sendWOL(this);
		   			break;
		   };
	   }
	   catch(android.content.ActivityNotFoundException ex)
	   {
		   //Show error when activity is not found
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
		
		//get tab tag
		String tabTag = _tabHost.getCurrentTabTag();
		
		//check for which tab has been selected
		if(tabTag.equals(NAME_NAV_TAB))
		{
			//setup navigation tab button events
			setupNavigationPanelButtonEvents();
		}
		else if(tabTag.equals(NAME_MEDIA_TAB))
		{
			//setup media tab button events
			setupMediaPanelButtonEvents();
		}
		else if(tabTag.equals(NAME_NUMPAD_TAB))
		{
			//setup number pad button events
			setupNumberPadButtonEvents();
		}
	}
	
	/**
	 * Called to create and add tabs to the tabhost
	 */
	private void createTabs()
	{
		//recreate tabs
    	_tabHost.addTab(_tabHost.newTabSpec(NAME_NAV_TAB).setIndicator(
        		this.getString(R.string.navigation_str),
        		this.getResources().getDrawable(R.drawable.starsmall)).setContent(this));
        _tabHost.addTab(_tabHost.newTabSpec(NAME_MEDIA_TAB).setIndicator(
        		this.getString(R.string.media_str),
        		this.getResources().getDrawable(R.drawable.media)).setContent(this));
        _tabHost.addTab(_tabHost.newTabSpec(NAME_NUMPAD_TAB).setIndicator(
        		this.getString(R.string.numpad_str),
        		this.getResources().getDrawable(R.drawable.numberpad)).setContent(this));
	}
	
	private void loadSharedPreferences()
	{
		//get selected frontend id
		selected = this.getSharedPreferences(MythMotePreferences.MYTHMOTE_SHARED_PREFERENCES_ID, MODE_PRIVATE)
        	.getInt(MythMotePreferences.PREF_SELECTED_LOCATION, -1);
		
		hapticFeedbackEnabled = this.getSharedPreferences(MythMotePreferences.MYTHMOTE_SHARED_PREFERENCES_ID, MODE_PRIVATE)
			.getBoolean(MythMotePreferences.PREF_HAPTIC_FEEDBACK_ENABLED, false);
	}
   
    /** Called when a tab is selected. Returns the layout for the selected tab. 
    * Default is navigation tab */
	public View createTabContent(String tag) {
		
		//check which tab content to return
		if(tag == NAME_NAV_TAB)
		{
			//get navigation tab view
			return this.getLayoutInflater().inflate(R.layout.navigation, this.getTabHost().getTabContentView(), false);
		}
		else if(tag == NAME_MEDIA_TAB)
		{
			//return media tab view
			return this.getLayoutInflater().inflate(R.layout.mediacontrol, this.getTabHost().getTabContentView(), false);
		}
		else if(tag == NAME_NUMPAD_TAB)
		{
			//return number pad view
			return this.getLayoutInflater().inflate(R.layout.numberpad, this.getTabHost().getTabContentView(), false);
		}
		else
		{
			//default to navigation tab view
			return this.getLayoutInflater().inflate(R.layout.navigation, this.getTabHost().getTabContentView(), false);
		}
	}

	/** Called when the frontend location is changed */
	public void LocationChanged() {
		if(_comm.IsConnected())
		    _comm.Disconnect();
		
    	if(this.setSelectedLocation())
    		_comm.Connect(_location);
	}
   
	/** Called when MythCom status changes **/
    public void StatusChanged(String StatusMsg, int statusCode) {
		//set titleJUMPPOINT_guidegrid
		setTitle(StatusMsg);
		
		//change color based on status code
		if(statusCode == MythCom.STATUS_ERROR)
		{
			setTitleColor(Color.RED);
		}
		else if(statusCode == MythCom.STATUS_DISCONNECTED)
		{
			setTitleColor(Color.RED);
		}
		else if(statusCode == MythCom.STATUS_CONNECTED)
		{
			setTitleColor(Color.GREEN);
		}
		else if(statusCode == MythCom.STATUS_CONNECTING)
		{
			setTitleColor(Color.YELLOW);
		}
	}
	
    /** Reads the selected frontend from preferences and attempts to connect with MythCom.Connect() **/
	private boolean setSelectedLocation() {
		
		//load shared preferences
		this.loadSharedPreferences();

		//create location database adapter
        LocationDbAdapter dbAdapter = new LocationDbAdapter(this);
        
        //open connect
        dbAdapter.open();
        
        //get the selected location information by it's ID
        Cursor cursor = dbAdapter.fetchFrontendLocation(selected);
        
        //make sure returned cursor is valid
        if(cursor != null && cursor.getCount() > 0)
        {
        	//set selected location from Cursor
        	_location.ID = cursor.getInt(cursor.getColumnIndex(LocationDbAdapter.KEY_ROWID));
        	_location.Name = cursor.getString(cursor.getColumnIndex(LocationDbAdapter.KEY_NAME));
        	_location.Address = cursor.getString(cursor.getColumnIndex(LocationDbAdapter.KEY_ADDRESS));
        	_location.Port = cursor.getInt(cursor.getColumnIndex(LocationDbAdapter.KEY_PORT));
        }
        else
        {
        	return false;
        }
        
        //close cursor and db adapter
        cursor.close();
        dbAdapter.close();
        return true;
    	
	}
	
	/** Sets up the navigation tab's button events **/
    private void setupNavigationPanelButtonEvents()
    {
    	// jump buttons.
    	// TODO: Make these user definable in a later release
    	this.setupJumpButtonEvent(R.id.ButtonJump1, MythCom.JUMPPOINT_mainmenu);
    	this.setupJumpButtonEvent(R.id.ButtonJump2, MythCom.JUMPPOINT_livetv);
    	this.setupJumpButtonEvent(R.id.ButtonJump3, MythCom.JUMPPOINT_playbackrecordings);
    	this.setupJumpButtonEvent(R.id.ButtonJump4, MythCom.JUMPPOINT_playmusic);
    	this.setupJumpButtonEvent(R.id.ButtonJump5, MythCom.JUMPPOINT_videogallery);
    	this.setupJumpButtonEvent(R.id.ButtonJump6, MythCom.JUMPPOINT_statusbox);
    	
	    //navigation buttons
    	this.setupKeyButtonEvent(R.id.ButtonInfo, "i");
    	this.setupKeyButtonEvent(R.id.ButtonGuide, "s");
    	this.setupKeyButtonEvent(R.id.ButtonEsc, MythCom.KEY_esc);
    	this.setupKeyButtonEvent(R.id.ButtonMenu, "m");
	    this.setupKeyButtonEvent(R.id.ButtonUp, MythCom.KEY_up);
	    this.setupKeyButtonEvent(R.id.ButtonDown, MythCom.KEY_down);
	    this.setupKeyButtonEvent(R.id.ButtonLeft, MythCom.KEY_left);
	    this.setupKeyButtonEvent(R.id.ButtonRight, MythCom.KEY_right);
	    this.setupKeyButtonEvent(R.id.ButtonSelect, MythCom.KEY_enter);
	    
    }
    
    /** Sets up the Media playback tab's buttons **/
    private void setupMediaPanelButtonEvents()
    {
    	// media playback
    	this.setupKeyButtonEvent(R.id.ButtonRecord, "r");
    	this.setupPlaybackCmdButtonEvent(R.id.ButtonStop, MythCom.PLAY_STOP);
    	this.setupPlaybackCmdButtonEvent(R.id.ButtonPlay, MythCom.PLAY_PLAY);
    	this.setupPlaybackCmdButtonEvent(R.id.ButtonRew, MythCom.PLAY_SEEK_BW);
    	this.setupPlaybackCmdButtonEvent(R.id.ButtonFF, MythCom.PLAY_SEEK_FW);
    	this.setupKeyButtonEvent(R.id.ButtonPause, "p");
    	this.setupKeyButtonEvent(R.id.ButtonSkipBack, "home");
    	this.setupKeyButtonEvent(R.id.ButtonSkipForward, "end");
    	
    	//volume
    	this.setupKeyButtonEvent(R.id.ButtonVolUp, MythCom.VOLUME_UP);
    	this.setupKeyButtonEvent(R.id.ButtonVolDown, MythCom.VOLUME_DOWN);
    	this.setupKeyButtonEvent(R.id.ButtonMute, MythCom.VOLUME_MUTE);
    	
    	//ch
    	this.setupPlaybackCmdButtonEvent(R.id.ButtonChUp, MythCom.PLAY_CH_UP);
    	this.setupPlaybackCmdButtonEvent(R.id.ButtonChDown, MythCom.PLAY_CH_DW);
	    this.setupKeyButtonEvent(R.id.ButtonChReturn, MythCom.CH_RETURN);
    }
    
    /** Sets up the number pad tab's buttons **/
    private void setupNumberPadButtonEvents()
    {
    	//numbers
    	this.setupKeyButtonEvent(R.id.Button0, "0");
	    this.setupKeyButtonEvent(R.id.Button1, "1");
	    this.setupKeyButtonEvent(R.id.Button2, "2");
	    this.setupKeyButtonEvent(R.id.Button3, "3");
	    this.setupKeyButtonEvent(R.id.Button4, "4");
	    this.setupKeyButtonEvent(R.id.Button5, "5");
	    this.setupKeyButtonEvent(R.id.Button6, "6");
	    this.setupKeyButtonEvent(R.id.Button7, "7");
	    this.setupKeyButtonEvent(R.id.Button8, "8");
	    this.setupKeyButtonEvent(R.id.Button9, "9");
	    
	    
	    //control
	    this.setupKeyButtonEvent(R.id.ButtonBackspace, MythCom.KEY_backspace);
	    this.setupKeyButtonEvent(R.id.ButtonEnter, MythCom.KEY_enter);
	    
	    //send keyboard input
	    final Button buttonJump = (Button) this.findViewById(R.id.ButtonSend);
	    final EditText textBox = (EditText) this.findViewById(R.id.EditTextKeyboardInput);
	    if(buttonJump != null && textBox != null)
	    {
		    buttonJump.setOnClickListener(new OnClickListener() {
		        public void onClick(View v) {
		            
		        	//get send keyboard text
		        	Editable text = textBox.getText();
		        	int count = text.length();
		        	
		        	//for each character
		        	for(int i=0; i<count; i++)
		        	{
		        		//get char
		        		char c = text.charAt(i);
		        		
		        		//check if it's whitespace
		        		if(Character.isWhitespace(c))
		        		{
		        			if(c == '\t')//tab
		        			{
		        				_comm.SendKey("tab");
		        			}
		        			else if(c == ' ')//space
		        			{
		        				_comm.SendKey("space");
		        			}
		        			else if(c == '\r')//enter/return
		        			{
		        				_comm.SendKey("enter");
		        			}
		        		}
		        		else//not white space. Just send as is
		        		{
		        			_comm.SendKey(c);
		        		}
		        	}
		        }
		    });
	    }
	    
    }
    
    /** Sets up a mythcom jump button click event  **/
    private final void setupJumpButtonEvent(int buttonViewId, final String jumpPoint)
    {
    	final Button button = (Button) this.findViewById(buttonViewId);
	    button.setOnClickListener(new OnClickListener() {
	        public void onClick(View v) {
	            // Perform action on clicks
	            _comm.SendJumpCommand(jumpPoint);
	            
	            if(hapticFeedbackEnabled)
	            	button.performHapticFeedback(1, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
	        }
	    });
    }
    
    /** Sets up a mythcom keyboard button click event **/
    private final void setupKeyButtonEvent(int buttonViewId, final String sendKey)
    {
    	final Button button = (Button) this.findViewById(buttonViewId);
	    button.setOnClickListener(new OnClickListener() {
	        public void onClick(View v) {
	            // Perform action on clicks
	            _comm.SendKey(sendKey);
	            
	            if(hapticFeedbackEnabled)
	            	button.performHapticFeedback(1, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
	        }
	    });
    }
    
    /** Sets up a mythcom playback command button click event **/
    private final void setupPlaybackCmdButtonEvent(int buttonViewId, final String sendCmd)
    {
    	final Button button = (Button) this.findViewById(buttonViewId);
	    button.setOnClickListener(new OnClickListener() {
	        public void onClick(View v) {
	            // Perform action on clicks
	            _comm.SendPlaybackCmd(sendCmd);
	            
	            if(hapticFeedbackEnabled)
	            	button.performHapticFeedback(1, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
	        }
	    });
    }



}






