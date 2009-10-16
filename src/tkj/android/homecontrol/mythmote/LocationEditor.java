package tkj.android.homecontrol.mythmote;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;

/*
 * Edits a FrontendLocation object
 * */
public class LocationEditor extends Activity {

	private FrontendLocation _location;
	
	public LocationEditor(){ }
	public LocationEditor(FrontendLocation location)
	{
		_location = location;
	}
	
	
	
	/** Called when the activity is first created.*/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
     
        this.setContentView(this.getLayoutInflater().inflate(R.layout.locationeditor, null));
        
        this.setupSaveButtonEvent(R.id.ButtonLocationSave);
        this.setupCancelButtonEvent(R.id.ButtonLocationCancel);
        
        int id = this.getIntent().getIntExtra(FrontendLocation.STR_ID, -1);
        if(id != -1)
        {
        	this._location = new FrontendLocation();
        	this._location.ID = id;
        	this._location.Name = this.getIntent().getStringExtra(FrontendLocation.STR_NAME);
        	this._location.Address = this.getIntent().getStringExtra(FrontendLocation.STR_ADDRESS);
        	this._location.Port = this.getIntent().getIntExtra(FrontendLocation.STR_PORT, 6456);
        	
        	SetUiFromLocation();
        }
    }
    
    private void SetUiFromLocation()
    {
		this.SetName(this._location.Name);
		this.SetAddress(this._location.Address);
		this.SetPort(this._location.Port);
    }
    
	
	private boolean Save()
	{
		if(this._location == null)
			this._location = new FrontendLocation();
		this._location.Name = this.GetName();
		this._location.Address = this.GetAddress();
		this._location.Port = this.GetPort();
		
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.error_input_str);
		builder.setNeutralButton(R.string.ok_str, new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			}});
		if(this._location.Name == null)
		{
			builder.setMessage(R.string.error_invalid_name_str);
			builder.show();
		}
		else if(this._location.Address == null)
		{
			builder.setMessage(R.string.error_invalid_address_str);
			builder.show();
		}
		else if(this._location.Port < 0)
		{
			builder.setMessage(R.string.error_invalid_port_str);
			builder.show();
		}
		else
		{
			LocationDbAdapter adapter = new LocationDbAdapter(this);
			adapter.open();
			if(this._location.ID == -1)
			{
				adapter.createFrontendLocation(this._location.Name, this._location.Address, this._location.Port);
			}
			else
			{
				return adapter.updateFrontendLocation(this._location.ID, this._location.Name, this._location.Address, this._location.Port);
			}
			adapter.close();
			
			return true;
		}
		
		return false;
	}
	
	private void SaveAndExit()
	{
		//only exit if save is successful.
		if(Save())
			this.setResult(RESULT_OK);
	}
	
	
	
	private final String GetName()
	{
		return this.GetTextBoxText(R.id.EditTextLocationName);
	}
	
	private final String GetAddress()
	{
		return this.GetTextBoxText(R.id.EditTextAddress);
	}
	
	private final int GetPort()
	{
		try
		{
			return Integer.parseInt(this.GetTextBoxText(R.id.EditTextPort));
		}
		catch(NumberFormatException e)
		{
			return -1;
		}
	}
	
	private final void SetName(String name)
	{
		this.SetTextBoxText(R.id.EditTextLocationName, name);
	}
	
	private final void SetAddress(String address)
	{
		this.SetTextBoxText(R.id.EditTextAddress, address);
	}
	
	private final void SetPort(int port)
	{
		this.SetTextBoxText(R.id.EditTextPort, Integer.toString(port));
	}
	
	private final String GetTextBoxText(int textBoxViewId)
	{
		final EditText text = (EditText)this.findViewById(textBoxViewId);
		return text.getText().toString();
	}
	
	private final void SetTextBoxText(int textBoxViewId, String text)
	{
		final EditText textBox = (EditText)this.findViewById(textBoxViewId);
		textBox.setText(text);
	}
	
	
	private final void setupSaveButtonEvent(int buttonViewId)
    {
    	final Button button = (Button) this.findViewById(buttonViewId);
	    button.setOnClickListener(new OnClickListener() {
	        public void onClick(View v) {
	            //save location and exit
	        	SaveAndExit();
	        }
	    });
    }
	
	private final void setupCancelButtonEvent(int buttonViewId)
    {
    	final Button button = (Button) this.findViewById(buttonViewId);
	    button.setOnClickListener(new OnClickListener() {
	        public void onClick(View v) {
	            //just exit
	        	setResult(RESULT_CANCELED);
	        }
	    });
    }
	
	
}
