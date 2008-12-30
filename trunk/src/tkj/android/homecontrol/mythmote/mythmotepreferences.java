
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
	
	static final String PREF_ADDRESSES = "frontend-addresses";
	
	private String[] addressesArray = null;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //create base preference screen
        PreferenceScreen prefScreen = this.getPreferenceManager().createPreferenceScreen(this);
        
        //load private string preference that contains all saved locations
        String addressesStr = this.getPreferences(0).getString(PREF_ADDRESSES, null);
        if(addressesStr != null)
        {
        	//split into individual addresses
	        this.addressesArray = addressesStr.split("|");
	        
        }
        else
        {
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