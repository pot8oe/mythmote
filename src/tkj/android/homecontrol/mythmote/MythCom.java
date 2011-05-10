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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.EventListener;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.view.Gravity;
import android.widget.Toast;
import android.util.Log;

/** Class that handles network communication with mythtvfrontend **/
public class MythCom {

	public interface StatusChangedEventListener extends EventListener {

		public void StatusChanged(String StatusMsg, int statusCode);
	}

	public static final int DEFAULT_MYTH_PORT = 6546;
	public static final int SOCKET_TIMEOUT = 2000;
	public static final int ENABLE_WIFI = 0;
	public static final int CANCEL = 1;
	public static final int STATUS_DISCONNECTED = 0;
	public static final int STATUS_CONNECTED = 1;
	public static final int STATUS_CONNECTING = 3;
	public static final int STATUS_ERROR = 99;

	private static Timer sTimer;
	private static Toast sToast;
	private static Socket sSocket;
	private static BufferedWriter sOutputStream;
	private static BufferedReader  sInputStream;
	private static Activity sParent;
	private static ConnectivityManager sConMgr;
	private static String sStatus;
	private static int sStatusCode;
	private static StatusChangedEventListener sStatusListener;
	private static FrontendLocation sFrontend;

	private final Handler mHandler = new Handler();
	private final Runnable mSocketActionComplete = new Runnable()
	{
		public void run()
		{
			setStatus(sStatus, sStatusCode);
			if(sStatusCode!=STATUS_CONNECTING)
			    sToast.cancel();
		}

	};
	
	/** TimerTask that probes the current connection for its mythtv screen.  **/
	private static TimerTask timerTaskCheckStatus;

	
	/** Parent activity is used to get context */
	public MythCom(Activity parentActivity)
	{
		sParent = parentActivity;
		sStatusCode=STATUS_DISCONNECTED;
	}
	
	/** Connects to the given address and port. Any existing connection will be broken first **/
	public void Connect(FrontendLocation frontend)
	{
		//read status update interval preference
		int updateInterval = sParent.getSharedPreferences(MythMotePreferences.MYTHMOTE_SHARED_PREFERENCES_ID, Context.MODE_PRIVATE)
		.getInt(MythMotePreferences.PREF_STATUS_UPDATE_INTERVAL, 5000);
		
		//schedule update timer
		scheduleUpdateTimer(updateInterval);

		//get connection manager
		sConMgr = (ConnectivityManager) sParent.getSystemService(Context.CONNECTIVITY_SERVICE);

		// set address and port
		sFrontend = frontend;

		//create toast for all to eat and enjoy
		sToast = Toast.makeText(sParent.getApplicationContext(), R.string.attempting_to_connect_str, Toast.LENGTH_SHORT);
		sToast.setGravity(Gravity.CENTER, 0, 0);
		sToast.show();

		this.setStatus("Connecting", STATUS_CONNECTING);

		// create a socket connecting to the address on the requested port
		this.connectSocket();
	}
	
	/** Closes the socket if it exists and it is already connected **/
	public void Disconnect()
	{
        sStatusCode=STATUS_DISCONNECTED;
		try
		{
			//send exit if connected
			if(this.IsConnected())
				this.sendData("exit\n");

			// check if output stream exists
			if (sOutputStream != null) {
				sOutputStream.close();
				sOutputStream = null;
			}

			// check if input stream exists
			if (sInputStream != null) {
				// close input stream
				sInputStream.close();
				sInputStream = null;
			}
			if(sSocket != null)
			{
			    if(!sSocket.isClosed())
				    sSocket.close();
			
				sSocket = null;
			}
			if(sConMgr != null)
				sConMgr = null;
		}
		catch(IOException ex)
		{
			this.setStatus("Disconnect I/O error", STATUS_ERROR);
		}
	}

	public void SendCommand(String jumpPoint) {
		// send command data
		this.sendData(String.format("%s\n", jumpPoint));
	}

	public void SendJumpCommand(String jumpPoint) {
		// send command data
		this.sendData(String.format("jump %s\n", jumpPoint));
	}

	public void SendKey(String key) {
		// send command data
		this.sendData(String.format("key %s\n", key));
	}

	public void SendKey(char key) {
		// send command data
		this.sendData(String.format("key %s\n", key));
	}

	public void SendPlaybackCmd(String cmd) {
		// send command data
		this.sendData(String.format("play %s\n", cmd));
	}

	public void SetOnStatusChangeHandler(StatusChangedEventListener listener) {
		sStatusListener = listener;
	}

	public String GetStatusStr() {
		return sStatus;
	}

	public boolean IsNetworkReady() {
		if (sConMgr != null && sConMgr.getActiveNetworkInfo().isConnected())
			return true;
		return false;
	}
	
	public boolean IsConnected()
	{
		if(sStatusCode==STATUS_CONNECTED) return true;
		return false;
	}

	public boolean IsConnecting()
	{
		if(sStatusCode==STATUS_CONNECTING) return true;
		return false;
	}
	
	/** Connects _socket to _frontend using a separate thread  **/
	private void connectSocket()
	{
		if(sSocket==null)
		    sSocket = new Socket();
		
		Thread thread = new Thread()
		{
			public void run()
			{
				try
				{
					//connect
					sSocket.connect(new InetSocketAddress(sFrontend.Address, sFrontend.Port));
					
					//check if connected
					if(sSocket.isConnected())
					{
					    sOutputStream = new BufferedWriter(new OutputStreamWriter(sSocket.getOutputStream()));
					    sInputStream = new BufferedReader(new InputStreamReader(sSocket.getInputStream()));
					}
					else
					{
						sStatus = "Could not open socket.";
						sStatusCode = STATUS_ERROR;
					}

					//check if everything was connected OK
					if(!sSocket.isConnected() || sOutputStream == null)
					{
						sStatus = "Unknown error getting output stream.";
						sStatusCode = STATUS_ERROR;
					}
					else
					{
						sStatus = sFrontend.Name + " - Connected";
						sStatusCode = STATUS_CONNECTED;
					}

				}
				catch (UnknownHostException e)
				{
					sStatus = "Unknown host: " + sFrontend.Address;
					sStatusCode = STATUS_ERROR;
				}
				catch (IOException e)
				{
					sStatus = "IO Except: " + e.getLocalizedMessage() + ": " + sFrontend.Address;
					sStatusCode = STATUS_ERROR;
					if(sInputStream!=null)
					{
						sInputStream=null;
					}
					if(sSocket!=null)
					{
						if(!sSocket.isClosed())
						{
							try { sSocket.close(); } 
							catch (IOException e1) { }
							sSocket = null;
						}
					}
				}

				// post results
				mHandler.post(mSocketActionComplete);
			}
		};

		// run thread
		thread.start();
	}
	
	/** Sends data to the output stream of the socket.
	 * Attempts to reconnect socket if connection does not already exist. **/
	private boolean sendData(String data)
	{
		if(this.IsConnected() && sOutputStream != null)
		{
			try
			{
				if(!data.endsWith("\n")) 
					data = String.format("%s\n", data);
				
				sOutputStream.write(data);
				sOutputStream.flush();
				return true;
			}
			catch (IOException e)
			{
				e.printStackTrace();
				this.setStatus(e.getLocalizedMessage() + ": " + sFrontend.Address , STATUS_ERROR);
				this.Disconnect();
				return false;
			}
		}
		return false;
	}
	
	/** Reads data from the input stream of the socket.
	 * Returns null if no data in received **/
	private String readData()
	{
		String outString = "";
		if(this.IsConnected() && sInputStream != null )
		{
			
			try 
			{
				if(sInputStream.ready())
					outString =sInputStream.readLine() ;

			} 
			catch (IOException e) 
			{
				Log.e(MythMote.LOG_TAG, "IO Error reading data", e);
				this.setStatus(e.getLocalizedMessage() + ": " + sFrontend.Address , STATUS_ERROR);
				this.Disconnect();
				return null;
			}
		}
		
		if(outString!="")
			return outString;
		else
		{
			Log.e(MythMote.LOG_TAG, "Null outstring");
			return null;
		}
	}
	
	/** Sets _status and fires the StatusChanged event **/
	private void setStatus(final String StatusMsg, final int code)
	{
		sParent.runOnUiThread(new Runnable(){

			public void run() 
			{
				sStatus = StatusMsg;
				if (sStatusListener != null)
					sStatusListener.StatusChanged(StatusMsg, code);
			}

		});
	}
	
	/** Returns the string representation of the current mythfrontend
	 * screen location. Returns null on error **/
	private String queryMythScreen()
	{

		if(this.sendData("query location"))
		{
		    if(this.IsConnected())
		    	return this.readData();
		    else
		    {
				Log.e(MythMote.LOG_TAG, sStatus + ": Not connected on receive");
				return null;
		    }
		}
		else
		{
			Log.e(MythMote.LOG_TAG, sStatus + ": Send failed");
			return null;
		}

	}
	
	/** Creates the update timer and schedules it for the given interval.
	 * If the timer already exists it is destroyed and recreated. */
	private void scheduleUpdateTimer(int updateInterval)
	{
		try
		{
			//close down the existing timer.
			if(sTimer != null)
			{
				sTimer.cancel();
				sTimer.purge();
				sTimer = null;
			}
			
			//clear timer task
			if(timerTaskCheckStatus != null)
			{
				timerTaskCheckStatus.cancel();
				timerTaskCheckStatus = null;
			}

			//(re)schedule the update timer
			if(updateInterval > 0)
			{
				//create timer task
				timerTaskCheckStatus = new TimerTask()
				{
					//Run at every timer tick
					public void run() 
					{
						//only if socket is connected
						if(IsConnected() && !IsConnecting())
						{
							//set disconnected status if nothing is returned.
							if(queryMythScreen() == null)
							{
								setStatus("Disconnected", STATUS_DISCONNECTED);
							}
							else
							{
								setStatus(sFrontend.Name + " - Connected", STATUS_CONNECTED);
							}
						}
					}
				};
					
				sTimer = new Timer();
				sTimer.schedule(timerTaskCheckStatus, updateInterval, updateInterval);
			}
		}
		catch(Exception ex)
		{
			Log.e(MythMote.LOG_TAG, "Error scheduling status update timer.", ex);
		}
	}
	
}
