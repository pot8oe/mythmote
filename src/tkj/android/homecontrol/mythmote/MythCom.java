package tkj.android.homecontrol.mythmote;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
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

/** Class that handles network communication with mythtvfrontend **/
public class MythCom {

	public interface StatusChangedEventListener extends EventListener {
		
		public void StatusChanged(String StatusMsg, int statusCode);
	}
	
	public static final String JUMPPOINT_channelpriorities = "channelpriorities";    //- Channel Recording Priorities
	public static final String JUMPPOINT_channelrecpriority = "channelrecpriority";   //- Channel Recording Priorities
	public static final String JUMPPOINT_deletebox = "deletebox";          //- TV Recording Deletion
	public static final String JUMPPOINT_deleterecordings = "deleterecordings";    //- TV Recording Deletion
	public static final String JUMPPOINT_flixbrowse = "flixbrowse";           //- Netflix Browser
	public static final String JUMPPOINT_flixhistory = "flixhistory";          //- Netflix History
	public static final String JUMPPOINT_flixqueue = "flixqueue";            //- Netflix Queue
	public static final String JUMPPOINT_guidegrid = "guidegrid";            //- Program Guide
	public static final String JUMPPOINT_livetv = "livetv";              //- Live TV
	public static final String JUMPPOINT_livetvinguide = "livetvinguide";        //- Live TV In Guide
	public static final String JUMPPOINT_mainmenu = "mainmenu";             //- Main Menu
	public static final String JUMPPOINT_managerecordings = "managerecordings";     //- Manage Recordings / Fix Conflicts
	public static final String JUMPPOINT_manualbox = "manualbox";            //- Manual Record Scheduling
	public static final String JUMPPOINT_manualrecording = "manualrecording";      //- Manual Record Scheduling
	public static final String JUMPPOINT_musicplaylists = "musicplaylists";       //- Select music playlists
	public static final String JUMPPOINT_mythgallery = "mythgallery";          //- MythGallery
	public static final String JUMPPOINT_mythgame = "mythgame";             //- MythGame
	public static final String JUMPPOINT_mythmovietime = "mythmovietime";        //- MythMovieTime
	public static final String JUMPPOINT_mythnews = "mythnews";             //- MythNews
	public static final String JUMPPOINT_mythvideo = "mythvideo";            //- MythVideo
	public static final String JUMPPOINT_mythweather = "mythweather";          //- MythWeather
	public static final String JUMPPOINT_playbackbox = "playbackbox";          //- TV Recording Playback
	public static final String JUMPPOINT_playbackrecordings = "playbackrecordings";   //- TV Recording Playback
	public static final String JUMPPOINT_playdvd = "playdvd";              //- Play DVD
	public static final String JUMPPOINT_playmusic = "playmusic";            //- Play music
	public static final String JUMPPOINT_previousbox = "previousbox";          //- Previously Recorded
	public static final String JUMPPOINT_progfinder = "progfinder";           //- Program Finder
	public static final String JUMPPOINT_programfinder = "programfinder";        //- Program Finder
	public static final String JUMPPOINT_programguide = "programguide";         //- Program Guide
	public static final String JUMPPOINT_programrecpriority = "programrecpriority";   //- Program Recording Priorities
	public static final String JUMPPOINT_recordingpriorities = "recordingpriorities";  //- Program Recording Priorities
	public static final String JUMPPOINT_ripcd = "ripcd";                //- Rip CD
	public static final String JUMPPOINT_ripdvd = "ripdvd";               //- Rip DVD
	public static final String JUMPPOINT_statusbox = "statusbox";            //- Status Screen
	public static final String JUMPPOINT_videobrowser = "videobrowser";         //- Video Browser
	public static final String JUMPPOINT_videogallery = "videogallery";         //- Video Gallery
	public static final String JUMPPOINT_videolistings = "videolistings";        //- Video Listings
	public static final String JUMPPOINT_videomanager = "videomanager";         //- Video Manager
	public static final String JUMPPOINT_viewscheduled = "viewscheduled";        //- Manage Recordings / Fix Conflicts
	public static final String JUMPPOINT_zoneminderconsole = "zoneminderconsole";    //- ZoneMinder Console
	public static final String JUMPPOINT_zoneminderevents = "zoneminderevents";     //- ZoneMinder Events
	public static final String JUMPPOINT_zoneminderliveview = "zoneminderliveview";   //- ZoneMinder Live View
	
	public static final String KEY_up = "up";
	public static final String KEY_down = "down";
	public static final String KEY_left = "left";
	public static final String KEY_right = "right";
	public static final String KEY_enter = "enter";
	public static final String KEY_backspace = "backspace";
	public static final String KEY_esc = "escape";
	
	public static final String PLAY_CH_UP = "channel up";
	public static final String PLAY_CH_DW = "channel down";
	public static final String PLAY_STOP = "stop";
	public static final String PLAY_PLAY = "speed normal";
	public static final String PLAY_SEEK_FW = "seek forward";
	public static final String PLAY_SEEK_BW = "seek backward";
	
	public static final int DEFAULT_MYTH_PORT = 6546;
	public static final int SOCKET_TIMEOUT = 2000;
	public static final int ENABLE_WIFI = 0;
	public static final int CANCEL = 1;
	public static final int STATUS_DISCONNECTED = 0;
	public static final int STATUS_CONNECTED = 1;
	public static final int STATUS_CONNECTING = 3;
	public static final int STATUS_ERROR = 99;
	
	private static Timer _timer;
	private static Socket _socket;
	private static OutputStreamWriter _outputStream;
	private static InputStreamReader  _inputStream;
	private static Activity _parent;
	private static ConnectivityManager _conMgr;
	private static String _status;
	private static String _tmpStatus;
	private static int _tmpStatusCode;
	private static StatusChangedEventListener _statusListener;
	private static FrontendLocation _frontend;
	
	private final Handler mHandler = new Handler();
	private final Runnable mSocketActionComplete = new Runnable()
	{
		public void run()
		{
			setStatus(_tmpStatus, _tmpStatusCode);
		}
		
	};
	
	/** TimerTask that probes the current connection for its mythtv screen.  **/
	private final TimerTask timerTaskCheckStatus = new TimerTask()
	{
		//Run at every timer tick
		public void run() 
		{
			//only if socket is connected
			if(IsConnected())
			{
				//set disconnected status if nothing is returned.
				if(queryMythScreen() == null)
				{
					//_timer.cancel();
					setStatus("Disconnected", STATUS_DISCONNECTED);
				}
			}
		}
	};

	
	/** Parent activity is used to get context */
	public MythCom(Activity parentActivity)
	{
		_parent = parentActivity;
		_conMgr = (ConnectivityManager) _parent.getSystemService(Context.CONNECTIVITY_SERVICE);
		_timer = new Timer();
		_timer.schedule(timerTaskCheckStatus, 5000, 5000);
	}
	
	/** Connects to the given address and port. Any existing connection will be broken first **/
	public void Connect(FrontendLocation frontend)
	{
			//disconnect before we connect
			this.Disconnect();
			
			//set address and port
			_frontend = frontend;
			
			//create toast for all to eat and enjoy
			Toast toast = Toast.makeText(_parent.getApplicationContext(), R.string.attempting_to_connect_str, Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();

			this.setStatus("Connecting", STATUS_CONNECTING);
			
			//create a socket connecting to the address on the requested port
			this.connectSocket();
	}
	
	/** Closes the socket if it exists and it is already connected **/
	public void Disconnect()
	{
		try
		{
			//check if output stream exists
			if(_outputStream != null)
			{
				//close output stream
				_outputStream.close();
				_outputStream = null;
			}
			
			//check if socket exists
			if(_socket != null)
			{
				//close if connected
				if(_socket.isConnected()) _socket.close();

				//set socket to null
				_socket = null;
			}
		}
		catch(IOException ex)
		{
			this.setStatus("Disconnect I/O error", STATUS_ERROR);
		}
	}
	
	public void SendJumpCommand(String jumpPoint)
	{
		//send command data
		this.sendData(String.format("jump %s\n", jumpPoint));
	}
	
	public void SendKey(String key)
	{
		//send command data
		this.sendData(String.format("key %s\n", key));
	}
	
	public void SendKey(char key)
	{
		//send command data
		this.sendData(String.format("key %s\n", key));
	}
	
	public void SendPlaybackCmd(String cmd)
	{
		//send command data
		this.sendData(String.format("play %s\n", cmd));
	}
	
	public void SetOnStatusChangeHandler(StatusChangedEventListener listener)
	{
		_statusListener = listener;
	}
	
	public String GetStatusStr()
	{
		return _status;
	}
	
	public boolean IsNetworkReady()
	{
		if(_conMgr != null && _conMgr.getActiveNetworkInfo().isConnected())
			return true;
		return false;
	}
	
	public boolean IsConnected()
	{
		if(_socket == null) return false;
		
		return _socket.isConnected() && _socket.isBound();
	}


	
	
	
	/** Connects _socket to _frontend using a separate thread  **/
	private void connectSocket()
	{
		//create socket
		_socket = new Socket();
		
		Thread thread = new Thread()
		{
			public void run()
			{
				try
				{
					InetAddress address = java.net.InetAddress.getByName(_frontend.Address);
					
				//	int ipHash = address.hashCode();
				// This check for a route to the host is failing since android SDK 1.6
				//	if(_conMgr.requestRouteToHost(ConnectivityManager.TYPE_WIFI, ipHash))// || _conMgr.requestRouteToHost(ConnectivityManager.TYPE_MOBILE, ipHash)
				//	{
						_socket.connect(new InetSocketAddress(address, _frontend.Port), SOCKET_TIMEOUT);
						_outputStream = new OutputStreamWriter(_socket.getOutputStream());
						_inputStream = new InputStreamReader(_socket.getInputStream());
						
						//check if everything was connected OK
						if(!_socket.isConnected() || _outputStream == null)
						{
							_tmpStatus = "Unknown error getting output stream.";
							_tmpStatusCode = STATUS_ERROR;
						}
						else
						{
							_tmpStatus = _frontend.Name + " - Connected";
							_tmpStatusCode = STATUS_CONNECTED;
						}
				//	}
				//	else
				//	{
				//		_tmpStatus = "No route to host: " + _frontend.Address;
				//		_tmpStatusCode = STATUS_ERROR;
				//	}
				}
				catch (UnknownHostException e)
				{
					_tmpStatus = "Unknown host: " + _frontend.Address;
					_tmpStatusCode = STATUS_ERROR;
				}
				catch (IOException e)
				{
					_tmpStatus = "I/O Error connecting to host: " + _frontend.Address;
					_tmpStatusCode = STATUS_ERROR;
				}
				
				//post results
				mHandler.post(mSocketActionComplete);
			}
		};
		
		//run thread
		thread.start();
	}
	
	/** Sends data to the output stream of the socket.
	 * Attempts to reconnect socket if connection does not already exist. **/
	private void sendData(String data)
	{
		if(this.IsConnected() && _outputStream != null)
		{
			try
			{
				if(!data.endsWith("\n")) 
					data = String.format("%s\n", data);
				
				_outputStream.write(data);
				_outputStream.flush();
			}
			catch (IOException e)
			{
				this.setStatus("I/O Error data", STATUS_ERROR);
			}
		}
		else
		{
			this.connectSocket();
		}
	}
	
	/** Reads data from the input stream of the socket.
	 * Returns null if no data in received **/
	private char[] readData()
	{
		if(this.IsConnected() && _inputStream != null )
		{
			char[] buf = new char[100];
			try 
			{
				if(_inputStream.ready())
				{
					int len = _inputStream.read(buf);
					if(len > 0)
						return buf;
				}
			} 
			catch (IOException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//return null if no data was received
		return null;
	}
	
	/** Sets _status and fires the StatusChanged event **/
	private void setStatus(final String StatusMsg, final int code)
	{
		_parent.runOnUiThread(new Runnable(){

			public void run() 
			{
				_status = StatusMsg;
				if(_statusListener != null)
					_statusListener.StatusChanged(StatusMsg, code);
			}
			
		});
	}
	
	/** Returns the string representation of the current mythfrontend
	 * screen location. Returns null on error **/
	private String queryMythScreen()
	{
		//send query location command
		this.sendData("query location");
		
		//read input stream
		char[] data = this.readData();
		
		if(data != null && data.length > 0)
		{
			return data.toString();
		}
		else
		{
			//we're not receiving data the other 
			//end must have disconnected
			return null;
		}
	}


	
}
