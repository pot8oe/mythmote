
package tkj.android.homecontrol.mythmote;


import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.view.Menu;

public class MythMotePreferences extends PreferenceActivity{

	public static final int NEW_LOCATION_ID = Menu.FIRST;
	
	static final String SEPERATOR_PIPE_REGEX = "\\|";
	static final String PREF_ADDRESSES = "frontend-addresses";
	static final String PREF_NAMES = "frontend-names";
	
	private String[] locationNames = null;
	private String[] locationAddresses = null;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //create base preference screen
        PreferenceScreen prefScreen = this.getPreferenceManager().createPreferenceScreen(this);
        
        //load preferences
        final String nameStr = this.getPreferences(0).getString(PREF_NAMES, null);			//"Living Room|Bed Room|Kitchen";
        final String addressesStr = this.getPreferences(0).getString(PREF_ADDRESSES, null);	//"192.168.1.101|192.168.1.102|192.168.1.3";
        
        if(addressesStr != null && nameStr != null)
        {
        	//split into individual addresses
	        this.locationAddresses = addressesStr.split(SEPERATOR_PIPE_REGEX);
	        this.locationNames = nameStr.split(SEPERATOR_PIPE_REGEX);
	        
	        if(this.locationAddresses == null || this.locationNames == null || this.locationAddresses.length != this.locationNames.length)
	        {
	        	//warn that settings may be corrupt
	        	AlertDialog.Builder diag = new AlertDialog.Builder(this);
				diag.setMessage("Settings may be corrupt!");
				diag.setTitle("Warning!");
				diag.setNeutralButton("OK", null);
				diag.show();
	        }
	        
	        //create preference objects and add to preference screen
	        for(int i=0; 
	        	i<this.locationAddresses.length && i<this.locationNames.length; 
	        	i++)
	        {
	        	prefScreen.addPreference(
	        			MythMotePreferences.createPreference(this, this.locationNames[i], this.locationAddresses[i]));
	        }
	        
        }
        else
        {
        	// no preferences saved yet
        	prefScreen.addPreference(
    	        	MythMotePreferences.createPreference(this, "Create a new location", null));
        }
        
        //set preference screen
		this.setPreferenceScreen(prefScreen);
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
        menu.add(0, NEW_LOCATION_ID, 0, R.string.add_location).setIcon(R.drawable.settings);
        return result;
	}
	
	
	
	
	
	
	private static Preference createPreference(Context context, String name, String value)
	{
		Preference pref = new Preference(context);
		pref.setKey(name);
		pref.setTitle(name);
		pref.setDefaultValue(value);
		pref.setEnabled(true);
		pref.setSummary(value);
		return pref;
	}
	private static Preference createPreference(Context context, String name, String value, boolean enabled)
	{
		Preference pref = new Preference(context);
		pref.setKey(name);
		pref.setTitle(name);
		pref.setDefaultValue(value);
		pref.setEnabled(enabled);
		return pref;
	}
}