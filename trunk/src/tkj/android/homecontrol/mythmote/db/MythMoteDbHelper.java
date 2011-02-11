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

import tkj.android.homecontrol.mythmote.keymanager.KeyBindingEntry;
import tkj.android.homecontrol.mythmote.keymanager.KeyBindingManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MythMoteDbHelper extends SQLiteOpenHelper {
	public static final String DATABASE_NAME = "mythmotedata";
	public static final int DATABASE_VERSION = 2;
	public static final String TAG = "MythMoteDB";

	/**
	 * Field Name declarations
	 */
	public static final String KEY_NAME = "name";
	public static final String KEY_ADDRESS = "address";
	public static final String KEY_PORT = "port";
	public static final String KEY_ROWID = "_id";
	public static final String KEYBINDINGS_ROWID = "_id";
	public static final String KEYBINDINGS_COMMAND = "myth_command";
	public static final String KEYBINDINGS_UI_KEY = "ui_key";
	public static final String KEYBINDINGS_FRIENDLY_NAME = "friendly_name";
	// to enable a dialog to popup confirming the button action
	public static final String KEYBINDINGS_REQUIRE_CONFIRMATION = "req_confirm";

	public static final String FRONTEND_TABLE = "frontends";
	public static final String KEY_BINDINGS_TABLE = "keybindings";

	/**
	 * Table declarations
	 */
	private static final String CREATE_FRONTENDS_TABLE = "create table "
			+ FRONTEND_TABLE + " (_id integer primary key autoincrement, "
			+ "name text not null, address text not null, port int);";

	private static final String CREATE_KEY_BINDINGS_TABLE = "create table "
			+ KEY_BINDINGS_TABLE + " (" + KEYBINDINGS_ROWID
			+ " integer primary key autoincrement, " + KEYBINDINGS_COMMAND
			+ " text not null, " + KEYBINDINGS_UI_KEY + " text not null, "
			+ KEYBINDINGS_FRIENDLY_NAME + " text not null, "
			+ KEYBINDINGS_REQUIRE_CONFIRMATION + " INTEGER not null );";

	public MythMoteDbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_FRONTENDS_TABLE);
		db.execSQL(CREATE_KEY_BINDINGS_TABLE);
		createDefaultEntries(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
				+ newVersion + ", which will destroy all old data");
		if (1 == oldVersion && 2 == newVersion) {
			// we are just adding key bindings in this case
			db.execSQL(CREATE_KEY_BINDINGS_TABLE);
			createDefaultEntries(db);
			db.setVersion(newVersion);
		} else {
			db.execSQL("DROP TABLE IF EXISTS frontends");
			db.execSQL("DROP TABLE IF EXISTS keybindings");
			onCreate(db);
		}
	}

	private static void createDefaultEntries(SQLiteDatabase db) {
		for (KeyBindingEntry entry : KeyBindingManager.MythKey
				.createDefaultList()) {
			ContentValues values = new ContentValues();
			values.put(KEYBINDINGS_COMMAND, entry.getCommand());
			values.put(KEYBINDINGS_UI_KEY, entry.getMythKey().name());
			values.put(KEYBINDINGS_FRIENDLY_NAME, entry.getFriendlyName());
			values.put(KEYBINDINGS_REQUIRE_CONFIRMATION,
					entry.requiresConfirmation() ? 1 : 0);
			Log.d("KBDA", "Adding default entry " + entry.getFriendlyName()
					+ " to " + entry.getCommand());
			db.insert(KEY_BINDINGS_TABLE, null, values);
		}
	}
}
