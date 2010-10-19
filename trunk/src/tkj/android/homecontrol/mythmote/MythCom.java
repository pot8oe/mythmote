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
	
    public static final String VOLUME_DOWN = "[";
    public static final String VOLUME_UP = "]";
    public static final String VOLUME_MUTE = "|";
    public static final String CH_RETURN = "h";
	public static final String PLAY_CH_UP = "channel up";
	public static final String PLAY_CH_DW = "channel down";
	public static final String PLAY_STOP = "stop";
	public static final String PLAY_PLAY = "speed normal";
	public static final String PLAY_SEEK_FW = "seek forward";
	public static final String PLAY_SEEK_BW = "seek backward";
	public static final String EXIT = "exit";
	
	public static final int DEFAULT_MYTH_PORT = 6546;
	public static final int SOCKET_TIMEOUT = 10000;
	public static final int ENABLE_WIFI = 0;
	public static final int CANCEL = 1;
	public static final int STATUS_DISCONNECTED = 0;
	public static final int STATUS_CONNECTED = 1;
	public static final int STATUS_CONNECTING = 3;
	public static final int STATUS_ERROR = 99;
	
	private static Timer _timer;
	private static Toast _toast;
	private static Socket _socket;
	private static BufferedWriter _outputStream;
	private static BufferedReader  _inputStream;
	private static Activity _parent;
	private static ConnectivityManager _conMgr;
	private static String _status;
	private static int _statusCode;
	private static StatusChangedEventListener _statusListener;
	private static FrontendLocation _frontend;
	
	private final Handler mHandler = new Handler();
	private final Runnable mSocketActionComplete = new Runnable()
	{
		public void run()
		{
			setStatus(_status, _statusCode);
			if(_statusCode!=STATUS_CONNECTING)
			    _toast.cancel();
		}
		
	};
	
	/** TimerTask that probes the current connection for its mythtv screen.  **/
	private static TimerTask timerTaskCheckStatus;

	
	/** Parent activity is used to get context */
	public MythCom(Activity parentActivity)
	{
		_parent = parentActivity;
		_statusCode=STATUS_DISCONNECTED;
	}
	
	/** Connects to the given address and port. Any existing connection will be broken first **/
	public void Connect(FrontendLocation frontend)
	{
		//read status update interval preference
		int updateInterval = _parent.getSharedPreferences(MythMotePreferences.MYTHMOTE_SHARED_PREFERENCES_ID, Context.MODE_PRIVATE)
		.getInt(MythMotePreferences.PREF_STATUS_UPDATE_INTERVAL, 5000);
		
		//schedule update timer
		scheduleUpdateTimer(updateInterval);

		//get connection manager
		_conMgr = (ConnectivityManager) _parent.getSystemService(Context.CONNECTIVITY_SERVICE);

		//set address and port
		_frontend = frontend;

		//create toast for all to eat and enjoy
		_toast = Toast.makeText(_parent.getApplicationContext(), R.string.attempting_to_connect_str, Toast.LENGTH_SHORT);
		_toast.setGravity(Gravity.CENTER, 0, 0);
		_toast.show();

		this.setStatus("Connecting", STATUS_CONNECTING);

		//create a socket connecting to the address on the requested port
		this.connectSocket();
	}
	
	/** Closes the socket if it exists and it is already connected **/
	public void Disconnect()
	{
        _statusCode=STATUS_DISCONNECTED;
		try
		{
			//send exit if connected
			if(this.IsConnected())
				this.sendData("exit\n");
			
			//check if output stream exists
			if(_outputStream != null)
			{
				_outputStream.close();
				_outputStream = null;
			}
			
			//check if input stream exists
			if(_inputStream != null)
			{
				//close input stream
				_inputStream.close();
				_inputStream = null;
			}
			if(_socket != null)
			{
			    if(!_socket.isClosed())
				    _socket.close();
			
				_socket = null;
			}
			if(_conMgr != null)
				_conMgr = null;
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
		if(_statusCode==STATUS_CONNECTED) return true;
		return false;
	}

	public boolean IsConnecting()
	{
		if(_statusCode==STATUS_CONNECTING) return true;
		return false;
	}
	
	/** Connects _socket to _frontend using a separate thread  **/
	private void connectSocket()
	{
		if(_socket==null)
		    _socket = new Socket();
		
		Thread thread = new Thread()
		{
			public void run()
			{
				try
				{
					_socket.connect(new InetSocketAddress(_frontend.Address, _frontend.Port));
					
					if(_socket.isConnected())
					{
					    _outputStream = new BufferedWriter(new OutputStreamWriter(_socket.getOutputStream()));
					    _inputStream = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
					}
					else
					{
						_status = "Could not open socket.";
						_statusCode = STATUS_ERROR;
					}

					//check if everything was connected OK
					if(!_socket.isConnected() || _outputStream == null)
					{
						_status = "Unknown error getting output stream.";
						_statusCode = STATUS_ERROR;
					}
					else
					{
						_status = _frontend.Name + " - Connected";
						_statusCode = STATUS_CONNECTED;
					}

				}
				catch (UnknownHostException e)
				{
					_status = "Unknown host: " + _frontend.Address;
					_statusCode = STATUS_ERROR;
				}
				catch (IOException e)
				{
					_status = "IO Except: " + e.getLocalizedMessage() + ": " + _frontend.Address;
					_statusCode = STATUS_ERROR;
					if(_inputStream!=null)
					{
						_inputStream=null;
					}
					if(_socket!=null)
					{
						if(!_socket.isClosed())
						{
							try { _socket.close(); } 
							catch (IOException e1) { }
							_socket = null;
						}
					}
				}
				
				//post results
				mHandler.post(mSocketActionComplete);
			}
		};
		
		thread.start();
	}
	
	/** Sends data to the output stream of the socket.
	 * Attempts to reconnect socket if connection does not already exist. **/
	private boolean sendData(String data)
	{
		if(this.IsConnected() && _outputStream != null)
		{
			try
			{
				if(!data.endsWith("\n")) 
					data = String.format("%s\n", data);
				
				_outputStream.write(data);
				_outputStream.flush();
				return true;
			}
			catch (IOException e)
			{
				e.printStackTrace();
				this.setStatus(e.getLocalizedMessage() + ": " + _frontend.Address , STATUS_ERROR);
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
		if(this.IsConnected() && _inputStream != null )
		{
			
			try 
			{
				if(_inputStream.ready())
					outString =_inputStream.readLine() ;

			} 
			catch (IOException e) 
			{
				Log.e(MythMote.LOG_TAG, "IO Error reading data", e);
				this.setStatus(e.getLocalizedMessage() + ": " + _frontend.Address , STATUS_ERROR);
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

		if(this.sendData("query location"))
		{
		    if(this.IsConnected())
		    	return this.readData();
		    else
		    {
				Log.e(MythMote.LOG_TAG, _status + ": Not connected on receive");
				return null;
		    }
		}
		else
		{
			Log.e(MythMote.LOG_TAG, _status + ": Send failed");
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
			if(_timer != null)
			{
				_timer.cancel();
				_timer.purge();
				_timer = null;
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
								setStatus(_frontend.Name + " - Connected", STATUS_CONNECTED);
							}
						}
					}
				};
					
				_timer = new Timer();
				_timer.schedule(timerTaskCheckStatus, updateInterval, updateInterval);
			}
		}
		catch(Exception ex)
		{
			Log.e(MythMote.LOG_TAG, "Error scheduling status update timer.", ex);
		}
	}
	
}
