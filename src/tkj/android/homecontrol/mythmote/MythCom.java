package tkj.android.homecontrol.mythmote;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.EventListener;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.net.ConnectivityManager;

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
	
	
	public static final int SOCKET_TIMEOUT = 2000;
	public static final int ENABLE_WIFI = 0;
	public static final int CANCEL = 1;
	public static final int STATUS_DISCONNECTED = 0;
	public static final int STATUS_CONNECTED = 1;
	public static final int STATUS_ERROR = 99;
	
	private static Socket _socket;
	private static OutputStreamWriter _outputStream;
	private static Activity _parent;
	private static ConnectivityManager _conMgr;
	private static String _status;
	private static StatusChangedEventListener _statusListener;
	

	
	/** Parent activity is used to get context and to close app when wifi is denied */
	public MythCom(Activity parentActivity)
	{
		_parent = parentActivity;
		_conMgr = (ConnectivityManager) _parent.getSystemService(Context.CONNECTIVITY_SERVICE);
	}
	
	/** Connects to the given address and port. Any existing connection will be broken first **/
	public void Connect(String address, int port)
	{
		try
		{
			//disconnect before we connect
			this.Disconnect();

			int ipHash = java.net.InetAddress.getByName(address).hashCode();
			
			this.SetStatus("Connecting", STATUS_DISCONNECTED);
			if(_conMgr.requestRouteToHost(ConnectivityManager.TYPE_WIFI, ipHash))// || _conMgr.requestRouteToHost(ConnectivityManager.TYPE_MOBILE, ipHash)
			{
				//create a socket connecting to the address on the requested port
				_socket = new Socket();//address, port
				_socket.connect(new InetSocketAddress(InetAddress.getByName(address), port), SOCKET_TIMEOUT);
				_outputStream = new OutputStreamWriter(_socket.getOutputStream());
				if(_socket.isConnected() && _outputStream != null)
					this.SetStatus(_parent.getString(R.string.connected_str), STATUS_CONNECTED);
				else
					this.SetStatus("Unknown error getting output stream.", STATUS_ERROR);
			}
			else
			{
				//TODO: report to user that they need WiFi turned on and connected
				this.SetStatus(_parent.getString(R.string.no_route_to_host_str), STATUS_ERROR);
			}
		
		} 
		catch (UnknownHostException e)
		{
			this.SetStatus("Unknown host: " + address, STATUS_ERROR);
		}
		catch (IOException e)
		{
			this.SetStatus("I/O Failed for host: " + address, STATUS_ERROR);
		}
	}
	
	/** Closes the socket if it exists and it is already connected **/
	public void Disconnect()
	{
		try
		{
			if(_outputStream != null)
			{
				_outputStream.close();
				_outputStream = null;
			}
			
			if(_socket != null && _socket.isConnected())
			{
				_socket.close();
			}
		}
		catch(IOException ex)
		{
			this.SetStatus("Disconnect I/O error", STATUS_ERROR);
		}
	}
	
	public void SendJumpCommand(String jumpPoint)
	{
		if(_outputStream != null)
		{
			try
			{
				_outputStream.write(String.format("jump %s\r", jumpPoint));
				_outputStream.flush();
			}
			catch (IOException e)
			{
				this.SetStatus("I/O Error sending Jump", STATUS_ERROR);
			}
		}
	}
	
	public void SendKey(String key)
	{
		if(_outputStream != null)
		{
			try
			{
				_outputStream.write(String.format("key %s\r", key));
				_outputStream.flush();
			}
			catch (IOException e)
			{
				this.SetStatus("I/O Error sending Jump", STATUS_ERROR);
			}
		}
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
		return _socket.isConnected();
	}
	
	
	
	
	
	
	
	
	private void SetStatus(String StatusMsg, int code)
	{
		_status = StatusMsg;
		if(_statusListener != null)
			_statusListener.StatusChanged(StatusMsg, code);
	}
	
	
	
	
	
}
