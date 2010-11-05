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
