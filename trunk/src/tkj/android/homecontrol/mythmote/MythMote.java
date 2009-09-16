package tkj.android.homecontrol.mythmote;


import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TabHost;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;



public class MythMote extends TabActivity  implements TabHost.TabContentFactory {
    
	public static final int SETTINGS_ID = Menu.FIRST;
	public static final int RECONNECT_ID = Menu.FIRST + 1;
	public static final String NAME_NAV_TAB = "TabNavigation";
	public static final String NAME_MEDIA_TAB = "TabNMediaControl";
	public static final String NAME_NUMPAD_TAB = "TabNumberPad";
	
	private static MythCom _comm;
	private static MythCom.StatusChangedEventListener _statusChanged;
	private static String _address = "192.168.1.100";
	private static int _port = 6546;

	
	/** Called when the activity is first created.*/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //Create status changed event handler
        _statusChanged = new MythCom.StatusChangedEventListener(){

    		public void StatusChanged(String StatusMsg, int code) {
    			//set title
    			setTitle(String.format("%s - %s", getString(R.string.app_name), StatusMsg));
    			
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
        
    	//request window features
        super.requestWindowFeature(Window.FEATURE_RIGHT_ICON);
       // super.setProgressBarIndeterminate(true);
        super.setProgressBarIndeterminateVisibility(true);
        this.setProgress(9000);
        
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
        		"Num Pad").setContent(this)); 
        
        
        //setup event handlers
        setupNavigationPanelButtonEvents();
    }
    
    @Override
    public void onResume()
    {
    	super.onResume();
    	_comm.Connect(_address, _port);
    }
    
    @Override
    public void onPause()
    {
    	super.onPause();
    	
    	_comm.Disconnect();

    }
    
    @Override
    public void onWindowFocusChanged  (boolean hasFocus)
    {
    	super.onWindowFocusChanged(hasFocus);
    	
    	if(hasFocus)
    	{
    		
    	}
    	else
    	{
    		
    	}
    }
    
    /** Called to create the options menu once.  */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        menu.add(0, SETTINGS_ID, 0, R.string.settings_menu_str).setIcon(R.drawable.settings);
        menu.add(0, RECONNECT_ID, 0, R.string.reconnect_str);
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
    		_comm.Connect(_address, _port);
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
    
    public static void SendMythFrontendCommand()
    {
    	
    }
    
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
	    this.setupKeyButtonEvent(R.id.ButtonUp, MythCom.KEY_up);
	    this.setupKeyButtonEvent(R.id.ButtonDown, MythCom.KEY_down);
	    this.setupKeyButtonEvent(R.id.ButtonLeft, MythCom.KEY_left);
	    this.setupKeyButtonEvent(R.id.ButtonRight, MythCom.KEY_right);
	    this.setupKeyButtonEvent(R.id.ButtonSelect, MythCom.KEY_enter);
	    
    }
    
    
    private void setupJumpButtonEvent(int buttonViewId, final String jumpPoint)
    {
    	final Button buttonJump = (Button) this.findViewById(buttonViewId);
	    buttonJump.setOnClickListener(new OnClickListener() {
	        public void onClick(View v) {
	            // Perform action on clicks
	            _comm.SendJumpCommand(jumpPoint);
	        }
	    });
    }
    
    private void setupKeyButtonEvent(int buttonViewId, final String sendKey)
    {
    	final Button button = (Button) this.findViewById(buttonViewId);
	    button.setOnClickListener(new OnClickListener() {
	        public void onClick(View v) {
	            // Perform action on clicks
	            _comm.SendKey(sendKey);
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






