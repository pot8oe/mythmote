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

import tkj.android.homecontrol.mythmote.keymanager.KeyBindingManager.MythKey;

/**
 * Simple holder class to associate (at run time) a key with the myth command to
 * execute
 * 
 * @author rob elsner
 * 
 */
public class KeyBindingEntry
{

	/**
	 * The user shown name of this key, for editing
	 */
	private String friendlyName;
	/**
	 * The binding entry
	 */
	private MythKey mythKey;

	/**
	 * The myth command we should send
	 */
	private String command;

	private boolean requiresConfirmation = false;
	private int rowId;

	public KeyBindingEntry(final int rowId, final String friendlyName, final MythKey mythKey,
			final String command, final boolean requiresConfirm)
	{
		this.rowId = rowId;
		this.command = command;
		this.mythKey = mythKey;
		this.friendlyName = friendlyName;
		this.requiresConfirmation = requiresConfirm;
	}

	public KeyBindingEntry(String command, MythKey mythKey,
			String friendlyName, boolean requiresConfirm)
	{
		this(-1, command, mythKey, friendlyName, requiresConfirm);
	}

	public String getFriendlyName()
	{
		return friendlyName;
	}

	public MythKey getMythKey()
	{
		return mythKey;
	}

	public String getCommand()
	{
		return command;
	}

	public boolean requiresConfirmation()
	{
		return requiresConfirmation;
	}

	public int getRowID()
	{
		return rowId;
	}

}
