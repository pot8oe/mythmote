/*
 * Copyright (C) 2010 Rob Elsner
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

package tkj.android.homecontrol.mythmote.keymanager;

import static tkj.android.homecontrol.mythmote.R.id.Button0;
import static tkj.android.homecontrol.mythmote.R.id.Button1;
import static tkj.android.homecontrol.mythmote.R.id.Button2;
import static tkj.android.homecontrol.mythmote.R.id.Button3;
import static tkj.android.homecontrol.mythmote.R.id.Button4;
import static tkj.android.homecontrol.mythmote.R.id.Button5;
import static tkj.android.homecontrol.mythmote.R.id.Button6;
import static tkj.android.homecontrol.mythmote.R.id.Button7;
import static tkj.android.homecontrol.mythmote.R.id.Button8;
import static tkj.android.homecontrol.mythmote.R.id.Button9;
import static tkj.android.homecontrol.mythmote.R.id.ButtonBackspace;
import static tkj.android.homecontrol.mythmote.R.id.ButtonChDown;
import static tkj.android.homecontrol.mythmote.R.id.ButtonChReturn;
import static tkj.android.homecontrol.mythmote.R.id.ButtonChUp;
import static tkj.android.homecontrol.mythmote.R.id.ButtonDown;
import static tkj.android.homecontrol.mythmote.R.id.ButtonEnter;
import static tkj.android.homecontrol.mythmote.R.id.ButtonEsc;
import static tkj.android.homecontrol.mythmote.R.id.ButtonFF;
import static tkj.android.homecontrol.mythmote.R.id.ButtonGuide;
import static tkj.android.homecontrol.mythmote.R.id.ButtonInfo;
import static tkj.android.homecontrol.mythmote.R.id.ButtonJump1;
import static tkj.android.homecontrol.mythmote.R.id.ButtonJump2;
import static tkj.android.homecontrol.mythmote.R.id.ButtonJump3;
import static tkj.android.homecontrol.mythmote.R.id.ButtonJump4;
import static tkj.android.homecontrol.mythmote.R.id.ButtonJump5;
import static tkj.android.homecontrol.mythmote.R.id.ButtonJump6;
import static tkj.android.homecontrol.mythmote.R.id.ButtonLeft;
import static tkj.android.homecontrol.mythmote.R.id.ButtonMenu;
import static tkj.android.homecontrol.mythmote.R.id.ButtonMute;
import static tkj.android.homecontrol.mythmote.R.id.ButtonPause;
import static tkj.android.homecontrol.mythmote.R.id.ButtonPlay;
import static tkj.android.homecontrol.mythmote.R.id.ButtonRecord;
import static tkj.android.homecontrol.mythmote.R.id.ButtonRew;
import static tkj.android.homecontrol.mythmote.R.id.ButtonRight;
import static tkj.android.homecontrol.mythmote.R.id.ButtonSelect;
import static tkj.android.homecontrol.mythmote.R.id.ButtonSkipBack;
import static tkj.android.homecontrol.mythmote.R.id.ButtonSkipForward;
import static tkj.android.homecontrol.mythmote.R.id.ButtonStop;
import static tkj.android.homecontrol.mythmote.R.id.ButtonUp;
import static tkj.android.homecontrol.mythmote.R.id.ButtonVolDown;
import static tkj.android.homecontrol.mythmote.R.id.ButtonVolUp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tkj.android.homecontrol.mythmote.MythCom;
import tkj.android.homecontrol.mythmote.MythMote;
import tkj.android.homecontrol.mythmote.R;
import tkj.android.homecontrol.mythmote.db.MythMoteDbManager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.EditText;

public class KeyBindingManager implements KeyMapBinder, OnClickListener,
		OnLongClickListener {
	
	AlertDialog alertDialog;
	
	/**
	 * Add a value here which is the button name (preferably similar to the
	 * layout id) and the default action
	 * 
	 * @author robelsner
	 * 
	 */
	public enum MythKey {
		BUTTON_0("key 0", Button0), BUTTON_1("key 1", Button1), BUTTON_2(
				"key 2", Button2), BUTTON_3("key 3", Button3), BUTTON_4(
				"key 4", Button4), BUTTON_5("key 5", Button5), BUTTON_6(
				"key 6", Button6), BUTTON_7("key 7", Button7), BUTTON_8(
				"key 8", Button8), BUTTON_9("key 9", Button9), BUTTON_BACKSPACE(
				"key backspace", ButtonBackspace), BUTTON_CHANNEL_DOWN(
				"play channel down", ButtonChDown), BUTTON_CHANNEL_UP(
				"play channel up", ButtonChUp), BUTTON_CHANNEL_RECALL("key h",
				ButtonChReturn), BUTTON_ESCAPE("key escape", ButtonEsc), BUTTON_ENTER(
				"key enter", ButtonEnter), BUTTON_RECORD("key r", ButtonRecord), BUTTON_STOP(
				"play stop", ButtonStop), BUTTON_PLAY("play speed normal",
				ButtonPlay), BUTTON_PAUSE("play speed pause", ButtonPause), BUTTON_DOWN(
				"key down", ButtonDown), BUTTON_UP("key up", ButtonUp), BUTTON_LEFT(
				"key left", ButtonLeft), BUTTON_RIGHT("key right", ButtonRight), BUTTON_SELECT(
				"key enter", ButtonSelect), BUTTON_FAST_FORWARD(
				"play seek forward", ButtonFF), BUTTON_REWIND(
				"play seek backward", ButtonRew), BUTTON_SKIP_FORWARD(
				"key end", ButtonSkipForward), BUTTON_SKIP_BACKWARD("key home",
				ButtonSkipBack), BUTTON_GUIDE("key s", ButtonGuide), BUTTON_INFO(
				"key i", ButtonInfo), BUTTON_JUMP_1("jump mainmenu",
				ButtonJump1), BUTTON_JUMP_2("jump livetv", ButtonJump2), BUTTON_JUMP_3(
				"jump playbackrecordings", ButtonJump3), BUTTON_JUMP_4(
				"jump playmusic", ButtonJump4), BUTTON_JUMP_5(
				"jump videogallery", ButtonJump5), BUTTON_JUMP_6(
				"jump statusbox", ButtonJump6), BUTTON_MENU("key m", ButtonMenu), BUTTON_MUTE(
				"key |", ButtonMute), BUTTON_VOLUME_UP("key ]", ButtonVolUp), BUTTON_VOLUME_DOWN(
				"key [", ButtonVolDown);

		private final String defaultCommand;
		private final int layoutId;

		private MythKey(final String command, final int layoutId) {
			this.defaultCommand = command;
			this.layoutId = layoutId;
		}

		public final String getDefaultCommand() {
			return defaultCommand;
		}

		public final int getButtonId() {
			return layoutId;
		}

		public static MythKey getByName(final String name) {
			for (MythKey key : MythKey.values()) {
				if (key.name().equals(name))
					return key;
			}
			return MythKey.BUTTON_0;
		}

		public static List<KeyBindingEntry> createDefaultList() {
			ArrayList<KeyBindingEntry> entries = new ArrayList<KeyBindingEntry>();
			entries.add(new KeyBindingEntry("0", BUTTON_0,
					BUTTON_0.defaultCommand, false));
			entries.add(new KeyBindingEntry("1", BUTTON_1,
					BUTTON_1.defaultCommand, false));
			entries.add(new KeyBindingEntry("2", BUTTON_2,
					BUTTON_2.defaultCommand, false));
			entries.add(new KeyBindingEntry("3", BUTTON_3,
					BUTTON_3.defaultCommand, false));
			entries.add(new KeyBindingEntry("4", BUTTON_4,
					BUTTON_4.defaultCommand, false));
			entries.add(new KeyBindingEntry("5", BUTTON_5,
					BUTTON_5.defaultCommand, false));
			entries.add(new KeyBindingEntry("6", BUTTON_6,
					BUTTON_6.defaultCommand, false));
			entries.add(new KeyBindingEntry("7", BUTTON_7,
					BUTTON_7.defaultCommand, false));
			entries.add(new KeyBindingEntry("8", BUTTON_8,
					BUTTON_8.defaultCommand, false));
			entries.add(new KeyBindingEntry("9", BUTTON_9,
					BUTTON_9.defaultCommand, false));
			entries.add(new KeyBindingEntry("Backspace", BUTTON_BACKSPACE,
					BUTTON_BACKSPACE.defaultCommand, false));
			entries.add(new KeyBindingEntry("Down", BUTTON_CHANNEL_DOWN,
					BUTTON_CHANNEL_DOWN.defaultCommand, false));
			entries.add(new KeyBindingEntry("Up", BUTTON_CHANNEL_UP,
					BUTTON_CHANNEL_UP.defaultCommand, false));
			entries.add(new KeyBindingEntry("Recall", BUTTON_CHANNEL_RECALL,
					BUTTON_CHANNEL_RECALL.defaultCommand, false));
			entries.add(new KeyBindingEntry("Esc", BUTTON_ESCAPE,
					BUTTON_ESCAPE.defaultCommand, false));
			entries.add(new KeyBindingEntry("Enter", BUTTON_ENTER,
					BUTTON_ENTER.defaultCommand, false));
			entries.add(new KeyBindingEntry("Rec", BUTTON_RECORD,
					BUTTON_RECORD.defaultCommand, false));
			entries.add(new KeyBindingEntry("Stop", BUTTON_STOP,
					BUTTON_STOP.defaultCommand, false));
			entries.add(new KeyBindingEntry("Play", BUTTON_PLAY,
					BUTTON_PLAY.defaultCommand, false));
			entries.add(new KeyBindingEntry("Pause", BUTTON_PAUSE,
					BUTTON_PAUSE.defaultCommand, false));
			entries.add(new KeyBindingEntry("Down", BUTTON_DOWN,
					BUTTON_DOWN.defaultCommand, false));
			entries.add(new KeyBindingEntry("Up", BUTTON_UP,
					BUTTON_UP.defaultCommand, false));
			entries.add(new KeyBindingEntry("Left", BUTTON_LEFT,
					BUTTON_LEFT.defaultCommand, false));
			entries.add(new KeyBindingEntry("Right", BUTTON_RIGHT,
					BUTTON_RIGHT.defaultCommand, false));
			entries.add(new KeyBindingEntry("Select", BUTTON_SELECT,
					BUTTON_SELECT.defaultCommand, false));
			entries.add(new KeyBindingEntry("FF", BUTTON_FAST_FORWARD,
					BUTTON_FAST_FORWARD.defaultCommand, false));
			entries.add(new KeyBindingEntry("Rew", BUTTON_REWIND,
					BUTTON_REWIND.defaultCommand, false));
			entries.add(new KeyBindingEntry("Start", BUTTON_SKIP_BACKWARD,
					BUTTON_SKIP_BACKWARD.defaultCommand, false));
			entries.add(new KeyBindingEntry("End", BUTTON_SKIP_FORWARD,
					BUTTON_SKIP_FORWARD.defaultCommand, false));
			entries.add(new KeyBindingEntry("Guide", BUTTON_GUIDE,
					BUTTON_GUIDE.defaultCommand, false));
			entries.add(new KeyBindingEntry("Info", BUTTON_INFO,
					BUTTON_INFO.defaultCommand, false));
			entries.add(new KeyBindingEntry("Main", BUTTON_JUMP_1,
					BUTTON_JUMP_1.defaultCommand, false));
			entries.add(new KeyBindingEntry("TV", BUTTON_JUMP_2,
					BUTTON_JUMP_2.defaultCommand, false));
			entries.add(new KeyBindingEntry("Recordings", BUTTON_JUMP_3,
					BUTTON_JUMP_3.defaultCommand, false));
			entries.add(new KeyBindingEntry("Music", BUTTON_JUMP_4,
					BUTTON_JUMP_4.defaultCommand, false));
			entries.add(new KeyBindingEntry("Videos", BUTTON_JUMP_5,
					BUTTON_JUMP_5.defaultCommand, false));
			entries.add(new KeyBindingEntry("Status", BUTTON_JUMP_6,
					BUTTON_JUMP_6.defaultCommand, false));
			entries.add(new KeyBindingEntry("Menu", BUTTON_MENU,
					BUTTON_MENU.defaultCommand, false));
			entries.add(new KeyBindingEntry("Mute", BUTTON_MUTE,
					BUTTON_MUTE.defaultCommand, false));
			entries.add(new KeyBindingEntry("Vol Up", BUTTON_VOLUME_UP,
					BUTTON_VOLUME_UP.defaultCommand, false));
			entries.add(new KeyBindingEntry("Vol Down", BUTTON_VOLUME_DOWN,
					BUTTON_VOLUME_DOWN.defaultCommand, false));
			return entries;
		}
	}

	private KeyMapBinder binder = null;

	private Map<View, KeyBindingEntry> viewToEntryMap = new HashMap<View, KeyBindingEntry>();

	private MythCom communicator;

	private MythMoteDbManager databaseAdapter;
	
	private boolean mHapticFeedbackEnabled = false;
	
	private boolean mEditingEnabled = true;

	public KeyBindingManager(final Context ctx, final KeyMapBinder binder,
			final MythCom communicator) {
		Log.d(MythMote.LOG_TAG, "Created KeyBindingManager with ctx " + ctx
				+ " binder " + binder + " comm " + communicator);
		this.databaseAdapter = new MythMoteDbManager(ctx);

		this.binder = binder;
		this.communicator = communicator;

	}

	public void loadKeys() {
		Log.d(MythMote.LOG_TAG, "loadKeys with dba " + databaseAdapter);
		databaseAdapter.open();
		databaseAdapter.loadKeyMapEntries(this);
		databaseAdapter.close();
	}

	public View bind(KeyBindingEntry entry) {
		Log.d(MythMote.LOG_TAG, "Bind " + entry.getFriendlyName() + " to "
				+ entry.getCommand());
		View v = binder.bind(entry);
		viewToEntryMap.put(v, entry);
		return v;
	}

	public KeyBindingEntry getCommand(final View initiatingView) {
		return viewToEntryMap.get(initiatingView);
	}
	
	public boolean getHapticFeedbackEnabled(){
		return mHapticFeedbackEnabled;
	}
	
	public void setHapticFeedbackEnabled(boolean enabled){
		mHapticFeedbackEnabled = enabled;
	}
	
	public boolean getEditingEnabled(){
		return mEditingEnabled;
	}
	
	public void setEditingEnabled(boolean enabled){
		mEditingEnabled = enabled;
	}

	public void onClick(View v) {

		KeyBindingEntry entry = viewToEntryMap.get(v);

		if (null != entry && null != communicator) {
			Log.d(MythMote.LOG_TAG, "onClick " + entry.getFriendlyName()
					+ " command " + entry.getCommand());
			
			//send command
			communicator.SendCommand(entry.getCommand());
			
			//perform haptic feedback if enabled
			if(mHapticFeedbackEnabled){
			v.performHapticFeedback(
					HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING,
					HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
			}
		}

	}

	public boolean onLongClick(final View v) {
		
		//do not consume the onLongClick event if editing is disabled
		if(!mEditingEnabled) {
			onClick(v);
			return true;
		}

		// prevent a repeated onLongClick from opening multiple alert dialogs
		if ( alertDialog != null && alertDialog.isShowing())
		{
			return true;
		} else {
			alertDialog = null;
		}
			
		//create alert dialog 
		AlertDialog.Builder alert= new AlertDialog.Builder(v.getContext());

		//set alert title and message
		alert.setTitle(R.string.command_edit_title_str);
		alert.setMessage(R.string.command_edit_msg_str);

		// Set an EditText view to get user input
		final EditText input = new EditText(v.getContext());
		KeyBindingEntry currentEntry = viewToEntryMap.get(v);
		if (null != currentEntry)
			input.setText(currentEntry.getCommand());
		alert.setView(input);

		//set positive action button
		alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				Editable value = input.getText();
				KeyBindingEntry oldEntry = viewToEntryMap.get(v);
				if (null != oldEntry && null != communicator) {
					Log.d(MythMote.LOG_TAG,
							"onLongClick " + oldEntry.getFriendlyName());
					KeyBindingEntry entry = new KeyBindingEntry(oldEntry
							.getRowID(), oldEntry.getFriendlyName(), oldEntry
							.getMythKey(), value.toString(), oldEntry
							.requiresConfirmation());
					viewToEntryMap.put(v, entry);
					databaseAdapter.save(entry);
				}
			}
		});

		//set negative action button
		alert.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
					}
				});

		//present alert dialog to user
		alertDialog = alert.show();
		
		//return true, we consumed the long-press
		return true;
	}
}
