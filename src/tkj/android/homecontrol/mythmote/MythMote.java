package tkj.android.homecontrol.mythmote;


import tkj.android.homecontrol.mythmote.MythMotePreferences.LocationChangedEventListener;
import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;



public class MythMote extends TabActivity  implements TabHost.TabContentFactory {	


	public static final int SETTINGS_ID = Menu.FIRST;
	public static final int RECONNECT_ID = Menu.FIRST + 1;
	public static final int SELECTLOCATION_ID = Menu.FIRST + 2;
	public static final String NAME_NAV_TAB = "TabNavigation";
	public static final String NAME_MEDIA_TAB = "TabNMediaControl";
	public static final String NAME_NUMPAD_TAB = "TabNumberPad";
	
	private static MythCom _comm;
	private static MythCom.StatusChangedEventListener _statusChanged;
	private static FrontendLocation _location = new FrontendLocation();
	private static int selected = -1;
	
	/** Called when the activity is first created.*/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //Create status changed event handler
        _statusChanged = new MythCom.StatusChangedEventListener(){

    		public void StatusChanged(String StatusMsg, int code) {
    			//set title
    			setTitle(StatusMsg);
    			
    			//change color based on status code
    			if(code == MythCom.STATUS_ERROR)
    			{
    				setTitleColor(Color.RED);
    			}
    			else if(code == MythCom.STATUS_DISCONNECTED)
    			{
    				setTitleColor(Color.YELLOW);
    			}
    			else if(code == MythCom.STATUS_CONNECTED)
    			{
    				setTitleColor(Color.GREEN);
    			}
    			else if(code == MythCom.STATUS_CONNECTING)
    			{
    				setTitleColor(Color.YELLOW);
    			}
    		}
    		
    	};
        
        //create comm class
        _comm = new MythCom(this);
        //set status changed event handler
        _comm.SetOnStatusChangeHandler(_statusChanged);
        
        //create tab UI
        final TabHost tabHost = getTabHost();
        tabHost.addTab(tabHost.newTabSpec(NAME_NAV_TAB).setIndicator(
        		this.getString(R.string.navigation_str),
        		this.getResources().getDrawable(R.drawable.starsmall)).setContent(this));
        tabHost.addTab(tabHost.newTabSpec(NAME_MEDIA_TAB).setIndicator(
        		this.getString(R.string.media_str),
        		this.getResources().getDrawable(R.drawable.media)).setContent(this));
        tabHost.addTab(tabHost.newTabSpec(NAME_NUMPAD_TAB).setIndicator(
        		this.getString(R.string.numpad_str),
        		this.getResources().getDrawable(R.drawable.numberpad)).setContent(this)); 
        
        
        tabHost.setOnTabChangedListener(new OnTabChangeListener(){

			public void onTabChanged(String arg0) {
				
				int tabIndex = tabHost.getCurrentTab();
				
				switch(tabIndex)
				{
				case 0://navigation
					setupNavigationPanelButtonEvents();
					break;
					
				case 1://media
					setupMediaPanelButtonEvents();
					break;
					
				case 2://num pad
					setupNumberPadButtonEvents();
					break;
				};
				
			}
        	
        });
        
        //set navigation tab and setup events
        tabHost.setCurrentTab(0);
        setupNavigationPanelButtonEvents();
    }
    
    /** Called when the activity is resumed **/
    @Override
    public void onResume()
    {
    	super.onResume();
    	 
    	//connect to saved location
        connectToSelectedLocation();
    }
    
	/** Called when the activity is paused **/
    @Override
    public void onPause()
    {
    	super.onPause();
    	
    	_comm.Disconnect();

    }
    
    /** Called to create the options menu once.  */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        menu.add(0, SETTINGS_ID, 0, R.string.settings_menu_str).setIcon(R.drawable.settings);
        menu.add(0, RECONNECT_ID, 0, R.string.reconnect_str).setIcon(R.drawable.menu_refresh);
        menu.add(0, SELECTLOCATION_ID, 0, R.string.selected_location_str).setIcon(R.drawable.home);
        return result;
    }
    
   @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
	   try
	   {
    	if(item.getItemId() == SETTINGS_ID)
    	{/* This fails */
    		Intent intent = new Intent(this, tkj.android.homecontrol.mythmote.MythMotePreferences.class);
    		this.startActivity(intent);
    	}
    	else if(item.getItemId() == RECONNECT_ID)
    	{
    		connectToSelectedLocation();
    	}
    	else if(item.getItemId() == SELECTLOCATION_ID)
    	{
    		//Displays the list of configured frontend locations.
			//Fires the locationChanged event when the user selects a location
			//even if the user selects the same location already selected.
    		MythMotePreferences.SelectLocation(this, new LocationChangedEventListener()
			{
				@Override
				public void LocationChanged() {
					//reconnect to selected location
					connectToSelectedLocation();
				}

			});
    	}
	   }
	   catch(android.content.ActivityNotFoundException ex)
	   {
		   AlertDialog.Builder diag = new AlertDialog.Builder(this);
		   diag.setMessage(ex.getMessage());
		   diag.setTitle("Error");
		   diag.setNeutralButton("OK", null);
		   diag.show();
	   }
    	return false;
    }
    
   /** Reads the selected frontend from preferences and attempts to connect with MythCom.Connect() **/
	private void connectToSelectedLocation() {

		//get selected frontend id
		selected = this.getSharedPreferences(MythMotePreferences.MYTHMOTE_SHARED_PREFERENCES_ID, MODE_PRIVATE)
        	.getInt(MythMotePreferences.PREF_SELECTED_LOCATION, -1);
        
        LocationDbAdapter dbAdatper = new LocationDbAdapter(this);
        dbAdatper.open();
        Cursor cursor = dbAdatper.fetchFrontendLocation(selected);
        if(cursor != null && cursor.getCount() > 0)
        {
        	_location.ID = cursor.getInt(cursor.getColumnIndex(LocationDbAdapter.KEY_ROWID));
        	_location.Name = cursor.getString(cursor.getColumnIndex(LocationDbAdapter.KEY_NAME));
        	_location.Address = cursor.getString(cursor.getColumnIndex(LocationDbAdapter.KEY_ADDRESS));
        	_location.Port = cursor.getInt(cursor.getColumnIndex(LocationDbAdapter.KEY_PORT));
        }
        cursor.close();
        dbAdatper.close();
    	
    	
    	if(_location != null)
    		_comm.Connect(_location);
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
    	this.setupJumpButtonEvent(R.id.ButtonJump6, MythCom.JUMPPOINT_livetvinguide);
	    
	    //navigation buttons
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
    	this.setupKeyButtonEvent(R.id.ButtonVolUp, "]");
    	this.setupKeyButtonEvent(R.id.ButtonVolDown, "[");
    	this.setupKeyButtonEvent(R.id.ButtonMute, "|");
    	
    	//ch
    	this.setupPlaybackCmdButtonEvent(R.id.ButtonChUp, MythCom.PLAY_CH_UP);
    	this.setupPlaybackCmdButtonEvent(R.id.ButtonChDown, MythCom.PLAY_CH_DW);
	    this.setupKeyButtonEvent(R.id.ButtonChReturn, "h");
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
		            // Perform action on clicks
		        	Editable text = textBox.getText();
		        	int count = text.length();
		        	for(int i=0; i<count; i++)
		        	{
		        		_comm.SendKey(text.charAt(i));
		        	}
		        }
		    });
	    }
	    
    }
    
    
    private final void setupJumpButtonEvent(int buttonViewId, final String jumpPoint)
    {
    	final Button buttonJump = (Button) this.findViewById(buttonViewId);
	    buttonJump.setOnClickListener(new OnClickListener() {
	        public void onClick(View v) {
	            // Perform action on clicks
	            _comm.SendJumpCommand(jumpPoint);
	        }
	    });
    }
    
    private final void setupKeyButtonEvent(int buttonViewId, final String sendKey)
    {
    	final Button button = (Button) this.findViewById(buttonViewId);
	    button.setOnClickListener(new OnClickListener() {
	        public void onClick(View v) {
	            // Perform action on clicks
	            _comm.SendKey(sendKey);
	        }
	    });
    }
    
    private final void setupPlaybackCmdButtonEvent(int buttonViewId, final String sendCmd)
    {
    	final Button button = (Button) this.findViewById(buttonViewId);
	    button.setOnClickListener(new OnClickListener() {
	        public void onClick(View v) {
	            // Perform action on clicks
	            _comm.SendPlaybackCmd(sendCmd);
	        }
	    });
    }


    /** Called when a tab is selected. Returns the layout for the selected tab. 
     * Default is navigation tab */
	public View createTabContent(String tag) {
		
		if(tag == NAME_NAV_TAB)
		{
			return this.getLayoutInflater().inflate(R.layout.navigation, this.getTabHost().getTabContentView(), false);
		}
		else if(tag == NAME_MEDIA_TAB)
		{
			return this.getLayoutInflater().inflate(R.layout.mediacontrol, this.getTabHost().getTabContentView(), false);
		}
		else if(tag == NAME_NUMPAD_TAB)
		{
			return this.getLayoutInflater().inflate(R.layout.numberpad, this.getTabHost().getTabContentView(), false);
		}
		else
		{
			return this.getLayoutInflater().inflate(R.layout.navigation, this.getTabHost().getTabContentView(), false);
		}
	}

}






