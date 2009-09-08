package tkj.android.homecontrol.mythmote;


import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;



public class MythMote extends TabActivity  implements TabHost.TabContentFactory {
    
	public static final int SETTINGS_ID = Menu.FIRST;
	
	public static final String NAME_NAV_TAB = "TabNavigation";
	public static final String NAME_MEDIA_TAB = "TabNMediaControl";
	public static final String NAME_NUMPAD_TAB = "TabNumberPad";
	
	private static MythCom _comm;

	
	/** Called when the activity is first created.*/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //create comm class
        _comm = new MythCom(this);
        
        //create tab UI
        final TabHost tabHost = getTabHost();
        tabHost.addTab(tabHost.newTabSpec(NAME_NAV_TAB).setIndicator(
        		this.getResources().getString(R.string.navigation_str),
        		this.getResources().getDrawable(R.drawable.starsmall)).setContent(this));
        tabHost.addTab(tabHost.newTabSpec(NAME_MEDIA_TAB).setIndicator(
        		this.getResources().getString(R.string.media_str),
        		this.getResources().getDrawable(R.drawable.media)).setContent(this));
        tabHost.addTab(tabHost.newTabSpec(NAME_NUMPAD_TAB).setIndicator(
        		"Num Pad").setContent(this));
    }
    
    @Override
    public void onResume()
    {
    	super.onResume();
    	_comm.Connect("192.168.1.100", 6564);
    }
    
    @Override
    public void onWindowFocusChanged  (boolean hasFocus)
    {
    	if(hasFocus)
    	{
    		
    		
    		
    		
    		
    	}
    	else
    	{
    		_comm.Disconnect();
    	}
    }
    
    /** Called to create the options menu once.  */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        menu.add(0, SETTINGS_ID, 0, R.string.settings_menu_str).setIcon(R.drawable.settings);
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






