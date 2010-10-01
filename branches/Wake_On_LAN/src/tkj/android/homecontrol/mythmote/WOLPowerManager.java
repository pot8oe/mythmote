package tkj.android.homecontrol.mythmote;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

/**
 * 
 * Send a Wake-On-Lan packet to the given host
 * 
 * @author rob elsner
 * 
 */
public class WOLPowerManager
{
	/**
	 * Remove this function once the MAC has been stored
	 */
	public void sendWOL(final Context ctx)
	{
		AlertDialog.Builder alert = new AlertDialog.Builder(ctx);

		alert.setTitle("MAC");
		alert.setMessage("Enter MAC in the form ##:##:##:##:##:##");

		// Set an EditText view to get user input
		final EditText input = new EditText(ctx);
		alert.setView(input);

		alert.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int whichButton)
					{
						String value = input.getText().toString();
						if (!value.matches("^([0-9a-fA-F]{2}:){5}[0-9a-fA-F]{2}$"))
						{
							Toast toast = Toast.makeText(ctx, "MAC of form ##:##:##:##:##:## only.", Toast.LENGTH_LONG);
							toast.show();
						}
						try
						{
							sendWOL(value, ctx);
						} catch (IOException e)
						{
							Toast toast = Toast.makeText(ctx, "error " + e.getLocalizedMessage(),Toast.LENGTH_LONG);
							toast.show();
						}
					}
				});

		alert.setNegativeButton(android.R.string.cancel,
				new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int whichButton)
					{
						// Canceled.
					}
				});

		alert.create().show();
	}

	/**
	 * Send a WOL broadcast packet to the specified MAC
	 * 
	 * @param macAddress
	 *            00:00:00:00:00 format
	 * @return true on success
	 * @throws IOException
	 */
	public boolean sendWOL(final String macAddress, final Context mContext)
			throws IOException
	{
		try
		{
			byte[] wolPacket = buildWolPacket(macAddress);
			InetAddress broadcast = getBroadcastAddress(mContext);
			DatagramSocket socket = new DatagramSocket(7000);
			socket.setBroadcast(true);
			DatagramPacket packet = new DatagramPacket(wolPacket,
					wolPacket.length, broadcast, 7000);
			socket.send(packet);
			socket.close();
			return true;
		} catch (Exception e)
		{
			Log.e("wol", "failure", e);
			Toast t = Toast.makeText(mContext, e.getLocalizedMessage(), Toast.LENGTH_LONG);
			t.show();
			return false;
		}

	}

	InetAddress getBroadcastAddress(final Context mContext) throws IOException
	{
		
		WifiManager wifi = (WifiManager) mContext
				.getSystemService(Context.WIFI_SERVICE);
		DhcpInfo dhcp = wifi.getDhcpInfo();
		// handle null somehow

		int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
		byte[] quads = new byte[4];
		for (int k = 0; k < 4; k++)
			quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
		return InetAddress.getByAddress(quads);
	}

	/**
	 * 
	 * @param macAddress
	 *            should be formatted as 00:00:00:00:00:00 in a string
	 * @return the raw packet bytes to send along
	 */
	private byte[] buildWolPacket(final String macAddress)
	{
		final byte[] preamble =
		{ (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
				(byte) 0xFF };
		byte[] macBytes = new byte[6];
		String[] octets = macAddress.split(":");
		for (int i = 0; i < 6; i++)
		{
			
			macBytes[i] = (byte)(Integer.parseInt(octets[i], 16) & 0xFF);
		}
		final byte[] body = new byte[36]; // 6 bytes for mac, repeated 6 times
		for (int destPos = 0; destPos < 36; destPos += 6)
		{
			System.arraycopy(macBytes, 0, body, destPos, 6);
		}
		final byte[] packetBody = new byte[42];
		System.arraycopy(preamble, 0, packetBody, 0, 6);
		System.arraycopy(body, 0, packetBody, 6, 36);
		return packetBody;
	}

}
