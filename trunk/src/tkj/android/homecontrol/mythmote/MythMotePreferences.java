
package tkj.android.homecontrol.mythmote;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceClickListener;
import android.database.Cursor;
import android.database.sqlite.*;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MythMotePreferences extends PreferenceActivity{

	public static final int NEW_LOCATION_ID = Menu.FIRST;
	public static final int DELETE_LOCATION_ID = Menu.FIRST + 1;
	public static final String MYTHMOTE_SHARED_PREFERENCES_ID = "mythmote.preferences";
	public static final String PREF_SELECTED_LOCATION = "selected-frontend";
	public static final String SELECTED_LOCATION = "Selected Location";
	public static final int REQUEST_LOCATIONEDITOR = 0;
	
	private static int _idIndex;
	private static int _addressIndex;
	private static int _nameIndex;
	private static int _portIndex;
    
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
//        //create base preference screen
//        PreferenceScreen prefScreen = this.getPreferenceManager().createPreferenceScreen(this);
//       
//        //configure all preferences
//        setupPreferences(prefScreen);
//        
//        //set preference screen
//		this.setPreferenceScreen(prefScreen);
    }
	
	@Override
    public void onResume()
    {
		super.onResume();
		
		//create base preference screen
        PreferenceScreen prefScreen = this.getPreferenceManager().createPreferenceScreen(this);
        prefScreen.removeAll();
        
        //configure all preferences
        setupPreferences(prefScreen);
        
        //set preference screen
		this.setPreferenceScreen(prefScreen);
    }


	private void setupPreferences(PreferenceScreen prefScreen) {
		//create Categories
        PreferenceCategory selectedCat = new PreferenceCategory(this);
        selectedCat.setTitle(R.string.selected_location_str);
        PreferenceCategory locationListCat = new PreferenceCategory(this);
        locationListCat.setTitle(R.string.location_list_str);

        //add categories to preference screen
        prefScreen.addPreference(selectedCat);
        prefScreen.addPreference(locationListCat);
        
        //open DB
        LocationDbAdapter _dbAdapter = new LocationDbAdapter(this);
        _dbAdapter.open();
        
        //get list of locations
        Cursor cursor = _dbAdapter.fetchAllFrontendLocations();
        
        //get column indexes 
        _idIndex = cursor.getColumnIndex(LocationDbAdapter.KEY_ROWID);
        _addressIndex = cursor.getColumnIndex(LocationDbAdapter.KEY_ADDRESS);
        _nameIndex = cursor.getColumnIndex(LocationDbAdapter.KEY_NAME);
        _portIndex = cursor.getColumnIndex(LocationDbAdapter.KEY_PORT);
        
        //determine if we have locations saved
        int count = cursor.getCount();
        if(count > 0 && cursor.moveToFirst())
        {
            //get selected frontend id
            int selected = this.getSharedPreferences(MYTHMOTE_SHARED_PREFERENCES_ID, MODE_PRIVATE)
            	.getInt(MythMotePreferences.PREF_SELECTED_LOCATION, cursor.getInt(_idIndex));
        	
        	//put each location in the preference list
        	for(int i=0; i<count; i++)
        	{
        		locationListCat.addPreference(
        				MythMotePreferences.createLocationPreference(
        						this,
        						cursor.getString(_idIndex),
        						cursor.getString(_nameIndex), 
        						cursor.getString(_addressIndex)));
        		
        		if(cursor.getInt(_idIndex) == selected)
        		{
        			//create preference for selected location
        			selectedCat.addPreference(
                			MythMotePreferences.createSelectedLocationPreference(
                					this, SELECTED_LOCATION, cursor.getString(_nameIndex)));
        		}

        		cursor.moveToNext();
        	}
        	
        	//the saved selected location was not found just pick the first one
        	if(selectedCat.getPreferenceCount() <= 0)
        	{
        		cursor.moveToFirst();
        		selectedCat.addPreference(
            			MythMotePreferences.createSelectedLocationPreference(
            					this, SELECTED_LOCATION, cursor.getString(_nameIndex)));
        	}
        }
        cursor.close();
        _dbAdapter.close();
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
        menu.add(0, NEW_LOCATION_ID, 0, R.string.add_location_str).setIcon(R.drawable.menu_add);
        menu.add(0, DELETE_LOCATION_ID, 0, R.string.delete_location_str).setIcon(R.drawable.menu_close_clear_cancel);
        return result;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
		if(item.getItemId() == NEW_LOCATION_ID)
		{
			showLocationEditDialog(this, null);
		}
		else if(item.getItemId() == DELETE_LOCATION_ID)
		{
			LocationDbAdapter _dbAdapter = new LocationDbAdapter(this);
			_dbAdapter.open();
			final Cursor cursor = _dbAdapter.fetchAllFrontendLocations();
	        
	        int count = cursor.getCount();
	        if(count > 0 && cursor.moveToFirst())
	        {
	        	final String[] names = new String[count];
	        	final int[] ids = new int[count];
	        	for(int i=0; i<count; i++)
	        	{
	        		names[i] = cursor.getString(cursor.getColumnIndex(LocationDbAdapter.KEY_NAME));
	        		ids[i] = cursor.getInt(cursor.getColumnIndex(LocationDbAdapter.KEY_ROWID));
	        		cursor.moveToNext();
	        	}
	        	
	        	//show list of locations as a single selected list
	        	final Context context = this;
	        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        	builder.setTitle(R.string.selected_location_str);
	        	builder.setItems(names, new DialogInterface.OnClickListener(){

	        		@Override
	        		public void onClick(DialogInterface dialog,
	        				int which) {
	        			LocationDbAdapter dbAdapter = new LocationDbAdapter(context);
	        			dbAdapter.open();
	        			dbAdapter.deleteFrontendLocation(ids[which]);
	        			dbAdapter.close();
	        		}});
	        	builder.show();
	        }
			cursor.close();
	        _dbAdapter.close();
		}
		return true;
    }

	
	
	private static void showLocationEditDialog(Activity context, FrontendLocation location)
	{
		Intent intent = new Intent(context, tkj.android.homecontrol.mythmote.LocationEditor.class);
		
		//put extra information is needed
		if(location != null)
		{
			intent.putExtra(FrontendLocation.STR_ID, location.ID);
			intent.putExtra(FrontendLocation.STR_NAME, location.Name);
			intent.putExtra(FrontendLocation.STR_ADDRESS, location.Address);
			intent.putExtra(FrontendLocation.STR_PORT, location.Port);
		}
		
		//start activity
		context.startActivity(intent);
	}
	
	private static Preference createLocationPreference(final Activity context, String key, String name, String value)
	{
		Preference pref = new Preference(context);
		pref.setKey(key);
		pref.setTitle(name);
		pref.setDefaultValue(value);
		pref.setEnabled(true);
		pref.setSummary(value);
		pref.setOnPreferenceClickListener(new OnPreferenceClickListener(){

			@Override
			public boolean onPreferenceClick(Preference preference) {
				//Open location edit dialog with a location loaded
				FrontendLocation location = new FrontendLocation();
				
				
				location.ID = Integer.parseInt(preference.getKey());
				
				LocationDbAdapter dbAdapter = new LocationDbAdapter(context);
				dbAdapter.open();
				Cursor cursor = dbAdapter.fetchFrontendLocation(location.ID);
				
				//get column indexes 
		        _idIndex = cursor.getColumnIndex(LocationDbAdapter.KEY_ROWID);
		        _addressIndex = cursor.getColumnIndex(LocationDbAdapter.KEY_ADDRESS);
		        _nameIndex = cursor.getColumnIndex(LocationDbAdapter.KEY_NAME);
		        _portIndex = cursor.getColumnIndex(LocationDbAdapter.KEY_PORT);
				
				if(cursor != null && cursor.getCount() > 0)
				{
					location.Name = cursor.getString(_nameIndex);
					location.Address = cursor.getString(_addressIndex);
					location.Port = cursor.getInt(_portIndex);
					showLocationEditDialog(context, location);
				}
				return false;
			}
			
		});
		return pref;
	}
	
	private static Preference createDeleteLocationPreference(final Activity context, String name, String value)
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
				final LocationDbAdapter _dbAdapter = new LocationDbAdapter(context);
				_dbAdapter.open();
				Cursor cursor = _dbAdapter.fetchAllFrontendLocations();
		        
				//get column indexes 
		        _idIndex = cursor.getColumnIndex(LocationDbAdapter.KEY_ROWID);
		        _addressIndex = cursor.getColumnIndex(LocationDbAdapter.KEY_ADDRESS);
		        _nameIndex = cursor.getColumnIndex(LocationDbAdapter.KEY_NAME);
		        _portIndex = cursor.getColumnIndex(LocationDbAdapter.KEY_PORT);
		        
		        int count = cursor.getCount();
		        if(count > 0 && cursor.moveToFirst())
		        {
		        	String[] names = new String[count];

		        	for(int i=0; i<count; i++)
		        	{
		        		names[i] = cursor.getString(_nameIndex);
		        		cursor.moveToNext();
		        	}
		        	cursor.close();
		        	//show list of locations as a single selected list
		        	AlertDialog.Builder builder = new AlertDialog.Builder(context);
		        	builder.setTitle(R.string.selected_location_str);
		        	builder.setItems(names, new DialogInterface.OnClickListener(){

		        		@Override
		        		public void onClick(DialogInterface dialog,
		        				int which) {
		        			_dbAdapter.deleteFrontendLocation(which);
		        		}});
		        	builder.show();
		        }
				
		        _dbAdapter.close();
		        
				return false;
			}
			
		});
		return pref;
	}
	
	private static Preference createSelectedLocationPreference(final Activity context, String name, String value)
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
				
				LocationDbAdapter _dbAdapter = new LocationDbAdapter(context);
				_dbAdapter.open();
				final Cursor cursor = _dbAdapter.fetchAllFrontendLocations();
		        
		        int count = cursor.getCount();
		        if(count > 0 && cursor.moveToFirst())
		        {
		        	final String[] names = new String[count];
		        	final int[] ids = new int[count];
		        	for(int i=0; i<count; i++)
		        	{
		        		names[i] = cursor.getString(cursor.getColumnIndex(LocationDbAdapter.KEY_NAME));
		        		ids[i] = cursor.getInt(cursor.getColumnIndex(LocationDbAdapter.KEY_ROWID));
		        		cursor.moveToNext();
		        	}
		        	
		        	//show list of locations as a single selected list
		        	AlertDialog.Builder builder = new AlertDialog.Builder(context);
		        	builder.setTitle(R.string.selected_location_str);
		        	builder.setItems(names, new DialogInterface.OnClickListener(){

		        		@Override
		        		public void onClick(DialogInterface dialog,
		        				int which) {
		        			SharedPreferences settings = context.getSharedPreferences(MYTHMOTE_SHARED_PREFERENCES_ID, MODE_PRIVATE);
		        			SharedPreferences.Editor editor = settings.edit();
		        			editor.putInt(MythMotePreferences.PREF_SELECTED_LOCATION, ids[which]);
		        			editor.commit();
		        		}});
		        	builder.show();
		        }
				cursor.close();
		        _dbAdapter.close();
				
				return false;
			}
			
		});
		return pref;
	}
	

}