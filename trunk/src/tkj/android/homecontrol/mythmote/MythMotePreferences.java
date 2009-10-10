
package tkj.android.homecontrol.mythmote;


import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceClickListener;
import android.view.Menu;
import android.view.MenuItem;

public class MythMotePreferences extends PreferenceActivity{

	public static final int NEW_LOCATION_ID = Menu.FIRST;
	
	static final String SEPERATOR_PIPE_REGEX = "\\|";
	static final String PREF_ADDRESSES = "frontend-addresses";
	static final String PREF_NAMES = "frontend-names";
	static final String PREF_PORTS = "frontend-ports";
	
	private String[] locationNames = null;
	private String[] locationAddresses = null;
	private int[] locationPorts = null;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //create base preference screen
        PreferenceScreen prefScreen = this.getPreferenceManager().createPreferenceScreen(this);
        
        //load preferences
        final String nameStr = this.getPreferences(MODE_PRIVATE).getString(PREF_NAMES, null);			//"Living Room|Bed Room|Kitchen";
        final String addressesStr = this.getPreferences(MODE_PRIVATE).getString(PREF_ADDRESSES, null);	//"192.168.1.101|192.168.1.102|192.168.1.3";
        final String portsStr = this.getPreferences(MODE_PRIVATE).getString(PREF_PORTS, null);			//"6546|6546|6546"
        
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
	        int locationLen = this.locationAddresses.length;
	        int namesLen = this.locationNames.length;
	        for(int i=0; i<locationLen && i<namesLen; i++)
	        {
	        	prefScreen.addPreference(
	        			MythMotePreferences.createPreference(this, this.locationNames[i], this.locationAddresses[i]));
	        }
	        
        }
        else
        {
        	// no preferences saved yet
        	prefScreen.addPreference(
    	        	MythMotePreferences.createPreference(this, this.getString(R.string.add_location_str), null));
        }
        
        //set preference screen
		this.setPreferenceScreen(prefScreen);
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
        menu.add(0, NEW_LOCATION_ID, 0, R.string.add_location_str).setIcon(R.drawable.settings);
        return result;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
		if(item.getItemId() == NEW_LOCATION_ID)
		{
			showLocationEditDialog(this);
		}
		return true;
    }
	
	
	
	private void showLocationEditDialog(Context context)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(context.getString(R.string.add_location_str));
		builder.setView(this.findViewById(R.layout.locationeditor));
		builder.show();
	}
	
	private static Preference createPreference(final Context context, String name, String value)
	{
		Preference pref = new Preference(context);
		pref.setKey(name);
		pref.setTitle(name);
		pref.setDefaultValue(value);
		pref.setEnabled(true);
		pref.setSummary(value);
		pref.setOnPreferenceClickListener(new OnPreferenceClickListener(){

			@Override
			public boolean onPreferenceClick(Preference preference) {
				
				//if the add new location preference is clicked
				if(preference.getKey() == context.getString(R.string.add_location_str))
				{
					
				}
				else
				{
					
				}
				
				return false;
			}
			
		});
		return pref;
	}
	
	
	
//	private static Preference createPreference(Context context, String name, String value, boolean enabled)
//	{
//		Preference pref = new Preference(context);
//		pref.setKey(name);
//		pref.setTitle(name);
//		pref.setDefaultValue(value);
//		pref.setEnabled(enabled);
//		return pref;
//	}
}