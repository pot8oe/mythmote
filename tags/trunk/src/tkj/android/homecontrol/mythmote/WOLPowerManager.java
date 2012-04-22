package tkj.android.homecontrol.mythmote;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
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
	public static int MACPORT = 7000;



	/**
	 * Send a WOL broadcast packet to the specified MAC
	 * 
	 * @param macAddress
	 *            00:00:00:00:00 format
	 * @return true on success
	 * @throws IOException
	 */
	public static boolean sendWOL(final String macAddress, final Context mContext) throws IOException
	{
		try
		{
			byte[] wolPacket = buildWolPacket(macAddress);
			InetAddress broadcast = getBroadcastAddress(mContext);
			DatagramSocket socket = new DatagramSocket(MACPORT);
			socket.setBroadcast(true);
			DatagramPacket packet = new DatagramPacket(wolPacket,
					wolPacket.length, broadcast, MACPORT);
			socket.send(packet);
			socket.close();
			return true;
		} catch (Exception e)
		{
			Log.e(MythMote.LOG_TAG, "failure", e);
			Toast t = Toast.makeText(mContext, e.getLocalizedMessage(), Toast.LENGTH_LONG);
			t.show();
			return false;
		}

	}

	/**
	 * PJRS Wake On Lan based on code at www.jibble.org which is
	 * 	 * similar to that by rob elsner; the original sendWOL doesn't work
	 */
	public static void sendWOL(final Context context, final String MACAddress, int nos_to_send) throws IOException 
	{
		//check for errors
		if(MACAddress == null || MACAddress.length() == 0) return;

		try {
			byte[] macBytes = getMacBytes(MACAddress);
			byte[] bytes = new byte[6 + 16 * macBytes.length];
			for (int i = 0; i < 6; i++) {
				bytes[i] = (byte) 0xff;
			}
			for (int i = 6; i < bytes.length; i += macBytes.length) {
				System.arraycopy(macBytes, 0, bytes, i, macBytes.length);
			}

			InetAddress address = getBroadcastAddress(context);
			DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, MACPORT);
			DatagramSocket socket = new DatagramSocket(MACPORT);
			for (int i = 0; i < nos_to_send; i++)
				socket.send(packet);
			socket.close();

			System.out.println("Wake-on-LAN packet sent.");
		}
		catch (Exception e) {

		}

	}

	private static InetAddress getBroadcastAddress(final Context mContext) throws IOException
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
	 * PJRS WOL's getBytes
	 * @param macStr
	 * @return
	 * @throws IllegalArgumentException
	 */
	private static byte[] getMacBytes(String macStr) throws IllegalArgumentException 
	{

		byte[] bytes = new byte[6];
		String[] hex = macStr.split("(\\:|\\-)");
		if (hex.length != 6) {
			//throw new IllegalArgumentException("Invalid MAC address.");
		}
		try {
			for (int i = 0; i < 6; i++) {
				bytes[i] = (byte) Integer.parseInt(hex[i], 16);
			}
		}
		catch (NumberFormatException e) {
			//throw new IllegalArgumentException("Invalid hex digit in MAC address.");
		}
		return bytes;
	}

	/**
	 * 
	 * @param macAddress
	 *            should be formatted as 00:00:00:00:00:00 in a string
	 * @return the raw packet bytes to send along
	 */
	private static byte[] buildWolPacket(final String macAddress)
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
