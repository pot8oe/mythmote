package tkj.android.homecontrol.mythmote;


import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.Button;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;



public class MythMote extends TabActivity  implements TabHost.TabContentFactory {
    
	public static final int SETTINGS_ID = Menu.FIRST;
	
	public static final String NAME_NAV_TAB = "TabNavigation";
	public static final String NAME_MEDIA_TAB = "TabNMediaControl";
	public static final String NAME_NUMPAD_TAB = "TabNumberPad";
	
	/** Called when the activity is first created.*/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        

        final TabHost tabHost = getTabHost();
        tabHost.addTab(tabHost.newTabSpec(NAME_NAV_TAB).setIndicator(
        		this.getResources().getString(R.string.navigation),
        		this.getResources().getDrawable(R.drawable.starsmall)).setContent(this));
        tabHost.addTab(tabHost.newTabSpec(NAME_MEDIA_TAB).setIndicator(
        		this.getResources().getString(R.string.media),
        		this.getResources().getDrawable(R.drawable.media)).setContent(this));
        tabHost.addTab(tabHost.newTabSpec(NAME_NUMPAD_TAB).setIndicator(
        		"Num Pad").setContent(this));
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
			return this.getNavigationTabLayout();
		}
		else if(tag == NAME_MEDIA_TAB)
		{
			return this.getMediaControlTabLayout();
		}
		else if(tag == NAME_NUMPAD_TAB)
		{
			return this.getNumberPadTabLayout();
		}
		else
		{
			return this.getNavigationTabLayout();
		}
	}
	
	/** Reads the Navigation tab layout from resources */
	protected View getNavigationTabLayout()
	{
		return this.getLayoutInflater().inflate(R.layout.navigation, this.getTabHost().getTabContentView(), false);
	}
	
	/** Reads the Media Control tab layout from resources */
	protected View getMediaControlTabLayout()
	{
		return this.getLayoutInflater().inflate(R.layout.mediacontrol, this.getTabHost().getTabContentView(), false);
	}
	
	/** Reads the Number pad tab layout from resources */
	protected View getNumberPadTabLayout()
	{
		return this.getLayoutInflater().inflate(R.layout.numberpad, this.getTabHost().getTabContentView(), false);
	}

}




















////
////
////	OLD GUI CREATION DONE IN CODE
////
////
///* Table */
//final TableLayout tLayout = new TableLayout(this);
//tLayout.setStretchAllColumns(true);
//
///* Rows */
//final TableRow row1 = new TableRow(this);
//final TableRow row2 = new TableRow(this);
//
///*  Buttons */
//final Button homeButton = mythmote.CreateButton(this, "Home", JUMP_BUTTON_WIDTH, JUMP_BUTTON_HEIGHT);
//final Button guideButton = mythmote.CreateButton(this, "Guide", JUMP_BUTTON_WIDTH, JUMP_BUTTON_HEIGHT);
//final Button tvButton = mythmote.CreateButton(this, "Live TV", JUMP_BUTTON_WIDTH, JUMP_BUTTON_HEIGHT);
//final Button recordedTvButton = mythmote.CreateButton(this, "Rec TV", JUMP_BUTTON_WIDTH, JUMP_BUTTON_HEIGHT);
//final Button musicButton = mythmote.CreateButton(this, "Music", JUMP_BUTTON_WIDTH, JUMP_BUTTON_HEIGHT);
//final Button videoButton = mythmote.CreateButton(this, "Video", JUMP_BUTTON_WIDTH, JUMP_BUTTON_HEIGHT);
//
///* Add buttons to rows */
//row1.addView(homeButton);
//row1.addView(guideButton);
//row1.addView(tvButton);
//
//row2.addView(recordedTvButton);
//row2.addView(musicButton);
//row2.addView(videoButton);
//
///* Add rows to table */
//tLayout.addView(row1);
//tLayout.addView(row2);
//
//
//return tLayout;

