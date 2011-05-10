/*
 * Copyright (C) 2010 Thomas G. Kenny Jr
 *
 * Licensed under the GNU General Public License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 *
 *      http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package tkj.android.homecontrol.mythmote;

import tkj.android.homecontrol.mythmote.db.MythMoteDbManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

/*
 * Edits a FrontendLocation object
 * */
public class LocationEditor extends Activity
{

	private FrontendLocation _location;

	public LocationEditor()
	{
	}

	public LocationEditor(FrontendLocation location)
	{
		_location = location;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		this.setContentView(this.getLayoutInflater().inflate(
				R.layout.locationeditor, null));

		this.setupSaveButtonEvent(R.id.ButtonLocationSave);
		this.setupCancelButtonEvent(R.id.ButtonLocationCancel);

		int id = this.getIntent().getIntExtra(FrontendLocation.STR_ID, -1);
		if (id != -1)
		{
			this._location = new FrontendLocation();
			this._location.ID = id;
			this._location.Name = this.getIntent().getStringExtra(
					FrontendLocation.STR_NAME);
			this._location.Address = this.getIntent().getStringExtra(
					FrontendLocation.STR_ADDRESS);
			this._location.Port = this.getIntent().getIntExtra(
					FrontendLocation.STR_PORT, 6456);

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
		boolean retVal = false;
		if (this._location == null)
			this._location = new FrontendLocation();
		this._location.Name = this.GetName();
		this._location.Address = this.GetAddress();
		this._location.Port = this.GetPort();

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.error_input_str);
		builder.setNeutralButton(R.string.ok_str,
				new DialogInterface.OnClickListener()
				{

					public void onClick(DialogInterface dialog, int which)
					{
						// TODO Auto-generated method stub

					}
				});
		if (this._location.Name.trim().equals(""))
		{
			builder.setMessage(R.string.error_invalid_name_str);
			builder.show();
		}
		else if(this._location.Address.trim().equals(""))
		{
			builder.setMessage(R.string.error_invalid_address_str);
			builder.show();
		}
		else
		{
			// set default port if port was not set.
			if (this._location.Port <= 0)
				this._location.Port = MythCom.DEFAULT_MYTH_PORT;

			MythMoteDbManager adapter = new MythMoteDbManager(this);
			adapter.open();
			if (this._location.ID == -1)
			{
				this._location.ID = (int) adapter.createFrontendLocation(
						this._location.Name, this._location.Address,
						this._location.Port);
				retVal = true;
			} else
			{
				retVal = adapter.updateFrontendLocation(this._location.ID,
						this._location.Name, this._location.Address,
						this._location.Port);
			}
			adapter.close();

			return retVal;
		}

		return retVal;
	}

	private void SaveAndExit()
	{
		// only exit if save is successful.
		if (Save())
			this.finish();
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
		final EditText text = (EditText) this.findViewById(textBoxViewId);
		return text.getText().toString();
	}

	private final void SetTextBoxText(int textBoxViewId, String text)
	{
		final EditText textBox = (EditText) this.findViewById(textBoxViewId);
		textBox.setText(text);
	}

	private final void setupSaveButtonEvent(int buttonViewId)
	{
		final Button button = (Button) this.findViewById(buttonViewId);
		button.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				// save location and exit
				SaveAndExit();
			}
		});
	}

	private final void setupCancelButtonEvent(int buttonViewId)
	{
		final Button button = (Button) this.findViewById(buttonViewId);
		button.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				// just exit
				finish();
			}
		});
	}

}
