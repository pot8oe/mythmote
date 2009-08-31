package tkj.android.homecontrol.mythmote;

import java.io.IOException;
import java.net.Socket;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;

public class MythCom {

	private static Socket _socket;
	
	
	public MythCom()
	{
		
	}
	
	/** Connects to the given address and port. Any existing connection will be broken first **/
	public void Connect(String address, int port)
	{
		try
		{
			//disconnect before we connect
			this.Disconnect();
			_socket = new Socket(address, port);
		}
		catch(IOException ex)
		{

		}
	}
	
	/** Closes the socket if it exists and it is already connected **/
	public void Disconnect()
	{
		try
		{
			if(_socket != null && _socket.isConnected())
				_socket.close();
		}
		catch(IOException ex)
		{
			
		}
	}
	
	public boolean IsConnected()
	{
		return this._socket.isConnected();
	}
	
	
	
	
	
}
