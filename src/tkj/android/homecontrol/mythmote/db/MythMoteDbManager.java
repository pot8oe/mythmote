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

package tkj.android.homecontrol.mythmote.db;

import static tkj.android.homecontrol.mythmote.db.MythMoteDbHelper.FRONTEND_TABLE;
import static tkj.android.homecontrol.mythmote.db.MythMoteDbHelper.KEYBINDINGS_COMMAND;
import static tkj.android.homecontrol.mythmote.db.MythMoteDbHelper.KEYBINDINGS_FRIENDLY_NAME;
import static tkj.android.homecontrol.mythmote.db.MythMoteDbHelper.KEYBINDINGS_REQUIRE_CONFIRMATION;
import static tkj.android.homecontrol.mythmote.db.MythMoteDbHelper.KEYBINDINGS_ROWID;
import static tkj.android.homecontrol.mythmote.db.MythMoteDbHelper.KEYBINDINGS_UI_KEY;
import static tkj.android.homecontrol.mythmote.db.MythMoteDbHelper.KEY_ADDRESS;
import static tkj.android.homecontrol.mythmote.db.MythMoteDbHelper.KEY_BINDINGS_TABLE;
import static tkj.android.homecontrol.mythmote.db.MythMoteDbHelper.KEY_NAME;
import static tkj.android.homecontrol.mythmote.db.MythMoteDbHelper.KEY_PORT;
import static tkj.android.homecontrol.mythmote.db.MythMoteDbHelper.KEY_ROWID;
import static tkj.android.homecontrol.mythmote.db.MythMoteDbHelper.KEY_MAC;
import static tkj.android.homecontrol.mythmote.db.MythMoteDbHelper.KEY_WIFIONLY;
import tkj.android.homecontrol.mythmote.FrontendLocation;
import tkj.android.homecontrol.mythmote.R;
import tkj.android.homecontrol.mythmote.keymanager.KeyBindingEntry;
import tkj.android.homecontrol.mythmote.keymanager.KeyBindingManager.MythKey;
import tkj.android.homecontrol.mythmote.keymanager.KeyMapBinder;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class MythMoteDbManager {
	private SQLiteDatabase db;
	private MythMoteDbHelper dbHelper;
	private final Context context;

	public MythMoteDbManager(final Context ctx) {
		this.context = ctx;
	}

	public void open() {
		this.dbHelper = new MythMoteDbHelper(context);
		this.db = this.dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	/**
	 * Create a new note using the title and body provided. If the note is
	 * successfully created return the new rowId for that note, otherwise return
	 * a -1 to indicate failure.
	 * 
	 * @param title
	 *            the title of the note
	 * @param body
	 *            the body of the note
	 * @return rowId or -1 if failed
	 */
	public long createFrontendLocation(FrontendLocation location) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_NAME, location.Name);
		initialValues.put(KEY_ADDRESS, location.Address);
		initialValues.put(KEY_PORT, location.Port);
		initialValues.put(KEY_MAC, location.Port);
		initialValues.put(KEY_WIFIONLY, location.WifiOnly);

		return db.insert(FRONTEND_TABLE, null, initialValues);
	}

	/**
	 * Delete the note with the given rowId
	 * 
	 * @param rowId
	 *            id of note to delete
	 * @return true if deleted, false otherwise
	 */
	public boolean deleteFrontendLocation(long rowId) {

		return db.delete(FRONTEND_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
	}

	/**
	 * Return a Cursor over the list of all notes in the database
	 * 
	 * @return Cursor over all notes
	 */
	public Cursor fetchAllFrontendLocations() {

		return db.query(FRONTEND_TABLE, new String[] { KEY_ROWID, KEY_NAME,
				KEY_ADDRESS, KEY_PORT, KEY_MAC, KEY_WIFIONLY }, null, null, null, null, null);
	}

	/**
	 * Return a Cursor positioned at the note that matches the given rowId
	 * 
	 * @param rowId
	 *            id of note to retrieve
	 * @return Cursor positioned to matching note, if found
	 */
	public Cursor fetchFrontendLocation(long rowId) {
		Cursor mCursor = null;
		try {
			mCursor = db.query(true, FRONTEND_TABLE, new String[] { KEY_ROWID,
					KEY_NAME, KEY_ADDRESS, KEY_PORT, KEY_MAC, KEY_WIFIONLY }, KEY_ROWID + "=" + rowId,
					null, null, null, null, null);
			if (mCursor != null) {
				mCursor.moveToFirst();
			}
		} catch (SQLException e) {
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setTitle("DataBase Error");
			builder.setMessage(e.getLocalizedMessage());
			builder.setNeutralButton(R.string.ok_str, new OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub

				}
			});

		}
		return mCursor;

	}

	/**
	 * Update the note using the details provided. The note to be updated is
	 * specified using the rowId, and it is altered to use the title and body
	 * values passed in
	 * 
	 * @param location
	 * 				FrontendLocation object containing values to save to DB
	 * @return true if the note was successfully updated, false otherwise
	 */
	public boolean updateFrontendLocation(FrontendLocation location) {
		open();
		ContentValues args = new ContentValues();
		args.put(KEY_NAME, location.Name);
		args.put(KEY_ADDRESS, location.Address);
		args.put(KEY_PORT, location.Port);
		args.put(KEY_MAC, location.MAC);
		args.put(KEY_WIFIONLY, location.WifiOnly);

		int rows = db.update(FRONTEND_TABLE, args, KEY_ROWID + "=" + location.ID,
				null);
		close();
		return rows > 0;
	}

	public boolean save(final KeyBindingEntry entry) {
		open();
		ContentValues values = new ContentValues();
		values.put(KEYBINDINGS_COMMAND, entry.getCommand());
		values.put(KEYBINDINGS_UI_KEY, entry.getMythKey().name());
		values.put(KEYBINDINGS_FRIENDLY_NAME, entry.getFriendlyName());
		values.put(KEYBINDINGS_REQUIRE_CONFIRMATION,
				entry.requiresConfirmation() ? 1 : 0);
		Log.d("KBDA", "Adding entry " + entry.getFriendlyName() + " to "
				+ entry.getCommand());
		boolean success = false;
		if (entry.getRowID() != -1)
			success = db.update(KEY_BINDINGS_TABLE, values,
					MythMoteDbHelper.KEYBINDINGS_ROWID + " = ?",
					new String[] { String.format("%d", entry.getRowID()) }) == 1;
		else
			success = db.insert(KEY_BINDINGS_TABLE, null, values) != -1;
		close();
		return success;
	}

	public void loadKeyMapEntries(final KeyMapBinder binder) {
		Cursor mCursor = null;
		try {
			mCursor = db.query(true, KEY_BINDINGS_TABLE,
					new String[] { KEYBINDINGS_ROWID, KEYBINDINGS_COMMAND,
							KEYBINDINGS_UI_KEY, KEYBINDINGS_FRIENDLY_NAME,
							KEYBINDINGS_REQUIRE_CONFIRMATION }, null, null,
					null, null, null, null);
			if (mCursor != null) {
				mCursor.moveToFirst();
			}
		} catch (SQLException e) {
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setTitle("DataBase Error");
			builder.setMessage(e.getLocalizedMessage());
			builder.show();
		}
		if (mCursor == null)
			return;
		do {
			String friendlyName = mCursor.getString(mCursor
					.getColumnIndex(KEYBINDINGS_FRIENDLY_NAME));
			String mythKeyName = mCursor.getString(mCursor
					.getColumnIndex(KEYBINDINGS_UI_KEY));
			String command = mCursor.getString(mCursor
					.getColumnIndex(KEYBINDINGS_COMMAND));
			Integer req = mCursor.getInt(mCursor
					.getColumnIndex(KEYBINDINGS_REQUIRE_CONFIRMATION));
			MythKey mythKey = MythKey.getByName(mythKeyName);
			Integer id = mCursor.getInt(mCursor
					.getColumnIndex(KEYBINDINGS_ROWID));
			boolean requiresConfirmation = (req == 1);
			KeyBindingEntry entry = new KeyBindingEntry(id, friendlyName,
					mythKey, command, requiresConfirmation);
			binder.bind(entry);
		} while (mCursor.moveToNext());
		mCursor.close();
	}

}
