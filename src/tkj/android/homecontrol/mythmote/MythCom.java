package tkj.android.homecontrol.mythmote;

import java.io.IOException;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiManager;

public class MythCom {

	public static final int ENABLE_WIFI = 0;
	public static final int CANCEL = 1;
	
	private static Socket _socket;
	private static Activity _parent;
	private static Timer _checkWifiStatus;
	private static AlertDialog _wifiStatusDialog;
	private WifiManager _wifiManager;
	
	
	
	/** Parent activity is used to get context and to close app when wifi is denied */
	public MythCom(Activity parentActivity)
	{
		_parent = parentActivity;
		_wifiManager = (WifiManager) _parent.getSystemService(Context.WIFI_SERVICE);
		
        //setup timer to make sure wifi is enabled and connected
		_checkWifiStatus = new Timer("wifiChecker", true);
		_checkWifiStatus.schedule(new TimerTask(){

			@Override
			public void run() {
				
				if(!IsWifiEnabled())
				{
					ShowEnableWifiDialog();
				}
				if(!IsWiFiReady())
				{
					if(_wifiStatusDialog == null)
						_wifiStatusDialog = CreateMessageAlertDialog(_parent.getString(R.string.enabling_wifi_str));
					if(!_wifiStatusDialog.isShowing())
						_wifiStatusDialog.show();
				}
				else
				{
					if(_wifiStatusDialog != null)
					{
						_wifiStatusDialog.dismiss();
						_wifiStatusDialog = null;
					}
				}
			}}, 100);
	}
	
	/** Connects to the given address and port. Any existing connection will be broken first **/
	public void Connect(String address, int port)
	{
		try
		{
			//disconnect before we connect
			this.Disconnect();

			int ipHash = java.net.InetAddress.getByName(address).hashCode();
			
			if(this.IsWiFiReady())
			{
				//create a socket connecting to the address on the requested port
				_socket = new Socket(address, port);
			}
			else
			{
				//TODO: report to user that they need WiFi turned on and connected
			}
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
	
	public boolean IsWifiEnabled()
	{
		if(this._wifiManager != null && this._wifiManager.isWifiEnabled())
			return true;
		return false;
	}
	
	public boolean IsWiFiReady()
	{
		if(this._wifiManager != null && this._wifiManager.isWifiEnabled() && this._wifiManager.getConnectionInfo().getNetworkId() != -1)
			return true;
		return false;
	}
	
	public boolean IsConnected()
	{
		return this._socket.isConnected();
	}
	
	
	public void TurnOnWiFi()
	{
		if(_wifiManager != null && !_wifiManager.isWifiEnabled())
		{
			_wifiManager.setWifiEnabled(true);

		}
	}
	
	public void ExitApp()
	{
		_parent.finish();
	}


	private void ShowEnableWifiDialog() 
	{
	    AlertDialog.Builder builder = new AlertDialog.Builder(_parent);
	    builder.setMessage(_parent.getString(R.string.enable_wifi_question_str));
	    builder.setPositiveButton(
	    		_parent.getString(R.string.enable_wifi_str), 
	    		new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						TurnOnWiFi();
					}
				});
	    builder.setOnCancelListener(
	    		new DialogInterface.OnCancelListener() {
			        public void onCancel(DialogInterface dialog) {
			        	ExitApp();
			        }
			    });
	    builder.show();
	} 
	
	private AlertDialog CreateMessageAlertDialog(String msg)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(_parent);
		builder.setCancelable(false);
		builder.setMessage(msg);
		return builder.create();
	}
	
	
	
	
	
	
}
